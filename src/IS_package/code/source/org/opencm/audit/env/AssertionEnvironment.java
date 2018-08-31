package org.opencm.audit.env;

import java.util.LinkedList;

/**
 * Initialized for all environments/properties, regardless whether there is a node or not.
 * An absent node means that there is no extraction information to use
 * A NULL value in the value field means that the node exists but there is no property available (.e.g fix, package, settings, etc.)
 * 
 * An AssertionEnvironment object holds all values for a particular property and an environment.
 * A single node can possess multiple values for a single property (e.g. two IS instances)
 * 
 */
public class AssertionEnvironment {

	private String envName;
	private LinkedList<AssertionValue> values;
	
	public AssertionEnvironment(String envName) {
		setEnvironment(envName);
		this.values = new LinkedList<AssertionValue>(); 
	}
	
    public String getEnvironment() {
        return this.envName;
    }
    public void setEnvironment(String env) {
        this.envName = env;
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
