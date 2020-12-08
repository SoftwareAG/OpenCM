package org.opencm.ui;

public class Messaging {
	
    private String instance;
    private String id;
    private String type;
    private String url = "";	
    private String username = "";
     
    public Messaging (String instance, String id, String type, String url, String username) { 
    	setInstanceName(instance);
    	setId(id);
    	setType(type);
    	setUrl(url);
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
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String user) {
        this.username = user;
    }
    
}
