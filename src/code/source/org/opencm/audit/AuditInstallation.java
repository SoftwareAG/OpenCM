package org.opencm.audit;

import org.opencm.inventory.*;

public class AuditInstallation {

	private Organisation organisation;
	private Department department;
	private Server server;
	private Installation installation;
	
	/*
	 * Represents the Node installation as defined in the inventory
	 * 
	 */
    public AuditInstallation(Inventory inv, String nodeName) {
    	setOrganisation(inv.getNodeOrganisation(nodeName));
    	setDepartment(inv.getNodeDepartment(nodeName));
    	setServer(inv.getNodeServer(nodeName));
    	setInstallation(inv.getInstallation(nodeName));
    }

    public AuditInstallation(Inventory inv, Installation inst) {
    	setOrganisation(inv.getNodeOrganisation(inst.getName()));
    	setDepartment(inv.getNodeDepartment(inst.getName()));
    	setServer(inv.getNodeServer(inst.getName()));
    	setInstallation(inst);
    }
    
    public Organisation getOrganisation() {
        return this.organisation;
    }
    public void setOrganisation(Organisation org) {
        this.organisation = org;
    }
	
    public Department getDepartment() {
        return this.department;
	}
    public void setDepartment(Department dept) {
        this.department = dept;
    }

    public Server getServer() {
        return this.server;
	}
    public void setServer(Server srv) {
        this.server = srv;
    }

    public Installation getInstallation() {
        return this.installation;
	}
    public void setInstallation(Installation inst) {
        this.installation = inst;
    }

    
    
}
