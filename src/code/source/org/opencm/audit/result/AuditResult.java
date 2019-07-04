package org.opencm.audit.result;

import java.util.HashMap;
import java.util.Iterator;
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
    private HashMap<String,LinkedList<Location>> hmDistinctLocations;  

    public AuditResult(String auditType) {
    	if (auditType.equals(AuditConfiguration.AUDIT_TYPE_BASELINE)) {
        	this.locationItems = new LinkedList<LocationItem>();
    	} else {
        	this.propertyItems = new LinkedList<PropertyItem>();
    	}
    	this.hmDistinctLocations = new HashMap<String,LinkedList<Location>>();
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
    	for (int i = 0; i < properties.size(); i++) {
    		addDistinctLocations(properties.get(i));
    	}
    }

    public void addPropertyItem(PropertyItem propItem) {
        this.propertyItems.add(propItem);
		addDistinctLocations(propItem);
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
        			addDistinctLocation(pl.getLocation());
        			return;
    	    	}
    		}
    	}
    }
    
    public int getNumDistinctLocations(String component) {
        return getDistinctLocations(component).size();
    }
    
    /**
     * Back-filling means populating location values for individual properties where they are missing
     * I.e. there is an expectation that they should be present but aren't.
     * For example, an installation may not have a fix installed, but other have.
     */
    public void backfillLocations() {
       	
		Iterator<PropertyItem> it = getPropertyItems().iterator();
		while (it.hasNext()) {
			PropertyItem pi = it.next();
	    	LinkedList<PropertyLocation> pls = pi.getPropertyLocations();
	    	LinkedList<Location> distinctLocsList = getDistinctLocations(pls.getFirst().getLocation().getComponent());
	    	
        	for (int i = 0; i < distinctLocsList.size(); i++) {
	    		Location distLocation = distinctLocsList.get(i);
	    		boolean distLocationIncluded = false;
		    	for (int p = 0; p < pls.size(); p++) {
		    		Location l = pls.get(p).getLocation();
	        		if (sameLocation(distLocation,l)) {
	                	distLocationIncluded = true;
	        			break;
	        		}
	        	}
		    	if (!distLocationIncluded) {
		    		distLocation.setInstance(pls.getFirst().getLocation().getInstance());
	            	pi.addPropertyLocation(new PropertyLocation(distLocation, AUDIT_VALUE_MISSING));
		    	}
	    	}
	    	
		}
    	
    }

    private LinkedList<Location> getDistinctLocations(String component) {
        return this.hmDistinctLocations.get(component);
    }
    
    private void addDistinctLocations(PropertyItem pi) {
    	for (int i = 0; i < pi.getPropertyLocations().size(); i++) {
    		addDistinctLocation(pi.getPropertyLocations().get(i).getLocation());
    	}
    }
    
    private void addDistinctLocation(Location loc) {
    	LinkedList<Location> locs = getDistinctLocations(loc.getComponent());
    	
    	if (locs == null) {
    		locs = new LinkedList<Location>();
    		locs.add(loc);
    		this.hmDistinctLocations.put(loc.getComponent(), locs);
    		return;
    	}
    	for (int i = 0; i < locs.size(); i++) {
    		Location l = locs.get(i);
    		if (sameLocation(loc,l)) {
    			return;
    		}
    	}
    	locs.add(loc);
    }
    
    
    private boolean sameLocation(Location loc1, Location loc2) {
    	if (!loc1.getOrganisation().equals(loc2.getOrganisation())) {
    		return false;
    	}
    	if (!loc1.getDepartment().equals(loc2.getDepartment())) {
    		return false;
    	}
    	if (!loc1.getEnvironment().equals(loc2.getEnvironment())) {
    		return false;
    	}
    	if (!loc1.getInstallation().equals(loc2.getInstallation())) {
    		return false;
    	}
    	return true;
    }
    
}
