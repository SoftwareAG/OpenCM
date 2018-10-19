package org.opencm.util;

import java.util.HashMap;

public class Cache {

	private HashMap<String, Object> hm;
	 
	private static Cache sc = new Cache();
		
	public static Cache getInstance() {
			return sc; 
	}
		
	//Private constructor to prevent instantiation
	private Cache() {
		hm = new HashMap<String, Object>();
	}
	 
	public void set(String key, Object value) {
		hm.put(key, value);
	}
	 
	public Object get(String key) {
		return hm.get(key);
	}

}
