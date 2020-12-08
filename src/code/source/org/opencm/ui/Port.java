package org.opencm.ui;

public class Port {
	
    private String instance;		// This is for InvGroup instance name; e.g. install_ports + [_um_default]
    private String component;		// e.g. OSGI-SPM
    private String identifier;		// e.g. Diagnostic
    private String number;
    private String protocol;
    private String enabled;
    private String bindAddress;
     
    public Port (String instance, String component, String identifier, String portNum, String protocol, String enabled, String bindAddress) { 
    	setInstanceName(instance);
    	setComponent(component);
    	setIdentifier(identifier);
    	setNumber(portNum);
    	setProtocol(protocol);
    	setEnabled(enabled);
    	setBindAddress(bindAddress);
    }
    
    public String getInstanceName() {
        return this.instance;
    }
    public void setInstanceName(String inst) {
        this.instance = inst;
    }
    public String getComponent() {
        return this.component;
    }
    public void setComponent(String component) {
        this.component = component;
    }
    public String getIdentifier() {
        return this.identifier;
    }
    public void setIdentifier(String ident) {
        this.identifier = ident;
    }
    
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String num) {
        this.number = num;
    }
    
    public String getProtocol() {
        return this.protocol.toLowerCase();
    }
    public void setProtocol(String prot) {
        this.protocol = prot;
    }
    
    public String getEnabled() {
        return this.enabled;
    }
    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }
    
    public String getBindAddress() {
        return this.bindAddress;
    }
    public void setBindAddress(String bAddress) {
        this.bindAddress = bAddress;
    }
}
