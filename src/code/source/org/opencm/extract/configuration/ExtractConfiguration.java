package org.opencm.extract.configuration;

import java.io.File;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.model.Organisation;
import org.opencm.util.LogUtils;

public class ExtractConfiguration {
    private LinkedList<Organisation> extracts;
    
    public static ExtractConfiguration instantiate(Configuration opencmConfig) {
    	ExtractConfiguration extractProps = null;
    	File extractFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_EXCTRACT_PROPS);
    	if (!extractFile.exists()) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"ExtractConfiguration - File not found: " + extractFile.getPath());
    		return null;
    	}
    	
    	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    	try {
    		extractProps = mapper.readValue(extractFile, ExtractConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," ExtractConfiguration - Exception: " + e.toString());
    	}
    	return extractProps;
    	
    }
    
    public LinkedList<Organisation> getExtract() {
        return this.extracts;
    }
    public void setExtract(LinkedList<Organisation> orgs) {
        this.extracts = orgs;
    }
 
}
