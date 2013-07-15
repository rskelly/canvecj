package ca.dijital.canvec;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts CanVec data from the archive set into PostGIS sql files, based on a
 * configurable pattern matching strategy.
 * 
 * @author Rob Skelly <rob@dijital.ca>
 */
public class Extractor {

    private static Logger logger = LoggerFactory.getLogger(Extractor.class);

    private static final String FILE_TABLE_CACHE_FILE = "canvec_extractor_file_table.dat";
	private static final String DEFAULT_CHARSET = "latin1";
	
    private String canvecDir;
    private String tempDir;
    private List<ExtractorJob> jobs;
    private int numWorkers;
    private List<ExtractorWorker> workers;
    private List<File> tempFiles;

    private boolean useStdOut = false;
    private boolean deleteTempFiles = true;
    private boolean compressOutput = false;
	private String charset = DEFAULT_CHARSET;
	
    /**
     * Construct a new Extractor.
     */
    public Extractor() {
		tempDir = System.getProperty("java.io.tmpdir");
		jobs = Collections.synchronizedList(new ArrayList<ExtractorJob>());
		tempFiles = Collections.synchronizedList(new ArrayList<File>());
		numWorkers = 5;
    }

    /**
     * The directory where CanVec archives are stored.
     * 
     * @param canvecSourcePath
     */
    public void setCanvecDir(String canvecDir) {
		this.canvecDir = canvecDir;
    }

    /**
     * Set the temporary work directory. Archives will be extracted to here.
     * 
     * @param tempDir
     */
    public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
    }

    /**
     * If true, output will be compressed using gzip.
     * 
     * @param compressOutput
     */
    public void setCompressOutput(boolean compressOutput) {
		this.compressOutput = compressOutput;
    }

    /**
     * If true, temporary files will be deleted.
     * 
     * @param deleteTempFiles
     */
    public void setDeleteTempFiles(boolean deleteTempFiles) {
		this.deleteTempFiles = deleteTempFiles;
    }


    /**
     * Set the temporary work directory. Archives will be extracted to here.
     */
    public String getTempDir() {
		return tempDir;
    }

    /**
     * Add an {@link ExtractorJob} to the jobs list.
     * 
     * @param job
     */
    public void addJob(ExtractorJob job) {
		jobs.add(job);
    }

    /**
     * Adds a temp file to the list. Will be used to stream output if set to use
     * STDOUT.
     * 
     * @param tempFile
     */
    void addTempFile(File tempFile) {
		tempFiles.add(tempFile);
    }

    /**
     * Called by each worker as it finishes. Checks whether there are remaining
     * workers. Finishes up if not.
     */
    void workerFinished(final ExtractorWorker worker) {
		ExtractorJob job = worker.getJob();
		File tempFile = job.getTempFile();
		if(tempFile != null)
			tempFiles.add(tempFile);
		if (jobs.size() == 0)
			writeFinalOutput();
		logger.info("Finished.");
    }

    /**
     * If useStdOut has been set to true, outputs all the temporary output files
     * to STDOUT before exiting.
     */
    private void writeFinalOutput() {
		if (!useStdOut)
			return;
		File file = null;
		InputStream in = null;
		while (tempFiles.size() > 0 && (file = tempFiles.remove(0)) != null) {
			int read = 0;
			byte[] buf = new byte[4096];
			try {
			in = new FileInputStream(file);
			while ((read = in.read(buf)) > -1) {
				System.out.write(buf, 0, read);
			}
			} catch (IOException e) {
			logger.error("Failed to output a temporary file. Quitting.");
			System.exit(1);
			} finally {
			try {
				in.close();
			} catch (IOException e1) {
			}
			try {
				if(deleteTempFiles)
				file.deleteOnExit();
			} catch (Exception e) {
			}
			}
		}
    }

    /**
     * Starts the extractor. If the useStdOut param is true, all output will be
     * streamed to STDOUT regardless of the settings in the jobs file. This
     * allows output to be piped directly into PostGIS, like so:
     * 
     * <pre>
     * java -jar canvec_extractor.jar extractor.jobs -|psql -d mydatabase
     * </p>
     * 
     * Keep in mind that this will DROP ALL EXISTING TABLES that have the same name, so be VERY CAREFUL!
     * 
     * @param useStdOut
     */
    public void execute(boolean useStdOut) {
		logger.info("Starting CanVec extractor. Using STDOUT: " + useStdOut);
		this.useStdOut = useStdOut;
		if (jobs.size() == 0) {
			logger.error("No jobs. Exiting.");
			return;
		}
		logger.info("Checking jobs...");
		for (ExtractorJob job : jobs) {
			if (!job.isValid()) {
			logger.error("Job {} is invalid. Stopping.", job.getName());
			return;
			}
		}
		try {
			extractFiles();
			initWorkers();
			ExtractorJob job = null;
			while (jobs.size() > 0 && (job = jobs.remove(0)) != null) {
			if (job.getFiles().size() == 0) {
				logger.warn("No files available for job with pattern: {}.",
					job.getPattern());
			} else {
				ExtractorWorker worker = null;
				while ((worker = getFreeWorker()) == null)
				Thread.sleep(500);
				worker.start(this, job, useStdOut);
				if (worker.isFailure()) {
				logger.info("There was a failure in a worker. Shutting down...");
				stopAllWorkers();
				break;
				}
			}
			}
		} catch (Exception e) {
			logger.error("Failed to execute", e);
		}
		logger.info("Done.");
    }

    /**
     * Stop all running workers.
     */
    private void stopAllWorkers() {
		for (ExtractorWorker worker : workers)
			worker.stop();
    }

    /**
     * Returns the first free {@link ExtractorWorker}.
     * 
     * @return
     */
    private ExtractorWorker getFreeWorker() {
		for (ExtractorWorker worker : workers) {
			if (!worker.isBusy())
			return worker;
		}
		return null;
    }

    /**
     * Initialize a collection of {@link ExtractorWorker}s.
     */
    private void initWorkers() {
		workers = new ArrayList<ExtractorWorker>();
		for (int i = 0; i < numWorkers; ++i)
			workers.add(new ExtractorWorker());
    }

    /**
     * Sets the number of {@link ExtractorWorker}s to use.
     * 
     * @param numWorkers
     */
    public void setNumWorkers(int numWorkers) {
		this.numWorkers = numWorkers;
    }

    /**
     * Extracts the required files from the CanVec archives, and appends a
     * {@link File} object to each {@link ExtractorJob}'s file list.
     * 
     * If a list of archives has been cached, the program will use the cached
     * list, otherwise, it will walk the directory recursively to catalogue the
     * files and build a new cache.
     * 
     * @throws IOException
     */
    public void extractFiles() throws IOException {
		// Get the list of zip files.
		List<File> archives = getArchives(false);

		logger.info("Extracting " + archives.size() + " archives to " + tempDir
			+ ".");

		// A set to keep track of files that have already been extracted.
		Set<String> extracted = new HashSet<String>();
		// Cache for file objects.
		Map<String, File> fileCache = new HashMap<String, File>();
		// Iterate over the archives. If an entry in an archive matches one of
		// our jobs' feature IDs, we'll unzip it.
		for (File file : archives) {
			logger.info("Extracting " + file.getName() + ".");
			ZipFile archive = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = archive.entries();
			while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			String entryName = entry.getName();
			// Iterate over jobs to find a match.
			for (ExtractorJob job : jobs) {
				String pattern = job.getPattern();
				Matcher matcher = Pattern.compile(pattern).matcher(
					entry.getName());
				// If an entry matches the pattern, add it to the extract
				// set.
				if (matcher.find()) {
				if (!fileCache.containsKey(entryName))
					fileCache.put(entryName, new File(tempDir,
						entryName));
				File outFile = fileCache.get(entryName);
				if(deleteTempFiles)
					outFile.deleteOnExit();
				job.addFile(outFile);
				// If it's already extracted skip it.
				if (!extracted.contains(entryName)) {
					try {
					saveZipEntry(archive, entry, outFile);
					extracted.add(entryName);
					} catch (IOException e) {
					logger.error("Failed to unzip an archive.", e);
					}
				}
				}
			}
			}
			archive.close();
		}
    }

    /**
     * Save a {@link ZipEntry} to a file.
     * 
     * @param archive
     * @param entry
     * @param outFile
     * @throws IOException
     */
    private void saveZipEntry(ZipFile archive, ZipEntry entry, File outFile)
	    throws IOException {
		if (!outFile.exists()) {
			InputStream zin = archive.getInputStream(entry);
			OutputStream zout = new BufferedOutputStream(new FileOutputStream(
				outFile));
			int read;
			byte[] buf = new byte[4096];
			while ((read = zin.read(buf)) > 0)
			zout.write(buf, 0, read);
			zout.close();
			zin.close();
		}
    }

    /**
     * Returns the contents of a text file as a list of {@link Strings}.
     * 
     * @param cacheFile
     * @return
     * @throws IOException
     */
    private List<String> getLines(File cacheFile) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(cacheFile));
		String line = null;
		while ((line = in.readLine()) != null)
			lines.add(line);
		in.close();
		return lines;
    }

    /**
     * Returns a list of {@link File}s corresponding to Zip files in the CanVec
     * folder. If {@code discardCache} is false, attempts to read from a cached
     * list of files.
     * 
     * @param discardCache
     * @return
     * @throws IOException
     */
    private List<File> getArchives(boolean discardCache) throws IOException {
		logger.info("Enumerating archives (discarding cache: " + discardCache
			+ ")");
		if (tempDir == null)
			throw new IOException("The temp dir has not been configured.");
		if (canvecDir == null)
			throw new IOException("The canvec dir has not been configured.");
		List<File> archives = null;
		// Create the cache file, then try to create its parent, if it doesn't
		// exist.
		File cacheDir = new File(tempDir, "canvec");
		if (!cacheDir.exists() && !cacheDir.mkdirs())
			throw new IOException("The temporary directory, " + tempDir
				+ "/canvec, does not exist and could not be created.");
		File cacheFile = new File(cacheDir, FILE_TABLE_CACHE_FILE);
		if (!discardCache && (cacheFile.exists() || cacheFile.canRead())) {
			archives = new ArrayList<File>();
			for (String line : getLines(cacheFile))
			archives.add(new File(line));
		} else {
			archives = findArchives(new File(canvecDir));
			BufferedWriter out = new BufferedWriter(new FileWriter(cacheFile));
			for (File file : archives)
			out.write(file.getAbsolutePath() + "\n");
			out.close();
		}
		return archives;
    }

    /**
     * Recursively searches a path for Zip files.
     * 
     * @param path
     * @return
     */
    private static List<File> findArchives(File path) {
		List<File> files = new ArrayList<File>();
		if (path.isDirectory()) {
			for (File sub : path.listFiles()) {
			if (sub.isDirectory()) {
				List<File> subFiles = findArchives(sub);
				files.addAll(subFiles);
			} else if (sub.getName().toLowerCase().endsWith(".zip")) {
				files.add(sub);
			}
			}
		}
		return files;
    }

    /**
     * Parse the jobs file and construct a list of {@link ExtractorJob}s.
     * 
     * @param jobsFile
     * @return
     * @throws IOException
     * @throws Exception
     */
    private static List<ExtractorJob> parseJobsFile(File jobsFile,
	    Map<String, String> config) throws IOException {
		List<ExtractorJob> jobs = new ArrayList<ExtractorJob>();
		BufferedReader in = new BufferedReader(new FileReader(jobsFile));
		String line = null;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("#") || line.length() == 0)
				continue;
			if (line.startsWith("@")) {
				String[] parts = line.split(" ");
				String name = parts[0].substring(1).trim();
				String value = parts[1].trim();
				config.put(name, value);
			} else {
				String[] parts = line.split(" ");
				if (parts.length < 4) {
					logger.warn("Each job configuration must have six properties: {}", line);
					continue;
				}
				ExtractorJob job = new ExtractorJob();
				job.setPattern(parts[0].trim());
				job.setSchemaName(parts[1].trim());
				job.setTableName(parts[2].trim());
				job.setOutFile(parts[3].trim());
				if(parts.length > 4) {
					try {
						int srid = Integer.parseInt(parts[4].trim());
						job.setSrid(srid);
					} catch (NumberFormatException e) {
						logger.error("A value was given for the SRID, but it was invalid. Using the default.");
					}
				}
				jobs.add(job);
				logger.info("Loaded job: " + job.toString());
			}
		}
		in.close();
		return jobs;
    }

    /**
     * Runs the CanVec Extractor program. One parameter is expected: a path to a
     * jobs file.
     * 
     * @param args
     *            One argument is required: a path to a jobs file.
     * @throws IOException
     */
    public static void main(String[] args) {
		// If there are no args, just quit with a message.
		if (args.length == 0) {
			logger.error("Usage: java -jar canvec_extractor.jar <configuration> [stdout]");
			System.exit(1);
		}
		// Check the jobs file. Exit with a message if it's unreadable.
		File jobsFile = new File(args[0]);
		if (!jobsFile.exists() || !jobsFile.canRead()) {
			logger.error("The given configuration file does not exist or cannot be read.");
			System.exit(1);
		}
		// If there's a second parameter, and it's "-", stream the output to
		// stdout,
		// regardless of the output files specified in the jobs file.
		boolean useStdOut = false;
		if (args.length > 1 && "-".equals(args[1]))
			useStdOut = true;
		// Parse the jobs file; build the jobs list and config map.
		Map<String, String> config = new HashMap<String, String>();
		List<ExtractorJob> jobs = null;
		try {
			jobs = parseJobsFile(jobsFile, config);
		} catch (IOException e) {
			logger.error("Failed to parse jobs file.", e);
			System.exit(1);
		}
		// If there are no jobs, just quit.
		if (jobs == null || jobs.size() == 0) {
			logger.error("No jobs to process. Quitting.");
			System.exit(0);
		}
		// Build and configure the extractor.
		Extractor extractor = new Extractor();
		// Set the global props from the jobs file.
		for (String key : config.keySet()) {
			if ("canvecDir".equals(key)) {
				extractor.setCanvecDir(config.get(key));
			} else if ("tempDir".equals(key)) {
				extractor.setTempDir(config.get(key));
			} else if ("numWorkers".equals(key)) {
				try {
					extractor.setNumWorkers(Integer.parseInt(config.get(key)));
				} catch (Exception e) {
					logger.error("The value for numWorkers was invalid.", e);
					System.exit(1);
				}
			} else if("deleteTempFiles".equals(key)) {
				extractor.setDeleteTempFiles("true".equals(config.get(key)));
			} else if("compress".equals(key)) {
				extractor.setCompressOutput("true".equals(config.get(key)));
			} else if("charset".equals(key)) {
				extractor.setCharset(config.get(key));
			} else {
				logger.warn("An unknown program configuration was found: {}.", key);
			}
		}
		// Add the jobs.
		for (ExtractorJob job : jobs)
			extractor.addJob(job);
		extractor.execute(useStdOut);
    }

    /**
     * Returns true if output should be compressed.
     * @return
     */
    public boolean isCompressOutput() {
		return compressOutput;
    }

	public String getCharset() {
		return charset;
	}
	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
}
