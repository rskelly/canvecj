package ca.dijital.canvec;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link ExtractorJob} contains configuration for CanVec extraction tasks. A
 * job is run by an instance of {@link ExtractorWorker}.
 * 
 * @author Rob Skelly <rob@dijital.ca>
 */
public class ExtractorJob {

    public static final int DEFAULT_SRID = 4326;
    
    private String schemaName;
    private String tableName;
    private String pattern;
    private String outFile;
    private String name;
    private Set<File> files;
    private File tempFile;
    private int srid;

    /**
     * Construct an {@link ExtractorJob}.
     */
    public ExtractorJob() {
	srid = DEFAULT_SRID;
	files = new HashSet<File>();
	this.name = "job_" + Long.toString(System.currentTimeMillis(), 16);
    }

    /**
     * Set the name of the PostGIS table.
     * 
     * @param tableName
     */
    public void setTableName(String tableName) {
	this.tableName = tableName;
    }

    /**
     * Get the name of the PostGIS table.
     * 
     * @return
     */
    public String getTableName() {
	return tableName;
    }

    /**
     * Set the name of the PostGIS table's schema.
     * 
     * @param schemaName
     */
    public void setSchemaName(String schemaName) {
	this.schemaName = schemaName;
    }

    /**
     * Get the name of the PostGIS table's schema.
     * 
     * @return
     */
    public String getSchemaName() {
	return schemaName;
    }

    /**
     * Get the list of files related to this job. The returned list is a copy.
     * 
     * @return
     */
    public Set<File> getFiles() {
	Set<File> ret = new HashSet<File>();
	ret.addAll(files);
	return ret;
    }

    /**
     * Get the list of shapefiles related to this job. The returned list is a
     * copy.
     * 
     * @return
     */
    public Set<File> getShapeFiles() {
	Set<File> shp = new HashSet<File>();
	if (files != null) {
	    for (File file : files) {
		if (file.getName().toLowerCase().endsWith(".shp"))
		    shp.add(file);
	    }
	}
	return shp;
    }

    /**
     * Add a file to this job.
     * 
     * @param file
     */
    public void addFile(File file) {
	files.add(file);
    }

    /**
     * Get the name of the job. This name is generated and cannot be changed.
     * 
     * @return
     */
    public String getName() {
	return name;
    }

    /**
     * Sets the feature ID pattern with which to match the CanVec files.
     * 
     * @param pattern
     */
    public void setPattern(String pattern) {
	this.pattern = pattern;
    }

    /**
     * Returns the feature ID pattern with which to match the CanVec files.
     * 
     * @return
     */
    public String getPattern() {
	return pattern;
    }

    /**
     * Sets the name of the output file.
     * 
     * @param outFile
     */
    public void setOutFile(String outFile) {
	this.outFile = outFile;
    }

    /**
     * Gets the name of the output file.
     * 
     * @return
     */
    public String getOutFile() {
	return outFile;
    }

    /**
     * Returns true if this job's properties are adequate for performing a job.
     * 
     * @return
     */
    public boolean isValid() {
	return outFile != null && outFile.length() > 0 && pattern != null
		&& pattern.length() > 0 && name != null && name.length() > 0
		&& schemaName != null && schemaName.length() > 0
		&& tableName != null && tableName.length() > 0;
    }

    /**
     * @Override
     */
    public String toString() {
	return "[ExtractorJob: pattern: " + pattern + "; table name: "
		+ tableName + "; output file: " + outFile + "]";
    }

    /**
     * Sets the temporary file for this job. If the extractor is set to output
     * to STDOUT, the output must be streamed in order, so it must be stored in
     * temporary files until the end. This method handles the file for this job.
     * 
     * @param tempFile
     *            The temporary file for this job.
     */
    public void setTempFile(File tempFile) {
	this.tempFile = tempFile;
    }

    /**
     * Returns the temporary file.
     * 
     * @see {@link ExtractorJob#setTempFile(File)}.
     * 
     * @return The temporary file for this job.
     */
    public File getTempFile() {
	return tempFile;
    }

    /**
     * Sets the SRID for the geometries in the table.
     * 
     * @param srid
     *            The SRID of the geometry in the table.
     */
    public void setSrid(int srid) {
	this.srid = srid;
    }

    /**
     * Gets the SRID for the geometries in the table.
     * 
     * @return The SRID of the geometry in the table.
     */
    public int getSrid() {
	return srid;
    }

}
