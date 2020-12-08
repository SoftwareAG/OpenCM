package org.opencm.synch;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import org.opencm.configuration.Configuration;
import org.opencm.util.FileUtils;
import org.opencm.util.LogUtils;
import org.opencm.util.JsonUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SynchConfiguration {
	
	public static final File SYNCH_CONFIG_DIRECTORY 	= new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_SYNCH);
	
	private String name;
	private ArrayList<String> target_path;
	private LinkedList<ArrayList<String>> paths;
	
	
    public static SynchConfiguration instantiate(String config) {
    	SynchConfiguration syncConf = null;

    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		syncConf = mapper.readValue(config, SynchConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("ExtractConfiguration - Exception: " + e.toString());
    	}
    	return syncConf;
    }

    public static SynchConfiguration instantiate(File template) {
    	SynchConfiguration extConf = null;

    	if (!template.exists()) {
    		LogUtils.logError("SynchConfiguration - File not found: " + template.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		extConf = mapper.readValue(template, SynchConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("SynchConfiguration - Exception: " + e.toString());
    	}
    	return extConf;
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonGetter("target_path")
    public ArrayList<String> getTargetPath() {
        return this.target_path;
    }
    public void setTargetPath(ArrayList<String> path) {
        this.target_path = path;
    }
    
    @JsonGetter("paths")
    public LinkedList<ArrayList<String>> getPaths() {
        return this.paths;
    }
    public void setPaths(LinkedList<ArrayList<String>> paths) {
        this.paths = paths;
    }


    @JsonIgnore
    public static void checkDirectory() {
    	if (!SYNCH_CONFIG_DIRECTORY.exists()) {
    		if (!SYNCH_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + SYNCH_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }
    
    @JsonIgnore
    public void save() {
    	File templateFile = new File(SYNCH_CONFIG_DIRECTORY.getPath() + File.separator + getName() + ".json");
		String json = JsonUtils.convertJavaObjectToJson(this);
		FileUtils.writeToFile(templateFile.getPath(), json);
    }

}
