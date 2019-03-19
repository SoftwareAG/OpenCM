package org.opencm.audit.result;

public class PropertyLocation {

    private Location location;
    private String value;

    public PropertyLocation(Location loc, String value) {
    	setLocation(loc);
    	setValue(value);
    }
	
    public Location getLocation() {
        return this.location;
    }
    public void setLocation(Location loc) {
        this.location = loc;
    }

    public String getValue() {
        return this.value;
    }
    public void setValue(String val) {
        this.value = val;
    }

}
