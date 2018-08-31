package org.opencm.audit.configuration;

public class ComponentFilter {
    private String nodeAlias;
    private String component;
    public ComponentFilter() {
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
}
