package org.opencm.audit.result;

import java.util.LinkedList;

public class PropertyItem {

    private String propertyName;
    private LinkedList<PropertyLocation> propertyLocations;

    public PropertyItem(String propertyName) {
    	setPropertyName(propertyName);
    	this.propertyLocations = new LinkedList<PropertyLocation>();
    }
	
    public String getPropertyName() {
        return this.propertyName;
    }
    public void setPropertyName(String propName) {
        this.propertyName = propName;
    }
    
    public LinkedList<PropertyLocation> getPropertyLocations() {
        return this.propertyLocations;
    }
    public void setPropertyLocations(LinkedList<PropertyLocation> proplocs) {
        this.propertyLocations = proplocs;
    }

    public void addPropertyLocation(PropertyLocation proploc) {
        this.propertyLocations.add(proploc);
    }
    
    public boolean isEqual() {
    	String propValue = null;
    	for (int i = 0; i < getPropertyLocations().size(); i++) {
    		PropertyLocation pl = getPropertyLocations().get(i);
    		if (propValue == null) {
    			propValue = pl.getValue();
    		} else if (!pl.getValue().equals(propValue)) {
    			return false;
    		}
    	}
    	return true;
    }
}
