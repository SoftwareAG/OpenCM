package org.opencm.repository;

import java.io.Serializable;

public class Property implements Serializable {

	private static final long serialVersionUID = 1L;
	
    public static final String 	PROPERTY_NAME_ID					= "id";
	public static final String 	PROPERTY_NAME_DISPLAY_NAME			= "displayName";
    public static final String 	PROPERTY_NAME_PLAIN_TEXT_PROP		= "plain-text-property";
    public static final String 	PROPERTY_NAME_PRODUCT_VERSION		= "products.product.version";
    public static final String 	PROPERTY_NAME_INSTALL_TIME			= "products.product.installTime";
    public static final String 	PROPERTY_NAME_CPU_PHYSICAL			= "physicalCpuCores";
    public static final String 	PROPERTY_NAME_CPU_LOGICAL			= "logicalCpuCores";
    public static final String 	PROPERTY_NAME_PORT_NUMBER			= "Port.Number";
    public static final String 	PROPERTY_NAME_PORT_PROTOCOL			= "Port.Protocol";
    public static final String 	PROPERTY_NAME_PORT_ENABLED			= "Port.Enabled";
    public static final String 	PROPERTY_NAME_PORT_BINDADDRESS		= "Port.BindAddress";
    public static final String 	PROPERTY_NAME_WMDB_URL				= "Pool.DatabaseServer.URL";
    public static final String 	PROPERTY_NAME_WMDB_USERNAME			= "Pool.DatabaseServer.User";
    public static final String 	PROPERTY_NAME_JDBC_ENABLED			= "Enabled";
    public static final String 	PROPERTY_NAME_JDBC_HOSTNAME			= "Hostname";
    public static final String 	PROPERTY_NAME_JDBC_PORT				= "Port";
    public static final String 	PROPERTY_NAME_JDBC_DATABASE			= "Database";
    public static final String 	PROPERTY_NAME_JDBC_USERNAME			= "Username";
    public static final String 	PROPERTY_NAME_SAP_ENABLED			= "Enabled";
    public static final String 	PROPERTY_NAME_SAP_APP_SERVER		= "ApplicationServer";
    public static final String 	PROPERTY_NAME_SAP_MSG_SERVER		= "MessageServer";
    public static final String 	PROPERTY_NAME_SAP_SYSTEM_ID			= "SystemId";
    public static final String 	PROPERTY_NAME_SAP_CLIENT			= "Client";
    public static final String 	PROPERTY_NAME_SAP_USERNAME			= "Username";
    
    public static final String 	PROPERTY_KEY_MSG_WM_URL				= "Messaging.Provider.URL";
    public static final String 	PROPERTY_KEY_MSG_WM_USERNAME		= "Messaging.Provider.Auth.User";
    public static final String 	PROPERTY_KEY_MSG_JNDI_URL			= "JNDI.Provider.URL";
    public static final String 	PROPERTY_KEY_MSG_JNDI_USERNAME		= "JNDI.Provider.User";
    public static final String 	PROPERTY_NAME_MSG_TYPE				= "Type";
    public static final String 	PROPERTY_NAME_MSG_URL				= "Url";
    public static final String 	PROPERTY_NAME_MSG_USERNAME			= "Username";
        
    private String key;
    private String value;

	public Property() {
	}
	
	public Property(String key, String val) {
		if (key.startsWith("/")) {
			key = key.substring(1);
		}
		if (key.indexOf("/") > 0) {
			key = key.replace("/", ".");
		}
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
    	if ((this.value == null) || this.value.equals("null")) {
    		return "";
    	}
        return this.value.replace("\"", "");
    }
    public void setValue(String val) {
        this.value = val;
    }
    
}
