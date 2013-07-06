package ca.dijital.canvec;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link ExtractorJob} contains configuration for CanVec extraction 
 * tasks. A job is run by an instance of {@link ExtractorWorker}.
 * 
 * @author Rob Skelly <rob@dijital.ca>
 */
public class ExtractorJob {

	private String schemaName;
	private String tableName;
	private String pattern;
	private String outFile;
	private String name;
	private boolean deleteFilesOnComplete;
	private boolean compress;
	private Set<File> files;
	private File tempFile;

	/**
	 * Construct an {@link ExtractorJob}.
	 */
	public ExtractorJob(){
		files = new HashSet<File>();
		this.deleteFilesOnComplete = true;
		this.name = "job_" + Long.toString(System.currentTimeMillis(), 16);
	}

	/**
	 * Sets whether to delete the extracted canvec files (shp files, etc.) when
	 * the job completes. If you're going to run more than one set of jobs that
	 * may use the same files, set this to false.
	 * 
	 * @param deleteFilesOnComplete
	 */
	public void setDeleteFilesOnComplete(boolean deleteFilesOnComplete){
		this.deleteFilesOnComplete = deleteFilesOnComplete;
	}
	
	/**
	 * Returns true if the extracted canvec files (shp files, etc.) are to be deleted
	 * when the job completes. If you're going to run more than one set of jobs that
	 * may use the same files, set this to false.
	 * @return
	 */
	public boolean isDeleteFilesOnComplete() {
		return deleteFilesOnComplete;
	}

	/**
	 * Set the name of the PostGIS table.
	 * 
	 * @param tableName
	 */
	public void setTableName(String tableName){
		this.tableName = tableName;
	}
	
	/**
	 * Get the name of the PostGIS table.
	 * @return
	 */
	public String getTableName(){
		return tableName;
	}
	
	/**
	 * Set the name of the PostGIS table's schema.
	 * @param schemaName
	 */
	public void setSchemaName(String schemaName){
		this.schemaName = schemaName;
	}
	
	/**
	 * Get the name of the PostGIS table's schema.
	 * @return
	 */
	public String getSchemaName(){
		return schemaName;
	}
	
	/**
	 * Get the list of files related to this job. The returned list is a copy.
	 * @return
	 */
	public Set<File> getFiles(){
		Set<File> ret = new HashSet<File>();
		ret.addAll(files);
		return ret;
	}
	
	/**
	 * Get the list of shapefiles related to this job. The returned list is a copy.
	 * @return
	 */
	public Set<File> getShapeFiles(){
		Set<File> shp = new HashSet<File>();
		if(files != null){
			for(File file:files){
				if(file.getName().toLowerCase().endsWith(".shp"))
					shp.add(file);
			}
		}
		return shp;
	}
	
	/**
	 * Add a file to this job.
	 * @param file
	 */
	public void addFile(File file){
		files.add(file);
	}
	
	/**
	 * Get the name of the job. This name is generated and cannot be changed.
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Sets whether compression is enabled for this job. If true, the output will be
	 * compressed in an archive named with the given name + ".gz".
	 * @param compress
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	/**
	 * Returns true if compression is enabled for this job. The output will be
	 * compressed in an archive named with the given name + ".gz".
	 * @return
	 */
	public boolean isCompress(){
		return compress;
	}
	
	/**
	 * Sets the feature ID pattern with which to match the CanVec files.
	 * @param pattern
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Returns the feature ID pattern with which to match the CanVec files.
	 * @return
	 */
	public String getPattern(){
		return pattern;
	}

	/**
	 * Sets the name of the output file.
	 * @param outFile
	 */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	/**
	 * Gets the name of the output file.
	 * @return
	 */
	public String getOutFile(){
		return outFile;
	}

	/**
	 * Returns true if this job's properties are adequate for performing a job.
	 * @return
	 */
	public boolean isValid() {
		return outFile != null && outFile.length() > 0 &&
				pattern != null && pattern.length() > 0 &&
				name != null && name.length() > 0 &&
				schemaName != null && schemaName.length() > 0 &&
				tableName != null && tableName.length() > 0;
	}

	/**
	 * @Override
	 */
	public String toString() {
		return "[ExtractorJob: pattern: " + pattern + "; table name: " + tableName + "; output file: " + outFile + "]";
	}

	/**
	 * Sets the temp file for this job.
	 * @param tempFile
	 */
	public void setTempFile(File tempFile) {
	    this.tempFile = tempFile;
	}
	
	public File getTempFile() {
	    return tempFile;
	}
	
	
}
