package org.opencm.util;

import java.util.HashMap;

public class Cache {

	private HashMap<String, String> hm;
	 
	private static Cache sc = new Cache();
		
	public static Cache getInstance() {
			return sc; 
	}
		
	//Private constructor to prevent instantiation
	private Cache() {
		hm = new HashMap<String, String>();
	}
	 
	public void set(String key, String value) {
		hm.put(key, value);
	}
	 
	public String get(String key) {
		return hm.get(key);
	}

}
