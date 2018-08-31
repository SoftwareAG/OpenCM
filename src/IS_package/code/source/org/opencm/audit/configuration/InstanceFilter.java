package org.opencm.audit.configuration;

public class InstanceFilter {
    private String nodeAlias;
    private String component;
    private String instance;
    public InstanceFilter() {
    }
    public String getNodeAlias() {
        return this.nodeAlias;
    }
    public void setNodeAlias(String alias) {
        this.nodeAlias = alias;
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
}
