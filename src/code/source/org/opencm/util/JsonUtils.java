package org.opencm.util;

import java.io.File;
import java.util.LinkedList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.XML;

import com.wm.app.b2b.server.ServiceException;

public class JsonUtils {

	private JsonNode node;
	
    public JsonUtils(String json) throws ServiceException {
    	try {
			ObjectMapper mapper = new ObjectMapper();
			this.node = mapper.readTree(json);
			
    	} catch (Exception e) {
    		LogUtils.logError("JsonUtils: " + e.toString());
    		throw new ServiceException(e.toString());
    	}
    }

    public HashMap<String,String> getArray(String path, String fieldName) {
		HashMap<String,String> hmList = new HashMap<String,String>();
		JsonNode jNode = this.node.at(path);
		if (jNode.isMissingNode()) {
			return hmList;
		}
		if (jNode.has(fieldName)) {
			// Single item (no Array)
			hmList.put(jNode.get(fieldName).textValue(),jNode.toString());
		} else {
	    	ArrayNode arrayNode = (ArrayNode) jNode;
			for (int i = 0; i < arrayNode.size(); i++) {
				jNode = arrayNode.get(i);
				hmList.put(jNode.get(fieldName).textValue(),jNode.toString());
			}
		}
		return hmList;
    }

    public LinkedList<String> getEnvironmentNodesArray(String path) {
		LinkedList<String> lList = new LinkedList<String>();
		JsonNode jNode = this.node.at(path);
		if (jNode.isArray()) {
			for (int i = 0; i < jNode.size(); i++) {
				lList.add(jNode.get(i).textValue());
			}
		}
		if (jNode.isTextual()) {
			lList.add(jNode.textValue());
		}

    	return lList;
    }
    
    public static String convertToJson(String inString) {
    	try {
    		if (inString.startsWith("<")) {
        		return XML.toJSONObject(inString).toString();
    		} else {
    			// Escape double-ticks and remove all new line characters...
        		return "{\"plain-text-property\":\"" + inString.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r","") + "\"}";
    		}
    	} catch (Exception e) {
    		LogUtils.logError("JsonUtils - convertToJson: " + e.toString());
    	}
    	return null;
    }

    public static String convertJavaObjectToJson(Object inObject) {
    	try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inObject);

    	} catch (Exception e) {
    		LogUtils.logWarning("JsonUtils - convertToJson: " + e.toString());
    	}
    	return null;
    }
    
    public static String getJsonValue(File jsonFile, String stKey) {
    	try {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			return mapper.readTree(jsonFile).at(stKey).textValue();
    	} catch (Exception e) {
    		LogUtils.logWarning("JsonUtils - getJsonValue: " + e.toString());
    	}
    	return null;
    }
    
    public static String getJsonValue(String jsonString, String stKey) {
    	try {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			return mapper.readTree(jsonString).at(stKey).textValue();
    	} catch (Exception e) {
    		LogUtils.logWarning("JsonUtils - getJsonValue: " + e.toString());
    	}
    	return null;
    }
    
    public static String addNode(String jsonString, String jsonField, String stNode) {
    	try {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			JsonNode jnMain = mapper.readTree(jsonString);
			JsonNode jnAdd = mapper.readTree(stNode);
			return ((ObjectNode) jnMain).set(jsonField,jnAdd).toString();
    	} catch (Exception e) {
    		LogUtils.logWarning("JsonUtils - addElement: " + e.toString());
    	}
    	return null;
    }
    
    public static String createJsonField(String jsonField, String stValue) {
    	try {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			JsonNode jn = mapper.createObjectNode();
			return ((ObjectNode) jn).put(jsonField,stValue).toString();
    	} catch (Exception e) {
    		LogUtils.logWarning("JsonUtils - createJsonField: " + e.toString());
    	}
    	return null;
    }
    
    public static String addField(String jsonString, String jsonField, String stValue) {
    	try {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			JsonNode jn = mapper.readTree(jsonString);
			return ((ObjectNode) jn).put(jsonField,stValue).toString();
    	} catch (Exception e) {
    		LogUtils.logWarning("JsonUtils - addElement: " + e.toString());
    	}
    	return null;
    }
    
    /*
     * Used for getting Components and Instances
     * 
     */
    public static HashMap<String,String> getArray(String json, String path, String fieldName) {
		HashMap<String,String> hmList = new HashMap<String,String>();
		
		JsonNode jNode = getNode(json).at(path);
		if (jNode.isMissingNode()) {
			return hmList;
		}
		if (jNode.has(fieldName)) {
			// Single item (no Array)
			hmList.put(jNode.get(fieldName).textValue(),jNode.toString());
		} else {
	    	ArrayNode arrayNode = (ArrayNode) jNode;
			for (int i = 0; i < arrayNode.size(); i++) {
				jNode = arrayNode.get(i);
				hmList.put(jNode.get(fieldName).textValue(),jNode.toString());
			}
		}
		return hmList;
    }

    private static JsonNode getNode(String json) {
    	try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readTree(json);
			
    	} catch (Exception ex) {
    		LogUtils.logError(" JsonUtils :: getNode - Exception: " + ex.getMessage());
    	}
    	return null;
    }

}
