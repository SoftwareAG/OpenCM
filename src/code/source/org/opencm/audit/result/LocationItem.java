package org.opencm.audit.result;

import java.util.LinkedList;

public class LocationItem {

    private Location location;
    private LinkedList<LocationProperty> locationProperties;

    public LocationItem(Location loc) {
    	setLocation(loc);
    	this.locationProperties = new LinkedList<LocationProperty>();
    }
	
    public Location getLocation() {
        return this.location;
    }
    public void setLocation(Location loc) {
        this.location = loc;
    }

    public LinkedList<LocationProperty> getLocationProperties() {
        return this.locationProperties;
    }
    public void setLocationProperties(LinkedList<LocationProperty> locprops) {
        this.locationProperties = locprops;
    }

    public void addLocationProperty(LocationProperty locprop) {
        this.locationProperties.add(locprop);
    }

    public boolean isEqual() {
    	for (int i = 0; i < this.locationProperties.size(); i++) {
    		LocationProperty lp = this.locationProperties.get(i);
    		if (!lp.isEqual()) {
    			return false;
    		}
    	}
    	return true;
    }
    
}
