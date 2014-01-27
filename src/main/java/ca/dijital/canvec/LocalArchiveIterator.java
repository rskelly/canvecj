package ca.dijital.canvec;

import java.util.List;


public class LocalArchiveIterator implements ArchiveIterator {

    private List<Archive> archives;
    private int idx;
    private boolean canRemove;
    
    public LocalArchiveIterator(final List<Archive> archives) {
	this.archives = archives;
    }
    
    @Override
    public boolean hasNext() {
	return idx < archives.size() -1;
    }

    @Override
    public Archive next() {
	if(hasNext()) {
	    canRemove = true;
	    return archives.get(idx++);
	}
	return null;
    }

    @Override
    public void remove() {
	if(canRemove) {
	    canRemove = false;
	    archives.remove(--idx);
	}
    }

}
