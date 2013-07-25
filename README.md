canvecj
=======

This is a CanVec dataset extractor for PostGIS, implemented in Java.

The Government of Canada provides a nifty dataset called CanVec* which contains all the features you'd find on a standard NTS map in Shapefile format. The data is provided as zip archives, each corresponding to a map sheet in the NTS system.

The trouble arises when you want to extract a specific feature type across several sheets; you have to navigate a deep folder hierarchy, opening, searching and extracting files as you go.

This Java program is a more intelligent successor to the original Python script (https://github.com/rskelly/canvec). It navigates the CanVec directory (in whatever form it exists on your drive), finds the files that match your search criterion and extracts them to a temporary folder. Then it creates an SQL file containing the instructions to create and populate a table in a PostGIS database with the data. This last step requires that shp2pgsql exist on your PATH. 

This version of the program has several advantages over the Python version:
- Uses the concept of "jobs" to extract many feature sets into separate files.
- Jobs are configured using a file.
- It compiles a table for all the required featuresets across all jobs and extracts them once (which will save you a ton of money if you're using a mounted S3 bucket, for example).
- It caches a list of available archives and their locations, so the directory structure needs to be traversed exactly once.
- Uses the notion of "workers" to split up the jobs.

Here's the invocation:

	java -jar canvec_extractor.jar extractor.jobs

Where canvec_extractor.jar is the executable archive and extractor.jobs is the jobs file (there's a sample of the jobs file in this directory).

NOTE: This program uses Runtime.exec to launch the shp2pgsql program. When it does this, Java forks, creating a new process with the same memory footprint as the original one, which may exceed the available memory on your machine. When this happens, you get an IOException ("Cannot allocate memory"). You can prevent this by declaring the size of the VM's heap on startup. Something like this should do it:

	java -Xms64m -Xmx128m -jar canvec_extractor.jar extractor.jobs

The format of the jobs file is simple. Each job is a single line with five space-separated values. For example:

	# Aboriginal lands
	1690009 public canvec_aboriginal canvec_aboriginal.sql.gz 3347

- 160009 -- the code for "Aboriginal Lands"
- public -- the default schema for postgresql tables.
- canvec_aboriginal -- the name of the PostGIS table.
- canvec_aboriginal.sql.gz -- the name of the output file. If ".gz" is appended, it will be compressed. For raw text, leave the ".gz" off.
- 3347 -- the SRID of the dataset. This will default to 4326, which is the native SRID of the canvec set, so it can be safely left off.

Blank lines and those beginning with # are ignored.

There are several application-wide settings, each of which is prefixed with an @:

	@canvecDir 			The directory where canvec archives are located. To only search a subset of the directories, name a sub-directory.
	@tempDir 			The temporary directory where extracted archives will be stored. Defaults to the system temp directory.
	@numWorkers			The number of workers or threads that will process the files.
	@deleteTempFiles	If false, will prevent the temporary files from being deleted. Useful for debugging. Defaults to true.
	@charset			This is the character set that will be used with the -W parameter in shp2pgsql. Defaults to LATIN1.

This is a Maven project. You can set it up to use eclipse by running 
	
	mvn eclipse:eclipse 
	
in the project root. Build from eclipse, or run 

	mvn clean package 

to compile it.

*http://geogratis.cgdi.gc.ca/geogratis/en/product/search.do?id=5460AA9D-54CD-8349-C95E-1A4D03172FDF