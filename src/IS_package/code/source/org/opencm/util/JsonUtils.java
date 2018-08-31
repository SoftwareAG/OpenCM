package org.opencm.util;

import java.util.LinkedList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.wm.app.b2b.server.ServiceException;

public class JsonUtils {

	private JsonNode node;
	
    public JsonUtils(String json) throws ServiceException {
    	try {
			ObjectMapper mapper = new ObjectMapper();
			this.node = mapper.readTree(json);
			
    	} catch (Exception e) {
    		throw new ServiceException("OpenCM [CRITICAL] : JsonUtils: " + e.toString());
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
        		return org.json.XML.toJSONObject(inString).toString();
    		} else {
    			// Escape double-ticks and remove all new line characters...
        		return "{\"plain-text-property\":\"" + inString.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r","") + "\"}";
    		}
    	} catch (Exception e) {
    		System.out.println("OpenCM [CRITICAL] : JsonUtils - convertToJson: " + e.toString());
    	}
    	return null;
    }

    public static String convertJavaObjectToJson(Object inObject) {
    	try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inObject);

    	} catch (Exception e) {
    		System.out.println("OpenCM [CRITICAL] : JsonUtils - convertToJson: " + e.toString());
    	}
    	return null;
    }
    
    public static String getJsonValue(String jsonString, String stKey) {
    	try {
			ObjectMapper mapper = new ObjectMapper(new JsonFactory());
			return mapper.readTree(jsonString).at(stKey).textValue();
    	} catch (Exception e) {
    		System.out.println("OpenCM [WARNING] : JsonUtils - getJsonValue: " + e.toString());
    	}
    	return null;
    }
    
    
}
