package org.opencm.audit;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuditValue {

	private ArrayList<String> path;
	private String component;
	private String instance;
    private String value;
    private String governance_value;

    public AuditValue(ArrayList<String> path, String component, String instance, String value) {
    	setPath(path);
    	setComponent(component);
    	setInstance(instance);
    	setValue(value);
    }
	
    public AuditValue(ArrayList<String> path, String component, String instance, String value, String governanceValue) {
    	setPath(path);
    	setComponent(component);
    	setInstance(instance);
    	setValue(value);
    	setGovernanceValue(governanceValue);
    }
	
    public ArrayList<String> getPath() {
        return this.path;
    }
    public void setPath(ArrayList<String> path) {
        this.path = path;
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

    public String getValue() {
        return this.value;
    }
    public void setValue(String val) {
        this.value = val;
    }

    @JsonGetter("governance_value")
    public String getGovernanceValue() {
        return this.governance_value;
    }
    public void setGovernanceValue(String val) {
        this.governance_value = val;
    }
    
    @JsonIgnore
    public boolean conformsToGovernanceRule() {
		if (!getValue().equals(getGovernanceValue())) {
			return false;
		}
    	return true;
    }
}
