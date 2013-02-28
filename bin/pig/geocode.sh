#!/bin/bash

ROOT=/home/scox
APP=$ROOT/app
DEV=$ROOT/dev
MVN=$ROOT/.m2/repository

PIG_JAR=$APP/pig-0.11.0/pig-0.11.0.jar
PIG_JAR=$PIG_JAR:$APP/pig-0.11.0/lib/jython-standalone-2.7-b1.jar
UDF_JAR=$DEV/crcsim/pig/target/pig-1.0-SNAPSHOT.jar
UDF_JAR=$UDF_JAR:$DEV/crcsim/common/geography/target/epi-geography-1.0-SNAPSHOT-deps.jar

java -cp $PIG_JAR:$UDF_JAR org.apache.pig.Main -x local $*


