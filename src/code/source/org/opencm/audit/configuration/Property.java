package org.opencm.audit.configuration;

public class Property {
    private String component;
    private String instance;
    private String key;

    public Property() {
    }
    public String getComponent() {
        return this.component;
    }
    public void setComponent(String comp) {
        this.component = comp;
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
