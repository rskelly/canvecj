package ca.dijital.canvec;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Represents a store of CanVec archive files.
 * 
 * @author Rob Skelly <rob@dijital.ca>
 */
public abstract class CanvecStore {

    private String workDir;
    private Cache cache;

    public void setWorkDir(String workDir) {
	this.workDir = workDir;
	if (cache == null) {
	    cache = new Cache(workDir);
	} else {
	    cache.setWorkDir(workDir);
	}
    }

    public Cache getCache() {
	return cache;
    }
    
    public String getWorkDir() {
	return workDir;
    }

    /**
     * Returns an {@link Iterator} that iterates over a collection of
     * {@link Archive}s.
     * 
     * @return
     * @throws IOException
     */
    public abstract ArchiveIterator getArchiveIterator() throws IOException;

    /**
     * Configure this store using the {@link Map} of properties given. There
     * must be a setter for each property, otherwise an exception is thrown.
     * 
     * @param storeProperties
     * @throws Exception
     */
    public void configure(Map<String, String> storeProperties) throws Exception {
	for (String name : storeProperties.keySet()) {
	    try {
		String setterName = "set" + name.substring(0, 1).toUpperCase()
			+ name.substring(1);
		Method setter = getClass().getMethod(setterName,
			new Class<?>[] { String.class });
		setter.invoke(this, new Object[] { storeProperties.get(name) });
	    } catch (Exception e) {
		throw new Exception("Couldn't set property named {}.".replace(
			"{}", name));
	    }
	}
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
    /*
     * public void extractFiles() throws IOException { // Get the list of zip
     * files. List<File> archives = getArchives(false);
     * 
     * //logger.info("Extracting " + archives.size() + " archives to " + tempDir
     * + ".");
     * 
     * // A set to keep track of files that have already been extracted.
     * Set<String> extracted = new HashSet<String>(); // Cache for file objects.
     * Map<String, File> fileCache = new HashMap<String, File>(); // Iterate
     * over the archives. If an entry in an archive matches one of // our jobs'
     * feature IDs, we'll unzip it. for (File file : archives) {
     * logger.info("Extracting " + file.getName() + "."); ZipFile archive = new
     * ZipFile(file); Enumeration<? extends ZipEntry> entries =
     * archive.entries(); while (entries.hasMoreElements()) { ZipEntry entry =
     * entries.nextElement(); String entryName = entry.getName(); // Iterate
     * over jobs to find a match. for (ExtractorJob job : jobs) { String pattern
     * = job.getPattern(); Matcher matcher = Pattern.compile(pattern).matcher(
     * entry.getName()); // If an entry matches the pattern, add it to the
     * extract // set. if (matcher.find()) { if
     * (!fileCache.containsKey(entryName)) fileCache.put(entryName, new
     * File(tempDir, entryName)); File outFile = fileCache.get(entryName); if
     * (deleteTempFiles) outFile.deleteOnExit(); job.addFile(outFile); // If
     * it's already extracted skip it. if (!extracted.contains(entryName)) { try
     * { saveZipEntry(archive, entry, outFile); extracted.add(entryName); }
     * catch (IOException e) { logger.error("Failed to unzip an archive.", e); }
     * } } } } archive.close(); } }
     */

}
