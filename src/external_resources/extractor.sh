#!/bin/sh

# This script runs the CanVec extractor using the arguments
# configured below, and send the output to stdout.
# You can pipe the output directly into PostGIS like so:
#
# ./extractor.sh|psql -d mydatabase
#
# Assuming you run the extractor as a user with privileges on the 
# database.

# Limiting the JVM's heap allows it to fork when it invokes
# the shp2pgsql program.

JAVA_OPTS=-Xms64m -Xmx128m

EXTRACTOR_JAR=canvec_extractor.jar
EXTRACTOR_JOBS=extractor.jobs

java $JAVA_OPTS -jar $EXTRACTOR_JAR $EXTRACTOR_JOBS -

