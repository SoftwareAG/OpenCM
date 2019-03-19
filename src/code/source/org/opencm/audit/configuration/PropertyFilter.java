package org.opencm.audit.configuration;

public class PropertyFilter {
    private String component;
    private String instance;
    private String key;
    
    public PropertyFilter() {
    }
    
    public String getComponent() {
        return this.component;
    }
    public void setComponent(String component) {
        this.component = component;
    }
    
    public String getInstance() {
        return this.instance;
    }
    public void setInstance(String instance) {
        this.instance = instance;
    }
    
    public String getKey() {
        return this.key;
    }
    public void setKey(String key) {
        this.key = key;
    }
}
