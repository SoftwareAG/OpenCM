package org.opencm.configuration;

import org.opencm.security.KeyUtils;

public class Instance {
	
	public static final String		ENCRYPT_PREFIX		= "ENCRYPTED::";
	
    private String name;
    private String protocol;
    private String port;
    private String username;
    private String password;
    
    public Instance() {
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
    public String getDecryptedPassword(String masterPwd) {
		KeyUtils keyUtils = new KeyUtils(masterPwd);
		String passwd = getPassword();
		passwd = passwd.substring(12);
		passwd = passwd.substring(0, passwd.lastIndexOf("]"));
		return keyUtils.decrypt(passwd);
    }
    
    public void setPassword(String pass) {
        this.password = pass;
    }
    public boolean passwordEncrypted() {
    	if (getPassword().startsWith("[" + ENCRYPT_PREFIX)) {
    		return true;
    	}
    	return false;
    }
    
    public void decryptPassword(String masterPwd) {
		KeyUtils keyUtils = new KeyUtils(masterPwd);
		String passwd = getPassword();
		passwd = passwd.substring(12);
		passwd = passwd.substring(0, passwd.lastIndexOf("]"));
		setPassword(keyUtils.decrypt(passwd));
    }
    
    public void encryptPassword(String masterPwd) {
		KeyUtils keyUtils = new KeyUtils(masterPwd);
		setPassword("[" + ENCRYPT_PREFIX + keyUtils.encrypt(getPassword()) + "]");
    }
}
