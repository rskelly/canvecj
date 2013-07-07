package ca.dijital.canvec;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the work of an ExtractorJob in a separate thread by invoking the
 * command-line utility.
 * 
 * @author Rob Skelly <rob@dijital.ca>
 */
public class ExtractorWorker implements Runnable {

    private static Logger logger = LoggerFactory
	    .getLogger(ExtractorWorker.class);

    private Thread thread;
    private ExtractorJob job;
    private boolean running;
    private boolean busy;
    private boolean failed;
    private boolean useStdOut;

    private Extractor extractor;

    /**
     * Starts the worker with the given job. If the worker is in use, an
     * exception is thrown. If the stdOutParameter is true, outputs all SQL to
     * STDOUT.
     * 
     * @param job
     * @param useStdOut
     */
    public void start(final Extractor extractor, final ExtractorJob job,
	    boolean useStdOut) throws Exception {
	if (!running) {
	    busy = true;
	    running = true;
	    failed = false;
	    this.extractor = extractor;
	    this.useStdOut = useStdOut;
	    this.job = job;
	    this.thread = new Thread(this);
	    this.thread.start();
	} else {
	    throw new Exception("This worker is not free.");
	}
    }

    /**
     * Stops the worker.
     */
    public void stop() {
	if (running) {
	    running = false;
	    try {
		thread.join();
	    } catch (InterruptedException e) {
		logger.error("Error stopping worker.", e);
	    } finally {
		job = null;
		busy = false;
	    }
	}
    }

    /**
     * Returns true if the worker is running.
     * 
     * @return
     */
    public boolean isBusy() {
	return busy;
    }

    /**
     * Returns true if the job failed.
     * 
     * @return
     */
    public boolean isFailure() {
	return failed;
    }

    @Override
    public void run() {
	while (running) {
	    Set<File> shapeFiles = job.getShapeFiles();
	    Iterator<File> files = shapeFiles.iterator();
	    boolean compress = extractor.isCompressOutput();
	    // Create output file.
	    String fileName = job.getOutFile();
	    if (compress)
		fileName += ".gz";
	    File outFile = new File(fileName);
	    OutputStream out = null;
	    try {
		// If useStdOut is true, we'll need a temporary file.
		if (useStdOut) {
		    outFile = File.createTempFile("canvec_", ".tmp", new File(
			    extractor.getTempDir()));
		    compress = false;
		    job.setTempFile(outFile);
		}
		// Create a file output, and GZIP it if necessary.
		out = new BufferedOutputStream(new FileOutputStream(outFile));
		// If compression is desired, wrap the output in a gzip stream.
		if (compress)
		    out = new GZIPOutputStream(out);
	    } catch (IOException e) {
		logger.error("Failed to open output file {} in job {}.",
			job.getOutFile(), job.getName(), e);
		failed = true;
		break;
	    }
	    int i = 0;
	    int end = shapeFiles.size() - 1;
	    while (running && files.hasNext()) {
		File file = files.next();
		if (file.getName().toLowerCase().endsWith("shp")) {
		    logger.info("Processing file {}.", file.getName());

		    // Build shp2pgsql command.
		    StringBuffer command = new StringBuffer("shp2pgsql ");
		    if (i == 0) {
			command.append("-d ");
		    } else {
			command.append("-a ");
		    }
		    if (i == end)
			command.append("-I ");
		    // Set the SRID
		    command.append("-s ").append(job.getSrid()).append(" ");
		    // Set the ouput file name.
		    command.append(file.getAbsolutePath()).append(" ");
		    // Set the schema/table name.
		    command.append(job.getSchemaName()).append(".")
			    .append(job.getTableName());

		    String commandStr = command.toString();
		    
		    logger.info("shp2pgsql command: " + commandStr);
		    
		    try {
			// Start a process for the command and get the input
			// stream.
			Process proc = Runtime.getRuntime().exec(
				commandStr);
			InputStream in = proc.getInputStream();
			// Write the shp2pgsql output to the output stream.
			byte[] buf = new byte[1024];
			int read = 0;
			int accum = 0;
			int maxAccum = 1024 * 1024;
			while (running && (read = in.read(buf)) > -1) {
			    out.write(buf, 0, read);
			    // If we've accumulated more than 1M of data, flush
			    // it.
			    accum += read;
			    if (accum > maxAccum)
				out.flush();
			}
			in.close();

			// Wait for the process to finish, and report any
			// problems.
			int retval = proc.waitFor();
			if (retval != 0) {
			    String err = stringFromStream(proc.getErrorStream());
			    logger.warn("Return value from shp2pgsql was {}; {}",
				    retval, err);
			}
			logger.info("File complete", file.getName());
		    } catch (IOException e) {
			logger.error("Failed while processing job {}.",
				job.getName(), e);
			failed = true;
			break;
		    } catch (InterruptedException e) {
			logger.error("Failed while processing job {}.",
				job.getName(), e);
			failed = true;
			break;
		    }
		    ++i;
		}
	    }
	    try {
		// If gzipping, finish the archive.
		if (!useStdOut && compress)
		    ((GZIPOutputStream) out).finish();

		// Close the stream.
		out.close();
	    } catch (IOException e) {
		logger.error("Failed to close outputstream in job {}.",
			job.getName());
		failed = true;
	    } finally {
		running = false;
		busy = false;
		extractor.workerFinished(this);
	    }
	}
    }

    /**
     * Returns the {@link ExtractorJob} being serviced by this worker.
     * 
     * @return
     */
    public ExtractorJob getJob() {
	return job;
    }

    /**
     * Reads a {@link String} from an {@link InputStream}.
     * @param in
     * @return
     */
    private String stringFromStream(InputStream in) {
	StringBuffer str = new StringBuffer();
	int read = 0;
	byte[] buf = new byte[1024];
	try {
	    while((read = in.read(buf)) > -1) {
	        str.append(new String(buf, 0, read));
	    }
	} catch (IOException e) {
	}
	return str.toString();
    }
}
