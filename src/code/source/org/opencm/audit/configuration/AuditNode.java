package org.opencm.audit.configuration;

public class AuditNode {
    private String repoType;
    private String environment;
    private String nodeAlias;
    public AuditNode() {
    }
    public String getRepoType() {
        return this.repoType;
    }
    public void setRepoType(String type) {
        this.repoType = type;
    }
    public String getEnvironment() {
        return this.environment;
    }
    public void setEnvironment(String env) {
        this.environment = env;
    }
    public String getNodeAlias() {
        return this.nodeAlias;
    }
    public void setNodeAlias(String alias) {
        this.nodeAlias = alias;
    }
}
