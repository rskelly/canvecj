package ca.dijital.geo.canvec;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
 *
 */
public class Extractor {

	private static Logger logger = LoggerFactory.getLogger(Extractor.class);
	
	private static final String FILE_TABLE_CACHE_FILE = "/tmp/canvec_extractor_file_table";
	private static final String TEMP_DIR = "/tmp";
	private static final String CANVEC_SOURCE_DIR = "./canvec";
	
	private String canvecSourceDir = CANVEC_SOURCE_DIR;
	private String tempDir = TEMP_DIR;
	private List<ExtractorJob> jobs;
	private int numWorkers;
	private List<ExtractorWorker> workers;
	
	/**
	 * Construct a new Extractor.
	 */
	public Extractor(){
		jobs = new ArrayList<ExtractorJob>();
		numWorkers = 5;
	}
	
	/**
	 * The directory where canvec archives are stored.
	 * @param canvecSourcePath
	 */
	public void setCanVecSourcePath(String canvecSourcePath){
		this.canvecSourceDir = canvecSourcePath;
	}
	
	/**
	 * Set the temporary work directory. Archives will be extracted to here.
	 * @param tempDir
	 */
	public void setTempDir(String tempDir){
		this.tempDir = tempDir;
	}
	
	/**
	 * Add an ExtractorJob to the jobs.
	 * @param job
	 */
	public void addJob(ExtractorJob job){
		jobs.add(job);
	}	
	
	/**
	 * Starts the extractor.
	 */
	public void execute(){
		logger.debug("Starting CanVec extractor...");
		if(jobs.size() == 0){
			logger.error("No jobs. Exiting.");
			return;
		}
		for(ExtractorJob job:jobs){
			if(!job.isValid()){
				logger.error("Job ", job.getName(), " is invalid. Stopping.");
				return;
			}
		}
		try{
			extractFiles();
			initWorkers();
			for(ExtractorJob job:jobs){
				if(job.getFiles().size() == 0){
					logger.warn("No files available for job with pattern: ", job.getPattern());
				}else{
					ExtractorWorker worker = null;
					while((worker = getFreeWorker()) == null)
						Thread.sleep(500);
					worker.start(job);
				}
			}
			while(true){
				if(!hasBusyWorker())
					break;
				Thread.sleep(500);
			}
			for(ExtractorJob job:jobs){
				Set<File> files = job.getFiles();
				for(File file:files){
					try{
						file.delete();
					}catch(Exception e){
						logger.warn("Failed to delete file: ", file.getName(), " (may already be deleted).");
					}
				}
			}
		}catch(Exception e){
			logger.error("Failed to execute", e);
		}
		logger.debug("Done.");
	}
	
	/**
	 * Returns the first free worker.
	 * @return
	 */
	private ExtractorWorker getFreeWorker(){
		for(ExtractorWorker worker:workers){
			if(!worker.isRunning())
				return worker;
		}
		return null;
	}
	
	/**
	 * Returns true if there is at least one worker busy.
	 * @return
	 */
	private boolean hasBusyWorker(){
		for(ExtractorWorker worker:workers){
			if(worker.isRunning())
				return true;
		}
		return false;
	}
	
	/**
	 * Initialize a collection of workers.
	 */
	private void initWorkers(){
		workers = new ArrayList<ExtractorWorker>();
		for(int i=0;i<numWorkers;++i)
			workers.add(new ExtractorWorker());
	}
	
	/**
	 * Appends the files required for each job to the job objects.
	 * @return
	 * @throws IOException
	 */
	public void extractFiles() throws IOException{
		// Get the list of zip files.
		List<File> archives = getArchives(false);
		// A set to keep track of files that have already been extracted.
		Set<String> extracted = new HashSet<String>();
		// Cache for file objects.
		Map<String, File> fileCache = new HashMap<String, File>();
		// Iterate over the archives. If an entry in an archive matches one of
		// our jobs' feature IDs, we'll unzip it.
		for(File file:archives){
			ZipFile archive = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = archive.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry = entries.nextElement();
				String entryName = entry.getName();
				// Iterate over jobs to find a match.
				for(ExtractorJob job:jobs){
					String pattern = job.getPattern();
					Matcher matcher = Pattern.compile(pattern).matcher(entry.getName());
					// If an entry matches the pattern, add it to the extract set.
					if(matcher.find()){
						if(!fileCache.containsKey(entryName))
							fileCache.put(entryName, new File(tempDir, entryName));
						File outFile = fileCache.get(entryName);
						job.addFile(outFile);
						// If it's already extracted skip it.
						if(!extracted.contains(entryName)){
							try{
								saveZipEntry(archive, entry, outFile);
								extracted.add(entryName);
							}catch(IOException e){
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
	 * Save a ZipEntry to a file.
	 * @param archive
	 * @param entry
	 * @param outFile
	 * @throws IOException
	 */
	private void saveZipEntry(ZipFile archive, ZipEntry entry, File outFile) throws IOException{
		InputStream zin = archive.getInputStream(entry);
		OutputStream zout = new BufferedOutputStream(new FileOutputStream(outFile));
		int read;
		byte[] buf = new byte[4096];
		while((read = zin.read(buf)) > 0)
			zout.write(buf, 0, read);
		zout.close();
		zin.close();
	}

	/**
	 * Returns the contents of a text file as a list of Strings.
	 * @param cacheFile
	 * @return
	 * @throws IOException
	 */
	private List<String> getLines(File cacheFile) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(cacheFile));
		String line = null;
		while((line = in.readLine()) != null)
			lines.add(line);
		in.close();
		return lines;
	}

	/**
	 * Returns a list of File objects corresponding to zip files in the canvec folder.
	 * If discardCache is false, attempts to read from a cached list of files.
	 * @param discardCache
	 * @return
	 * @throws IOException
	 */
	private List<File> getArchives(boolean discardCache) throws IOException{
		List<File> archives = null;
		File cacheFile = new File(FILE_TABLE_CACHE_FILE);
		if(!discardCache && (cacheFile.exists() || cacheFile.canRead())){
			archives = new ArrayList<File>();
			for(String line:getLines(cacheFile))
				archives.add(new File(line));
		}else{
			archives = findArchives(new File(canvecSourceDir));
			BufferedWriter out = new BufferedWriter(new FileWriter(cacheFile));
			for(File file:archives)
				out.write(file.getAbsolutePath() + "\n");
			out.close();
		}
		return archives;
	}
	
	/**
	 * Recursively searches a path for zip files.
	 * @param path
	 * @return
	 */
	private static List<File> findArchives(File path){
		List<File> files = new ArrayList<File>();
		if(path.isDirectory()){
			for(File sub:path.listFiles()){
				if(sub.isDirectory()){
					List<File> subFiles = findArchives(sub);
					files.addAll(subFiles);
				}else if(sub.getName().toLowerCase().endsWith(".zip")){
					files.add(sub);
				}
			}
		}
		return files;
	}
	
	/**
	 * Parse the jobs file and construct a list of jobs.
	 * @param jobsFile
	 * @return
	 * @throws IOException
	 */
	private static List<ExtractorJob> parseJobsFile(String jobsFile) throws IOException{
		List<ExtractorJob> jobs = new ArrayList<ExtractorJob>();
		BufferedReader in = new BufferedReader(new FileReader(new File(jobsFile)));
		String line = null;
		while((line = in.readLine()) != null){
			if(line.startsWith("#") || line.length() == 0)
				continue;
			String[] parts = line.split(" ");
			if(parts.length < 5)
				continue;
			ExtractorJob job = new ExtractorJob();
			job.setPattern(parts[0].trim());
			job.setSchemaName(parts[1].trim());
			job.setTableName(parts[2].trim());
			job.setOutFile(parts[3].trim());
			job.setCompress("true".equals(parts[4].trim().toLowerCase()));
			jobs.add(job);
		}
		in.close();
		return jobs;
	}
	
	public static void main(String[] args) throws IOException{
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
		String jobsFile = args[0];
		List<ExtractorJob> jobs = parseJobsFile(jobsFile);
		Extractor e = new Extractor();
		for(ExtractorJob job:jobs)
			e.addJob(job);
		e.execute();
	}
}
