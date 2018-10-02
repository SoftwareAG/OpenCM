package org.opencm.audit.env;

import java.io.File;
import org.opencm.repository.util.RepoUtils;


public class AssertionValue {

	private static final String 	PROPERTY_TYPE_PRODUCTS				= "NODE-PRODUCTS";
	private static final String 	PROPERTY_TYPE_FIXES					= "NODE-FIXES";
	private static final String 	PROPERTY_TYPE_IS_PACKAGE			= "IS-PACKAGES";
	
	private String missing_info;			// Used for identifying missing information
	private String node;
    private String component;
    private String instance;
    private String key;
    private String value;
    private String value_default;
    private File propertyFile;
    private boolean filtered;
    private boolean matched;

    public AssertionValue(String missingInfo) {
    	setMissingInfo(missingInfo);
    }
    
    public AssertionValue(String node, String comp, String instance, String key, String value, File propFile, boolean filtered) {
    	setNode(node);
    	setComponent(comp);
    	setInstance(instance);
    	setKey(key);
    	setValue(value);
    	setPropertyFile(propFile);
    	setFiltered(filtered);
    }
    
    public AssertionValue(String node, String comp, String instance, File propFile) {
    	setNode(node);
    	setComponent(comp);
    	setInstance(instance);
    	setPropertyFile(propFile);
    }
    
    public String getMissingInfo() {
        return this.missing_info;
    }
    public void setMissingInfo(String missingInfo) {
        this.missing_info = missingInfo;
    }
    
    public String getNode() {
        return this.node;
    }
    public void setNode(String node) {
        this.node = node;
    }
    public String getComponent() {
        return this.component;
    }
    public void setComponent(String comp) {
        this.component = comp;
    }
    public String getInstance() {
        return this.instance;
    }
    public void setInstance(String instance) {
        this.instance = instance;
    }
    public String getKey() {
        return this.key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String val) {
        this.value = val;
    }
    public String getDefaultValue() {
        return this.value_default;
    }
    public void setDefaultValue(String val) {
        this.value_default = val;
    }
    
    public File getPropertyFile() {
        return this.propertyFile;
    }
    public void setPropertyFile(File propFile) {
        this.propertyFile = propFile;
    }
    
    public boolean isFiltered() {
        return this.filtered;
    }
    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }
    
    public boolean isMatched() {
        return this.matched;
    }
    public void setMatched(boolean matched) {
        this.matched = matched;
    }
    
    public String getPropertyName() {
    	if (componentIsFixed()) {
    		if (this.key.startsWith("/")) {
    			return this.instance + " [" + this.key.substring(1) + "]";
    		} else {
    			return this.instance + " [" + this.key + "]";
    		}
    	}
    	
    	if (isPackage() && this.key.lastIndexOf("/") > 14) {
    	    // Key = /packageName/<package_name>/enabled or /packageName/<package_name>/version 
    		String packageName = this.key.substring(13,this.key.lastIndexOf("/")) + " [" + this.key.substring(this.key.lastIndexOf("/") + 1) + "]";
    		return packageName;
    	}
    	
    	// Key name with the root path prefixed ("/")
		if (this.key.startsWith("/")) {
			return this.key.substring(1);
		}
    	
    	// Default: key name
        return this.key;
    }
    
    public boolean componentIsFixed() {
    	if ((this.component != null) && (this.component.equals(PROPERTY_TYPE_PRODUCTS) || this.component.equals(PROPERTY_TYPE_FIXES))) {
    		return true;
    	}
    	
        return false;
    }

    public boolean isPackage() {
    	if ((this.instance != null) && (this.instance.equals(PROPERTY_TYPE_IS_PACKAGE))) {
    		return true;
    	}
    	
        return false;
    }
    
    public boolean isMatchedWith(AssertionValue av) {
    	if (RepoUtils.normalizeComponentName(getComponent()).equals(RepoUtils.normalizeComponentName(av.getComponent()))) {
    		if (getInstance().equals(av.getInstance())) {
    			if (getPropertyName().equals(av.getPropertyName())) {
    				setMatched(true);
    				return true;
    			}
    		}
    	}
        return false;
    }
    
}
