package org.opencm.ui;

public class SapConnection {
	
    private String instance;
    private String id;
    private String enabled = "";
    private String applicationServer = "";
    private String messageServer = "";
    private String systemId = "";
    private String client = "";
    private String username = "";
     
    public SapConnection (String instance, String id, String username) { 
    	setInstanceName(instance);
    	setId(id);
    	setUsername(username);
    }
    
    public String getInstanceName() {
        return this.instance;
    }
    public void setInstanceName(String inst) {
        this.instance = inst;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getEnabled() {
        return this.enabled;
    }
    public void setEnabled(String flag) {
        this.enabled = flag;
    }
    public String getApplicationServer() {
        return this.applicationServer;
    }
    public void setApplicationServer(String host) {
        this.applicationServer = host;
    }
    public String getMessageServer() {
        return this.messageServer;
    }
    public void setMessageServer(String host) {
        this.messageServer = host;
    }
    public String getSystemId() {
        return this.systemId;
    }
    public void setSystemId(String id) {
        this.systemId = id;
    }
    public String getClient() {
        return this.client;
    }
    public void setClient(String client) {
        this.client = client;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String user) {
        this.username = user;
    }
   
}
