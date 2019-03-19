package org.opencm.audit;

public class AuditValue {

	private String component;
	private String instance;
	private String property;
	private String value;

    public AuditValue(String comp, String inst, String prop, String val) {
    	setComponent(comp);
    	setInstance(inst);
    	setProperty(prop);
    	setValue(val);
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

    public String getProperty() {
        return this.property;
    }
    public void setProperty(String prop) {
        this.property = prop;
    }
    
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
}
