package ca.dijital.canvec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class Cache {

    private File cacheDir;
    private Map<String, Node> cache;
    private Node head;
    private Node tail;
    private int size;

    Cache(String workDir) {
	size = 1000;
	cache = new HashMap<String, Node>();
	setWorkDir(workDir);
    }

    public List<File> getShapeFiles(File archiveFile, String pattern)
	    throws ZipException, IOException {
	String name = archiveFile.getAbsolutePath();
	List<File> files = null;
	if (cache.containsKey(name)) {
	    files = cache.get(name).files;
	} else {
	    if (size == cache.size()) {
		files = moveToHead(tail, archiveFile);
	    } else {
		files = newNode(archiveFile);
	    }
	}
	List<File> shapeFiles = new ArrayList<File>();
	for (File file : files) {
	    if (file.getName().matches(pattern))
		shapeFiles.add(file);
	}
	return shapeFiles;
    }

    private List<File> newNode(File archiveFile) throws ZipException,
	    IOException {
	Node node = new Node(archiveFile.getAbsolutePath());
	node.dir = extract(archiveFile);
	node.archive = archiveFile;

	if (head == null) {
	    head = tail = node;
	} else {
	    head.prev = node;
	    node.next = head;
	    head = node;
	}

	cache.put(node.name, node);

	List<File> files = new ArrayList<File>();
	for (File file : node.dir.listFiles())
	    files.add(file);
	node.files = files;

	return files;
    }

    private List<File> moveToHead(Node node, File archiveFile)
	    throws ZipException, IOException {
	// Clean up the node's files.
	cache.remove(node.name);
	for (File file : node.dir.listFiles())
	    file.delete();
	node.dir.delete();
	node.archive.delete();

	// Populate the node with new data.
	node.dir = extract(archiveFile);
	node.archive = archiveFile;
	node.name = archiveFile.getName();

	// Read out the files.
	List<File> files = new ArrayList<File>();
	for (File file : node.dir.listFiles())
	    files.add(file);
	node.files = files;

	// Move to head.
	if (node == tail)
	    tail = node.prev;
	if (node.next != null)
	    node.next.prev = node.prev;
	if (node.prev != null)
	    node.prev.next = node.next;
	head.prev = node;
	node.next = head;
	head = node;
	cache.put(node.name, node);

	return files;
    }

    public static boolean isValidZipFile(File file) {
	try {
	    ZipFile zfile = new ZipFile(file);
	    zfile.size();
	    zfile.close();
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Extracts the archive into the cache dir and returns the folder where it
     * was extracted.
     * 
     * @param archiveFile
     * @return
     * @throws ZipException
     * @throws IOException
     */
    private File extract(File archiveFile) throws ZipException, IOException {
	String name = archiveFile.getName();
	ZipFile zfile = new ZipFile(archiveFile);

	File folder = new File(cacheDir, name.substring(0,
		name.lastIndexOf(".")));
	folder.mkdirs();

	@SuppressWarnings("unchecked")
	Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zfile.entries();
	while (entries.hasMoreElements()) {
	    ZipEntry entry = entries.nextElement();
	    saveZipEntry(zfile, entry, new File(folder, entry.getName()));
	}
	return folder;
    }

    /**
     * Save a {@link ZipEntry} to a file.
     * 
     * @param archive
     * @param entry
     * @param outFile
     * @throws IOException
     */
    private void saveZipEntry(ZipFile archive, ZipEntry entry, File outFile)
	    throws IOException {
	if (!outFile.exists()) {
	    InputStream zin = archive.getInputStream(entry);
	    OutputStream zout = new BufferedOutputStream(new FileOutputStream(
		    outFile));
	    int read;
	    byte[] buf = new byte[4096];
	    while ((read = zin.read(buf)) > 0)
		zout.write(buf, 0, read);
	    zout.close();
	    zin.close();
	}
    }

    private static class Node {

	Node next;
	Node prev;
	String name;
	File dir;
	File archive;
	List<File> files;

	Node(String name) {
	    this.name = name;
	}
    }

    public void setWorkDir(String workDir) {
	cacheDir = new File(workDir, "cache");
	cacheDir.mkdirs();
    }

}
