#!/bin/bash

cat synth_hh_2005_2009_v1_NC_zipcode.txt | tr "," "," | sed -e "s,\",,g" -e "s,ZIP,zipcode," -e "s,.000000,," >  synth_hh_2005_2009_v1_NC_tsv_zipcode.txt