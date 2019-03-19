package org.opencm.audit.result;

import org.opencm.audit.AuditInstallation;

public class Location {

	private String organisation;
	private String department;
	private String environment;
	private String layer;
	private String sublayer;
	private String server;
	private String installation;
	private String component;
	private String instance;
    
	// For location centric audits
    public Location(AuditInstallation ai) {
    	setOrganisation(ai.getOrganisation().getName());
    	setDepartment(ai.getDepartment().getName());
    	setEnvironment(ai.getInstallation().getEnvironment());
    	setLayer(ai.getInstallation().getLayer());
    	setSubLayer(ai.getInstallation().getSublayer());
    	setServer(ai.getServer().getUnqualifiedHostname());
    	setInstallation(ai.getInstallation().getName());
    }
	
	// For property centric audits
    public Location(AuditInstallation ai, String component, String instance) {
    	setOrganisation(ai.getOrganisation().getName());
    	setDepartment(ai.getDepartment().getName());
    	setEnvironment(ai.getInstallation().getEnvironment());
    	setLayer(ai.getInstallation().getLayer());
    	setSubLayer(ai.getInstallation().getSublayer());
    	setServer(ai.getServer().getUnqualifiedHostname());
    	setInstallation(ai.getInstallation().getName());
    	setComponent(component);
    	setInstance(instance);
    }
	
    public String getOrganisation() {
        return this.organisation;
    }
    public void setOrganisation(String org) {
        this.organisation = org;
    }

    public String getDepartment() {
        return this.department;
    }
    public void setDepartment(String dep) {
        this.department = dep;
    }

    public String getEnvironment() {
        return this.environment;
    }
    public void setEnvironment(String env) {
        this.environment = env;
    }

    public String getLayer() {
        return this.layer;
    }
    public void setLayer(String lay) {
        this.layer = lay;
    }

    public String getSubLayer() {
        return this.sublayer;
    }
    public void setSubLayer(String sublay) {
        this.sublayer = sublay;
    }

    public String getServer() {
        return this.server;
    }
    public void setServer(String srv) {
        this.server = srv;
    }
    
    public String getInstallation() {
        return this.installation;
    }
    public void setInstallation(String inst) {
        this.installation = inst;
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
    public void setInstance(String inst) {
        this.instance = inst;
    }
}
