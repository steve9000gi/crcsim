#!/bin/bash

set -e

DIR=$1
OUT=$2

unset header_output

# Scan all output files below DIR
for f in $(/bin/find $DIR -name "summary_replication*" -print)
do
    echo $f

    # Figure out and output a header
    if [ -z $header_output ]
    then
	header="$(head -1 $f)"
	printf "%s\t%s\t%s\t" age intervention replication > $OUT
        echo $header | sed "s, ,\t,g" >> $OUT
	header_output=true
    fi

    # Collect age, intervention and replication from the file name
    age=$(echo $f | sed -e "s,.*population.tsv.,," -e "s,\..*,,")
    replication=$(echo $f | sed -e "s,.*\.,,")
    intervention=$(basename $( dirname $(echo $f)) | sed -e "s,.*\.,,")
    if [[ "control" = $intervention ]]
	then
	intervention=-1
    fi

    # Output a row.
    printf "%s\t%s\t%s\t" $age $intervention $replication >> $OUT
    grep baseline $f >> $OUT
done

exit 0

