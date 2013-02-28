#!/bin/bash

set -e

PIG_JAR=$(find $PIG_PATH -name "*pig-*.jar" -print | grep -v hadoop)
PIG_JAR=$PIG_JAR:$(find lib -name "*jython*.jar" -print)

java -cp $PIG_JAR org.apache.pig.Main -x local $*

exit 0