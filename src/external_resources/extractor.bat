rem This script runs the CanVec extractor using the arguments
rem configured below, and send the output to stdout.
rem You can pipe the output directly into PostGIS like so:
rem
rem ./extractor.bat|psql -d mydatabase
rem
rem Assuming you run the extractor as a user with privileges on the 
rem database.

rem Limiting the JVM's heap allows it to fork when it invokes
rem the shp2pgsql program.

set JAVA_OPTS=-Xms64m -Xmx128m

set EXTRACTOR_JAR=canvec_extractor.jar
set EXTRACTOR_JOBS=extractor.jobs

java %JAVA_OPTS% -jar %EXTRACTOR_JAR% %EXTRACTOR_JOBS% -

