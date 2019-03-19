package org.opencm.audit.configuration;

public class TreeNode {
    private String org;
    private String dep;
    private String env;
    private String layer;
    private String inst; 
     
    public TreeNode() { 
    }
    
    public String getOrganisation() {
        return this.org;
    }
    public void setOrganisation(String org) {
        this.org = org;
    }
    
    public String getDepartment() {
        return this.dep;
    }
    public void setDepartment(String dep) {
        this.dep = dep;
    }
    
    public String getEnvironment() {
        return this.env;
    }
    public void setEnvironment(String env) {
        this.env = env;
    }

    public String getLayer() {
        return this.layer;
    }
    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getInstallation() {
        return this.inst;
    }
    public void setInstallation(String inst) {
        this.inst = inst;
    }
    
}
