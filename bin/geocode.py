#!/usr/bin/env python

'''

Copyright (c) 2011 RENCI/UNC Chapel Hill

@author Steven Cox

Permission is hereby granted, free of charge, to any person obtaining a copy of this software
and/or hardware specification (the "Work") to deal in the Work without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Work, and to permit persons to whom the Work is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Work.

THE WORK IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE WORK OR THE USE OR OTHER DEALINGS
IN THE WORK.


Goal:

   Geocode agent based model output data, scaling to terabyte range.

Approach:

   Overview:

      Designed for RENCI Blueridge's large memory nodes [ 1TB RAM / 32 hyper-threaded cores ],
      Parses CSV output files recording the state of agents at a series of timeframes
      Uses output data format of the UNC CRC Agent Based Model.

   Process:

      Overview:

         Import data, select location and timeslice information from relevant rows. Geocode
         and output summary data as JSON, optionally, archiving output data files.

      Select:

         Start a process per CPU to -
         Load each CSV model output file into an in-memory SQLite3 database.
         Select relevant data from the data set.
         Write selected data to text files as timeslice, latitude, longitude tuples.

      Geocode:

         Export polygons as JSON.
         Start a worker process to count point-in-polygon relationships and write the output as a JSON array.
         Start N-1 worker processes to parse timeslice-location data and send messages to the counter.
         Wait for all threads to join.

Usage:

     ./geocoder.py /projects/systemsscience/var/census2010/tl_2010_37_county10.shp \
                   /projects/systemsscience/out/population.tsv.0035.control/       \
                   --output /projects/systemsscience/var/geo                       \
                   --archive                                                       \
                   --loglevel debug

'''

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

    def import_data (self, input_file, columns, delimiter = '\t'):
        ''' Clean the database, create the table and import data from CSV. '''
        self.drop_table ()
        self.create_table ()
        with open (input_file, 'rb') as stream:
            data = csv.reader (stream, delimiter = delimiter)
            for index, row in enumerate (data):
                try:
                    self.cur.executemany (self.insert_statement, (row,))
                except sqlite3.ProgrammingError:
                    pass #logger.error ("Invalid Line: file: %s, index: %s, length(%s): %s", input_file, index, len(row), row)
        self.con.commit ()

    def get_coordinates (self, file_name):
        ''' Apply the filter to get location data. '''
        timeslice_id_index = file_name.rindex ('.') + 1
        assert timeslice_id_index < len (file_name), "Problem parsing filename: %s" % file_name
        timeslice_id = file_name [timeslice_id_index:]
        
        #self.cur.execute ("select latitude, longitude from simulation where num_lesions > 0 and never_compliant = 'true'")
        self.cur.execute (self.query)
        rows = self.cur.fetchall ()
        coordinates = []
        for row in rows:
            coordinates.append ( (timeslice_id, row[0], row[1]) )
        return coordinates
    
    def data_import (self, file_name, stats):
        ''' Create the output directory, import input data and select and write output data. '''
        fs_lock.acquire ()
        if not os.path.exists (self.output_directory):
            os.makedirs (self.output_directory)
        fs_lock.release ()

        s = time.time ()
        self.import_data (file_name, self.columns)
        e = time.time ()
        stats ['import.%s' % file_name] = e - s
        stats ['import.total'] += e - s

        s = time.time ()
        coordinates = self.get_coordinates (file_name)
        e = time.time ()
        stats['select.%s' % file_name] = e - s
        stats['select.total'] += e - s

        path = file_name.split (os.path.sep)
        path = "_".join (path [-2:])
        output_file_path = os.path.join (self.output_directory, path)
        if not output_file_path.endswith (".txt"):
            output_file_path = "%s.txt" % output_file_path
        with open (output_file_path, 'w') as stream:
            for tuple in coordinates:
                stream.write ("%s\n" % " ".join (tuple))        
        return stats

class Counter (object):
    ''' Abstraction of a matrix of counts by polygon over time. '''
    def __init__(self, polygon_count, timeslices = 100):
        self.timeline = [
            [ 0 for i in range (timeslices + 1) ] for j in range (polygon_count + 1)
            ]
        self.increment_count = 0

    def increment (self, polygon_id, timeslice):
        ''' Count a hit at the polygon and timeslice '''
        self.increment_count += 1
        try:
            if self.increment_count % 1000 == 0:               
                logger.debug ("Counter.increment (increment count: %s) (polygon_id=>%s, timeslice=>%s)",
                              self.increment_count,
                              polygon_id,
                              timeslice)
            self.timeline [polygon_id][timeslice] += 1
        except IndexError:
            logger.error ("Index Error: Counter.increment (polygon_id=>%s, timeslice=>%s)", polygon_id, timeslice)

    def __str__ (self):
        return str (self.timeline)

class Geocoder (object):
    ''' Geocode point data to a set of polygons. '''

    def calculate_polygon_intersection (self, geometry, points):
        ''' Apply nxutils algorithm to determine point-in-polygon relationship. '''
        rings = geometry ['coordinates']
        ring_points = []
        for ring in rings:
            for point in ring:
                ring_points.append (list (point))
        numpy_geometry = numpy.array (ring_points)
        return nx.points_inside_poly (points, numpy_geometry)

    def find_polygon_intersections_batch (self, shapefile, points):
        ''' Iterate over polygons in a shapefile, determining point-in-polygon relationships for the given points. '''
        results = []
        with collection (shapefile, "r") as source:
            for feature in source:
                polygon_id = int (feature ['id'])
                geometry = feature ['geometry']
                assert geometry ['type'] == "Polygon", "Non-polygon object encountered reading %s" % shapefile
                results.append (self.calculate_polygon_intersection (geometry, points))
        return results

    def export_polygons (self, shapefile):
        ''' Write the list of polygons as json. '''
        polygon_count = -1
        from fiona import collection
        with collection (shapefile, "r") as stream:
            for feature in stream:
                polygon_count = int (feature ['id'])
                geometry = feature ['geometry']
                obj = {
                    'count' : polygon_count,
                    'points' : geometry ['coordinates']
                    }
                self.write_json_object ('polygon-%s' % polygon_count, obj)
        return polygon_count
    
    def write_json_object (self, filename_prefix, obj):
        with open ('%s.json' % filename_prefix, 'w') as stream:
            stream.write (json.dumps (obj, indent=1, sort_keys=True))

    def geocode_batch_parallel (self, shapefile, output_dir, buffer_size = 1000):
        '''
        Export and count polygons.
        Create work and result queues. Start a worker to count poin-in-poly relationships.
        Create and start workers to parse data selected by relational queries in phase one.
        Enumerate data files, adding each to work queue.
        Add done markers to the work queue to signal workers to exit, one per worker.
        Wait for all workers to complete. Then signal the counter to exit.
        Wait for the counter to exit.
        '''
        logger.debug ("Export polygons...")
        polygon_count = self.export_polygons (shapefile)
        logger.debug ("Geocode...")

        work_Q = multiprocessing.Queue ()
        result_Q = multiprocessing.Queue ()

        counting_process = multiprocessing.Process (target=counting_worker, args=(polygon_count, result_Q,))
        counting_process.start ()

        workers = []
        num_workers = multiprocessing.cpu_count ()
        if num_workers > 1: num_workers -= 1
        for c in range (num_workers):
            logger.debug ("Starting worker %s.", c)
            process = multiprocessing.Process (target=point_in_poly_worker, args=(work_Q, result_Q, shapefile, buffer_size))
            workers.append (process)

        for process in workers:
            process.start ()

        for root, dirnames, filenames in os.walk (output_dir):
            for idx, file_name in enumerate (fnmatch.filter (filenames, '*.txt')):
                coordinate_file = os.path.join (root, file_name)
                logger.debug ("Putting coordinate file: %s to work queue.", coordinate_file)
                work_Q.put (coordinate_file)
        for process in workers:
            work_Q.put ('done')
        work_Q.close ()
        work_Q.join_thread ()

        for process in workers:
            process.join ()           
        logger.info ("Notifying result Q consumer of completion")
        result_Q.put ('done')
            
        counting_process.join ()
        logger.debug ("Counter process joined.")

    def calculate_batched_intersections_parallel (self, shapefile, points, timeslice, result_Q):
        ''' Calculate point-in-polygon relationships for an array of points. '''
        if len (points) > 0:
            c = 0
            polygon_matches = self.find_polygon_intersections_batch (shapefile, points)
            for polygon_id, matches in enumerate (polygon_matches):
                for point_idx, state in enumerate (matches):
                    if state == True:
                        result_Q.put ( [ polygon_id, timeslice ] )
                        c += 1
            logger.info ("Recorded %s/%s matches at timeslice: %s", c, len (points), timeslice)
            del points [0 : len (points)]

def counting_worker (polygon_count, result_Q):
    ''' Read intersection result data from the result queue and record. Record occurrences. '''
    counter = Counter (polygon_count)
    geocoder = Geocoder ()
    while True:
        work = result_Q.get (timeout = 5)
        if not work:
            continue
        if work == 'done':
            break
        if len (work) < 2:
            continue
        try:
            counter.increment (work [0], work [1])
        except e:
            traceback.print_exc (e)
    obj = { "counts" : counter.timeline }
    geocoder.write_json_object ("occurrences", obj)

def point_in_poly_worker (work_Q, result_Q, shapefile, buffer_size = 1000):
    ''' Take filenames of data from the work queue. Process these to determine point/polygon intersections. '''
    geocoder = Geocoder ()
    while True:
        work = work_Q.get (timeout = 2)
        logger.debug ("Took geocoder work %s from queue.", work)
        if not work:
            continue
        if work == 'done':
            logger.info ("Ending point in poly worker process.")
            break
        with open (work, 'rb') as stream:
            timeslice = None
            points = []
            for line in stream:
                coords = line.strip ().split ()
                if not timeslice:
                    timeslice = int (coords [0])
                try:
                    lat_s = coords [2]
                    lon_s = coords [1]
                    lat = float (lat_s)
                    lon = float (lon_s)
                    points.append ( [ lat, lon ] )
                except ValueError:
                    pass
                if len (points) >= buffer_size:
                    geocoder.calculate_batched_intersections_parallel (shapefile, points, timeslice, result_Q)
            if len (points) > 0:
                geocoder.calculate_batched_intersections_parallel (shapefile, points, timeslice, result_Q)

def select_coordinates_worker (query, database, output_dir, work_Q):
    ''' Determine column names, create a data cruncher and process the input file. '''
    stats = defaultdict (int)
    while True:
        snapshot_file = work_Q.get (timeout = 2)
        logger.debug ("Took geocoder work %s from queue.", snapshot_file)
        if not snapshot_file:
            continue
        if snapshot_file == 'done':
            logger.info ("Ending point in poly worker process.")
            break
        columns = None
        with open (snapshot_file, 'rb') as stream:
            columns = stream.readline ().strip().split ('\t')
            data_importer = DataImporter (query, columns, database, output_dir)
            data_importer.data_import (snapshot_file, stats)
            
def select_coordinates (query, database, snapshotDB, output_dir):
    ''' Start workers - one per cpu - to import data and select items.'''
    workers = []
    num_workers = multiprocessing.cpu_count ()
    work_Q = multiprocessing.Queue ()
    for c in range (num_workers):
        logger.debug ("Starting import/select worker %s.", c)
        data_import_args = (query, database, output_dir, work_Q)
        process = multiprocessing.Process (target=select_coordinates_worker, args = data_import_args)
        workers.append (process)
        
    for process in workers:
        process.start ()

    for root, dirnames, filenames in os.walk (snapshotDB):
        for idx, file_name in enumerate (fnmatch.filter (filenames, 'person.*')):
            snapshot_file = os.path.join (root, file_name)
            logger.debug ("launch-snap[%s] %s", idx, snapshot_file)
            work_Q.put (snapshot_file)
    for worker in workers:
        work_Q.put ('done')
    work_Q.close ()
    work_Q.join_thread ()

    for worker in workers:
        worker.join ()
        logger.debug ("Joined process[%s]: %s", worker.pid, worker.name)

def archive (archive_name = "out.tar.gz"):
    ''' Archive and remove the output files. '''
    logger.debug ("Creating output archive: %s", archive_name)
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

class DefaultPaths (object):

    def __init__ (self):
        self.root = os.path.join ( "projects", "systemsscience" )
        self.shapefile = self.form_data_path ([ "var", "census2010", "tl_2010_37_country10.shp" ])
        self.snapDB = self.form_data_path ("out")
        self.query_store = self.form_data_path ("query")

    def form_data_path (args):
        tail = os.path.join (*args) if isinstance (args, list) else args
        return os.path.join (self.root, tail)

def main ():

    ''' Register signal handler. '''
    signal.signal (signal.SIGINT, signal_handler)


    ''' Parse arguments. '''
    parser = argparse.ArgumentParser ()
    parser.add_argument ("--shapefile",  help="Path to an ESRI shapefile", default=default_shapefile)
    parser.add_argument ("--snapshotDB", help="Path to a directory hierarchy containing population snapshot files.", default=default_snapDB)
    parser.add_argument ("--output",     help="Output directory. Default is 'output'.", default="output")
    parser.add_argument ("--database",   help="Database path. Default is in-memory.")
    parser.add_argument ("--loglevel",   help="Log level", default="error")
    parser.add_argument ("--archive",    help="Archive output files.", dest='archive', action='store_true', default=False)
    args = parser.parse_args ()

    ''' Configure logging. '''
    numeric_level = getattr (logging, args.loglevel.upper (), None)
    assert isinstance (numeric_level, int), "Undefined log level: %s" % args.loglevel
    logging.basicConfig (level=numeric_level, format='%(asctime)-15s %(message)s')

    database = args.database if args.database else ":memory:"
    shapefile = args.shapefile
    snapshotDB = args.snapshotDB
    output_dir = args.output if args.output else "output"
    
    select_coordinates (query, database, snapshotDB, output_dir)
    geocoder = Geocoder ()
    geocoder.geocode_batch_parallel (shapefile, output_dir)

    if args.archive:
        archive ()
        
    sys.exit (0)

if __name__ == '__main__':
    fs_lock = Lock ()
    main ()



