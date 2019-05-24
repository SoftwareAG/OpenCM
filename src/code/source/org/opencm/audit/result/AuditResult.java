package org.opencm.audit.result;

import java.util.LinkedList;

import org.opencm.audit.AuditValue;
import org.opencm.audit.configuration.AuditConfiguration;
import org.opencm.audit.util.Normalizer;

public class AuditResult {
	
	public static String AUDIT_VALUE_MISSING	= "_ABSENT_";
	
	private int numPropertiesAudited = 0;
	private int numPropertiesDifferent = 0;
	private LinkedList<LocationItem> locationItems;
    private LinkedList<PropertyItem> propertyItems;

    public AuditResult(String auditType) {
    	if (auditType.equals(AuditConfiguration.AUDIT_TYPE_BASELINE)) {
        	this.locationItems = new LinkedList<LocationItem>();
    	} else {
        	this.propertyItems = new LinkedList<PropertyItem>();
    	}
    }
	
    public int getNumPropertiesAudited() {
        return this.numPropertiesAudited;
    }
    public void addNumPropertiesAudited() {
        this.numPropertiesAudited += 1;
    }
    public void setNumPropertiesAudited(int count) {
        this.numPropertiesAudited = count;
    }
    
    public int getNumPropertiesDifferent() {
        return this.numPropertiesDifferent;
    }
    public void addNumPropertiesDifferent() {
        this.numPropertiesDifferent += 1;
    }
    public void setNumPropertiesDifferent(int count) {
        this.numPropertiesDifferent = count;
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
    
    public boolean containsPropertyItem(AuditValue av) {
    	for (int i = 0; i < getPropertyItems().size(); i++) {
    		PropertyItem pi = getPropertyItems().get(i);
    		if (pi.getPropertyName().equals(av.getProperty())) {
    			LinkedList<PropertyLocation> pls = pi.getPropertyLocations();
    	    	for (int p = 0; p < pls.size(); p++) {
    	    		PropertyLocation pl = pls.get(p);
	        		if (Normalizer.normalizeComponentName(pl.getLocation().getComponent()).equals(Normalizer.normalizeComponentName(av.getComponent())) && pl.getLocation().getInstance().equals(av.getInstance())) {
	        			return true;
	        		}
    	    	}
    		}
    	}
        return false;
    }
    
    public void addPropertyItem(AuditValue av, PropertyLocation pl) {
    	for (int i = 0; i < getPropertyItems().size(); i++) {
    		PropertyItem pi = getPropertyItems().get(i);
    		if (pi.getPropertyName().equals(av.getProperty())) {
    			PropertyLocation firstPiPl = pi.getPropertyLocations().getFirst();
        		if (Normalizer.normalizeComponentName(firstPiPl.getLocation().getComponent()).equals(Normalizer.normalizeComponentName(av.getComponent())) && firstPiPl.getLocation().getInstance().equals(av.getInstance())) {
        			pi.addPropertyLocation(pl);
        			return;
    	    	}
    		}
    	}
    }
    
}
