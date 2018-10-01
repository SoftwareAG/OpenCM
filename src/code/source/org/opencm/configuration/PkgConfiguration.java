package org.opencm.configuration;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opencm.util.LogUtils;
import org.opencm.util.PackageUtils;

public class PkgConfiguration {
	
    public static String OPENCM_PACKAGE_NAME 		= "OpenCM";
    public static String OPENCM_PROPERTY_FILE 		= "default.properties";
    
    private String config_directory;

    public PkgConfiguration() {
    }
    
    public static PkgConfiguration instantiate() {
		File opencmConfigFile = new File(PackageUtils.getPackageConfigPath() + File.separator + PkgConfiguration.OPENCM_PROPERTY_FILE);
		if (!opencmConfigFile.exists()) {
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_CRITICAL,"Unable to access OpenCM configuration file: " + opencmConfigFile.getPath());
		}

		ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); 
		try {
			return mapper.readValue(opencmConfigFile, PkgConfiguration.class);
		} catch (Exception e) {
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_CRITICAL,"OpenCM Properties Exception: " + e.toString());
		}
		return null;
    }
    
    
    public String getConfig_directory() {
        return this.config_directory;
    }
}
