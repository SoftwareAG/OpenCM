package org.opencm.util;

public class StringUtils {

	/* 
	 * Helper to avoid having regexp in property files
	 *  
     */
	public static String getRegexString(String inString) {
		if (inString.equals("*")) {
			return ".*";
		} else if (inString.startsWith("*")) {
			return ".*" + inString.substring(1);
		} else if (inString.endsWith("*")) {
			return inString.substring(0, inString.indexOf("*")) + ".*";
		} else if (inString.contains("*")) {
			// Assuming wildcard in the middle
			return inString.substring(0, inString.indexOf("*")) + ".*." + inString.substring(inString.indexOf("*") + 1);
		}
			
		return inString;
	}

}
