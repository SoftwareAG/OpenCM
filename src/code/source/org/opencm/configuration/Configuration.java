package org.opencm.configuration;

import java.io.File;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.opencm.configuration.model.Organisation;
import org.opencm.util.LogUtils;

public class Configuration {
	
    public static String OPENCM_MAIN_CONFIG 			= "opencm.properties";
    
    public static String OPENCM_INVENTORY 				= "inventory.properties";
    public static String OPENCM_EXCTRACT_PROPS 			= "extract.properties";
    
    public static String OPENCM_CONFIG_DIR_AUDIT 		= "audit"; 
    public static String OPENCM_REFERENCE_DIR_PREFIX 	= "REF_";
    
    public static String OPENCM_RESULTS_DIR_CLI 		= "cli";
    public static String OPENCM_RESULTS_DIR_DNDTREE 	= "dndTree";
    public static String OPENCM_FREEMARKER_DIR_TMPL 	= "freemarker";

    public static String OPENCM_BASELINE_DIR 			= "baseline";
    public static String OPENCM_RUNTIME_DIR 			= "runtime";
    public static String OPENCM_DEFAULT_DIR 			= "default";
    
    public static String OPENCM_LOG_CRITICAL 			= "CRITICAL";
    public static String OPENCM_LOG_ERROR 				= "ERROR";
    public static String OPENCM_LOG_WARNING 			= "WARNING";
    public static String OPENCM_LOG_INFO 				= "INFO";
    public static String OPENCM_LOG_DEBUG 				= "DEBUG";
    public static String OPENCM_LOG_TRACE 				= "TRACE";

    private String config_directory;
    private String cmdata_root;
    private String output_dir;
    private InventoryConfiguration inventory_config;
    private String cce_mgmt_node;
    private String cce_mgmt_group_syntax;
    private String cce_mgmt_group_delim;
    private LinkedList<Organisation> cce_mgmt_create;
    private String synch_local_opencm_node;
    private String synch_target_opencm_node;
    private String synch_ftps_timeout_ms;
    private LinkedList<Organisation> synch;
    private Smtp smtp;
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
    public InventoryConfiguration getInventory_config() {
        return this.inventory_config;
    }
    public void setInventory_config(InventoryConfiguration inventory_cfg) {
        this.inventory_config = inventory_cfg;
    }
    public String getCce_mgmt_node() {
        return this.cce_mgmt_node;
    }
    public void setCce_mgmt_node(String node) {
        this.cce_mgmt_node = node;
    }
    public String getCce_mgmt_group_syntax() {
        return this.cce_mgmt_group_syntax;
    }
    public void setCce_mgmt_group_syntax(String syntax) {
        this.cce_mgmt_group_syntax = syntax;
    }
    public String getCce_mgmt_group_delim() {
        return this.cce_mgmt_group_delim;
    }
    public void setCce_mgmt_group_delim(String delim) {
        this.cce_mgmt_group_delim = delim;
    }
    public LinkedList<Organisation> getCce_mgmt_create() {
        return this.cce_mgmt_create;
    }
    public void setCce_mgmt_create(LinkedList<Organisation> create) {
        this.cce_mgmt_create = create;
    }
    public String getSynch_local_opencm_node() {
        return this.synch_local_opencm_node;
    }
    public void setSynch_local_opencm_node(String node) {
        this.synch_local_opencm_node = node;
    }
    public String getSynch_target_opencm_node() {
        return this.synch_target_opencm_node;
    }
    public void setSynch_target_opencm_node(String node) {
        this.synch_target_opencm_node = node;
    }
    public String getSynch_ftps_timeout_ms() {
        return this.synch_ftps_timeout_ms;
    }
    public void setSynch_ftps_timeout_ms(String ms) {
        this.synch_ftps_timeout_ms = ms;
    }
    public LinkedList<Organisation> getSynch() {
        return this.synch;
    }
    public void setSynch(LinkedList<Organisation> synch) {
        this.synch = synch;
    }
    public Smtp getSmtp() {
        return this.smtp;
    }
    public void setSmtp(Smtp smtp) {
        this.smtp = smtp;
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
    
}
