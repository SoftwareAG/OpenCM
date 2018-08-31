package org.opencm.audit.configuration;

import java.io.File;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;

public class AuditNodesConfiguration {
    private LinkedList<AuditNodePair> nodes;
    
    public AuditNodesConfiguration () {
    }
    
    
    public static AuditNodesConfiguration instantiate(Configuration opencmConfig) {
    	AuditNodesConfiguration auditConf = null;
    	File nodesConfigFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_CONFIG_DIR_TWO_NODE_AUDIT + File.separator + Configuration.OPENCM_AUDIT_TWO_NODE_PROPS);
    	if (!nodesConfigFile.exists()) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"NodesConfiguration - File not found: " + nodesConfigFile.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    	try {
    		auditConf = mapper.readValue(nodesConfigFile, AuditNodesConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," AuditConfiguration - Exception: " + e.toString());
    	}
    	return auditConf;
    }
    
    public LinkedList<AuditNodePair> getNodes() {
        return this.nodes;
    }
    public void setNodes(LinkedList<AuditNodePair> pairs) {
        this.nodes = pairs;
    }
}
