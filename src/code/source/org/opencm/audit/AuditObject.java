package org.opencm.audit;

import java.util.LinkedList;

public class AuditObject {

	private String repository;
	private AuditInstallation installation;
	private LinkedList<AuditValue> auditValues;

    public AuditObject(String repo, AuditInstallation inst) {
    	this.repository = repo;
    	this.installation = inst;
    	this.auditValues = new LinkedList<AuditValue>();
    }

    public String getRepository() {
        return this.repository;
    }
    public void setRepository(String repo) {
        this.repository = repo;
    }

    public AuditInstallation getAuditInstallation() {
        return this.installation;
    }
    public void setAuditInstallation(AuditInstallation inst) {
        this.installation = inst;
    }
    
    public LinkedList<AuditValue> getAuditValues() {
        return this.auditValues;
    }
    public void setAuditValues(LinkedList<AuditValue> avs) {
        this.auditValues = avs;
    }
    public void addAuditValue(AuditValue av) {
        this.auditValues.add(av);
    }
    public void addAuditValues(LinkedList<AuditValue> avs) {
        this.auditValues.addAll(avs);
    }

    public AuditValue getAuditValue(String component, String instance, String property) {
    	for (int i = 0; i < getAuditValues().size(); i++) {
    		AuditValue av = getAuditValues().get(i);
    		if (av.getComponent().equals(component) && av.getInstance().equals(instance) && av.getProperty().equals(property)) {
    			return av;
    		}
    	}
        return null;
    }
}
