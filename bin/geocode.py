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

from fiona import collection
from collections import defaultdict
from multiprocessing import Lock

logger = logging.getLogger (__name__)

class DataCruncher (object):
    ''' Load CSV data into SQLLite3 accessible memory to filter. '''

    def __init__ (self, columns, database, output_directory = "output"):
        self.con = sqlite3.connect (database)
        #self.con = sqlite3.connect ("test.db")
        self.cur = self.con.cursor ()
        self.columns = columns
        insert_columns = ", ".join (self.columns)
        value_slots = ",".join ([ "?" for x in self.columns ])
        self.insert_statement = "INSERT INTO simulation (%s) VALUES (%s);" % (insert_columns, value_slots)
        self.output_directory = output_directory

    def create_table (self):
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
                    logger.error ("===========> file: %s, index: %s, length(%s): %s", input_file, index, len(row), row)
        self.con.commit ()

    def get_coordinates (self, file_name):
        timeslice_id_index = file_name.rindex ('.') + 1
        assert timeslice_id_index < len (file_name), "Problem parsing filename: %s" % file_name
        timeslice_id = file_name [timeslice_id_index:]
        self.cur.execute ("select latitude, longitude from simulation where num_lesions > 3 and never_compliant = 'true'")
        rows = self.cur.fetchall ()
        coordinates = []
        for row in rows:
            coordinates.append ( (timeslice_id, row[0], row[1]) )
        return coordinates
    
    def crunch (self, file_name, idx, stats):

        fs_lock = Lock ()
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
        logger.debug ("len timeline: %s", len (self.timeline))
        logger.debug ("len timeline 0: %s", len (self.timeline[0]))
        
    def increment (self, polygon_id, timeslice):
        try:
            logger.debug ("Counter.increment (polygon_id=>%s, timeslice=>%s)", polygon_id, timeslice)
            self.timeline [polygon_id][timeslice] += 1
        except IndexError:
            logger.error ("Index Error: Counter.increment (polygon_id=>%s, timeslice=>%s)", polygon_id, timeslice)

    def write (self):
        write_json_object ('occurences', { "counts" : self.timeline })
        
    def __str__ (self):
        return str (self.timeline)

class Geocoder (object):
    
    def calculate_polygon_intersection (self, geometry, points):
        rings = geometry ['coordinates']
        ring_points = []
        for ring in rings:
            for point in ring:
                ring_points.append (list (point))
        numpy_geometry = numpy.array (ring_points)
        return nx.points_inside_poly (points, numpy_geometry)

    def find_polygon_intersections_batch (self, shapefile, points):
        results = []
        with collection (shapefile, "r") as source:
            for feature in source:
                polygon_id = int (feature ['id'])
                geometry = feature ['geometry']
                assert geometry ['type'] == "Polygon", "Non-polygon object encountered reading %s" % shapefile
                results.append (self.calculate_polygon_intersection (geometry, points))
        return results

    def export_polygons (self, shapefile):
        ''' Write a list of polygons as json. '''
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

            '''
    def geocode (self, shapefile, output_dir):
        logger.debug ("Export polygons...")
        polygon_count = self.export_polygons (shapefile)
        logger.debug ("Geocode...")
        counter = Counter (polygon_count)
        for root, dirnames, filenames in os.walk (output_dir):
            for idx, file_name in enumerate (fnmatch.filter (filenames, '*.txt')):
                coordinate_file = os.path.join (root, file_name)
                #logger.debug ("geocoding[%s] %s", idx, coordinate_file)
                with open (coordinate_file, 'rb') as stream:
                    for line in stream:
                        print line
                        coords = line.strip ().split ()
                        timeslice = int (coords [0])
                        #points.append ( [ float (coords [1]), float (coords [2]) ] )
                        try:
                            lat_s = coords [2]
                            lon_s = coords [1]
                            key = "%s-%s" % ( lat_s, lon_s )
                            lat = float (lat_s)
                            lon = float (lon_s)
                            points = [ [ lat, lon ] ]
                            polygon_id = self.find_polygon_intersections (shapefile, points)
                            if polygon_id > -1:
                                counter.increment (polygon_id, timeslice)                                
                        except ValueError:
                            pass
        self.write_json_object ("occurrences", { "counts" : counter.timeline })

    def geocode_batch (self, shapefile, output_dir, buffer_size = 1000):
        logger.debug ("Export polygons...")
        polygon_count = self.export_polygons (shapefile)
        logger.debug ("Geocode...")
        counter = Counter (polygon_count)
        for root, dirnames, filenames in os.walk (output_dir):
            for idx, file_name in enumerate (fnmatch.filter (filenames, '*.txt')):
                coordinate_file = os.path.join (root, file_name)
                #logger.debug ("geocoding[%s] %s", idx, coordinate_file)
                with open (coordinate_file, 'rb') as stream:
                    points = []
                    timeslice = None
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
                            self.calculate_batched_intersections (shapefile, points, timeslice, counter)
                    if timeslice:
                        self.calculate_batched_intersections (shapefile, points, timeslice, counter)
        self.write_json_object ("occurrences", { "counts" : counter.timeline })
        '''
        
    def geocode_batch_parallel (self, shapefile, output_dir, buffer_size = 1000):
        logger.debug ("Export polygons...")
        polygon_count = self.export_polygons (shapefile)
        logger.debug ("Geocode...")

        work_Q = multiprocessing.Queue ()
        result_Q = multiprocessing.Queue ()
        cpus = multiprocessing.cpu_count ()

        counting_process = multiprocessing.Process (target=counting_worker, args=(polygon_count, result_Q,))
        counting_process.start ()

        workers = []
        for c in range (cpus - 1):
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

    def calculate_batched_intersections (self, shapefile, points, timeslice, counter):
        if len (points) > 0:
            logger.debug ("Calculating intersections of batched points: %s", points)
            c = 0
            polygon_matches = self.find_polygon_intersections_batch (shapefile, points)
            for polygon_id, matches in enumerate (polygon_matches):
                for point_idx, state in enumerate (matches):
                    if state == True:
                        #logger.info ("%s: poly:%s point:%s => state:%s.", c, polygon_id, point_idx, state)
                        counter.increment (polygon_id, timeslice)
                        c += 1
            logger.info ("Recorded %s/%s matches at timeslice: %s", c, len (points), timeslice)
            del points [0 : len (points)]

    def calculate_batched_intersections_parallel (self, shapefile, points, timeslice, result_Q):
        if len (points) > 0:
            #logger.debug ("Calculating intersections of batched points: %s (parallel)", points)
            c = 0
            polygon_matches = self.find_polygon_intersections_batch (shapefile, points)
            for polygon_id, matches in enumerate (polygon_matches):
                for point_idx, state in enumerate (matches):
                    if state == True:
                        #logger.info ("%s: poly:%s point:%s => state:%s.", c, polygon_id, point_idx, state)
                        result_Q.put ( [ polygon_id, timeslice ] )
                        c += 1
            logger.info ("Recorded %s/%s matches at timeslice: %s", c, len (points), timeslice)
            del points [0 : len (points)]

def counting_worker (polygon_count, result_Q):
    counter = Counter (polygon_count)
    geocoder = Geocoder ()
    while True:
        #logger.debug ("Getting counting work")
        work = result_Q.get (timeout = 5)
        if not work:
            logger.debug ("Got null work on result Q.")
            continue
        if work == 'done':
            logger.debug ("Counter process notified of completion.")
            break
        #logger.debug ("** Got counting work: %s", work)
        if len (work) < 2:
            continue
        try:
            counter.increment (work [0], work [1])
        except e:
            traceback.print_exc (e)
    obj = { "counts" : counter.timeline }
    geocoder.write_json_object ("occurrences", obj)

def point_in_poly_worker (work_Q, result_Q, shapefile, buffer_size = 1000):
    geocoder = Geocoder ()
    while True:
        logger.debug ("Taking work from queue.")
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

def crunch (file_name, database, idx, output_dir):
    stats = defaultdict (int)
    columns = None
    with open (file_name, 'rb') as stream:
        columns = stream.readline ().strip().split ('\t')
    cruncher = DataCruncher (columns, database, output_dir)
    cruncher.crunch (file_name, idx, stats)
    
def select_coordinates (database, snapshotDB, output_dir):
    workers = []
    logger.debug ("Queueing import/select work...")
    for root, dirnames, filenames in os.walk (snapshotDB):
        for idx, file_name in enumerate (fnmatch.filter (filenames, 'person.*')):
            snapshot_file = os.path.join (root, file_name)
            logger.debug ("launch-snap[%s] %s", idx, snapshot_file)
            crunch_args = (snapshot_file, database, idx, output_dir)
            worker_name = 'worker:%s' % snapshot_file
            process = multiprocessing.Process (name = worker_name, target = crunch, args = crunch_args)
            workers.append (process)
    for worker in workers:
        worker.start ()        
    for worker in workers:
        worker.join ()
        logger.debug ("Joined process[%s]: %s", worker.pid, worker.name)

def archive (archive_name="out.tar.gz"):
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

def main ():

    ''' Register signal handler. '''
    signal.signal (signal.SIGINT, signal_handler)

    ''' Parse arguments. '''
    parser = argparse.ArgumentParser ()
    parser.add_argument ("shapefile",  help="Path to a shapefile")
    parser.add_argument ("snapshotDB", help="Path to a directory hierarchy containing population snapshot files.")
    parser.add_argument ("--archive",  help="Archive output files.", dest='archive', action='store_true', default=False)
    parser.add_argument ("--output",   help="Output directory. Default is 'output'.", default="output")
    parser.add_argument ("--database", help="Database path. Default is in-memory.")
    parser.add_argument ("--loglevel", help="Log level", default="error")
    args = parser.parse_args ()

    ''' Configure logging. '''
    numeric_level = getattr (logging, args.loglevel.upper (), None)
    assert isinstance (numeric_level, int), "Undefined log level: %s" % args.loglevel
    logging.basicConfig (level=numeric_level, format='%(asctime)-15s %(message)s')
    #logger.setLevel (logging.DEBUG)

    database = args.database if args.database else ":memory:"
    shapefile = args.shapefile
    snapshotDB = args.snapshotDB
    output_dir = args.output if args.output else "output"
    
    select_coordinates (database, snapshotDB, output_dir)
    geocoder = Geocoder ()
    geocoder.geocode_batch_parallel (shapefile, output_dir)

    if args.archive:
        archive ()
        
    sys.exit (0)

if __name__ == '__main__':
    main ()





    '''
    def find_polygon_intersections (self, shapefile, points):
        polygon_id = -1
        result = polygon_id
        from fiona import collection
        with collection (shapefile, "r") as source:
            # cache features on big memory machine for performance...?
            for feature in source:
                polygon_id += 1
                geometry = feature ['geometry']
                assert geometry ['type'] == "Polygon", "Non-polygon object encountered reading %s" % shapefile
                results = self.calculate_polygon_intersection (geometry, points)
                #logger.debug ("Results: %s", results)
                if True in results:
                    #logger.debug ("  ** match ** ")
                    result = polygon_id
                    break
        return result
    '''
    


    '''
    def get_tables (self):
        self.cur.execute ("SELECT * FROM sqlite_master WHERE type='table'")
        rows = self.cur.fetchall ()
        for row in rows:
            print row
            '''
            
'''
./count.py --loglevel debug /projects/systemsscience/var/census2010/tl_2010_37_county10.shp /projects/systemsscience/out/population.tsv.0035.control/ --output /projects/systemsscience/var/geo

'''





'''
def run_parallel (cruncher, file_name):
    job_server = pp.Server ()
    jobs = []
    jobs.append (job_server.submit (crunch,
                                    (cruncher, file_name),
                                    (),
                                    ('csv', 'sqlite3', 'sys', 'time' )))
    job_server.wait ()
    job_server.print_stats ()
'''    


''' disk

import time: 3.82165813446
(u'35.6430151', u'-79.6991996')
select time: 0.107641935349
import time: 2.382584095
(u'35.6430151', u'-79.6991996')
select time: 0.522714138031
import time: 2.38607907295
(u'35.6430151', u'-79.6991996')
select time: 0.110921859741
import time: 2.32170701027
(u'35.6430151', u'-79.6991996')
select time: 0.421972990036
import time: 2.42829704285
(u'35.6430151', u'-79.6991996')
select time: 0.110719919205
import time: 2.40624713898
(u'35.6430151', u'-79.6991996')
select time: 0.483468055725
import time: 2.38340306282
(u'35.6430151', u'-79.6991996')
select time: 0.121204853058
import time: 2.4531891346
(u'35.6430151', u'-79.6991996')
select time: 0.478306055069
import time: 2.38926005363
(u'35.6430151', u'-79.6991996')
select time: 0.114572048187
import time: 2.46342396736
(u'35.6430151', u'-79.6991996')
select time: 0.477952003479

'''


''' memory

import time: 1.36247396469
(u'35.6430151', u'-79.6991996')
select time: 0.0546560287476
import time: 1.37978482246
(u'35.6430151', u'-79.6991996')
select time: 0.0557789802551
import time: 1.39188408852
(u'35.6430151', u'-79.6991996')
select time: 0.0572199821472
import time: 1.38933706284
(u'35.6430151', u'-79.6991996')
select time: 0.055969953537
import time: 1.38284993172
(u'35.6430151', u'-79.6991996')
select time: 0.0557329654694
import time: 1.384567976
(u'35.6430151', u'-79.6991996')
select time: 0.0554988384247
import time: 1.37177419662
(u'35.6430151', u'-79.6991996')
select time: 0.0564949512482
import time: 1.38096904755
(u'35.6430151', u'-79.6991996')
select time: 0.0551209449768
import time: 1.38450908661
(u'35.6430151', u'-79.6991996')
select time: 0.0565440654755
import time: 1.38226723671
(u'35.6430151', u'-79.6991996')
select time: 0.0551180839539


'''
