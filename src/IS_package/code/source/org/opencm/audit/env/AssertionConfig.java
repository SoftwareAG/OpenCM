package org.opencm.audit.env;

import java.io.File;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opencm.audit.configuration.Property;
import org.opencm.audit.configuration.FormatRule;
import org.opencm.audit.configuration.PropertyFilter;
import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;

public class AssertionConfig {

	    private String reportName;
	    private LinkedList<String> assertionGroups;
	    private LinkedList<String> environments;
	    private boolean includeDefaultValues;
	    private LinkedList<Property> properties;
	    private LinkedList<PropertyFilter> propertyFilters;
	    private boolean hideEquals;
	    private LinkedList<FormatRule> formatting;
	    
	    public AssertionConfig () {
	    }
	    
	    public static AssertionConfig instantiate(Configuration opencmConfig, String properties) {
	    	AssertionConfig envConf = null;
	    	File envConfigFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_CONFIG_DIR_LAYERED_AUDIT + File.separator + properties + ".properties");
	    	if (!envConfigFile.exists()) {
	    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Config - File not found: " + envConfigFile.getPath());
	    		return null;
	    	}
	    	
	    	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	    	try {
	    		envConf = mapper.readValue(envConfigFile, AssertionConfig.class);
	    	} catch (Exception e) {
	    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," Config - Exception: " + e.toString());
	    	}
	    	return envConf;
	    }
	    
	    public LinkedList<String> getAssertionGroups() {
	        return this.assertionGroups;
	    }
	    public void setAssertionGroups(LinkedList<String> groups) {
	        this.assertionGroups = groups;
	    }
	    public LinkedList<String> getEnvironments() {
	        return this.environments;
	    }
	    public void setEnvironments(LinkedList<String> envs) {
	        this.environments = envs;
	    }
	    public String getReportName() {
	        return this.reportName;
	    }
	    public void setReportName(String name) {
	        this.reportName = name;
	    }
	    
	    public boolean getIncludeDefaultValues() {
	        return this.includeDefaultValues;
	    }
	    public void setIncludeDefaultValues(boolean incl) {
	        this.includeDefaultValues = incl;
	    }
	    public LinkedList<Property> getProperties() {
	        return this.properties;
	    }
	    public LinkedList<PropertyFilter> getPropertyFilters() {
	        return this.propertyFilters;
	    }
	    public boolean hideEquals() {
	        return this.hideEquals;
	    }
	    public void setHideEquals(boolean hide) {
	        this.hideEquals = hide;
	    }
	    public LinkedList<FormatRule> getFormatting() {
	        return this.formatting;
	    }
}
