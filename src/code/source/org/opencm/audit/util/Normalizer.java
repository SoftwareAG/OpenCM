package org.opencm.audit.util;

import java.util.LinkedList;
import java.util.StringTokenizer;
import org.opencm.util.StringUtils;

public class Normalizer {


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

	
	
}
