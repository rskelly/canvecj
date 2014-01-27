package ca.dijital.canvec;

import java.io.File;

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
    private File tempFile;
    private int srid;

    private String databaseName;

    private String tempDir;
    
    /**
     * Construct an {@link ExtractorJob}.
     */
    public ExtractorJob() {
	srid = DEFAULT_SRID;
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

    public void setDatabaseName(String databaseName) {
	this.databaseName = databaseName;
    }
    
    public String getDatabaseName() {
	return databaseName;
    }

    private static boolean isEmpty(String s) {
	return s == null || s.length() == 0;
    }
    
    public void validate() throws Exception {
	if(isEmpty(databaseName))
	    throw new Exception("Database name is required.");
	if(isEmpty(pattern))
	    throw new Exception("Pattern is required.");
	if(isEmpty(outFile))
	    throw new Exception("SQL file name is required.");
	if(isEmpty(schemaName))
	    throw new Exception("Schema name is required.");
	if(isEmpty(tableName))
	    throw new Exception("Table name is required.");
	if(srid <= 0)
	    throw new Exception("SRID is required.");
	if(isEmpty(tempDir))
	    throw new Exception("Target folder is required.");
    }

    public void setTempDir(String tempDir) {
	this.tempDir = tempDir;
    }
    
    public String getTempDir() {
	return tempDir;
    }
    
}
