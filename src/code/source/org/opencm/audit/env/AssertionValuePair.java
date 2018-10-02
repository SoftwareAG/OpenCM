package org.opencm.audit.env;

import org.opencm.repository.util.RepoUtils;


public class AssertionValuePair {

    private AssertionValue av01;
    private AssertionValue av02;
    public AssertionValuePair(AssertionValue av01, AssertionValue av02) {
    	setAssertionValue_01(av01);
    	setAssertionValue_02(av02);
    }

    /**
     * toString => Used by TestNG to name the method test in the report
     * 
     */
    public String toString() {
    	
    	String componentName01 = "";
    	String componentName02 = "";
    	String value01 = "";
    	String value02 = "";
    	
    	if ((this.av01 == null) && (this.av02 == null)) {
            return "<br/>[ERROR IN PROCESSING] <br/>Node 01: NULL AND Node 02: NULL";
    	}
    	if (this.av01 == null) {
    		componentName01 = RepoUtils.ASSERTION_MISSING_COMPONENT;
    		value01 = RepoUtils.ASSERTION_MISSING_DATA;
    	} else {
    		componentName01 = getAssertionValue_01().getComponent();
    		value01 = getAssertionValue_01().getValue();
    	}
    	if (this.av02 == null) {
    		componentName02 = RepoUtils.ASSERTION_MISSING_COMPONENT;
    		value02 = RepoUtils.ASSERTION_MISSING_DATA;
    	} else {
    		componentName02 = getAssertionValue_02().getComponent();
    		value02 = getAssertionValue_02().getValue();
    	}
    	
    	StringBuffer sb = new StringBuffer();
    	if (((this.av01 != null) && !this.av01.componentIsFixed()) || ((this.av02 != null) && !this.av02.componentIsFixed())) {
        	sb.append("<br/><i>" + componentName01 + " <=> " + componentName02 + "</i>");
    	}
    	sb.append("<br/>Value 01: " + value01 + "<br/>Value 02: " + value02);
        return sb.toString();
    }
    
    public AssertionValue getAssertionValue_01() {
        return this.av01;
    }
    public void setAssertionValue_01(AssertionValue av01) {
        this.av01 = av01;
    }
    public AssertionValue getAssertionValue_02() {
        return this.av02;
    }
    public void setAssertionValue_02(AssertionValue av02) {
        this.av02 = av02;
    }
}
