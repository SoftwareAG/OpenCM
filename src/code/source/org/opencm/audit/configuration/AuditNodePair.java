package org.opencm.audit.configuration;

public class AuditNodePair {

    private AuditNode node01;
    private AuditNode node02;
    public AuditNodePair() {
    }

    public AuditNode getNode_01() {
        return this.node01;
    }
    public void setNode_01(AuditNode node01) {
        this.node01 = node01;
    }
    public AuditNode getNode_02() {
        return this.node02;
    }
    public void setNode_02(AuditNode node02) {
        this.node02 = node02;
    }
}
