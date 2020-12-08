package org.opencm.ui;

public class JdbcConnection {
	
    private String instance;
    private String id;
    private String enabled = "";
    private String hostname = "";
    private String port = "";
    private String db = "";
    private String username = "";
     
    public JdbcConnection (String instance, String id, String host) { 
    	setInstanceName(instance);
    	setId(id);
    	setHostname(host);
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
    public String getHostname() {
        return this.hostname;
    }
    public void setHostname(String host) {
        this.hostname = host;
    }
    public String getPort() {
        return this.port;
    }
    public void setPort(String port) {
        this.port = port;
    }
    public String getDatabase() {
        return this.db;
    }
    public void setDatabase(String db) {
        this.db = db;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String user) {
        this.username = user;
    }
    
}
