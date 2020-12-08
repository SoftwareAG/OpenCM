package org.opencm.repository;

import java.io.Serializable;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Instance implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String 	INSTANCE_NAME_INSTALL_DIR		= "SAG-INSTALL-DIR";
    public static final String 	INSTANCE_PREFIX_PORTS			= "COMMON-PORTS-";
    public static final String 	INSTANCE_PREFIX_WMDB			= "COMMON-JDBC-";
    public static final String 	INSTANCE_PREFIX_MSG_WM			= "COMMON-WMMESSAGING-";
    public static final String 	INSTANCE_PREFIX_MSG_JNDI		= "COMMON-JNDI-";
    
    private String name;
    private LinkedList<Property> properties;

    public Instance() {
    }
    
    public Instance(String name) {
    	setName(name);
    	setProperties(new LinkedList<Property>());
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Property> getProperties() {
        return this.properties;
    }
    public void setProperties(LinkedList<Property> props) {
        this.properties = props;
    }
    
    public void addProperty(Property prop) {
        this.properties.add(prop);
    }
    
    public void addProperties(LinkedList<Property> props) {
        this.properties.addAll(props);
    }

    @JsonIgnore
	public Property getProperty(String key) {
		for (Property prop: getProperties()) {
			if (prop.getKey().equals(key)) {
				return prop;
			}
		}
		return null;
	}
	
	public boolean hasPropertyKey(String key) {
		for (Property prop: getProperties()) {
			if (prop.getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasPropertyKeyValue(String key, String value) {
		for (Property prop: getProperties()) {
			if (prop.getKey().equals(key) && prop.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

}
