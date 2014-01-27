package ca.dijital.canvec.ftp.test;

import java.io.File;
import java.util.Iterator;

import ca.dijital.canvec.ftp.FtpBrowser;

public class FtpBrowserTest {

    public void test() throws Exception {
	FtpBrowser f = new FtpBrowser();
	Iterator<File> files = f.filesByNTSGrid(92, 94, "*");
	while(files.hasNext()) {
	    File file = files.next();
	    System.out.println(file.getName() + ": " + file.length());
	}
	    
	
    }
    
    public static void main(String[] args) throws Exception {
	FtpBrowserTest t = new FtpBrowserTest();
	t.test();
    }
}
