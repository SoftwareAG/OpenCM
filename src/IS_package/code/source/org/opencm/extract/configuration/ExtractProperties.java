package org.opencm.extract.configuration;

import java.io.File;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;

public class ExtractProperties {
    private boolean extractAll;
    private LinkedList<String> opencm_environments;
    private LinkedList<String> nodes;
    
    public static ExtractProperties instantiate(Configuration opencmConfig) {
    	ExtractProperties extractProps = null;
    	File extractPropFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_EXCTRACT_PROPS);
    	if (!extractPropFile.exists()) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"ExtractProperties - File not found: " + extractPropFile.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    	try {
    		extractProps = mapper.readValue(extractPropFile, ExtractProperties.class);
    	} catch (Exception e) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," ExtractProperties - Exception: " + e.toString());
    	}
    	return extractProps;
    	
    }
    
    public boolean getExtractAll() {
        return this.extractAll;
    }
    public LinkedList<String> getOpencm_environments() {
        return this.opencm_environments;
    }
    public LinkedList<String> getNodes() {
        return this.nodes;
    }
    
}
