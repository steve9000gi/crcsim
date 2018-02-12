#!/usr/bin/python

"""

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

   Geocode agent based model output data, scaling to terabyte range, possibly more.

Approach:

   Overview:

      Designed for a Hadoop Map-Reduce cluster with Pig. Capacity dependes on the cluster.
      Parses CSV output files recording the state of agents at a series of timeframes
      Uses output data format of the UNC CRC Agent Based Model.

   Process:

      Overview:

         Import data, select location and timeslice information from relevant rows. Geocode
         and output summary data as JSON, optionally, archiving output data files.

      Select:

         Adds Java library files required to support geocoding user defined function (UDF)
         Uses Pig - a relational algebra interface to Hadoop - to parse CSV inputs.
         Compiles and executes Pig script and aggregates outputs.
         Write occurrences data as JSON.

Usage:

     ./geocode.sh geocode.py

"""

import argparse
import glob
import os
import fnmatch
import json
import logging
import socket

from org.apache.pig.scripting import Pig

logger = logging.getLogger (__name__)

class Snout (object):
    ''' A client interface to Pig. '''

    def __init__ (self, jars = [], properties = {}):
        ''' Initialize Pig. '''
        for jar in jars:
            logger.debug (" >>> register jar: %s", jar)
            Pig.registerJar (jar)
        for key in properties:
            logger.debug (" >>> set property: %s => %s", key, properties[key])
            Pig.set (key, properties [key]) 

    def mkparams (self, input_file, shapefile, timeslice):
        ''' Create parameters. '''
        return {}

    def run (self, params, script_name, script_file, elements = []):
        ''' Execute pig. '''
        pig = Pig.compileFromFile (script_name, script_file)
        bound = pig.bind (params) 
        futures = bound.run () if isinstance (params, list) else bound.runSingle ()
        self.handle_future (futures, elements)
        self.complete ()

    def handle_future (self, future, elements):
        ''' Handle results. '''
        if isinstance (future, list):
            for each in future:
                self.handle_future (each)
        else:
            if future.isSuccessful ():
                for element_id in elements:
                    element = future.result (element_id)
                    element_items = element.iterator ()
                    for a_tuple in element_items:
                        self.process_result (element_id, a_tuple)
            else:
                logger.error ("Code: %s Code:%s: Message:%s", future.getReturnCode (), future.getErrorCode (), future.getErrorMessage ())

    def process_result (self, element_id, a_tuple):        
        ''' Process an element. '''        
        logger.info ("processing result: %s", element_id)

    def complete (self):        
        print json.dumps (self.count)        
            

class Geocoder (Snout):
    def __init__(self):
        jars = []
        lib_dir = "lib"
        jar_pattern = os.path.join (lib_dir, "*jar")
        if os.path.exists (lib_dir):
            for lib in glob.glob (jar_pattern):
                jars.append (lib)
        else:
            ROOT="/home/scox"
            DEV = os.path.join (ROOT, "dev")
            jars.append (os.path.join (DEV, "crcsim", "pig", "target", "epi-pig-1.0-SNAPSHOT.jar"))
            jars.append (os.path.join (DEV, "crcsim", "common", "geography", "target", "epi-geography-1.0-SNAPSHOT-deps.jar"))

        properties = { "default_parallel" : "100" }

        super (Geocoder, self).__init__(jars, properties)

        self.count = [ [ 0 for k in range (99) ] for j in range (100) ]

    def mkparams (self, input_file, shapefile, timeslice):
        ''' Create parameters. '''
        return {
            'timeslice' : timeslice,
            'shapefile' : shapefile,
            'input'     : input_file
	    }

    def process_result (self, element_id, a_tuple):        
        ''' Process an element. '''
        if element_id == 'polygon_count':
            polygon_id, timeslice, count = int (a_tuple.get (0)), a_tuple.get (1), a_tuple.get (2)
            timeslice = int (timeslice.split ('.')[-1])
            logger.info ("  --> polygon_id: %s timeslice: %s count: %s", polygon_id, timeslice, count)
            self.count [polygon_id][timeslice] = count

    def complete (self):        
        print json.dumps (self.count)        


def main ():

    ''' Parse arguments. '''
    parser = argparse.ArgumentParser ()
    parser.add_argument ("--shapefile",  help="Path to an ESRI shapefile", default="/home/scox/dev/var/crcsim/census2010/tl_2010_37_county10.shp")
    parser.add_argument ("--snapshotDB", help="Path to a directory hierarchy containing population snapshot files.", default='')
    parser.add_argument ("--loglevel",   help="Log level", default='error')
    parser.add_argument ("--dev",        help="Devlopment settings", dest='development', action='store_true', default=False)
    args = parser.parse_args ()

    logging.basicConfig (level=3, format='%(asctime)-15s %(message)s')

    geocoder = Geocoder ()

    environment_config_context = {
        'scox.europa.renci.org' : {
            'input_dir'    : '/home/scox/dev/hadoop/pig/input',
            #'input_files' : [ 'person.00001', 'person.00002', 'person.00003' ],
            'shapefile'   : '/home/scox/dev/var/crcsim/census2010/tl_2010_37_county10.shp'
            },
        'topsail-sn.unc.edu' : {
            #'input_dir'   : '/scratch/projects/systemsscience/inputs',
            'input_dir'    : '/user/scox/geocode/inputs',
            #'input_files' : [ 'person.00001', 'person.00002', 'person.00003' ],
            #'shapefile'   : '/export/home/scox/dev/pig/shapefile/tl_2010_37_county10.shp'
            'shapefile'   : '/user/scox/geocode/shapefile/tl_2010_37_tabblock10.shp'
           }
        }

    fqdn = socket.getfqdn ()
    config = environment_config_context [fqdn]
    logger.debug ("config @ %s => %s", fqdn, config)
    input_dir = config ['input_dir']
    shapefile = config ['shapefile']
    #input_files = config ['input_files']

    '''
    input_files = []
    for root, dirnames, filenames in os.walk (input_dir):
        #for idx, file_name in enumerate (fnmatch.filter (filenames, '.*')):
        for idx, file_name in enumerate (filenames):
            full_path = os.path.join (root, file_name)
            input_files.append (full_path)
            '''

    input_files = [ '/user/scox/geocode/inputs/person.00001',
                    '/user/scox/geocode/inputs/person.00002',
                    '/user/scox/geocode/inputs/person.00003' ]


    single = True
    if single:
        params = geocoder.mkparams (input_files [0], shapefile, 0)
    else:
        params = []
        for f in input_files:
            params.append (geocoder.mkparams (f, shapefile, 0))

    geocoder.run (params      = params,
                  script_name = "geocode",
                  script_file = "geocode.pig" if os.path.exists ("geocode.pig") else os.path.join ("bin", "geocode.pig"),
                  elements    = [ "polygon_count" ])


if __name__ == '__main__':
    main ()
