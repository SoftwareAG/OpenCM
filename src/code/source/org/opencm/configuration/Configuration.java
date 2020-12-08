package org.opencm.configuration;

import java.io.File;
import com.wm.app.b2b.server.globalvariables.GlobalVariablesManager;

import org.opencm.secrets.SecretsConfiguration;
import org.opencm.util.LogUtils;

public class Configuration {
	
    public static final String OPENCM_PACKAGE_NAME				= "OpenCM"; 
    public static final String OPENCM_GC_ROOT_DIR_KEY			= "OPENCM_ROOT_DIR";
    
    public static final String OPENCM_DIR_CONFIG		= "config";
    	public static final String OPENCM_DIR_AUDIT 		= "audit"; 
    	public static final String OPENCM_DIR_CCE 			= "cce"; 
    	public static final String OPENCM_DIR_EXTRACT		= "extraction"; 
    	public static final String OPENCM_DIR_GOVERNANCE	= "governance"; 
    	public static final String OPENCM_DIR_INVENTORY		= "inventory"; 
    	public static final String OPENCM_DIR_LOG_CONFIG	= "logging";
    	public static final String OPENCM_DIR_SECRETS		= "secrets";
    	public static final String OPENCM_DIR_SMTP			= "smtp";
    	public static final String OPENCM_DIR_SYNCH			= "synch"; 
    
    public static final String OPENCM_DIR_REPOSITORY	= "repository"; 
    
    public static final String OPENCM_PKG_DIR_RESOURCES	= "resources"; 
    
    /*
     * By default, all configurations are located under the package resource directory
     * 
     */
    public static void initConfiguration() {
		// Set Default Root Directory
		try {
	    	GlobalVariablesManager.getInstance().addGlobalVariable(OPENCM_GC_ROOT_DIR_KEY, getPackageResourceDirectory().getPath(), false);
			LogUtils.logInfo("Initialization: Default Global Variable for Root Directory created ... using package resources.");
		} catch (Exception e) {
			LogUtils.logInfo("Initialization: Global Variable for Root Directory already exists... ");
		}
		// Initialize Secrets Configuration
		SecretsConfiguration.instantiate();
    }
    
    public static String getRootDirectory() {
    	
		try {
	    	GlobalVariablesManager mgr = GlobalVariablesManager.getInstance();
	    	String val = mgr.getGlobalVariableValue(OPENCM_GC_ROOT_DIR_KEY).getValue();
			if (val == null) {
				LogUtils.logFatal("Configuration: No OpenCM Configuration root directory defined in Global Variables :: " + OPENCM_GC_ROOT_DIR_KEY);
				return null;
			}
	        return val; 
		} catch (Exception e) {
			LogUtils.logFatal("OpenCM Configuration getRootDirectory Exception: " + e.getMessage());
		}
		return null;
    }
    
    public static File getPackageConfigDirectory() {
		return new File(com.wm.app.b2b.server.ServerAPI.getPackageConfigDir(OPENCM_PACKAGE_NAME).getPath());
    }
    
    public static File getPackageResourceDirectory() {
		return new File(com.wm.app.b2b.server.ServerAPI.getPackageConfigDir(OPENCM_PACKAGE_NAME).getPath() + File.separator + ".." + File.separator + OPENCM_PKG_DIR_RESOURCES);
    }
    
}
