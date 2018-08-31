package org.opencm.audit.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import org.opencm.configuration.Node;
import org.opencm.audit.util.RepoParser;

public class AssertionProperty {

	private String propertyName;
	private String defaultPropertyValue;
	private HashMap<String,AssertionEnvironment> envs;		// Key = Environment Name
	
	public AssertionProperty(String propertyName) {
		setPropertyname(propertyName);
		this.envs = new HashMap<String,AssertionEnvironment>();
	}
	
    public String getPropertyName() {
        return this.propertyName;
    }
    public void setPropertyname(String prop) {
        this.propertyName = prop;
    }
    
    public String getDefaultValue() {
        return this.defaultPropertyValue;
    }
    public void setDefaultValue(String val) {
        this.defaultPropertyValue = val;
    }

    public HashMap<String,AssertionEnvironment> getAssertionEnvironments() {
        return this.envs;
    }
    public void setAssertionEnvironments(HashMap<String,AssertionEnvironment> envs) {
        this.envs = envs;
    }
    
    public AssertionEnvironment getAssertionEnvironment(String envName) {
        return this.envs.get(envName);
    }
    
    public void addAssertionEnvironment(AssertionEnvironment ae) {
		if (!this.envs.containsKey(ae.getEnvironment())) {
			this.envs.put(ae.getEnvironment(), ae);
		}
    }
    
    public void addAssertionValue(Node opencmNode, AssertionValue av) {
		if (!this.envs.containsKey(opencmNode.getOpencm_environment())) {
			AssertionEnvironment ae = new AssertionEnvironment(opencmNode.getOpencm_environment());
			ae.addAssertionValue(av);
			this.envs.put(ae.getEnvironment(), ae);
		} else {
			this.envs.get(opencmNode.getOpencm_environment()).addAssertionValue(av);
		}
    }
    
    public boolean isEqual() {
    	LinkedList<String> propValues = new LinkedList<String>();
		Iterator<String> itEnv = this.envs.keySet().iterator();
    	while (itEnv.hasNext()) {
    		AssertionEnvironment ae = this.envs.get(itEnv.next());
    		Iterator<AssertionValue> itVal = ae.getValues().iterator();
        	while (itVal.hasNext()) {
        		AssertionValue av = itVal.next();
        		if (av.isFiltered() || ((av.getMissingInfo() != null) && (av.getMissingInfo().equals(RepoParser.ASSERTION_UNDEFINED_NODE)))) {
        			// ignore...
        			continue;
        		}
           		propValues.add(av.getValue());
        	}
    	}
    	
    	return propValues.stream().distinct().limit(2).count() <= 1;
    }
    
}
