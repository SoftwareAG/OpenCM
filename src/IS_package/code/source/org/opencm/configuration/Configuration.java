package org.opencm.configuration;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.opencm.audit.assertion.AssertionStore;
import org.opencm.util.LogUtils;

public class Configuration {
	
    public static String OPENCM_MAIN_CONFIG 						= "opencm.properties";
    
    public static String OPENCM_NODES_PROPS 						= "nodes.properties";
    public static String OPENCM_EXCTRACT_PROPS 						= "extract.properties";
    public static String OPENCM_SYNCH_PROPS 						= "synch.properties";
    public static String OPENCM_AUDIT_DEFAULT_PROPS 				= "default_auditing.properties";
    public static String OPENCM_AUDIT_TWO_NODE_PROPS 				= "audit_nodes.properties";
    public static String OPENCM_AUDIT_BASELINE_RUNTIME_NODE_PROPS 	= "baseline_runtime_node.properties";
    
    public static String OPENCM_CONFIG_DIR_TWO_NODE_AUDIT 		= "audit_two_node";
    public static String OPENCM_CONFIG_DIR_LAYERED_AUDIT 		= "audit_layered";
    
    public static String OPENCM_REFERENCE_DIR_PREFIX 			= "REF_";
    
    public static String OPENCM_RESULTS_DIR_HTML 		= "html";
    public static String OPENCM_RESULTS_DIR_EXCEL 		= "excel";
    public static String OPENCM_RESULTS_DIR_CLI 		= "cli";
    public static String OPENCM_RESULTS_DIR_DNDTREE 	= "dndTree";

    public static String OPENCM_OUTPUT_CLI			= "cce_cli.bat";
    
    public static String OPENCM_BASELINE_DIR 		= "baseline";
    public static String OPENCM_RUNTIME_DIR 		= "runtime";
    public static String OPENCM_DEFAULT_DIR 		= "default";
    
    public static String OPENCM_LOG_CRITICAL 		= "CRITICAL";
    public static String OPENCM_LOG_ERROR 			= "ERROR";
    public static String OPENCM_LOG_WARNING 		= "WARNING";
    public static String OPENCM_LOG_INFO 			= "INFO";
    public static String OPENCM_LOG_DEBUG 			= "DEBUG";
    public static String OPENCM_LOG_TRACE 			= "TRACE";

    private String config_directory;
    private String cmdata_root;
    private String output_dir;
    private EndpointConfiguration endpoint_config;
    private String ftps_timeout_ms;
    private String debug_level;
    
    public Configuration() {
    }
    
    public static Configuration instantiate(String configDir) {
    	
		File opencmConfigFile = new File(configDir + File.separator + OPENCM_MAIN_CONFIG);
		if (!opencmConfigFile.exists()) {
			LogUtils.log(OPENCM_LOG_INFO,OPENCM_LOG_CRITICAL,"Unable to access OpenCM configuration file: " + opencmConfigFile.getPath());
		}

		ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); 
		try {
			return mapper.readValue(opencmConfigFile, Configuration.class);
		} catch (Exception e) {
			LogUtils.log(OPENCM_LOG_INFO,OPENCM_LOG_CRITICAL,"OpenCM Properties Exception: " + e.toString());
		}
		return null;
    }
    
    public String getCmdata_root() {
        return this.cmdata_root;
    }
    public void setCmdata_root(String path) {
        this.cmdata_root = path;
    }
    public String getOutput_dir() {
        return this.output_dir;
    }
    public void setOutput_dir(String path) {
        this.output_dir = path;
    }
    public EndpointConfiguration getEndpoint_config() {
        return this.endpoint_config;
    }
    public void setEndpoint_config(EndpointConfiguration endpoint_cfg) {
        this.endpoint_config = endpoint_cfg;
    }
    public String getFtps_timeout_ms() {
        return this.ftps_timeout_ms;
    }
    public void setFtps_timeout_ms(String ms) {
        this.ftps_timeout_ms = ms;
    }
    public String getDebug_level() {
        return this.debug_level;
    }
    public void setDebug_level(String level) {
        this.debug_level = level;
    }
    
    @JsonIgnore
    public String getConfigDirectory() {
    	return this.config_directory;
    }
    @JsonIgnore
    public void setConfigDirectory(String cnf_dir) {
    	this.config_directory = cnf_dir;
    }
    
	 /*
	  * Helper to avoid having regexp in property files
	  *  
	  */
	 
	public static String convertPropertyKey(String key) {
		if (key.equals(AssertionStore.ANY_ASSERTION_KEYWORD)) {
			return key;
		}
		if (key.startsWith("*")) {
			return ".*" + key.substring(1);
		} else if (key.endsWith("*")) {
			return key.substring(0, key.indexOf("*")) + ".*";
		} else if (key.contains("*")) {
			// Assuming wildcard in the middle
			return key.substring(0, key.indexOf("*")) + ".*." + key.substring(key.indexOf("*") + 1);
		}
			
		return key;
	}
	
    
}
