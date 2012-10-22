package ca.dijital.geo.canvec;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ExtractorJob {

	public static final String DEFAULT_SCHEMA = "public";
	private String schemaName = DEFAULT_SCHEMA;
	private String tableName;
	private String pattern;
	private String outFile;
	private boolean compress;
	private String name;
	private Set<File> files;
	private boolean deleteFilesOnComplete;
	
	public ExtractorJob(){
		files = new HashSet<File>();
		this.deleteFilesOnComplete = true;
		this.name = "job_" + Long.toString(System.currentTimeMillis(), 16);
	}
	
	public ExtractorJob(String pattern, String schemaName, String tableName, String outFile, boolean compress){
		this();
		this.pattern = pattern;
		this.outFile = outFile;
		this.compress = compress;
		this.schemaName = schemaName;
		this.tableName = tableName;
		
	}
	
	public void setDeleteFilesOnComplete(boolean deleteFilesOnComplete){
		this.deleteFilesOnComplete = deleteFilesOnComplete;
	}
	
	public void setTableName(String tableName){
		this.tableName = tableName;
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public void setSchemaName(String schemaName){
		this.schemaName = schemaName;
	}
	
	public String getSchemaName(){
		return schemaName;
	}
	
	public Set<File> getFiles(){
		return files;
	}
	
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
	
	public void addFile(File file){
		files.add(file);
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isCompress(){
		return compress;
	}
	
	public String getPattern(){
		return pattern;
	}
	
	public String getOutFile(){
		return outFile;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	public boolean isValid() {
		return outFile != null && outFile.length() > 0 &&
				pattern != null && pattern.length() > 0 &&
				name != null && name.length() > 0 &&
				schemaName != null && schemaName.length() > 0 &&
				tableName != null && tableName.length() > 0;
	}

	public boolean isDeleteFilesOnComplete() {
		return deleteFilesOnComplete;
	}

}
