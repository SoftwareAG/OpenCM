package org.opencm.audit;

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

public class AuditConfiguration {

	public static final File AUDIT_CONFIG_DIRECTORY = new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_AUDIT);
	
	private String name;
	private LinkedList<ArrayList<String>> paths;
	private String incl_component;
	private String incl_instance;
	private String incl_key;
	private LinkedList<ExcludeFilter> excl_filters; 
	private String option_equals;
	
    public static AuditConfiguration instantiate(String json) {
    	AuditConfiguration auditConf = null;

    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		auditConf = mapper.readValue(json, AuditConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("AuditConfiguration - Exception: " + e.toString());
    	}
    	return auditConf;
    }

    public static AuditConfiguration instantiate(File template) {
   	
    	AuditConfiguration auditConf = null;
    	
    	if (!template.exists()) {
    		LogUtils.logError("AuditConfiguration - File not found: " + template.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		auditConf = mapper.readValue(template, AuditConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError(" AuditConfiguration - Exception: " + e.toString());
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
    public LinkedList<ArrayList<String>> getInventoryPaths() {
        return this.paths;
    }
    public void setInventoryPaths(LinkedList<ArrayList<String>> paths) {
        this.paths = paths;
    }

    @JsonGetter("incl_component")
    public String getComponent() {
        return this.incl_component;
    }
    public void setComponent(String comp) {
        this.incl_component = comp;
    }

    @JsonGetter("incl_instance")
    public String getInstance() {
        return this.incl_instance;
    }
    public void setInstance(String inst) {
        this.incl_instance = inst;
    }

    @JsonGetter("incl_key")
    public String getKey() {
        return this.incl_key;
    }
    public void setKey(String key) {
        this.incl_key = key;
    }

    @JsonGetter("excl_filters")
    public LinkedList<ExcludeFilter> getExcludeFilters() {
        return this.excl_filters;
    }
    public void setExcludeFilters(LinkedList<ExcludeFilter> filters) {
        this.excl_filters = filters;
    }

    @JsonGetter("option_equals")
    public String getOptionEquals() {
        return this.option_equals;
    }
    public void setOptionEquals(String flag) {
        this.option_equals = flag;
    }

    @JsonIgnore
    public static void checkDirectory() {
    	if (!AUDIT_CONFIG_DIRECTORY.exists()) {
    		if (!AUDIT_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + AUDIT_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }

    @JsonIgnore
    public void save() {
    	File templateFile = new File(AUDIT_CONFIG_DIRECTORY.getPath() + File.separator + getName() + ".json");
		String json = JsonUtils.convertJavaObjectToJson(this);
		FileUtils.writeToFile(templateFile.getPath(), json);
    }

}
