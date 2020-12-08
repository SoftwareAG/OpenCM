package org.opencm.extract;

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

public class ExtractConfiguration {
	
	public static File EXTRACT_CONFIG_DIRECTORY = new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_EXTRACT);
	
	private String name;
	private LinkedList<ArrayList<String>> paths;
	
    public static ExtractConfiguration instantiate(String config) {
    	ExtractConfiguration extConf = null;

    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		extConf = mapper.readValue(config, ExtractConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("ExtractConfiguration - Exception: " + e.toString());
    	}
    	return extConf;
    }
    
    public static ExtractConfiguration instantiate(File template) {
    	ExtractConfiguration extConf = null;

    	if (!template.exists()) {
    		LogUtils.logError("ExtractConfiguration - File not found: " + template.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		extConf = mapper.readValue(template, ExtractConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("ExtractConfiguration - Exception: " + e.toString());
    	}
    	return extConf;
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
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
    	if (!EXTRACT_CONFIG_DIRECTORY.exists()) {
    		if (!EXTRACT_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + EXTRACT_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }

    @JsonIgnore
    public void save() {
    	File templateFile = new File(EXTRACT_CONFIG_DIRECTORY.getPath() + File.separator + getName() + ".json");
		String json = JsonUtils.convertJavaObjectToJson(this);
		FileUtils.writeToFile(templateFile.getPath(), json);
    }

}
