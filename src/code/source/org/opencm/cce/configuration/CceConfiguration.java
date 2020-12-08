package org.opencm.cce.configuration;

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

public class CceConfiguration {
	
	public static final File CCE_CONFIG_DIRECTORY = new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_CCE);

	private String name;
	private LinkedList<ArrayList<String>> paths;
	private String group_part_id_01;
	private String group_part_id_02;
	private String group_part_id_03;
	private String delimiter;
	
    public static CceConfiguration instantiate(String json) {
    	CceConfiguration cceConf = null;

    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		cceConf = mapper.readValue(json, CceConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("CceConfiguration - Exception: " + e.toString());
    	}
    	return cceConf;
    }

    public static CceConfiguration instantiate(File templateFile) {
    	CceConfiguration cceConf = null;
    	
    	if (!templateFile.exists()) {
    		LogUtils.logError("CceConfiguration - File not found: " + templateFile.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		cceConf = mapper.readValue(templateFile, CceConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("CceConfiguration - Exception: " + e.toString());
    	}
    	return cceConf;
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonGetter("paths")
    public LinkedList<ArrayList<String>> getInventoryPaths() {
        return this.paths;
    }
    public void setInventoryPaths(LinkedList<ArrayList<String>> paths) {
        this.paths = paths;
    }

    @JsonGetter("id_01")
    public String getGroupId01() {
        return this.group_part_id_01;
    }
    public void setGroupId01(String id) {
        this.group_part_id_01 = id;
    }

    @JsonGetter("id_02")
    public String getGroupId02() {
        return this.group_part_id_02;
    }
    public void setGroupId02(String id) {
        this.group_part_id_02 = id;
    }
    @JsonGetter("id_03")
    public String getGroupId03() {
        return this.group_part_id_03;
    }
    public void setGroupId03(String id) {
        this.group_part_id_03 = id;
    }
    
    @JsonGetter("delimiter")
    public String getDelimiter() {
        return this.delimiter;
    }
    public void setDelimiter(String del) {
        this.delimiter = del;
    }

    @JsonIgnore
    public static void checkDirectory() {
    	if (!CCE_CONFIG_DIRECTORY.exists()) {
    		if (!CCE_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + CCE_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }

    @JsonIgnore
    public void save() {
    	File templateFile = new File(CCE_CONFIG_DIRECTORY.getPath() + File.separator + getName() + ".json");
		String json = JsonUtils.convertJavaObjectToJson(this);
		FileUtils.writeToFile(templateFile.getPath(), json);
    }

}
