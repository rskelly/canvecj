canvecj
=======

This is a CanVec dataset extractor for PostGIS, implemented in Java.

The Government of Canada provides a nifty dataset called CanVec* which contains all the features you'd find on a standard NTS map in Shapefile format. The data is provided as zip archives, each corresponding to a map sheet in the NTS system.

The trouble arises when you want to extract a specific feature type across several sheets; you have to navigate a deep folder hierarchy, opening, searching and extracting files as you go.

This Java program is a more intelligent successor to the original Python script (https://github.com/rskelly/canvec). It navigates the CanVec directory (in whatever form it exists on your drive), finds the files that match your search criterion and extracts them to a temporary folder. Then it creates an SQL file containing the instructions to create and populate a table in a PostGIS database with the data. This last step requires that shp2pgsql exist on your PATH. 

This version of the program has several advanteges over the Python version:
- Uses the concept of "jobs" to extract many feature sets into separate files.
- Jobs are configured using a file.
- It compiles a table for all the required featuresets across all jobs and extracts them once (which will save you a ton of money if you're using a mounted S3 bucket, for example).
- It caches a list of available archives and their locations, so the directory structure needs to be traversed exactly once.
- Uses the notion of "workers" to split up the jobs.

Here's the invocation:

	java -jar canvec_extractor.jar extractor.jobs

Where canvec_extractor.jar is the executable archive (whatever you choose to name it) and extractor.jobs is the jobs file (there's a sample of the jobs file in this directory).

The format of the jobs file is simple. Each job is a single line with five space-separated values. For example:

	# Aboriginal lands
	1690009 public canvec_aboriginal canvec_aboriginal.sql true

- 160009 -- the code for "Aboriginal Lands"
- public -- the default schema for postgresql tables.
- canvec_aboriginal -- the name of the PostGIS table.
- canvec_aboriginal.sql -- the name of the output file.
- true -- the value for compression (any other value means false). If true is entered, the sql file will be gzipped on the fly into a file called [sqlfile].gz.

Blank lines and those beginning with # are ignored.

This program is dependent on slf4j. There is a binary in the root folder that contains the dependencies. Invoke it with the following command:

	java -jar canvec_extractor.jar extractor.jobs

* http://geogratis.cgdi.gc.ca/geogratis/en/product/search.do?id=5460AA9D-54CD-8349-C95E-1A4D03172FDF