package ca.dijital.canvec.remote;

import ca.dijital.canvec.CanvecStore;

/**
 * Represents a remote storage location where CanVec
 * archives are located. Provides methods for accessing
 * individual archives while implicitly downloading and
 * caching them.
 *   
 * @author Rob Skelly <rob@dijital.ca>
 *
 */
public abstract class RemoteCanvecStore extends CanvecStore {

    private int batchSize;

    public void setBatchSize(String batchSize) {
	setBatchSize(Integer.parseInt(batchSize));
    }
    
    public void setBatchSize(int batchSize) {
	this.batchSize = batchSize;
    }
    
    public int getBatchSize() {
	return batchSize;
    }
    
}
