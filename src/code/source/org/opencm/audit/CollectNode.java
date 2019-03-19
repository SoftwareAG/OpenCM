package org.opencm.audit;

import org.opencm.configuration.Configuration;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.opencm.audit.util.JsonParser;
import org.opencm.audit.configuration.AuditConfiguration;
import org.opencm.util.FileUtils;
import org.opencm.util.LogUtils;
import org.opencm.util.StringUtils;

public class CollectNode {

	private static final String 	PROPERTY_TYPE_PRODUCT				= "NODE-PRODUCTS";
	private static final String 	PROPERTY_TYPE_FIX					= "NODE-FIXES";
	private static final String 	PROPERTY_TYPE_IS_PACKAGE			= "IS-PACKAGES";

	public static AuditObject getPropertyValues(Configuration opencmConfig, AuditConfiguration ac, AuditObject ao) {
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CollectNode :: getPropertyValues :: " + ao.getAuditInstallation().getInstallation().getName());
		
		// -------------------------------------------------------
		// Determine path for node repository
		// -------------------------------------------------------
		File repoDir = new File(opencmConfig.getCmdata_root() + File.separator + ao.getRepository() + File.separator + ao.getAuditInstallation().getInstallation().getName());
		if (!repoDir.exists()) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CollectNode :: getPropertyValues: skipping (does not exist): " + repoDir.getPath());
			return null;
		}
		
		// -------------------------------------------------------
		// Traverse repository directories
		// -------------------------------------------------------
		LinkedList<String> compDirs = FileUtils.getSubDirectories(repoDir.getPath(), ac.getProp_component());
		for (int i = 0; i < compDirs.size(); i++) {
			String compDir = compDirs.get(i);
			LinkedList<String> instDirs = FileUtils.getSubDirectories(repoDir + File.separator + compDir, ac.getProp_instance());
			for (int j = 0; j < instDirs.size(); j++) {
				String instDir = instDirs.get(j);
				File propFile = new File(repoDir + File.separator + compDir + File.separator + instDir + File.separator + "ci-properties.json");
				if (propFile.exists()) {
					LinkedList<AuditValue> avs = getValues(opencmConfig, ac, ao, propFile, compDir, instDir);
					if (avs.size() > 0) {
						ao.addAuditValues(avs);
					}
				}
			}
		}
		
		return ao;
	}
	
	/**
	 * Method to retrieve the values from the property file.
	 */
	private static LinkedList<AuditValue> getValues(Configuration opencmConfig, AuditConfiguration ac, AuditObject ao, File propFile, String component, String instance) {
		LinkedList<AuditValue> avs = new LinkedList<AuditValue>();
		
		// -----------------------------------------------------
		// Support multiple keys (comma-delimited)
		// -----------------------------------------------------
		LinkedList<String> keyElements = new LinkedList<String>();
		String key = ac.getProp_key();
		while (true) {
			String parsedKey = parseKey(key);
			if (parsedKey == null) {
				break;
			}
			keyElements.add(parsedKey);
			key = key.substring(parsedKey.length()).trim();
			if (key.startsWith(",")) {
				key = key.substring(key.indexOf(",") + 1).trim();
			}
		}

		try {
			for (int i = 0; i < keyElements.size(); i++) {
				JsonParser jp = new JsonParser(opencmConfig, propFile.getPath(), keyElements.get(i));
				HashMap<String,String> jsonValues = jp.getProperties();
				// -----------------------------------------------------
				// Parse Keys and Values from the json property file
				// -----------------------------------------------------
				if (jsonValues != null) {
					java.util.Iterator<String> it = jsonValues.keySet().iterator();
					while (it.hasNext()) {
						String propKey = it.next();
						// ------------------------------------------------
						// Apply Configuration Filters
						// ------------------------------------------------
						boolean filtered = false;
						if (ac.getProp_filters() != null) {
							for (int f = 0; f < ac.getProp_filters().size(); f++) {
								// Component
								if (matches(component, ac.getProp_filters().get(f).getComponent())) {
									// Instance
									if (matches(instance, ac.getProp_filters().get(f).getInstance())) {
										// Key
										if (matches(propKey, ac.getProp_filters().get(f).getKey())) {
											LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"  :: getValues: filtering out " + propKey);
											filtered = true;
											break;
										}
									}
								}
							}
						}
						if (!filtered) {
							AuditValue av = new AuditValue(component, instance, getPropertyName(component, instance, propKey), jsonValues.get(propKey));
							avs.add(av);
						}
					}
				}
			}
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CollectNode :: getValues: " + ex.toString());
		}
		
		return avs;
	}
	
	
	 /*
	  * Helper to support lists and wild cards in component and instance names in the property files (property definitions and filters).
	  * Key can be a list of elements, and each element can contain the "*" wild card character (e.g. "Comp*Name_01,Component_02*", etc.)
	  * Wild cards are then converted to regexp and matched against the name passed.
	  *  
	  */
	 
	public static boolean matches(String name, String key) {
		LinkedList<String> keyElements = new LinkedList<String>();
		int multipleElementsIdx = key.indexOf(",");
		if (multipleElementsIdx > 0) {
			StringTokenizer st = new StringTokenizer(key,",");
			while (st.hasMoreTokens()) {
				keyElements.add(st.nextToken().trim());
			}
			
		} else {
			keyElements.add(key);
		}
			
		for (int i = 0; i < keyElements.size(); i++) {
			String stElement = StringUtils.getRegexString(keyElements.get(i));
			if (name.matches(stElement.toString())) {
				return true;
			}
			
		}
		return false;
	}

	/*
	 * Removes the instance names (IS instance, MWS instance, UM realm name, etc.)
	 * Replaces the instance name with search pattern, used for getting subdirectories 
	 * using the matches expression
	 */
	public static String normalizeComponentName(String name) {
		if (name.startsWith("integrationServer-")) {
			return name.substring(0,name.indexOf("-")) + ".*";
		}
		if (name.startsWith("Universal-Messaging-")) {
			return name.substring(0,name.indexOf("-",10)) + ".*";	// Multiple "-" in component name and there might be more in the realm name
		}
		if (name.startsWith("MwsProgramFiles-")) {
			return name.substring(0,name.indexOf("-")) + ".*";
		}
		if (name.startsWith("TaskEngineRuntime-")) {
			return name.substring(0,name.indexOf("-")) + ".*";
		}
		if (name.startsWith("TES-")) {
			return name.substring(0,name.indexOf("-")) + ".*";
		}
		if (name.startsWith("OSGI-IS_")) {
			if (name.endsWith("-EventRouting") || name.endsWith("-NERV") || name.endsWith("-WmBusinessRules") || name.endsWith("-WmMonitor") || name.endsWith("-WmTaskClient")) {
				return name.substring(0,(name.indexOf("_") + 1)) + ".*." + name.substring(name.lastIndexOf("-") + 1);
			} else {
				return name.substring(0,name.indexOf("_")) + ".*";
			}
		}
		if (name.startsWith("OSGI-MWS_")) {
			if (name.endsWith("-EventRouting")) {
				return name.substring(0,(name.indexOf("_") + 1)) + ".*." + name.substring(name.lastIndexOf("-") + 1);
			} else {
				return name.substring(0,name.indexOf("_")) + ".*";
			}
		}
		
		return name;
	}
    
	/*
	 * Converts a component name to a default name (used to locate default values)
	 */
	public static String convertComponentNameToDefault(String name) {
		if (name.startsWith("integrationServer-")) {
			return name.substring(0,name.indexOf("-")+1) + "default";
		}
		if (name.startsWith("Universal-Messaging-")) {
			return name.substring(0,name.indexOf("-",10)+1) + "umserver";	
		}
		if (name.startsWith("MwsProgramFiles-")) {
			return name.substring(0,name.indexOf("-")+1) + "default";
		}
		if (name.startsWith("TaskEngineRuntime-")) {
			return name.substring(0,name.indexOf("-")+1) + "default";
		}
		if (name.startsWith("TES-")) {
			return name.substring(0,name.indexOf("-")+1) + "default";
		}
		if (name.startsWith("OSGI-IS_")) {
			if (name.endsWith("-EventRouting") || name.endsWith("-NERV") || name.endsWith("-WmBusinessRules") || name.endsWith("-WmMonitor") || name.endsWith("-WmTaskClient")) {
				return name.substring(0,(name.indexOf("_") + 1)) + "default" + name.substring(name.lastIndexOf("-") + 1);
			} else {
				return name.substring(0,name.indexOf("_")+1) + "default";
			}
		}
		if (name.startsWith("OSGI-MWS_")) {
			if (name.endsWith("-EventRouting")) {
				return name.substring(0,(name.indexOf("_") + 1)) + "default" + name.substring(name.lastIndexOf("-") + 1);
			} else {
				return name.substring(0,name.indexOf("_")+1) + "default";
			}
		}
		
		return name;
	}
	
	private static String parseKey(String inKey) {
		// Verify that we don't have dynamic keys to deal with... e.g. /[key01,key02],/[key01,key02]
		int dynamicKeysStartIdx = inKey.indexOf("[");
		int dynamicKeysEndIdx = inKey.indexOf("]");
		int multipleKeysIdx = inKey.indexOf(",");
		if ((dynamicKeysStartIdx > 0) && (dynamicKeysEndIdx > 0) && (multipleKeysIdx > dynamicKeysStartIdx)) {
			return inKey.substring(0,dynamicKeysEndIdx + 1).trim();
		}
		if (multipleKeysIdx > 0) {
			return inKey.substring(0,multipleKeysIdx).trim();
		}
		if (!inKey.trim().equals("")) {
			return inKey; 
		}
		return null;
	}

    private static String getPropertyName(String component, String instance, String propertyKey) {
    	if (componentIsFixed(component)) {
    		if (propertyKey.startsWith("/")) {
    			return instance + " [" + propertyKey.substring(1) + "]";
    		} else {
    			return instance + " [" + propertyKey + "]";
    		}
    	}
    	
    	if (isPackage(instance) && propertyKey.lastIndexOf("/") > 14) {
    	    // Key = /packageName/<package_name>/enabled or /packageName/<package_name>/version 
    		String packageName = propertyKey.substring(13,propertyKey.lastIndexOf("/")) + " [" + propertyKey.substring(propertyKey.lastIndexOf("/") + 1) + "]";
    		return packageName;
    	}
    	
    	// Key name with the root path prefixed ("/")
		if (propertyKey.startsWith("/")) {
			return propertyKey.substring(1);
		}
    	
    	// Default: key name
        return propertyKey;
    }
    
    private static boolean componentIsFixed(String component) {
    	if ((component != null) && (component.equals(PROPERTY_TYPE_PRODUCT) || component.equals(PROPERTY_TYPE_FIX))) {
    		return true;
    	}
    	
        return false;
    }

    private static boolean isPackage(String instance) {
    	if ((instance != null) && (instance.equals(PROPERTY_TYPE_IS_PACKAGE))) {
    		return true;
    	}
    	
        return false;
    }

}
