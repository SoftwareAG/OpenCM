package org.opencm.inventory;

import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class Installation {

    @JsonPropertyOrder({ "name", "environment", "layer", "sublayer", "version", "runtimeComponents" })
    
	public static String NODE_VERSION_900 			= "V900";
	public static String NODE_VERSION_960 			= "V960";
	public static String NODE_VERSION_980 			= "V980";
	public static String NODE_VERSION_990 			= "V990";
	public static String NODE_VERSION_912 			= "V912";
	public static String NODE_VERSION_100 			= "V912";
	public static String NODE_VERSION_101 			= "V101";
	public static String NODE_VERSION_102 			= "V102";
	public static String NODE_VERSION_103 			= "V103";
	public static String NODE_VERSION_104 			= "V104";
	public static String NODE_VERSION_105 			= "V105";
	public static String NODE_VERSION_106 			= "V106";
	
	public static final String		INSTALLATION_METADATA_NAME			= "META_DATA";
	public static final String 		KEEPASS_PROPERTY_ENVIRONMENT 		= "env";
	public static final String 		KEEPASS_PROPERTY_LAYER 				= "layer";
	public static final String 		KEEPASS_PROPERTY_SUBLAYER 			= "sublayer";
	public static final String 		KEEPASS_PROPERTY_VERSION 			= "version";

    private String name;
    private String environment;
    private String layer;
    private String sublayer;
    private String version;
    private LinkedList<RuntimeComponent> runtimeComponents;

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironment() {
        return this.environment;
    }
    public void setEnvironment(String name) {
        this.environment = name;
    }
    public String getLayer() {
        return this.layer;
    }
    public void setLayer(String name) {
        this.layer = name;
    }
    public String getSublayer() {
        return this.sublayer;
    }
    public void setSublayer(String name) {
        this.sublayer = name;
    }
    public String getVersion() {
        return this.version;
    }
    public void setVersion(String name) {
        this.version = name;
    }
    
    public LinkedList<RuntimeComponent> getRuntimes() {
        return this.runtimeComponents;
    }
    public void setRuntimes(LinkedList<RuntimeComponent> installs) {
        this.runtimeComponents = installs;
    }
    
    @JsonIgnore
    public RuntimeComponent getRuntimeComponent(String rcName) {
    	if (getRuntimes() == null) {
    		return null;
    	}
    	for (int r = 0; r < getRuntimes().size(); r++) {
    		RuntimeComponent rc = getRuntimes().get(r);
    		if (rc.getName().equals(rcName)) {
    			return rc;
    		}
    	}
        return null;
    }
    
    @JsonIgnore
    public Installation getCopy() {
    	Installation node = new Installation();
		node.setName(getName());
		node.setEnvironment(getEnvironment());
		node.setLayer(getLayer());
		node.setSublayer(getSublayer());
		node.setVersion(getVersion());
		node.setRuntimes(getRuntimes());
		return node;
    }
    
}
