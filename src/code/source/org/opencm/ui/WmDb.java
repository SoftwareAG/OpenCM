package org.opencm.ui;

public class WmDb {
	
    private String instance;
    private String id;
    private String url = "";	
    private String username = "";
     
    public WmDb (String instance, String id, String url, String username) { 
    	setInstanceName(instance);
    	setId(id);
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
