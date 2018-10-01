package org.opencm.audit.util;

import java.util.Iterator;
import java.io.File;
import java.util.HashMap;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import com.wm.app.b2b.server.ServiceException;


public class JsonParser {

    private Configuration opencmConfig;
    private HashMap<String,String> hmProperties;
    private boolean isFieldArray = false;
    private String arrayKeyName;
    private String arrayKeyFieldName;
    private String arrayKeyFieldNameValue;
    private String arrayValueFieldName;
    
    public JsonParser(Configuration opencmConfig, String jsonFile, String key)  throws ServiceException {
        this.opencmConfig = opencmConfig;
        this.hmProperties = new HashMap<String,String>();
        parseKey(key);
        if (this.isFieldArray) {
        	key = this.arrayKeyName;
        }
        setJsonPaths(jsonFile, key);
    }

    public HashMap<String,String> getProperties() {
        return this.hmProperties;
    }

    public boolean isFieldArray() {
        return this.isFieldArray;
    }
    
    public String getArrayKeyFieldName() {
        return this.arrayKeyFieldName;
    }
    
    private void parseKey(String stKey) {
    	if ((stKey.indexOf("[") > 0) && (stKey.indexOf("]") > 0)) {
    		String arrayKeyValue = stKey.substring(stKey.indexOf("[") + 1, stKey.lastIndexOf("]"));
    		if (arrayKeyValue.indexOf(",") > 0) {
    			this.arrayKeyName = stKey.substring(0,stKey.indexOf("[")).trim();
    			this.arrayKeyFieldName = arrayKeyValue.substring(0, arrayKeyValue.indexOf(",")).trim();
    			// Allow for specifying pattern matching key field names
    			// e.g. [keyField=MyKey*,ValueField]
        		if (this.arrayKeyFieldName.indexOf("=") > 0) {
        			this.arrayKeyFieldNameValue = this.arrayKeyFieldName.substring(this.arrayKeyFieldName.indexOf("=") + 1).trim();
        			this.arrayKeyFieldName = this.arrayKeyFieldName.substring(0,this.arrayKeyFieldName.indexOf("=")).trim();
        		}
    			this.arrayValueFieldName = arrayKeyValue.substring(arrayKeyValue.indexOf(",") + 1).trim();
        		this.isFieldArray = true;
    		}
    	}
    }
    
    /*
     * The passed on key determines what to extract from the json file
     * If the key is simply the root path ("/") it is then assumed that all properties will be collected
     * The structure of the json is unknown, and can contain multiple levels and arrays. 
     * The result is placed in the hashmap with the format:
     *  - key = /path/key
     *  - value = value
     *  
     *  Certain keys are dynamic, which are handled as follows:
     *  - explicitly defined key-value pairs from the property definitions (e.g. key: /[key1,key2])
     *  - array of @name/$ pairs in the json property files - handled separately below
     */
    private void setJsonPaths(String jsonFile, String stKey) throws ServiceException {
    	
    	try {
			DocumentContext jsonContext = JsonPath.parse(new File(jsonFile));
			JsonFactory factory = new JsonFactory();
		
			ObjectMapper mapper = new ObjectMapper(factory);
			JsonNode rootNode = mapper.readTree(jsonContext.jsonString());
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Json: " + jsonContext.jsonString());
			if (stKey.equals("/") || stKey.equals("*")) {
				readNode(rootNode, "/");
			} else {
				JsonNode innerNode = rootNode.at(stKey);
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"setJsonPaths InnerNode: " + innerNode.toString());
				readNode(innerNode, stKey);
			}
    	} catch (Exception e) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM JsonParser: " + e.toString());
    		throw new ServiceException("OpenCM JsonParser: " + e.toString());
    	}
    }

    private void readNode(JsonNode jsonNode, String key) throws ServiceException {
		LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"--------> setJsonPaths readNode: isArray=" + jsonNode.isArray());
		
		Iterator<String> fieldsIterator = jsonNode.fieldNames();
		if (fieldsIterator.hasNext()) {
			String parsedArrayKey = null;
			String parsedArrayValue = null;
		    while (fieldsIterator.hasNext()) {
		    	String jsonFieldName = fieldsIterator.next();
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"setJsonPaths readNode: " + jsonFieldName);
				
		    	JsonNode innerNode = jsonNode.findPath(jsonFieldName);
		    	if (innerNode.isArray() || innerNode.fieldNames().hasNext()) {
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"setJsonPaths readNode hasNext = true or Array for key = " + key);
		    		if (key.equals("/")) {
			    		readNode(innerNode, key + jsonFieldName);
		    		} else {
			    		readNode(innerNode, key + "/" + jsonFieldName);
		    		}
		    	} else {
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"setJsonPaths readNode hasNext = false for key = " + key);
		    		if (this.isFieldArray) {
						LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  -- setJsonPaths readNode isFieldArray = true for key = " + key + ". jsonFieldName=" + jsonFieldName + " this.arrayKeyFieldName=" + this.arrayKeyFieldName + " this.arrayValueFieldName=" + this.arrayValueFieldName);
		    			if (jsonFieldName.equals(this.arrayKeyFieldName)) {
		    				parsedArrayKey = jsonNode.findValue(jsonFieldName).textValue();
		    				if ((this.arrayKeyFieldNameValue != null) && (!matches(parsedArrayKey,this.arrayKeyFieldNameValue))) {
		    					// Specified key name in the properties does not match the json key field name
			    				parsedArrayKey = null;
		    				}
		    			}
		    			
		    			if (jsonFieldName.equals(this.arrayValueFieldName)) {
		    				parsedArrayValue = jsonNode.findValue(jsonFieldName).textValue();
		    			}
		    		}
		    		if (this.isFieldArray) {
			    		if ((parsedArrayKey != null) && (parsedArrayValue != null)) {
							LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting Array: " + parsedArrayKey +  " = " + parsedArrayValue);
			    			this.hmProperties.put(key + "/" + parsedArrayKey + "/" + arrayValueFieldName, parsedArrayValue);
			    		}
		    		} else {
		    			String storeKey = key + "/" + jsonFieldName;
		    			if (key.equals("/")) {
		    				storeKey = key + jsonFieldName;
		    			}
		    			if (jsonNode.findValue(jsonFieldName).isTextual()) {
							LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting TextValue: " + jsonFieldName +  " = " + jsonNode.findValue(jsonFieldName).textValue());
				    		this.hmProperties.put(storeKey, jsonNode.findValue(jsonFieldName).textValue());
		    			} else if (jsonNode.findValue(jsonFieldName).isValueNode()) {
							LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting ValueNode: " + jsonFieldName +  " = " + jsonNode.findValue(jsonFieldName).toString());
		    				this.hmProperties.put(storeKey, jsonNode.findValue(jsonFieldName).toString());
		    			} else {
							LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting Other: " + jsonFieldName +  " = " + jsonNode.findValue(jsonFieldName).toString());
		    				this.hmProperties.put(storeKey, jsonNode.findValue(jsonFieldName).toString());
		    			}
		    		}
		    	}
		    }
		} else {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"OpenCM JsonParser: readNode :: NOT hasNext() for key " + key);
			
			if (jsonNode.isArray()) {
			//if (jsonNode.isArray() && this.isFieldArray) {
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"OpenCM JsonParser: readNode :: isArray and isFieldArray for key " + key);
				// Handle Arrays... assuming /arrayName[key,value] in user defined key
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"OpenCM JsonParser: readNode ::  Size of Json node: " + jsonNode.size());
				Iterator<JsonNode> arrayIterator = jsonNode.iterator();
				while (arrayIterator.hasNext()) {
			    	JsonNode arrayNode = arrayIterator.next();
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"OpenCM JsonParser: readNode ::  arrayNode: " + arrayNode.toString());
					Iterator<String> arrayFieldsIterator = arrayNode.fieldNames();
					if (arrayFieldsIterator.hasNext()) {
				    	String jsonFieldName = arrayFieldsIterator.next();
						LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"OpenCM JsonParser: readNode :: jsonFieldName= " + jsonFieldName);
				    	// ---------------------------------------------------------
				    	// Not pretty but, Hard coding array items with @name/$ key-value combinations...
				    	// ---------------------------------------------------------
				    	if (jsonFieldName.equals("@name")) {
				    		Iterator<String> arrayItemIterator = arrayNode.fieldNames();
				    		if (arrayItemIterator.hasNext()) {
				    			String jsonFieldKey = arrayItemIterator.next();
						    	String stArrayKey = arrayNode.findValue(jsonFieldKey).asText();
								LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Array @name: " + stArrayKey);
						    	if (arrayItemIterator.hasNext()) {	// Should...
					    			String jsonFieldValue = arrayItemIterator.next();
						    		String stArrayValue = arrayNode.findValue(jsonFieldValue).toString();
									LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Array " + jsonFieldValue + ": " + stArrayValue);
									LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting Array Key-Value: " + stArrayKey +  " = " + stArrayValue);
						    		this.hmProperties.put(key + "/" + stArrayKey, stArrayValue);
						    	}
				    		}
					    	continue;
				    	}
				    	if (key.equals("/")) {
				    		readNode(arrayNode, key + jsonFieldName);
			    		} else {
				    		readNode(arrayNode, key + "/" + jsonFieldName);
			    		}
					}
				}
			} else if (!this.isFieldArray) {
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"OpenCM JsonParser: readNode :: not a fieldArray for key " + key);
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"OpenCM JsonParser: json :: " + jsonNode.asText());
				
				if (jsonNode.isNumber()) {
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting Numer for Key: " + key +  " = " + jsonNode.toString());
		    		this.hmProperties.put(key, jsonNode.toString());
				} else if (jsonNode.isTextual()) {
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting TextValue for Key: " + key +  " = " + jsonNode.textValue());
		    		this.hmProperties.put(key, jsonNode.textValue());
    			} else if (jsonNode.isValueNode()) {
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting ValueNode for Key: " + key +  " = " + jsonNode.toString());
    				this.hmProperties.put(key, jsonNode.toString());
    			} else if (jsonNode.isMissingNode()) {
    					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"OpenCM JsonParser:: Gracefully ignoring missing key: " + key);
    			} else {
					LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Setting Other for Key: " + key +  " = " + jsonNode.toString());
    				this.hmProperties.put(key, jsonNode.toString());
    			}
			}
		}
    }
    
    
	 /*
	  * Helper to support lists and wild cards in dynamic key names in the property files (property definitions).
	  * 
	  * E.g. /[packageName=MyPackage*,version]
	  */
	 
	private static boolean matches(String name, String key) {
		String stElement = Configuration.convertPropertyKey(key);
		if (name.matches(stElement.toString())) {
			return true;
		}
			
		return false;
	}

    
}