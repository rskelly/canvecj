package ca.dijital.canvec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalCanvecStore extends CanvecStore {

    private static final String FILE_TABLE_CACHE_FILE = "canvec.cache";

    private final Logger logger;

    private String tempDir;
    private String canvecDir;

    public LocalCanvecStore(String canvecDir) {
	logger = LoggerFactory.getLogger(getClass());
	this.canvecDir = canvecDir;
	this.tempDir = System.getProperty("java.io.tmpdir");
    }

    public void setCanvecDir(String canvecDir) {
	this.canvecDir = canvecDir;
    }
    
    public void setTempDir(String tempDir) {
	this.tempDir = tempDir;
    }
    
    @Override
    public LocalArchiveIterator getArchiveIterator() throws IOException {
	return new LocalArchiveIterator(getArchives(false));
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
    private List<Archive> getArchives(boolean discardCache) throws IOException {
	logger.info("Enumerating archives (discarding cache: {})", discardCache);
	if (tempDir == null)
	    throw new IOException("The temp dir has not been configured.");
	if (canvecDir == null)
	    throw new IOException("The canvec dir has not been configured.");
	List<Archive> archives = null;
	// Create the cache file, then try to create its parent, if it doesn't
	// exist.
	File cacheDir = new File(tempDir, "canvec");
	if (!cacheDir.exists() && !cacheDir.mkdirs())
	    throw new IOException(
		    "The temporary directory, {}/canvec, does not exist and could not be created."
			    .replace("{}", tempDir));
	File cacheFile = new File(cacheDir, FILE_TABLE_CACHE_FILE);
	if (!discardCache && (cacheFile.exists() || cacheFile.canRead())) {
	    archives = new ArrayList<Archive>();
	    for (String line : getLines(cacheFile))
		archives.add(new LocalArchive(new File(line), getCache()));
	} else {
	    archives = findArchives(new File(canvecDir));
	    BufferedWriter out = new BufferedWriter(new FileWriter(cacheFile));
	    for (Archive file : archives)
		out.write(file.getFile().getAbsolutePath() + "\n");
	    out.close();
	}
	return archives;
    }

    /**
     * Returns the contents of a text file as a list of {@link Strings}.
     * 
     * @param cacheFile
     * @return
     * @throws IOException
     */
    private static List<String> getLines(File cacheFile) throws IOException {
	List<String> lines = new ArrayList<String>();
	BufferedReader in = new BufferedReader(new FileReader(cacheFile));
	String line = null;
	while ((line = in.readLine()) != null)
	    lines.add(line);
	in.close();
	return lines;
    }

    /**
     * Recursively searches a path for Zip files.
     * 
     * @param path
     * @return
     */
    private static List<Archive> findArchives(File path) {
	List<Archive> files = new ArrayList<Archive>();
	if (path.isDirectory()) {
	    for (File sub : path.listFiles()) {
		if (sub.isDirectory()) {
		    List<Archive> subFiles = findArchives(sub);
		    files.addAll(subFiles);
		} else if (sub.getName().toLowerCase().endsWith(".zip")) {
		    files.add(new LocalArchive(sub, null));
		}
	    }
	}
	return files;
    }

}
