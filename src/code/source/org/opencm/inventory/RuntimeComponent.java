package org.opencm.inventory;

import org.opencm.security.KeyUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class RuntimeComponent {
	
    @JsonPropertyOrder({ "name", "protocol", "port", "username", "password" })
    
	public static final String		ENCRYPT_PREFIX						= "ENCRYPTED::";
	public static final String		RUNTIME_COMPONENT_NAME_CCE			= "OSGI-CCE";
	public static final String		RUNTIME_COMPONENT_NAME_SPM			= "OSGI-SPM";
	public static final String		RUNTIME_COMPONENT_NAME_SYNCH		= "OPENCM-SYNCH";
	public static final String		RUNTIME_COMPONENT_NAME_IS_PREFIX	= "integrationServer-";
	public static final String		RUNTIME_COMPONENT_NAME_UM_PREFIX	= "Universal-Messaging-";
	
	public static final String 		KEEPASS_PROPERTY_PROTOCOL 			= "protocol";
	public static final String 		KEEPASS_PROPERTY_PORT 				= "port";
	
    private String name;
    private String protocol;
    private String port;
    private String username;
    private String password;
    
    public RuntimeComponent() {
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
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String name) {
        this.username = name;
    }
    
    public String getPassword() {
        return this.password;
    }

    @JsonIgnore
    public String getDecryptedPassword() {
    	if (!passwordEncrypted()) {
    		return getPassword();
    	}
		String passwd = getPassword();
		passwd = passwd.substring(12);
		passwd = passwd.substring(0, passwd.lastIndexOf("]"));
		return KeyUtils.decrypt(passwd);
    }
    
    public void setPassword(String pass) {
        this.password = pass;
    }
    
    @JsonIgnore
    public boolean passwordEncrypted() {
    	if (getPassword().startsWith("[" + ENCRYPT_PREFIX)) {
    		return true;
    	}
    	return false;
    }
    
    @JsonIgnore
    public void decryptPassword() {
    	if (!passwordEncrypted()) {
    		return;
    	}
		String passwd = getPassword();
		passwd = passwd.substring(12);
		passwd = passwd.substring(0, passwd.lastIndexOf("]"));
		setPassword(KeyUtils.decrypt(passwd));
    }
    
    @JsonIgnore
    public void encryptPassword() {
    	if (passwordEncrypted()) {
    		return;
    	}
		setPassword("[" + ENCRYPT_PREFIX + KeyUtils.encrypt(getPassword()) + "]");
    }
}
