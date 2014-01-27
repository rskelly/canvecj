package ca.dijital.canvec;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LocalArchive implements Archive {

    private Cache cache;
    private File file;

    public LocalArchive(final File file, Cache cache) {
	this.file = file;
	this.cache = cache;
    }
    
    @Override
    public File getFile() {
	return file;
    }

    @Override
    public String getName() {
	return file.getName();
    }

    @Override
    public List<File> getShapeFiles(String pattern) throws IOException {
	return cache.getShapeFiles(file, pattern);
    }

}
