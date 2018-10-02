package org.opencm.audit.env;

import java.util.HashMap;
import java.util.Iterator;

import org.opencm.configuration.Configuration;
import org.opencm.configuration.Node;
import org.opencm.repository.util.RepoUtils;

public class AssertionGroup {

	private String assertionGroup;
	private HashMap<String,AssertionProperty> properties;		// Key = Property Name:
	
	public AssertionGroup(String assGroup) {
		setAssertionGroup(assGroup);
		this.properties = new HashMap<String,AssertionProperty>(); 
	}
	
    public String getAssertionGroup() {
        return this.assertionGroup;
    }
    public void setAssertionGroup(String name) {
        this.assertionGroup = name;
    }
	
    public HashMap<String,AssertionProperty> getAssertionProperties() {
        return this.properties;
    }
    public void setAssertionProperties(HashMap<String,AssertionProperty> props) {
        this.properties = props;
    }
    
    public void addAssertionProperty(AssertionProperty ap) {
		if (!this.properties.containsKey(ap.getPropertyName())) {
			this.properties.put(ap.getPropertyName(), ap);
		}
    }

    public void addAssertionValue(Configuration opencmConfig, Node opencmNode, AssertionValue av, boolean includeDefault) {
		if (this.properties.containsKey(av.getPropertyName())) {
			// Verify that there are no existing properties but with different instance names
			AssertionProperty ap = properties.get(av.getPropertyName());
			HashMap<String,AssertionEnvironment> aes = ap.getAssertionEnvironments();
			
			Iterator<String> itEnv = aes.keySet().iterator();
	    	while (itEnv.hasNext()) {
	    		AssertionEnvironment ae = aes.get(itEnv.next());
	    		Iterator<AssertionValue> itVal = ae.getValues().iterator();
	        	while (itVal.hasNext()) {
	        		AssertionValue assValue = itVal.next();
	        		if (assValue.getInstance().equals(av.getInstance())) {
	        			// There are other properties with the same instance name (so just add it)
	        			this.properties.get(av.getPropertyName()).addAssertionValue(opencmNode, av);
	        			return;
	        		}
	        	}
	    	}
		}
	    	
    	// No property exist with the same name and existing instance name
		AssertionProperty ap = new AssertionProperty(av.getPropertyName());
		if (includeDefault) {
			ap.setDefaultValue(RepoUtils.getDefaultValue(opencmConfig, av));
		}
		ap.addAssertionValue(opencmNode, av);
		this.properties.put(av.getPropertyName(), ap);
    }

    public boolean hasAssertionProperties() {
    	if ((this.properties != null) && (this.properties.size() > 0)) {
    		return true;
    	}
    	return false;
    }
}
