package org.opencm.governance;

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

public class GovernanceConfiguration {
	
	public static File GOV_CONFIG_DIRECTORY = new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_GOVERNANCE);
	
	private String name;
	private LinkedList<ArrayList<String>> paths;
	private LinkedList<GovernanceProperty> properties;
	
    public static GovernanceConfiguration instantiate(String config) {
    	GovernanceConfiguration govConf = null;

    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		govConf = mapper.readValue(config, GovernanceConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("ExtractConfiguration - Exception: " + e.toString());
    	}
    	return govConf;
    }
	
    public static GovernanceConfiguration instantiate(File template) {
    	GovernanceConfiguration auditConf = null;
    	
    	if (!template.exists()) {
    		LogUtils.logError("GovernanceConfiguration - File not found: " + template.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		auditConf = mapper.readValue(template, GovernanceConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("GovernanceConfiguration - Exception: " + e.toString());
    	}
    	return auditConf;
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

    @JsonGetter("properties")
    public LinkedList<GovernanceProperty> getProperties() {
        return this.properties;
    }
    public void setProperties(LinkedList<GovernanceProperty> props) {
        this.properties = props;
    }
    
    @JsonIgnore
    public static void checkDirectory() {
    	if (!GOV_CONFIG_DIRECTORY.exists()) {
    		if (!GOV_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + GOV_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }
    
    @JsonIgnore
    public void save() {
    	File templateFile = new File(GOV_CONFIG_DIRECTORY.getPath() + File.separator + getName() + ".json");
		String json = JsonUtils.convertJavaObjectToJson(this);
		FileUtils.writeToFile(templateFile.getPath(), json);
    }

}
