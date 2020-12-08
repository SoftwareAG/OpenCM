package org.opencm.audit;

import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuditProperty {

    private String propertyName;
    private LinkedList<AuditValue> auditValues = new LinkedList<AuditValue>();

    public AuditProperty(String propertyName, AuditValue av) {
    	setPropertyName(propertyName);
    	addAuditValue(av);
    }
	
    public String getPropertyName() {
        return this.propertyName;
    }
    public void setPropertyName(String propName) {
        this.propertyName = propName;
    }
    
    public LinkedList<AuditValue> getAuditValues() {
        return this.auditValues;
    }
    public void setAuditValues(LinkedList<AuditValue> auditValues) {
        this.auditValues = auditValues;
    }

    public void addAuditValue(AuditValue auditValue) {
        this.auditValues.add(auditValue);
    }
    
    @JsonIgnore
    public boolean isEqual() {
    	String propValue = null;
    	for (AuditValue av : getAuditValues()) {
    		if (propValue == null) {
    			propValue = av.getValue();
    		} else if (!av.getValue().equals(propValue)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    @JsonIgnore
    public boolean conformsToGovernanceRule() {
    	for (AuditValue av : getAuditValues()) {
			if (!av.conformsToGovernanceRule()) {
				return false;
			}
    	}
    	return true;
    }
    
}
