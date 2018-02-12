#!/usr/bin/env python

import os
import fnmatch
import glob
import sys
import logging

logger = logging.getLogger (__name__)
logging.basicConfig (level=3, format='%(asctime)-15s %(message)s')

input_dir = sys.argv [1]
output_file = sys.argv [2]
header_output = False

out_stream = open (output_file, 'w')

for root, dirname, filename in os.walk (input_dir):
    for idx, file_name in enumerate (fnmatch.filter (filename, 'summary_replication.*')):
        path = os.path.join (root, file_name)
        stream = None
        logger.error (path)
        try:
            stream = open (path, 'r')
            if not header_output:
                header = stream.readline ()
                out_stream.write ("age\tintervention\treplication\t")
                out_stream.write (header)
                header_output = True
            else:
                stream.readline ()

            age = path.split ('/')[-2].split ('.')[2]

            scenario = path.split ('/')[-2].split ('.')

            ''' account for control vs intervention format. '''
            intervention = scenario [4] if len(scenario) > 4 else scenario [3]

            replication = path.split ('/')[-1].split ('.')[2]
            if 'control' == intervention:
                intervention = -1

            out_stream.write ("%s\t%s\t%s\t" % (age, intervention, replication))
            out_stream.write (stream.readline ())
        finally:
            if stream:
                stream.close ()
sys.exit (0)

