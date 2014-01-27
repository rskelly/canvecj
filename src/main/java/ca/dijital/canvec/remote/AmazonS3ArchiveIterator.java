package ca.dijital.canvec.remote;

import java.io.IOException;
import java.util.List;

import ca.dijital.canvec.Archive;
import ca.dijital.canvec.ArchiveIterator;

public class AmazonS3ArchiveIterator implements ArchiveIterator {

    private AmazonS3CanvecStore store;
    private List<String> batch;
    private int batchNumber;
    
    AmazonS3ArchiveIterator(final AmazonS3CanvecStore store) {
	this.store = store;
    }
    
    @Override
    public boolean hasNext() {
	if(batch == null || batch.size() == 0)
	    batch = store.getBatch(batchNumber++);
	return batch != null && batch.size() > 0;
    }

    @Override
    public Archive next() {
	try {
	    return store.getArchive(batch.remove(0));
	} catch (IOException e) {
	    throw new IllegalStateException(e.getMessage());
	}
    }

    @Override
    public void remove() {
	throw new IllegalStateException("Not implemented.");
    }

}
