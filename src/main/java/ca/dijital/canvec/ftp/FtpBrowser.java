package ca.dijital.canvec.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

public class FtpBrowser {

    private static String FTP_HOST = "ftp2.cits.rncan.gc.ca";
    private static String FTP_ROOT = "/pub/canvec/50k_shp";
    private static String TMP_DIR = "/tmp";
    
    private FileNode paths;
    private FTPFileFilter zipFilter;
    
    public FtpBrowser() {
	paths = new FileNode();
	zipFilter = new ZipFilter();
    }
    
    private FTPClient getFtpClient() throws Exception {
	FTPClient client = new FTPClient();
	FTPClientConfig config = new FTPClientConfig();
	client.configure(config);
	int reply;
	try {
	    client.connect(FTP_HOST);
	    reply = client.getReplyCode();
	    if(!FTPReply.isPositiveCompletion(reply)) {
		client.disconnect();
		client = null;
	    }
	}catch(Exception e) {
	    throw new Exception(e);
	}
	return client;
    }
    
    public void refresh() throws Exception {
	FTPClient client = getFtpClient();
	if(client != null) {
	    crawlDirectory(client, "", paths);
	    client.logout();
	    client.disconnect();
	}
    }
    
    private void crawlDirectory(FTPClient client, String dir,FileNode paths) throws IOException {
	FTPFile[] files = null;
	files = client.listDirectories(dir);
	for(FTPFile file:files) {
	    FileNode node = new FileNode(file.getName(), false, 0, paths);
	    paths.nodes.put(node.path, node);
	    crawlDirectory(client, FTP_ROOT + node.getFullPath(), paths);
	}
	files = client.listFiles(dir, zipFilter);
	for(FTPFile file:files) {
	    FileNode node = new FileNode(file.getName(), true, file.getSize(), paths);
	    paths.nodes.put(node.path, node);
	}
    }
    
    /**
     * Returns true if the given file is cached locally. Does not indicate
     * whether the file is valid or up to date.
     * 
     * @param path
     * @return
     * 
     * @see FtpBrowser#isValid(String)
     */
    public boolean isCached(String path) {
	File file = new File(TMP_DIR + "/" + path);
	return file.exists();
    }
    
    /**
     * Returns true if the file exists locally and its size matches the size of the
     * file on the server. If the sizes do not match, it is a sign that the download
     * failed, or that an update file is available.
     * 
     * @param path
     * @return
     */
    public boolean isValid(String path) {
	File file = new File(TMP_DIR, path);
	return file.exists() && file.length() == paths.findNode(path).size; 
    }
    
    /**
     * Returns the cached instance of the given file.
     * 
     * @param path
     * @return
     */
    protected File getCachedFile(String path) {
	return new File(TMP_DIR, path);
    }
    
    /**
     * Downloads the remote file and caches it, returning the cached
     * version. This method blocks while the download is in progress.
     * If another thread is downloading the same file, this method
     * blocks until the other thread has finished, and then both
     * return a unique instance of the same file.
     *  
     * @param path
     * @return
     * @throws Exception 
     */
    protected synchronized File getRemoteFile(String path) throws Exception {
	FTPClient client = getFtpClient();
	if(client == null)
	    throw new Exception("Failed to connect.");
	File tmp = File.createTempFile("ftp_", ".tmp");
	OutputStream out = new FileOutputStream(tmp);
	client.retrieveFile(FTP_ROOT + "/" + path, out);
	out.close();
	tmp.renameTo(new File(TMP_DIR, path));
	return getCachedFile(path);
    }
    
    public Iterator<File> filesByNTSGrid(int minGrid, int maxGrid, String cellRange) throws Exception {
	refresh();
	Set<String> paths = new HashSet<String>();
	List<String> cells = getCellRange(cellRange);
	for(int i = minGrid;i <= maxGrid; ++i) {
	    String grid = zeroPad(i);
	    for(String cell:cells) {
		paths.add(this.paths.findNode(grid + "/" + cell).getFullPath());
	    }
	}
	return new FileIterator(this, paths);
    }
    
    private static String zeroPad(int val) {
	String b = String.valueOf(val);
	while(b.length() < 3)
	    b = "0" + b;
	return b;
    }
    
    private static List<String> getCellRange(String range) throws Exception {
	int min = 97;
	int max = 122;
	if(range.equals("*")) {
	    // Do nothing
	}else if(range.indexOf("-") > -1) {
	    String a = range.substring(0, range.indexOf("-"));
	    String b = range.substring(range.indexOf("-") + 1);
	    if(a.length() != 1 || b.length() != 1 || !Character.isLowerCase(a.charAt(0)) || !Character.isLowerCase(b.charAt(0)))
		throw new Exception("The range is invalid: " + range);
	    min = (int) a.charAt(0);
	    max = (int) b.charAt(0);
	}else if(range.length() == 1 && Character.isLowerCase(range.charAt(0))) {
	    min = max = (int) range.charAt(0);
	}else {
	    throw new Exception("The range is invalid: " + range);
	}
	List<String> r = new ArrayList<String>();
	for(int i=min;i<=max;++i)
	    r.add(new String(new char[]{(char) i}));
	return r;
    }
    
    public static class FileIterator implements Iterator<File> {

	private FtpBrowser browser;
	private Iterator<String> iter;

	public FileIterator(final FtpBrowser browser, final Collection<String> paths) {
	    this.browser = browser;
	    this.iter = paths.iterator();
	}
	
	@Override
	public boolean hasNext() {
	    return iter.hasNext();
	}

	/**
	 * Returns the next file. If the file is not cached or is not valid,
	 * will download a new version. This method blocks while the file is 
	 * downloading.
	 */
	@Override
	public File next() {
	    String path = iter.next();
	    if(browser.isValid(path)) {
		return browser.getCachedFile(path);
	    } else {
		try {
		    return browser.getRemoteFile(path);
		} catch (Exception e) {
		    e.printStackTrace();
		    return null;
		}
	    }
	}

	@Override
	public void remove() {
	    iter.remove();
	}
	
	
    }
    
    public static class ZipFilter implements FTPFileFilter {

	@Override
	public boolean accept(FTPFile file) {
	    return file.getName().endsWith(".zip");
	}
	
    }
    
    public static class FileNode {
	
	public String path;
	public boolean isFile;
	public long size;
	public Map<String, FileNode> nodes;
	public FileNode parent;
	
	public FileNode(){
	    this(".", false, 0, null);
	}
	
	public FileNode(String path, boolean isFile, long size, FileNode parent) {
	    this.path = path;
	    this.isFile = isFile;
	    this.size = size;
	    this.nodes = new HashMap<String, FileNode>();
	    this.parent = parent;
	}
	
	public String getFullPath() {
	    String ret = new String();
	    FileNode n = this;
	    while(n.parent != null) {
		ret = n.parent.parent == null ? n.path : "/" + n.path;
		n = n.parent;
	    }
	    return ret;
	}
	
	public FileNode findNode(String path) {
	    String[] parts = path.split(File.pathSeparator);
	    FileNode node = this;
	    for(String part:parts) {
		if(node.nodes.containsKey(part)) {
		    node = node.nodes.get(part);
		} else {
		    return node;
		}
	    }
	    return null;
	}
    }
}
