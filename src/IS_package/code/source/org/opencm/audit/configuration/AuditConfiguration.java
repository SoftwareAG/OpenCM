package org.opencm.audit.configuration;

import java.io.File;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;
import org.opencm.util.PackageUtils;

public class AuditConfiguration {
	
    private String testName;
    private boolean assertComponents;
    private LinkedList<ComponentFilter> componentFilters;
    private boolean assertInstances;
    private LinkedList<InstanceFilter> instanceFilters;
    
    private boolean assertProperties;
    private LinkedList<Property> properties;
    private LinkedList<PropertyFilter> propertyFilters;
    
    public AuditConfiguration () {
    }
    
    
    public static AuditConfiguration instantiate(Configuration opencmConfig, String properties) {
    	AuditConfiguration auditConf = null;
    	File auditConfigFile;
    	if (properties.equals(Configuration.OPENCM_AUDIT_BASELINE_RUNTIME_NODE_PROPS)) {
    		auditConfigFile = new File(PackageUtils.getPackageConfigPath() + File.separator + Configuration.OPENCM_AUDIT_BASELINE_RUNTIME_NODE_PROPS);
    	} else if (properties.equals(Configuration.OPENCM_AUDIT_DEFAULT_PROPS)) {
    		auditConfigFile = new File(PackageUtils.getPackageConfigPath() + File.separator + Configuration.OPENCM_AUDIT_DEFAULT_PROPS);
    	} else {
    		auditConfigFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_CONFIG_DIR_TWO_NODE_AUDIT + File.separator + properties + ".properties");
    	}
    	
    	if (!auditConfigFile.exists()) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"AuditConfiguration - File not found: " + auditConfigFile.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    	try {
    		auditConf = mapper.readValue(auditConfigFile, AuditConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," AuditConfiguration - Exception: " + e.toString());
    	}
    	return auditConf;
    }
    
    public String getTestName() {
        return this.testName;
    }
    
    public void setTestName(String name) {
        this.testName = name;
    }

    public boolean getAssertComponents() {
        return this.assertComponents;
    }

    public LinkedList<ComponentFilter> getComponentFilters() {
        return this.componentFilters;
    }

    public boolean getAssertInstances() {
        return this.assertInstances;
    }
    
    public LinkedList<InstanceFilter> getInstanceFilters() {
        return this.instanceFilters;
    }
    
    public boolean getAssertProperties() {
        return this.assertProperties;
    }
    public LinkedList<Property> getProperties() {
        return this.properties;
    }
    public LinkedList<PropertyFilter> getPropertyFilters() {
        return this.propertyFilters;
    }
    
}
