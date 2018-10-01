package org.opencm.audit.env;

import java.util.LinkedList;

/**
 * 
 */
public class AssertionInstance {

	private String instanceName;
	private LinkedList<AssertionValue> values;
	
	public AssertionInstance(String instanceName) {
		setInstance(instanceName);
		this.values = new LinkedList<AssertionValue>(); 
	}
	
    public String getInstance() {
        return this.instanceName;
    }
    public void setInstance(String inst) {
        this.instanceName = inst;
    }
	
    public LinkedList<AssertionValue> getValues() {
        return this.values;
    }
    public void setValues(LinkedList<AssertionValue> vals) {
        this.values = vals;
    }
    
    public void addAssertionValue(AssertionValue av) {
		this.values.add(av);
    }

    public boolean nodeExists(String nodeName) {
    	for (int i = 0; i < this.values.size(); i++) {
    		if (this.values.get(i).getNode().equals(nodeName)) {
    			return true;
    		}
    	}
		return false;
    }

    public boolean componentExists(String nodeName, String componentName) {
    	for (int i = 0; i < this.values.size(); i++) {
    		if (this.values.get(i).getNode().equals(nodeName) && this.values.get(i).getComponent().equals(componentName)) {
    			return true;
    		}
    	}
		return false;
    }
}
