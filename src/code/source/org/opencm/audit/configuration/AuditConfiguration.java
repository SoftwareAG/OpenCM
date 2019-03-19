package org.opencm.audit.configuration;

import java.io.File;
import java.util.LinkedList;

import org.opencm.configuration.Configuration;
import org.opencm.util.FileUtils;
import org.opencm.util.LogUtils;
import org.opencm.util.JsonUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuditConfiguration {
	
	public static String AUDIT_TYPE_LAYERED		= "layered";
	public static String AUDIT_TYPE_BASELINE	= "baseline";
	public static String AUDIT_TYPE_REFERENCE	= "reference";

	private String audit_description;
	private String audit_type;
	private String repo_name;
	private String prop_component;
	private String prop_instance;
	private String prop_key;
	private LinkedList<PropertyFilter> prop_filters; 
	private LinkedList<TreeNode> tree_nodes;
	
    public static Configuration instantiate(Configuration opencmConfig, String templateName) {
    	Configuration auditConf = null;
    	File auditConfigFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_CONFIG_DIR_AUDIT + File.separator + templateName + ".properties");
    	
    	if (!auditConfigFile.exists()) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"AuditConfiguration - File not found: " + auditConfigFile.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		auditConf = mapper.readValue(auditConfigFile, Configuration.class);
    	} catch (Exception e) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," AuditConfiguration - Exception: " + e.toString());
    	}
    	return auditConf;
    }
    
    public String getAudit_description() {
        return this.audit_description;
    }
    public void setAudit_description(String desc) {
        this.audit_description = desc;
    }

    public String getAudit_type() {
        return this.audit_type;
    }
    public void setAudit_type(String type) {
        this.audit_type = type;
    }
    
    public String getRepo_name() {
        return this.repo_name;
    }
    public void setRepo_name(String name) {
        this.repo_name = name;
    }

    public String getProp_component() {
        return this.prop_component;
    }
    public void setProp_component(String comp) {
        this.prop_component = comp;
    }

    public String getProp_instance() {
        return this.prop_instance;
    }
    public void setProp_instance(String inst) {
        this.prop_instance = inst;
    }

    public String getProp_key() {
        return this.prop_key;
    }
    public void setProp_key(String key) {
        this.prop_key = key;
    }

    public LinkedList<PropertyFilter> getProp_filters() {
        return this.prop_filters;
    }
    public void setProp_filters(LinkedList<PropertyFilter> filters) {
        this.prop_filters = filters;
    }

    public LinkedList<TreeNode> getTree_nodes() {
        return this.tree_nodes;
    }
    public void setTree_nodes(LinkedList<TreeNode> treeNodes) {
        this.tree_nodes = treeNodes;
    }
    

    @JsonIgnore
    public void write(Configuration opencmConfig, String templateName) {
    	File templateFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_CONFIG_DIR_AUDIT + File.separator + templateName + ".properties");
		String json = JsonUtils.convertJavaObjectToJson(this);
		FileUtils.writeToFile(templateFile.getPath(), json);
    }

}
