package org.opencm.audit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.opencm.governance.GovernanceProperty;
import org.opencm.governance.GovernanceConfiguration;
import org.opencm.repository.Component;
import org.opencm.repository.Instance;
import org.opencm.repository.Property;
import org.opencm.repository.Repository;
import org.opencm.repository.RepoInstallation;
import org.opencm.util.LogUtils;
import org.opencm.util.StringUtils;

public class AuditUtils {

    public static AuditResult performAudit(AuditConfiguration auditConfig) {
    	
		LogUtils.logInfo("performAudit - Name: " + auditConfig.getName());
		LogUtils.logTrace("performAudit - Paths: " + auditConfig.getInventoryPaths().size());
		LogUtils.logTrace("performAudit - Exclude Filters: " + auditConfig.getExcludeFilters().size());
		
    	// -------------------------------------------------------------------------------
    	// Perform property/value collection from repositories
    	// -------------------------------------------------------------------------------
    	LinkedList<RepoInstallation> repoInstallations = new LinkedList<RepoInstallation>();
    	
    	// Retrieve repository installations based on audit configuration
    	for (ArrayList<String> path : auditConfig.getInventoryPaths()) {
    		RepoInstallation installation = Repository.getRepoInstallation(path);
    		if (installation != null) {
        		repoInstallations.add(installation);
    		}
    	}
    	
		LogUtils.logDebug("performAudit - Installations: " + repoInstallations.size());
		
    	// -------------------------------------------------------------------------------
    	// Restructure the collected node data into result objects
    	// -------------------------------------------------------------------------------
    	AuditResult auditResult = new AuditResult();
    	auditResult.setName(auditConfig.getName());

    	// -------------------------------------------------------------------------------
    	// Collecting property values from repo installations and populate AuditProperties
    	// -------------------------------------------------------------------------------
    	for (RepoInstallation installation : repoInstallations) {
    		LogUtils.logTrace("performAudit - Installation: " + installation.getName());
        	for (Component component : installation.getComponents()) {
				if (matches(component.getName(), auditConfig.getComponent())) {
	        		LogUtils.logTrace("performAudit - Component: " + component.getName());
                	for (Instance instance : component.getInstances()) {
        				if (matches(instance.getName(), auditConfig.getInstance())) {
        	        		LogUtils.logTrace("performAudit - Instance: " + instance.getName());
                        	for (Property property : instance.getProperties()) {
                				if (matches(property.getKey(), auditConfig.getKey())) {
                	        		LogUtils.logTrace("performAudit - Property Key: " + property.getKey());
                	        		LogUtils.logTrace("performAudit - Property Value: " + property.getValue());
                        			// Verify filters
                					if (!filtered(component.getName(), instance.getName(), property.getKey(), auditConfig.getExcludeFilters())) {
                    					// Populate AuditResult
                    					AuditValue av = new AuditValue(installation.getPath(),component.getName(),instance.getName(),property.getValue());
                    					AuditProperty ap = new AuditProperty(property.getKey(),av);
                						auditResult.addAuditProperty(ap);
                					}
                        		}
                        	}
                		}
                	}
        		}
        	}
    	}
    	
		LogUtils.logDebug("performAudit - Properties in Result: " + auditResult.getAuditProperties().size());
		
    	//	Add stats and prune result if they are equal (and when they should be ignored)
		Iterator<AuditProperty> it = auditResult.getAuditProperties().iterator();
		while (it.hasNext()) {
			auditResult.addNumPropertiesAudited();
			AuditProperty ap = it.next();
			if (ap.isEqual() && !auditConfig.getOptionEquals().equals("true")) {
				it.remove();
			} else {
				if (!ap.isEqual()) {
					auditResult.addNumPropertiesDifferent();
				}
			}
		}
		
		LogUtils.logDebug(" Audit - Backfilling missing values ... ");
		
		// Populate all missing properties with ABSENT value
		auditResult.backfillLocations(auditConfig.getInventoryPaths());

        return auditResult;
    }
	
    public static AuditResult performAudit(LinkedList<GovernanceConfiguration> rulesConfig) {
		LogUtils.logTrace("performAudit - Governance Rules: " + rulesConfig.size());
		
    	// -------------------------------------------------------------------------------
    	// Perform property/value collection from repositories
    	// -------------------------------------------------------------------------------
    	LinkedList<RepoInstallation> repoInstallations = new LinkedList<RepoInstallation>();
    	LinkedList<ArrayList<String>> paths = new LinkedList<ArrayList<String>>();
    	
		for (GovernanceConfiguration govConfig : rulesConfig) {
	    	// Retrieve repository installations based on audit configuration
			paths.addAll(govConfig.getPaths());
	    	for (ArrayList<String> path : govConfig.getPaths()) {
	    		if (Repository.repoExists(path)) {
		    		RepoInstallation installation = Repository.getRepoInstallation(path);
		    		if (installation != null) {
		        		repoInstallations.add(installation);
		    		}
	    		}
	    	}
		}
    	
		if (repoInstallations.size() == 0) {
			LogUtils.logWarning("performAudit - No Repo installations available for audit ");
			return null;
		}
		
		LogUtils.logDebug("performAudit - Installations: " + repoInstallations.size());
		
    	// -------------------------------------------------------------------------------
    	// Restructure the collected node data into result objects
    	// -------------------------------------------------------------------------------
    	AuditResult auditResult = new AuditResult();
    	auditResult.setName("Governance Audit");
		
    	// -------------------------------------------------------------------------------
    	// Collecting property values from repo installations and populate AuditProperties
    	// -------------------------------------------------------------------------------
		for (GovernanceConfiguration govConfig : rulesConfig) {
			for (GovernanceProperty govProperty : govConfig.getProperties()) {
		    	for (RepoInstallation installation : repoInstallations) {
		        	for (Component component : installation.getComponents()) {
						if (matches(component.getName(), govProperty.getComponent())) {
		                	for (Instance instance : component.getInstances()) {
		        				if (matches(instance.getName(), govProperty.getInstance())) {
		                        	for (Property property : instance.getProperties()) {
		                				if (matches(property.getKey(), govProperty.getKey())) {
	                    					// Populate AuditResult
	                    					AuditValue av = new AuditValue(installation.getPath(),component.getName(),instance.getName(),property.getValue(),govProperty.getValue());
	                    					AuditProperty ap = new AuditProperty(property.getKey(),av);
	                						auditResult.addAuditProperty(ap);
		                        		}
		                        	}
		                		}
		                	}
		        		}
		        	}
		    	}
			}
		}
    	
		LogUtils.logDebug("performAudit - Properties in Result: " + auditResult.getAuditProperties().size());
		
    	//	Add stats and prune result if they are equal (and when they should be ignored)
		Iterator<AuditProperty> it = auditResult.getAuditProperties().iterator();
		while (it.hasNext()) {
			auditResult.addNumPropertiesAudited();
			AuditProperty ap = it.next();
			if (!ap.conformsToGovernanceRule()) {
				auditResult.addNumPropertiesDifferent();
			}
		}
		
		LogUtils.logDebug(" Audit - Backfilling missing values ... ");
		
		// Populate all missing properties with ABSENT value
		auditResult.backfillLocations(paths);
		
        return auditResult;
    }
    
	public static boolean filtered(String component, String instance, String key, LinkedList<ExcludeFilter> filters) {
		for (ExcludeFilter filter : filters) {
			if (matches(component, filter.getComponent()) && matches(instance, filter.getInstance()) && matches(key, filter.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean filteredComponent(String name, LinkedList<ExcludeFilter> filters) {
		for (ExcludeFilter filter : filters) {
			if (matches(name, filter.getComponent())) {
				return true;
			}
		}
		return false;
	}
	public static boolean filteredInstance(String name, LinkedList<ExcludeFilter> filters) {
		for (ExcludeFilter filter : filters) {
			if (matches(name, filter.getInstance())) {
				return true;
			}
		}
		return false;
	}
	public static boolean filteredKey(String name, LinkedList<ExcludeFilter> filters) {
		for (ExcludeFilter filter : filters) {
			if (matches(name, filter.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	 /*
	  * Helper to support lists and wild cards in component and instance names in the configuration (property definitions and filters).
	  * Each element can contain the "*" wild card character and can be multiple divide by a comma (e.g. "Comp*Name_01,Component_02*", etc.)
	  * Wild cards are then converted to regexp and matched against the name passed.
	  *  
	  */
	public static boolean matches(String name, String key) {
		LinkedList<String> keyElements = new LinkedList<String>();
		int multipleElementsIdx = key.indexOf(",");
		if (multipleElementsIdx > 0) {
			StringTokenizer st = new StringTokenizer(key,",");
			while (st.hasMoreTokens()) {
				keyElements.add(st.nextToken().trim());
			}
			
		} else {
			keyElements.add(key.trim());
		}
			
		for (int i = 0; i < keyElements.size(); i++) {
			String stElement = StringUtils.getRegexString(keyElements.get(i));
			if (name.matches(stElement.toString())) {
				return true;
			}
			
		}
		return false;
	}

	
}
