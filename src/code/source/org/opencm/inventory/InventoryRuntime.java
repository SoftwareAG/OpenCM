package org.opencm.inventory;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class InventoryRuntime implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String	RUNTIME_NAME_SMTP			= "OPENCM-SMTP";
	public static final String	RUNTIME_NAME_CCE			= "OSGI-CCE";
	public static final String	RUNTIME_NAME_SPM			= "OSGI-SPM";
	public static final String	RUNTIME_NAME_SYNCH			= "OPENCM-SYNCH";
	public static final String	RUNTIME_NAME_IS_PREFIX		= "integrationServer-";
	public static final String	RUNTIME_NAME_UM_PREFIX		= "Universal-Messaging-";
	
	public static final String	RUNTIME_PROPERTY_ALT_HOST	= "Port.AltHost";
    
    private String name;
    private String protocol;
    private String port;
    private String alt_hostname;
    private String username;
    private String password;
    private String passwordHandle;

    public InventoryRuntime() {
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getProtocol() {
        return this.protocol;
    }
    public void setProtocol(String prot) {
        this.protocol = prot;
    }
    public String getPort() {
        return this.port;
    }
    public void setPort(String port) {
        this.port = port;
    }
    
    @JsonGetter("alt_hostname")
    public String getAltHostname() {
        return this.alt_hostname;
    }
    public void setAltHostname(String hostname) {
        this.alt_hostname = hostname;
    }
    
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String name) {
        this.username = name;
    }
    
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String pass) {
        this.password = pass;
    }
    
    @JsonGetter("password_handle")
    public String getPasswordHandle() {
        return this.passwordHandle;
    }
    
    @JsonSetter("password_handle")
    public void setPasswordHandle(String passHandle) {
        this.passwordHandle = passHandle;
    }
   
}
