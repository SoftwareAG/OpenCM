package org.opencm.governance;

public class GovernanceProperty {
    private String component;
    private String instance;
    private String key;
    private String value;
    
    public GovernanceProperty() {
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
    
    public String getValue() {
        return this.value;
    }
    public void setValue(String val) {
        this.value = val;
    }
}
