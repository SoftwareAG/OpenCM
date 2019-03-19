package org.opencm.audit.result;

public class LocationProperty {

    private String propertyName;
    private String repo1;
    private String value1;
    private String repo2;
    private String value2;

    public LocationProperty(String propertyName, String repo1, String value1, String repo2, String value2) {
    	setPropertyName(propertyName);
    	setRepo1(repo1);
    	setValue1(value1);
    	setRepo2(repo2);
    	setValue2(value2);
    }
    
    public String getPropertyName() {
        return this.propertyName;
    }
    public void setPropertyName(String propName) {
        this.propertyName = propName;
    }
	
    public String getRepo1() {
        return this.repo1;
    }
    public void setRepo1(String repo) {
        this.repo1 = repo;
    }

    public String getValue1() {
        return this.value1;
    }
    public void setValue1(String val) {
        this.value1 = val;
    }
    
    public String getRepo2() {
        return this.repo2;
    }
    public void setRepo2(String repo) {
        this.repo2 = repo;
    }

    public String getValue2() {
        return this.value2;
    }
    public void setValue2(String val) {
        this.value2 = val;
    }
    
    public boolean isEqual() {
    	if ((getValue1() == null) && (getValue2() == null)) {
    		return true;
    	}
    	if ((getValue1() == null) || (getValue2() == null)) {
    		return false;
    	}
    	if (getValue1().equals(getValue2())) {
    		return true;
    	}
    	return false;
    }
    
    
}
