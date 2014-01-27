package ca.dijital.canvec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final String DEFAULT_CHARSET = "latin1";

    private CanvecStore store;
    private List<ExtractorJob> jobs;
    private int numWorkers;
    private List<ExtractorWorker> workers;
    private List<File> tempFiles;

    private boolean useStdOut = false;
    private boolean deleteTempFiles = true;
    private String charset = DEFAULT_CHARSET;

    private String tempDir;

    /**
     * Construct a new Extractor.
     */
    public Extractor() {
	jobs = Collections.synchronizedList(new ArrayList<ExtractorJob>());
	tempFiles = Collections.synchronizedList(new ArrayList<File>());
	numWorkers = 5;
    }

    /**
     * The directory where CanVec archives are stored.
     * 
     * @param canvecSourcePath
     */
    public void setCanvecStore(CanvecStore store) {
	this.store = store;
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
	if (tempFile != null)
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
		    if (deleteTempFiles)
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
	    initWorkers();
	    ExtractorJob job = null;
	    while (jobs.size() > 0 && (job = jobs.remove(0)) != null) {
		ExtractorWorker worker = null;
		// TODO: Replace with wait.
		while ((worker = getFreeWorker()) == null)
		    Thread.sleep(500);
		worker.start(this, job, store, useStdOut);
		if (worker.isFailure()) {
		    logger.info("There was a failure in a worker. Shutting down...");
		    stopAllWorkers();
		    break;
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
     * Parse the jobs file and construct a list of {@link ExtractorJob}s.
     * 
     * @param jobsFile
     * @return
     * @throws IOException
     * @throws Exception
     */
    private static List<ExtractorJob> parseJobsFile(File jobsFile,
	    Map<String, String> config, Map<String, String> storeConfig)
	    throws IOException {
	List<ExtractorJob> jobs = new ArrayList<ExtractorJob>();
	BufferedReader in = new BufferedReader(new FileReader(jobsFile));
	try {
	    String line = null;
	    while ((line = in.readLine()) != null) {
		line = line.trim();
		if (line.startsWith("#") || line.length() == 0)
		    continue;
		if (line.startsWith("@")) {
		    String[] parts = line.split(" ");
		    String name = parts[0].substring(1).trim();
		    String value = parts[1].trim();
		    if ("storeProperty".equals(name)) {
			if (value == null || value.length() < 3
				|| value.indexOf("=") == -1)
			    throw new IOException(
				    "The storeProperty value {} is invalid."
					    .replace("{}", value));
			String[] props = value.split("=");
			storeConfig.put(props[0], props[1]);
		    } else {
			config.put(name, value);
		    }
		} else {
		    String[] parts = line.split(" ");
		    if (parts.length < 4) {
			logger.warn(
				"Each job configuration must have at least 4 properties: {}",
				line);
			continue;
		    }
		    ExtractorJob job = new ExtractorJob();
		    job.setPattern(parts[0].trim());
		    job.setSchemaName(parts[1].trim());
		    job.setTableName(parts[2].trim());
		    job.setOutFile(parts[3].trim());
		    if (parts.length > 4) {
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
	} finally {
	    in.close();
	}
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
	Map<String, String> storeConfig = new HashMap<String, String>();

	List<ExtractorJob> jobs = null;
	try {
	    jobs = parseJobsFile(jobsFile, config, storeConfig);
	} catch (IOException e) {
	    logger.error("Failed to parse jobs file.", e);
	    System.exit(1);
	}

	// If there are no jobs, just quit.
	if (jobs == null || jobs.size() == 0) {
	    logger.error("No jobs to process. Quitting.");
	    System.exit(1);
	}

	// Build and configure the extractor.
	Extractor extractor = new Extractor();

	CanvecStore store = null;

	// Set the props from the jobs file.
	try {
	    for (String key : config.keySet()) {
		String value = config.get(key);
		if ("storeClass".equals(key)) {
		    store = getStoreByClassName(value);
		} else if ("numWorkers".equals(key)) {
		    extractor.setNumWorkers(Integer.parseInt(value));
		} else if ("charset".equals(key)) {
		    extractor.setCharset(value);
		} else if ("tempDir".equals(key)) {
		    extractor.setTempDir(value);
		} else {
		    throw new Exception(
			    "An unknown program configuration was found: {}."
				    .replace("{}", key));
		}
	    }

	    store.configure(storeConfig);
	    extractor.setCanvecStore(store);
	    // Add the jobs.
	    for (ExtractorJob job : jobs)
		extractor.addJob(job);
	    extractor.execute(useStdOut);

	} catch (Exception e) {
	    logger.error(e.getMessage());
	    System.exit(1);
	}
    }

    /**
     * Sets the location of the temporary work dir.
     * 
     * @param tempDir
     */
    public void setTempDir(String tempDir) {
	this.tempDir = tempDir;
    }

    /**
     * Create a new {@link CanvecStore} using the given class name.
     * 
     * @param value
     * @return
     * @throws Exception
     */
    private static CanvecStore getStoreByClassName(String value)
	    throws Exception {
	try {
	    CanvecStore store = (CanvecStore) Class.forName(value)
		    .newInstance();
	    return store;
	} catch (Exception e) {
	    throw new Exception("Unable to instantiate CanvecStore.", e);
	}
    }

    /**
     * Returns the name of the configured characer set. Defaults to latin1.
     * 
     * @return
     */
    public String getCharset() {
	return charset;
    }

    /**
     * Sets the character set.
     * 
     * @param charset
     */
    public void setCharset(String charset) {
	this.charset = charset;
    }

    public String getTempDir() {
	return tempDir;
    }

}
