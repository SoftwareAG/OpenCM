package org.opencm.audit.result;

import java.util.LinkedList;

import org.opencm.audit.configuration.AuditConfiguration;

public class AuditResult {
	
	public static String AUDIT_VALUE_MISSING	= "_ABSENT_";
	
	private LinkedList<LocationItem> locationItems;
    private LinkedList<PropertyItem> propertyItems;

    public AuditResult(String auditType) {
    	if (auditType.equals(AuditConfiguration.AUDIT_TYPE_BASELINE)) {
        	this.locationItems = new LinkedList<LocationItem>();
    	} else {
        	this.propertyItems = new LinkedList<PropertyItem>();
    	}
    }
	
    public LinkedList<LocationItem> getLocationItems() {
        return this.locationItems;
    }
    public void setLocations(LinkedList<LocationItem> locItems) {
        this.locationItems = locItems;
    }

    public void addLocation(LocationItem locItem) {
        this.locationItems.add(locItem);
    }
    
    public LinkedList<PropertyItem> getPropertyItems() {
        return this.propertyItems;
    }
    public void setPropertyItems(LinkedList<PropertyItem> properties) {
        this.propertyItems = properties;
    }

    public void addPropertyItem(PropertyItem propItem) {
        this.propertyItems.add(propItem);
    }
}
