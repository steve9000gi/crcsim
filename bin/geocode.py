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

         Start N-1 worker processes to parse timeslice-location data.
         Geocode latitude and longitude to polygon data. Write new tuples.
         Wait for all threads to join.

      Export Polygons.

      Count:

         Load geocoded tuples. Count occurrences / polygon and export as JSON.

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
                    #logger.error ("Invalid Line: file: %s, index: %s, length(%s): %s", input_file, index, len(row), row)
                    pass
        self.con.commit ()

    def get_coordinates (self, file_name):
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
            self.cur.execute (query)
            rows = self.cur.fetchall ()
            count = 0
            for row in rows:
                count += 1
                if count == 1:
                    continue # skip header.
                coordinates.append ( (metric, scenario, timeslice_id, row[0], row[1]) )
        return coordinates
    
    def data_import (self, file_name, output_file_path):
        ''' Create the output directory, import input data and select and write output data. '''
        fs_lock.acquire ()
        if not os.path.exists (self.output_directory):
            os.makedirs (self.output_directory)
        fs_lock.release ()

        self.select_data (file_name, self.columns)
        coordinates = self.get_coordinates (file_name)

        with open (output_file_path, 'w') as stream:
            for tuple in coordinates:
                stream.write ("%s\n" % " ".join (tuple))        

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
                    'points' : geometry ['coordinates'][0]
                    }
                self.write_json_object ('polygon-%s' % polygon_count, obj)
        return polygon_count
    
    def write_json_object (self, filename_prefix, obj):
        with open ('%s.json' % filename_prefix, 'w') as stream:
            stream.write (json.dumps (obj, indent=1, sort_keys=True))

    def read_json_object (self, filename):
        obj = None
        if os.path.exists (filename):
            with open (filename, 'r') as stream:
                try:
                    obj = json.loads (stream.read ())
                except Exception:
                    pass
        return obj

    def geocode_batch (self, shapefile, output_dir, buffer_size = 10000):
        '''
        Create work and result queues. Start a worker to count poin-in-poly relationships.
        Create and start workers to parse data selected by relational queries in phase one.
        Enumerate data files, adding each to work queue.
        Add done markers to the work queue to signal workers to exit, one per worker.
        Wait for all workers to complete. Then signal the counter to exit.
        Wait for the counter to exit.
        '''
        work_Q = multiprocessing.Queue ()
        result_Q = multiprocessing.Queue ()

        workers = []
        num_workers = multiprocessing.cpu_count () * 10
        #if num_workers > 1: num_workers -= 1
        for c in range (num_workers):
            logger.info ("Starting geocode worker %s.", c)
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

    def calculate_batched_intersections_parallel (self, shapefile, points, metric, scenario, timeslice, out_stream):
        ''' Calculate point-in-polygon relationships for an array of points. '''
        if len (points) > 0:
            c = 0
            polygon_matches = self.find_polygon_intersections_batch (shapefile, points)
            for polygon_id, matches in enumerate (polygon_matches):
                for point_idx, state in enumerate (matches):
                    if state == True:
                        out_stream.write ("%s %s %s %s\n" % ( metric, scenario, polygon_id, timeslice ))
                        c += 1
            logger.debug ("Recorded %s/%s matches at timeslice: %s", c, len (points), timeslice)
            del points [0 : len (points)]

    def get_counter (self, counters, metric, scenario, polygon_count):
        '''  Get an existing counter object or construct, store and return a new one if
             no matching counter exists yet.'''
        metric_map = {}
        if metric in counters:
            metric_map = counters [metric]
        else:
            counters [metric] = metric_map
            
        scenario_counter = None
        if scenario in metric_map:
            scenario_counter = metric_map [scenario]
        else:
            scenario_counter = Counter (polygon_count)
            metric_map [scenario] = scenario_counter

        return scenario_counter

    def count_geocoded (self, polygon_count, output_dir):
        ''' Read intersection result data from the result queue and record. Record occurrences. '''
        counters = {}
        geocoder = Geocoder ()
        for root, dirnames, filenames in os.walk (output_dir):
            for idx, file_name in enumerate (fnmatch.filter (filenames, '*.geocoded')):
                geocoded_tuples = os.path.join (root, file_name)
                logger.debug ("Counting %s", geocoded_tuples)
                with open (geocoded_tuples, 'rb') as stream:
                    try:
                        metric, scenario, timeslice_id, polygon_id = stream.readline().split ()
                        counter = self.get_counter (counters, metric, scenario, polygon_count)
                        counter.increment (int (timeslice_id), int (polygon_id))
                    except Queue.Empty:
                        pass
                    except ValueError, e:
                        traceback.print_exc (e)                    
        for metric_key in counters:
            metric_scenarios = counters [metric]
            for scenario_key in metric_scenarios:
                counter = metric_scenarios [scenario_key]
                object_name = "%s-%s-occurrences" % (metric_key, scenario_key)
                obj = { "counts" : counter.timeline }
                geocoder.write_json_object (object_name, obj)

def point_in_poly_worker (work_Q, result_Q, shapefile, buffer_size = 10000):
    ''' Take filenames of data from the work queue. Process these to determine point/polygon intersections. '''
    geocoder = Geocoder ()
    while True:
        try:
            work = work_Q.get (timeout = 2)
            #logger.info ("Took geocoder work %s from queue.", work)
            if not work:
                continue
            if work == 'done':
                logger.info ("Ending point in poly worker process.")
                break

            out_file_name = "%s.geo" % work
            out_file_name_final_name = "%scoded" % out_file_name

            if os.path.exists (out_file_name_final_name):
                logger.debug ("Skipping already geocoded file: %s", out_file_name_final_name)
                pass
            else:
                with open (out_file_name, 'w') as out_stream:
                    with open (work, 'rb') as stream:
                        points = []
                        try:
                            for line in stream:
                                coords = line.strip ().split ()
                                metric, scenario, timeslice, lat_s, lon_s = coords
                                timeslice = int (timeslice)
                                lat = float (lat_s)
                                lon = float (lon_s)
                                points.append ( [ lon, lat ] )
                                if len (points) >= buffer_size:
                                    geocoder.calculate_batched_intersections_parallel (shapefile, points, metric, scenario, timeslice, out_stream)
                            if len (points) > 0:
                                geocoder.calculate_batched_intersections_parallel (shapefile, points, metric, scenario, timeslice, out_stream)
                        except ValueError, e:
                            traceback.print_exc (e)
                        except Exception, e: # Can't be exiting a worker for every oddity we encounter.
                            traceback.print_exc (e)
                os.rename (out_file_name, out_file_name_final_name)
                logger.debug ("geocode file: %s written.", out_file_name_final_name)
                work_Q.put (out_file_name_final_name)
        except Queue.Empty:
            pass
        except Exception, e: # Can't be exiting a worker for every oddity we encounter.
            traceback.print_exc (e)

def form_output_select_file_path (output_dir, file_name):
    path = file_name.split (os.path.sep)
    path = "_".join (path [-2:])
    output_file_path = os.path.join (output_dir, path)
    if not output_file_path.endswith (".txt"):
        output_file_path = "%s.txt" % output_file_path
    return output_file_path

def select_coordinates_worker (query, database, output_dir, work_Q):
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
                    data_importer.data_import (snapshot_file, output_file_path)
        except Queue.Empty:
            pass

def select_coordinates (query, database, snapshotDB, output_dir):
    ''' Start workers - one per cpu - to import data and select items.'''
    workers = []
    num_workers = multiprocessing.cpu_count () * 10
    work_Q = multiprocessing.Queue ()
    for c in range (num_workers):
        logger.info ("Starting import/select worker %s.", c)
        data_import_args = (query, database, output_dir, work_Q)
        process = multiprocessing.Process (target=select_coordinates_worker, args = data_import_args)
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
        self.query = "select latitude, longitude from simulation where num_lesions > 0 and never_compliant = 'true'"
        self.database = ":memory:"
        self.loglevel = "error"
        self.archive = False
        self.geocode_only = False

    def form_data_path (self, args):
        tail = os.path.join (*args) if isinstance (args, list) else args
        return os.path.join (self.root, tail)

def geocode (arguments = GeocodeArguments (), callback = None):

    ''' Configure logging. '''
    numeric_level = getattr (logging, arguments.loglevel.upper (), None)
    assert isinstance (numeric_level, int), "Undefined log level: %s" % arguments.loglevel
    logging.basicConfig (level=numeric_level, format='%(asctime)-15s %(message)s')

    logger.info (" shapefile: %s", arguments.shapefile)
    logger.info ("snapshotDB: %s", arguments.snapshotDB)
    logger.info ("    output: %s", arguments.output)
    logger.info ("  loglevel: %s", arguments.loglevel)

    logger.info ("Select...")
    if not arguments.geocode_only:
        select_coordinates (arguments.query,
                            arguments.database,
                            arguments.snapshotDB,
                            arguments.output)

    logger.info ("Geocoding...")
    geocoder = Geocoder ()
    geocoder.geocode_batch (arguments.shapefile, arguments.output)

    logger.info ("Export polygons...")
    polygon_count = geocoder.export_polygons (arguments.shapefile)

    logger.info ("Counting...")
    geocoder.count_geocoded (polygon_count, arguments.output)

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
    parser.add_argument ("--shapefile",  help="Path to an ESRI shapefile", default=parameters.shapefile)
    parser.add_argument ("--geocode_only",help="Geocode only - no select", dest='geocode_only', action='store_true', default=parameters.geocode_only)
    parser.add_argument ("--snapshotDB", help="Path to a directory hierarchy containing population snapshot files.", default=parameters.snapshotDB)
    parser.add_argument ("--output",     help="Output directory. Default is 'output'.", default=parameters.output)
    parser.add_argument ("--database",   help="Database path. Default is in-memory.", default=parameters.database)
    parser.add_argument ("--loglevel",   help="Log level", default=parameters.loglevel)
    parser.add_argument ("--archive",    help="Archive output files.", dest='archive', action='store_true', default=parameters.archive)
    parser.add_argument ("--query",      help="Query to apply.", default=parameters.query)
    args = parser.parse_args ()

    parameters.shapefile = args.shapefile
    parameters.snapshotDB = args.snapshotDB
    parameters.output = args.output
    parameters.database = args.database
    parameters.loglevel = args.loglevel
    parameters.archive = args.archive
    parameters.query = args.query
    parameters.geocode_only = args.geocode_only
    parameters.query = {
        "crc"         : "select latitude, longitude from simulation where cancer_free_years > 0",
        "colonoscopy" : "select latitude, longitude from simulation where num_colonoscopies > 0"
        }

    geocode (parameters)

    sys.exit (0)

if __name__ == '__main__':
    fs_lock = Lock ()
    main ()



