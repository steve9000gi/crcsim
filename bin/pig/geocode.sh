#!/bin/bash

set -e
set -x

PIG_JAR=$(find $PIG_PATH -name "*pig-*.jar" -print -follow | egrep -v "hadoop|tutorial")
if [ -d lib ]; then
    PIG_JAR=$PIG_JAR:$(find lib -name "*jython*.jar" -print -follow)
else
    PIG_JAR=$PIG_JAR:$(find ~/.m2 -name "*jython*.jar" -print -follow | grep 2.7)
fi
java -cp $PIG_JAR org.apache.pig.Main -x local $*

exit 0