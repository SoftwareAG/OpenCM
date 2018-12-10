package org.opencm.audit.env;

import java.io.File;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opencm.audit.configuration.Property;
import org.opencm.audit.configuration.FormatRule;
import org.opencm.audit.configuration.PropertyFilter;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.model.Organisation;
import org.opencm.util.LogUtils;

public class AssertionConfig {

	    private String reportName;
	    private LinkedList<String> layers;
	    private LinkedList<String> sublayers;
	    private LinkedList<String> versions;
	    private LinkedList<Organisation> audit;
	    private LinkedList<Property> properties;
	    private LinkedList<PropertyFilter> propertyFilters;
	    private boolean hideEquals;
	    private boolean includeDefaults;
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
	    
	    public String getReportName() {
	        return this.reportName;
	    }
	    public void setReportName(String name) {
	        this.reportName = name;
	    }
	    public LinkedList<String> getLayers() {
	        return this.layers;
	    }
	    public void setLayers(LinkedList<String> layers) {
	        this.layers = layers;
	    }
	    public LinkedList<String> getSublayers() {
	        return this.sublayers;
	    }
	    public void setSublayers(LinkedList<String> sublayers) {
	        this.sublayers = sublayers;
	    }
	    public LinkedList<String> getVersions() {
	        return this.versions;
	    }
	    public void setVersions(LinkedList<String> versions) {
	        this.versions = versions;
	    }
	    
	    public LinkedList<Organisation> getAudit() {
	        return this.audit;
	    }
	    public void setExtract(LinkedList<Organisation> orgs) {
	        this.audit = orgs;
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
	    public boolean getIncludeDefaultValues() {
	        return this.includeDefaults;
	    }
	    public void setIncludeDefaultValues(boolean def) {
	        this.includeDefaults = def;
	    }
	    
	    public LinkedList<FormatRule> getFormatting() {
	        return this.formatting;
	    }
}
