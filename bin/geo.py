#!/usr/bin/env python

import argparse
import csv
import fnmatch
import glob
import json
import logging
import matplotlib.nxutils as nx
import multiprocessing
import numpy
import os
import pp
import signal
import sqlite3
import string
import string
import sys
import tarfile
import time
import traceback
import Queue

from fiona import collection
from collections import defaultdict
from multiprocessing import Lock

logger = logging.getLogger (__name__)

class DataImporter (object):
    ''' Load CSV data into SQLite3 accessible memory to filter. '''

    def __init__ (self, query, columns, database, output_directory = "output"):
        ''' Initialize connection. '''
        self.con = sqlite3.connect (database)
        self.cur = self.con.cursor ()
        self.columns = columns
        insert_columns = ", ".join (self.columns)
        value_slots = ",".join ([ "?" for x in self.columns ])
        self.insert_statement = "INSERT INTO simulation (%s) VALUES (%s);" % (insert_columns, value_slots)
        self.output_directory = output_directory
        self.query = query

    def create_table (self):
        ''' Build the table from a dynamically constructed column list. '''
        column_ddl = map (lambda x : "%s varchar(10)" % x, self.columns)
        column_ddl = ", ".join (column_ddl)
        statement = "CREATE TABLE simulation (%s);" % column_ddl
        logger.debug ("SQL: %s", statement)
        self.cur.execute (statement)

    def drop_table (self):
        try:
            self.cur.execute ('DROP TABLE simulation')
        except sqlite3.OperationalError:
            pass

    def select_data (self, input_file, columns, delimiter = '\t'):
        ''' Clean the database, create the table and import data from CSV. '''
        self.drop_table ()
        self.create_table ()
        with open (input_file, 'rbU') as stream:
            data = csv.reader (stream, delimiter = delimiter)
            for index, row in enumerate (data):
                try:
                    self.cur.executemany (self.insert_statement, (row,))
                except sqlite3.ProgrammingError:
                    pass
        self.con.commit ()

    def get_counts (self, file_name, fips_county_map):
        ''' Apply the filter to get location data. '''
        timeslice_id_index = file_name.rindex ('.') + 1
        assert timeslice_id_index < len (file_name), "Problem parsing filename: %s" % file_name
        timeslice_id = file_name [timeslice_id_index:]

        ''' File name form: /population/population.tsv.0045.intervention.0004 '''
        scenario_dir = file_name.split ('/')[-2].split ('.')
        scenario = '.'.join ( [ scenario_dir [-2], scenario_dir [-1] ] )
        coordinates = []
        for metric in self.query:
            query = self.query [metric]
            print "Query metric: %s" % metric
            print fips_county_map
            for idx, county_code in enumerate (fips_county_map):
                formatted_query = query.format (county_code)
                print "Querying: %s" % formatted_query
                self.cur.execute (formatted_query)
                count = self.cur.fetchone ()
                coordinates.append ( [ metric, scenario, timeslice_id, county_code, idx, count ] )
        return coordinates
    
    def data_import (self, file_name, output_file_path, fips_county_map):
        ''' Create the output directory, import input data and select and write output data. '''
        fs_lock.acquire ()
        if not os.path.exists (self.output_directory):
            os.makedirs (self.output_directory)
        fs_lock.release ()

        self.select_data (file_name, self.columns)
        counts = self.get_counts (file_name, fips_county_map)

        with open (output_file_path, 'w') as stream:
            for tup in counts:
                text = "%s %s %s %s %s %s\n" % (tup[0], tup[1], tup[2], tup[3], tup[4], tup[5][0])
                stream.write (text)

def select_worker (query, database, output_dir, fips_county_map, work_Q):
    ''' Determine column names, create a data cruncher and process the input file. '''
    while True:
        try:
            snapshot_file = work_Q.get (timeout = 2)
            logger.debug ("Took geocoder work %s from queue.", snapshot_file)
            if not snapshot_file:
                continue
            if snapshot_file == 'done':
                logger.info ("Ending point in poly worker process.")
                break
            columns = None
            with open (snapshot_file, 'rb') as stream:
                line = stream.readline ()
                if line.startswith ("description"):
                    line = stream.readline ()
                columns = line.strip().split ('\t')
                output_file_path = form_output_select_file_path (output_dir, snapshot_file)
                if not os.path.exists (output_file_path):
                    data_importer = DataImporter (query, columns, database, output_dir)
                    data_importer.data_import (snapshot_file, output_file_path, fips_county_map)
        except Queue.Empty:
            pass

def select (query, database, snapshotDB, output_dir, fips_county_map):
    ''' Start workers - one per cpu - to import data and select items.'''
    workers = []
    num_workers = multiprocessing.cpu_count ()
    work_Q = multiprocessing.Queue () 
    logger.info ("Starting %s import/select workers.", num_workers)
    for c in range (num_workers):
        data_import_args = (query, database, output_dir, fips_county_map, work_Q)
        process = multiprocessing.Process (target=select_worker, args = data_import_args)
        workers.append (process)
        
    for process in workers:
        process.start ()

    for root, dirnames, filenames in os.walk (snapshotDB):
        for idx, file_name in enumerate (fnmatch.filter (filenames, 'person.*')):
            snapshot_file = os.path.join (root, file_name)

            base = os.path.basename (snapshot_file)
            last = base.split ('.')[1]
            timeslice = int (last)

            if timeslice > 38:
                logger.debug ("launch-snap[%s] %s", idx, snapshot_file)
                work_Q.put (snapshot_file)
    for worker in workers:
        work_Q.put ('done')
    work_Q.close ()
    work_Q.join_thread ()

    for worker in workers:
        worker.join ()
        logger.info ("Joined process[%s]: %s", worker.pid, worker.name)

class Geocoder (object):
    ''' Geocode point data to a set of polygons. '''
    def export_polygons (self, shapefile, output_dir):
        ''' Write the list of polygons as json. '''
        fips_map = []
        polygon_count = -1
        from fiona import collection
        with collection (shapefile, "r") as stream:
            for feature in stream:
                county_name = feature['properties']['NAME10']
                fips_county_code = feature['properties']['COUNTYFP10']
                fips_map.append (fips_county_code)
                print "%s : %s" % (county_name, fips_county_code)

                polygon_count = int (feature ['id'])
                geometry = feature ['geometry']
                obj = {
                    'count' : polygon_count,
                    'points' : geometry ['coordinates'][0]
                    }
        return fips_map

def form_output_select_file_path (output_dir, file_name):
    path = file_name.split (os.path.sep)
    path = "_".join (path [-2:])
    output_file_path = os.path.join (output_dir, path)
    if not output_file_path.endswith (".txt"):
        output_file_path = "%s.txt" % output_file_path
    return output_file_path

def archive (archive_name = "out.tar.gz"):
    ''' Archive and remove the output files. '''
    logger.info ("Creating output archive: %s", archive_name)
    with tarfile.open (archive_name, "w:gz") as archive:
        files = glob.glob ("*.json")
        for output in files:
            logger.debug ("   archive + %s", output)
            archive.add (output)
        for output in files:
            os.remove (output)

def signal_handler (signum, frame):
    logger.info ('Signal handler called with signal: %s', signum)
    sys.exit (0)

class GeocodeArguments (object):

    def __init__ (self):
        self.root = os.path.join ( os.path.sep, "projects", "systemsscience" )
        self.shapefile = self.form_data_path ([ "var", "census2010", "tl_2010_37_county10.shp" ])
        self.snapshotDB = self.form_data_path ("out")
        self.output = "output"
        self.database = ":memory:"
        self.loglevel = "error"
        self.archive = False

    def form_data_path (self, args):
        tail = os.path.join (*args) if isinstance (args, list) else args
        return os.path.join (self.root, tail)

def process_pipeline (arguments = GeocodeArguments (), callback = None):

    ''' Configure logging. '''
    numeric_level = getattr (logging, arguments.loglevel.upper (), None)
    assert isinstance (numeric_level, int), "Undefined log level: %s" % arguments.loglevel
    logging.basicConfig (level=numeric_level, format='%(asctime)-15s %(message)s')

    logger.info (" shapefile: %s", arguments.shapefile)
    logger.info ("snapshotDB: %s", arguments.snapshotDB)
    logger.info ("    output: %s", arguments.output)
    logger.info ("  loglevel: %s", arguments.loglevel)

    logger.info ("Geocoding...")
    geocoder = Geocoder ()
    fips_county_map = geocoder.export_polygons (arguments.shapefile, arguments.output)

    logger.info ("Select...")
    select (arguments.query,
            arguments.database,
            arguments.snapshotDB,
            arguments.output,
            fips_county_map)
    
    exported_polygons = os.path.join (arguments.output, "polygon*json")

    if arguments.archive:
        archive ()
        if callback:
            callback ('out.tar.gz')
        
def main ():

    ''' Register signal handler. '''
    signal.signal (signal.SIGINT, signal_handler)

    parameters = GeocodeArguments ()

    ''' Parse arguments. '''
    parser = argparse.ArgumentParser ()
    parser.add_argument ("--shapefile",    help="Path to an ESRI shapefile", default=parameters.shapefile)
    parser.add_argument ("--snapshotDB",   help="Path to a directory hierarchy containing population snapshot files.", default=parameters.snapshotDB)
    parser.add_argument ("--output",       help="Output directory. Default is 'output'.", default=parameters.output)
    parser.add_argument ("--database",     help="Database path. Default is in-memory.", default=parameters.database)
    parser.add_argument ("--loglevel",     help="Log level", default=parameters.loglevel)
    parser.add_argument ("--archive",      help="Archive output files.", dest='archive', action='store_true', default=parameters.archive)
    args = parser.parse_args ()

    parameters.shapefile = args.shapefile
    parameters.snapshotDB = args.snapshotDB
    parameters.output = args.output
    parameters.database = args.database
    parameters.loglevel = args.loglevel
    parameters.archive = args.archive
    parameters.query = {
        "crc"         : "select count(*) from simulation where cancer_free_years > 0 and stcotrbg like '37{0}%' ",
        "colonoscopy" : "select count(*) from simulation where num_colonoscopies > 0 and stcotrbg like '37{0}%' "
        }

    process_pipeline (parameters)

    sys.exit (0)

if __name__ == '__main__':
    fs_lock = Lock ()
    main ()



