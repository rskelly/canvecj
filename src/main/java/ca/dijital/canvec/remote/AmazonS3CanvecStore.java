package ca.dijital.canvec.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import ca.dijital.canvec.Archive;
import ca.dijital.canvec.ArchiveIterator;
import ca.dijital.canvec.Cache;
import ca.dijital.canvec.LocalArchive;

public class AmazonS3CanvecStore extends RemoteCanvecStore {

    private File fileDir;
    private File fileList;
    private String key;
    private String secret;
    private String bucketName = "maphost";
    private List<String> fileNames;

    public AmazonS3CanvecStore() {
	super();
    }

    public void setKey(String key) {
	this.key = key;
    }

    public void setSecret(String secret) {
	this.secret = secret;
    }

    @Override
    public ArchiveIterator getArchiveIterator() throws IOException {
	buildFileList(false);
	return new AmazonS3ArchiveIterator(this);
    }

    /**
     * Returns a subset of the file list using the batch number to compute the
     * indices of the sublist.
     * 
     * @param batchNumber
     * @return
     */
    List<String> getBatch(int batchNumber) {
	int start = batchNumber * getBatchSize();
	int end = start + getBatchSize();
	return new ArrayList<String>(fileNames.subList(start, end));
    }

    /**
     * Builds the file list based on the list of keys from the S3 bucket. If the
     * list exists, it is returned. If force is true, rebuilds the cached list.
     * 
     * @param force
     * @throws IOException
     */
    private synchronized void buildFileList(boolean force) throws IOException {
	fileDir = new File(getWorkDir(), "s3");
	if (!fileDir.exists())
	    fileDir.mkdirs();
	fileList = new File(fileDir, "files.lst");
	if (!force && fileList.exists()) {
	    readFileList();
	} else {
	    AmazonS3Client client = new AmazonS3Client(new BasicAWSCredentials(
		    key, secret));
	    if (!client.doesBucketExist(bucketName))
		throw new IOException("The bucket named " + bucketName
			+ " does not exist.");

	    List<String> fileNames = new ArrayList<String>();

	    ObjectListing objects = client.listObjects(bucketName);
	    while (objects.isTruncated()) {
		List<S3ObjectSummary> summaries = objects.getObjectSummaries();
		for (S3ObjectSummary summary : summaries) {
		    String key = summary.getKey();
		    if (key.toLowerCase().endsWith(".zip"))
			fileNames.add(key);
		}
		objects = client.listNextBatchOfObjects(objects);
	    }

	    BufferedWriter out = new BufferedWriter(new FileWriter(fileList));
	    for (String fileName : fileNames)
		out.write(fileName + "\n");
	    out.close();

	    Collections.sort(fileNames);
	    this.fileNames = fileNames;
	}
    }

    /**
     * Reads the extant file list into memory.
     * 
     * @throws IOException
     */
    private void readFileList() throws IOException {
	List<String> fileNames = new ArrayList<String>();
	BufferedReader in = new BufferedReader(new FileReader(fileList));
	String line = null;
	while ((line = in.readLine()) != null)
	    fileNames.add(line);
	in.close();
	Collections.sort(fileNames);
	this.fileNames = fileNames;
    }

    /**
     * Returns the {@link Archive} corresponding to the given key. If the file
     * does not exist, it is downloaded.
     * 
     * @param name
     * @return
     * @throws IOException
     */
    synchronized Archive getArchive(String name) throws IOException {
	// The name might be a key with a path part.
	String fileName = name.substring(name.lastIndexOf("/") + 1);
	File file = new File(fileDir, fileName);
	if (!file.exists() || !Cache.isValidZipFile(file)) {
	    AmazonS3Client client = new AmazonS3Client(new BasicAWSCredentials(
		    key, secret));
	    S3Object object = client.getObject(bucketName, name);
	    S3ObjectInputStream in = object.getObjectContent();
	    FileOutputStream out = new FileOutputStream(file);
	    int read = 0;
	    byte[] buf = new byte[4096];
	    while ((read = in.read(buf)) > -1)
		out.write(buf, 0, read);
	    out.close();
	    in.close();
	}
	return new LocalArchive(file, getCache());
    }

    public static void main(String[] args) throws IOException {
	AmazonS3CanvecStore store = new AmazonS3CanvecStore();
	store.setWorkDir("/tmp/canvec");
	store.setBatchSize(2);
	store.setKey("AKIAILQSZENJSX62LLTA");
	store.setSecret("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");

	ArchiveIterator iter = store.getArchiveIterator();
	while (iter.hasNext()) {
	    Archive a = iter.next();
	    File f = a.getFile();
	    System.out.println(f.getName() + " - " + f.exists());
	    List<File> files = a.getShapeFiles("1690009");
	    for (File file : files)
		System.out.println(" -- " + file.getName() + " - "
			+ file.exists());
	}
    }

}
