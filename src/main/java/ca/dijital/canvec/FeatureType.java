package ca.dijital.canvec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeatureType {

    private String name;
    private String id;
    
    public FeatureType() {
	
    }
    
    public FeatureType(String name, String id) {
	setId(id);
	setName(name);
    }
    
    public String toString() {
	return name + " (" + id + ")";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Loads the {@link List} of {@link FeatureType}s from a file and returns the list.
     * 
     * @param file
     * @return A {@link List} of {@link FeatureType}s.
     * @throws IOException
     */
    public static List<FeatureType> loadFromFile(File file) throws IOException {
	BufferedReader in = new BufferedReader(new FileReader(file));
	String line;
	List<FeatureType> types = new ArrayList<FeatureType>();
	while((line = in.readLine()) != null) {
	    String[] parts = line.split("\t");
	    if(parts.length < 2)
		break;
	    types.add(new FeatureType(parts[0].trim(), parts[1].trim()));
	}
	in.close();
	return types;
    }
}
