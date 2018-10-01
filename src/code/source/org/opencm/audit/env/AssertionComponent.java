package org.opencm.audit.env;

import java.util.HashMap;
import java.util.Iterator;

import org.opencm.configuration.Node;

public class AssertionComponent {

	private String assertionComponent;
	private HashMap<String,AssertionProperty> properties;		// Key = Property Name:
	
	public AssertionComponent(String assComp) {
		setAssertionComponent(assComp);
		this.properties = new HashMap<String,AssertionProperty>(); 
	}
	
    public String getAssertionComponent() {
        return this.assertionComponent;
    }
    public void setAssertionComponent(String name) {
        this.assertionComponent = name;
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

    public void addAssertionValue(Node opencmNode, AssertionValue av) {
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
