package org.opencm.audit;

import java.util.ArrayList;
import java.util.LinkedList;

import org.opencm.repository.Component;
import org.opencm.repository.Repository;

public class AuditResult {
	
	public static String AUDIT_VALUE_MISSING	= "_ABSENT_";
	public static String REPOSITORY_MISSING		= "_NO-REPO_";
	
	private String name;
	private int numPropertiesAudited = 0;
	private int numPropertiesDifferent = 0;
    private LinkedList<AuditProperty> auditProperties = new LinkedList<AuditProperty>();
    
    public AuditResult() {
    }
	
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
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

    public LinkedList<AuditProperty> getAuditProperties() {
        return this.auditProperties;
    }
    public void setAuditProperties(LinkedList<AuditProperty> properties) {
        this.auditProperties = properties;
    }

    /*
     *  Adding a single audit property:
     *  - Contains a single property, with a single AuditValue
     */
    public void addAuditProperty(AuditProperty auditProp) {
    	for (AuditProperty ap : getAuditProperties()) {
        	if (auditProp.getPropertyName().equals(ap.getPropertyName())) {
        		// Same property exists already 
        		// -> Need to check if component and instance are equals (with the same property key name
        		//    If so, add to existing Audit Property
        		// -> In case we are looking at the same component but with different runtime instances, they should also be treated as the same property
        		// -> In all cases, the instance name must be the same
            	for (AuditValue av : ap.getAuditValues()) {
                	if (auditProp.getAuditValues().getFirst().getInstance().equals(av.getInstance())) {
                		String newComponent = auditProp.getAuditValues().getFirst().getComponent();
                    	if (newComponent.equals(av.getComponent()) || 
                       		(newComponent.startsWith(Component.COMPONENT_PREFIX_OSGI_IS) && av.getComponent().startsWith(Component.COMPONENT_PREFIX_OSGI_IS)) ||
                    		(newComponent.startsWith(Component.COMPONENT_PREFIX_INTEGRATION_SERVER) && av.getComponent().startsWith(Component.COMPONENT_PREFIX_INTEGRATION_SERVER)) ||
                    		(newComponent.startsWith(Component.COMPONENT_PREFIX_UNIVERSAL_MESSAGING) && av.getComponent().startsWith(Component.COMPONENT_PREFIX_UNIVERSAL_MESSAGING)) ||
                       		(newComponent.startsWith(Component.COMPONENT_PREFIX_OSGI_MWS) && av.getComponent().startsWith(Component.COMPONENT_PREFIX_OSGI_MWS)) ||
                    		(newComponent.startsWith(Component.COMPONENT_PREFIX_MY_WEBMETHODS_SERVER) && av.getComponent().startsWith(Component.COMPONENT_PREFIX_MY_WEBMETHODS_SERVER))) 
                    	{
	                		ap.addAuditValue(auditProp.getAuditValues().getFirst());
	                		return;
                    	}
                	}
            	}
        	}
    	}
        this.auditProperties.add(auditProp);
    }
    
    /**
     * Back-filling means populating location values for individual properties where they are missing
     * I.e. there is an expectation that they should be present but aren't.
     * For example, an installation may not have a fix installed, but others have.
     * 
     */
    public void backfillLocations(LinkedList<ArrayList<String>> paths) {
       	for (AuditProperty ap : getAuditProperties()) {
       		LinkedList<ArrayList<String>> auditPaths = new LinkedList<ArrayList<String>>();
           	for (AuditValue av : ap.getAuditValues()) {
           		auditPaths.add(av.getPath());
           	}
           	LinkedList<ArrayList<String>> missingPaths = getAbsentPaths(paths,auditPaths);
       		if (missingPaths.size() > 0) {
       			// Add new missing audit values
       			for (ArrayList<String> newPath : missingPaths) {
       				String missingIdentifier = AUDIT_VALUE_MISSING;
       				if (!Repository.repoExists(newPath)) {
       					missingIdentifier = REPOSITORY_MISSING;
       				}
           			AuditValue newAv = new AuditValue(newPath,ap.getAuditValues().getFirst().getComponent(),ap.getAuditValues().getFirst().getInstance(), missingIdentifier); 
                	ap.addAuditValue(newAv);
       			}
       		}
       	}
    }

    private LinkedList<ArrayList<String>> getAbsentPaths(LinkedList<ArrayList<String>> allPaths, LinkedList<ArrayList<String>> foundPaths) {
    	LinkedList<ArrayList<String>> delta = new LinkedList<ArrayList<String>>();
    	for (ArrayList<String> path : allPaths) {
    		if (!isPathIncluded(path,foundPaths)) {
    			delta.add(path);
    		}
    	}
    	
    	return delta;
    }
    private boolean isPathIncluded(ArrayList<String> path, LinkedList<ArrayList<String>> pathList) {
    	for (ArrayList<String> p : pathList) {
    		if (path.equals(p)) {
    			return true;
    		}
    	}
    	return false;
    }
    
}
