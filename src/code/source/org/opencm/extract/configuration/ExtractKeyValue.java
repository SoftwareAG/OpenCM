package org.opencm.extract.configuration;

import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExtractKeyValue {

	private String key;
	private String value;

	public ExtractKeyValue(String key, String val) {
		setKey(key);
		setValue(val);
	}

	public String getKey() {
		return this.key;
	}
	public void setKey(String key) {
		this.key = key; 
	}

	public String getValue() {
		return this.value;
	}
	public void setValue(String val) {
		this.value = val; 
	}
	
    @JsonIgnore
	public static LinkedList<ExtractKeyValue> getKeyValuePairs(String keyValueString) {
    	LinkedList<ExtractKeyValue> kvs = new LinkedList<ExtractKeyValue>();
        String[] keyVals = keyValueString.split("\n");
        for(String keyVal:keyVals)
        {
          String[] parts = keyVal.split("=",2);
          ExtractKeyValue ekv = new ExtractKeyValue(parts[0],parts[1]);
          kvs.add(ekv);
        }
		return kvs;
	}

	
	
}
