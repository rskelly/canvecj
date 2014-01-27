package ca.dijital.canvec;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Represents a an archive file that may or may not be locally
 * cached. Calling {@link Archive#getFile} will download
 * the file if it is not locally cached, and return it once
 * or if it is.
 *  
 * @author Rob Skelly <rob@dijital.ca>
 */
public interface Archive {

    /**
     * If the file is locally cached, returns it.
     * If the file is not locally cached, downloads it,
     * saves it, then returns it. The 
     * 
     * @return
     */
    public File getFile();

    /**
     * Get the name of the archive.
     * 
     * @return
     */
    public String getName();
    
    /**
     * Returns the {@link List} of shape files in the archive
     * whose names match the given pattern.
     * 
     * @param pattern
     * @return
     * @throws IOException 
     */
    public List<File> getShapeFiles(String pattern) throws IOException;
    
    
}
