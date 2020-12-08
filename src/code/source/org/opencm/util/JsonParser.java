package org.opencm.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

import org.opencm.repository.Property;
import org.opencm.util.LogUtils;

public class JsonParser {

	/*
     * The structure of the json is unknown, and can contain multiple levels and arrays. 
     * The result is placed in the hashmap with the format:
     *  - key = path.key
     *  - value = value
     *  
     */
    public static LinkedList<Property> getProperties(String json)  {
    	
    	LinkedList<Property> properties = new LinkedList<Property>();
    	try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jNode = mapper.readTree(json);
			readNode(properties, jNode, "/");
			
    	} catch (Exception ex) {
    		LogUtils.logDebug("JsonParser :: gracefully ignoring malformed json: " + ex.getMessage());
    	}

    	return properties;
    }

    /*
     * Read arbitrary json structure
     * 
     */
    private static void readNode(LinkedList<Property> properties, JsonNode jsonNode, String key) {
		
		Iterator<Map.Entry<String,JsonNode>> fieldsIterator = jsonNode.fields();
		if (fieldsIterator.hasNext()) {
		    while (fieldsIterator.hasNext()) {
		    	Map.Entry<String,JsonNode> jsonField = fieldsIterator.next();
		    	String fieldName = jsonField.getKey();
		    	JsonNode fieldNode = jsonField.getValue();
		    	
		    	// Array
		    	if (jsonNode.isArray() || jsonNode.fieldNames().hasNext()) {
		    		// Call this recursively
		    		if (key.equals("/")) {
			    		readNode(properties, fieldNode, key + fieldName);
		    		} else {
			    		readNode(properties, fieldNode, key + "/" + fieldName);
		    		}
		    	} else {
		    		LogUtils.logTrace(" (1) ---- NOT Array : " + jsonNode.toString());
			    	// No array
					if (jsonNode.isNumber()) {
						LogUtils.logTrace(fieldName + " - (number) " + jsonNode.toString());
						properties.add(new Property(key,jsonNode.toString()));
					} else if (jsonNode.isTextual()) {
						LogUtils.logTrace(fieldName + " - (text) " + jsonNode.textValue());
						properties.add(new Property(key,jsonNode.textValue()));
	    			} else if (jsonNode.isValueNode()) {
	    				LogUtils.logTrace(fieldName + " - (value) " + jsonNode.toString());
						properties.add(new Property(key,jsonNode.toString()));
	    			} else if (jsonNode.isMissingNode()) {
	    				LogUtils.logTrace(fieldName + " - Missing Node :: " + jsonNode.toString());
	    			} else {
	    				LogUtils.logTrace(fieldName + " - (other) " + jsonNode.toString());
						properties.add(new Property(key,jsonNode.toString()));
	    			}
		    	}
		    }
		    
		} else {
			if (jsonNode.isArray()) {
				
				Iterator<JsonNode> arrayIterator = jsonNode.iterator();
				int i = 0;
				while (arrayIterator.hasNext()) {
			    	JsonNode arrayNode = arrayIterator.next();
		    		
					Iterator<String> arrayFieldsIterator = arrayNode.fieldNames();
					if (arrayFieldsIterator.hasNext()) {
				    	String jsonFieldName = arrayFieldsIterator.next();

				    	// ---------------------------------------------------------
				    	// Not pretty but, Hard coding array items with @name/$ key-value combinations...
				    	// ---------------------------------------------------------
				    	if (jsonFieldName.equals("@name")) {
				    		Iterator<String> arrayItemIterator = arrayNode.fieldNames();
				    		if (arrayItemIterator.hasNext()) {
				    			String jsonFieldKey = arrayItemIterator.next();
						    	String stArrayKey = arrayNode.findValue(jsonFieldKey).asText();

						    	if (arrayItemIterator.hasNext()) {	// Should...
					    			String jsonFieldValue = arrayItemIterator.next();
						    		String stArrayValue = arrayNode.findValue(jsonFieldValue).toString();
									properties.add(new Property(key + "/" + stArrayKey,stArrayValue));
						    	}
				    		}
					    	continue;
				    	}
				    	if (key.equals("/")) {
				    		readNode(properties, arrayNode, key + jsonFieldName);
			    		} else {
				    		readNode(properties, arrayNode, key + "/" + jsonFieldName);
			    		}
					} else {
						properties.add(new Property(key + "[" + i + "]",arrayNode.toString()));
					}
					i++;
				}
			} else {
				properties.add(new Property(key,jsonNode.toString()));
				// properties.add(new Property(key,jsonNode.textValue()));
			}
		}
		
    }
    
    /*
     * Used for getting Components and Instances
     * 
     */
    public static HashMap<String,LinkedList<Property>> getArray(String json, String path, String fieldName) {
		HashMap<String,LinkedList<Property>> hmList = new HashMap<String,LinkedList<Property>>();
    	try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jNode =  mapper.readTree(json).at(path);

			if (jNode.isMissingNode()) {
				return hmList;
			}
			if (jNode.has(fieldName)) {
				// Single item (no Array)
				hmList.put(jNode.get(fieldName).textValue(), getProperties(jNode.toString()));
			} else if (jNode.isArray()) {
		    	ArrayNode arrayNode = (ArrayNode) jNode;
				for (int i = 0; i < arrayNode.size(); i++) {
					jNode = arrayNode.get(i);
					hmList.put(jNode.get(fieldName).textValue(),getProperties(jNode.toString()));
				}
			}
			return hmList;
			
    	} catch (Exception ex) {
    		LogUtils.logError("JsonParser :: getArray - Exception: " + ex.getMessage());
    	}
		
    	return null;
    }

    
}

