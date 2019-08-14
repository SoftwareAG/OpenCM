package org.opencm.audit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.opencm.configuration.Configuration;
import org.opencm.audit.result.Location;
import org.opencm.audit.result.LocationItem;
import org.opencm.audit.result.LocationProperty;
import org.opencm.audit.result.PropertyItem;
import org.opencm.audit.result.PropertyLocation;
import org.opencm.audit.configuration.AuditConfiguration;
import org.opencm.audit.result.AuditResult;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.Installation;
import org.opencm.util.LogUtils;

public class Collection {

	private static String DEFAULT_INSTALLATION_DIR_PREFIX 			= "REF_V";
	
    public static AuditResult collect(Configuration opencmConfig, Inventory inv, AuditConfiguration ac) {
    	
    	// Loop through each node and collect what is possible, based on audit config property and filters 
    	// Each repository node information is stored in the AuditObject
    	// Layered based:
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	// 	=> Create a list of distinct properties
    	//	=> For each property, retrieve location and values and store into PropertyLocation
    	//
    	// Baseline:
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	// 	=> Create a pair list of distinct nodes
    	//	=> For each node pair, retrieve location and values and store into LocationProperty
    	//
    	// Reference:
    	//    - Ref Node + bunch of props and values
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	//    - Node + bunch of props and values
    	// 	=> Create a pair list of reference node and all other nodes
    	//	=> For each node pair, retrieve location and values and store into PropertyLocation
    	//  
    	// -------------------------------------------------------------------------------
    	// Perform property/value collection from repositories
    	// -------------------------------------------------------------------------------
    	LinkedList<String> defaultVersionsCollected = new LinkedList<String>();
    	LinkedList<AuditObject> aos = new LinkedList<AuditObject>();
    	
    	// For Reference audit, start with the reference node itself
		if (ac.getAudit_type().equals(AuditConfiguration.AUDIT_TYPE_REFERENCE)) {
    		AuditObject aoReference = new AuditObject(Configuration.OPENCM_BASELINE_DIR,new AuditInstallation(inv, ac.getRepo_name()));
    		aoReference = CollectNode.getPropertyValues(opencmConfig, ac, aoReference);
    		if (aoReference != null) {
    			aos.add(aoReference);
    		} else {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Collection :: collect :: Reference node cannot be collected: " + ac.getRepo_name());
				return null;
    		}
		}
		// Scan the nodes
    	for (int i = 0; i < ac.getTree_nodes().size(); i++) {
        	if (ac.getAudit_type().equals(AuditConfiguration.AUDIT_TYPE_LAYERED)) {
        		AuditObject aoLayered = new AuditObject(ac.getRepo_name(),new AuditInstallation(inv, ac.getTree_nodes().get(i).getInstallation()));
        		aoLayered = CollectNode.getPropertyValues(opencmConfig, ac, aoLayered);
        		if (aoLayered != null) {
            		aos.add(aoLayered);
        		}
        	}
    		if (ac.getAudit_type().equals(AuditConfiguration.AUDIT_TYPE_BASELINE)) {
        		AuditObject aoBaseline = new AuditObject(Configuration.OPENCM_BASELINE_DIR,new AuditInstallation(inv, ac.getTree_nodes().get(i).getInstallation()));
        		aoBaseline = CollectNode.getPropertyValues(opencmConfig, ac, aoBaseline);
        		if (aoBaseline != null) {
            		aos.add(aoBaseline);
        		}

        		if (ac.getRepo_name().equals(Configuration.OPENCM_DEFAULT_DIR)) {
        			// Only need to collect data from one default "installation" per version
        			Installation inst = aoBaseline.getAuditInstallation().getInstallation().getCopy();
        			String version = inst.getVersion();
        			if (!defaultVersionsCollected.contains(version)) {
                		inst.setName(DEFAULT_INSTALLATION_DIR_PREFIX + version);
                		AuditObject aoDefault = new AuditObject(Configuration.OPENCM_DEFAULT_DIR, new AuditInstallation(inv, inst));
                		aoDefault = CollectNode.getPropertyValues(opencmConfig, ac, aoDefault);
                		if (aoDefault != null) {
                			aos.add(aoDefault);
                    		defaultVersionsCollected.add(version);
                		}
        			}
        		} else {
        			// Collect from Runtime repo
        			AuditObject aoRuntime = new AuditObject(ac.getRepo_name(),new AuditInstallation(inv, ac.getTree_nodes().get(i).getInstallation()));
        			aoRuntime = CollectNode.getPropertyValues(opencmConfig, ac, aoRuntime);
            		if (aoRuntime != null) {
            			aos.add(aoRuntime);
            		}
        		}
    		}    		
        	if (ac.getAudit_type().equals(AuditConfiguration.AUDIT_TYPE_REFERENCE)) {
        		AuditObject aoReference = new AuditObject(Configuration.OPENCM_RUNTIME_DIR,new AuditInstallation(inv, ac.getTree_nodes().get(i).getInstallation()));
        		aoReference = CollectNode.getPropertyValues(opencmConfig, ac, aoReference);
        		if (aoReference != null) {
            		aos.add(aoReference);
        		}
        	}
    		
    	}
    	
    	// -------------------------------------------------------------------------------
    	// Restructure the collected node data into result objects
    	// -------------------------------------------------------------------------------
    	AuditResult ar = new AuditResult(ac.getAudit_type());
		if (ac.getAudit_type().equals(AuditConfiguration.AUDIT_TYPE_LAYERED)) {
	    	// -------------------------------------------------------------------------------
	    	// Property centric: Layered Audits
	    	// -------------------------------------------------------------------------------
	    	// 	=> Create a list of distinct properties
	    	//	=> For each property, retrieve location and values and store into PropertyLocation
			for (int a = 0; a < aos.size(); a++) {
				AuditObject ao = aos.get(a);
				for (int v = 0; v < ao.getAuditValues().size(); v++) {
					AuditValue av = ao.getAuditValues().get(v);
					if (ar.containsPropertyItem(av)) {
						Location l = new Location(ao.getAuditInstallation(),av.getComponent(),av.getInstance());
						PropertyLocation pl = new PropertyLocation(l,av.getValue());
						ar.addPropertyItem(av,pl);
					} else {
						PropertyItem pi = new PropertyItem(av.getProperty());
						Location l = new Location(ao.getAuditInstallation(),av.getComponent(),av.getInstance());
						PropertyLocation pl = new PropertyLocation(l,av.getValue());
						pi.addPropertyLocation(pl);
						ar.addPropertyItem(pi);
					}
				}
			}
			
	    	//	=> For each property, prune if they are equal
			Iterator<PropertyItem> it = ar.getPropertyItems().iterator();
			while (it.hasNext()) {
				ar.addNumPropertiesAudited();
				PropertyItem pi = it.next();
				if (pi.isEqual() && (pi.getPropertyLocations().size() == ar.getNumDistinctLocations())) {
					// Ability to include all result in audit, equal or different..
					if ((ac.getReport_diffs() == null) || ac.getReport_diffs().equals("true")) {
						it.remove();
					}
				} else {
					ar.addNumPropertiesDifferent();
				}
			}
			
			// => Now populate all properties with a full set of locations
			ar.backfillLocations();
			
		}
		if (ac.getAudit_type().equals(AuditConfiguration.AUDIT_TYPE_BASELINE)) {
	    	// -------------------------------------------------------------------------------
	    	// Location centric: Baseline Audits
	    	// -------------------------------------------------------------------------------
	    	// 	=> Place baseline collections into a baseline hash map and others into a separate hashmap
			HashMap<String,AuditObject> aoBaselineMap = new HashMap<String,AuditObject>();
			HashMap<String,AuditObject> aoRepoMap = new HashMap<String,AuditObject>();
			for (int a = 0; a < aos.size(); a++) {
				AuditObject ao = aos.get(a);
				if (ao.getRepository().equals(Configuration.OPENCM_BASELINE_DIR)) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Collection :: collect :: Adding to Baseline: " + ao.getAuditInstallation().getInstallation().getName());
					aoBaselineMap.put(ao.getAuditInstallation().getInstallation().getName(), ao);
				} else {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Collection :: collect :: Adding to Repo: " + ao.getAuditInstallation().getInstallation().getName());
					aoRepoMap.put(ao.getAuditInstallation().getInstallation().getName(), ao);
				}
			}
			
	    	//	=> For each node pair, retrieve location and values and store into LocationProperty
			Iterator<String> it = aoBaselineMap.keySet().iterator();
			while (it.hasNext()) {
				String aoBaselineNode = it.next();
				AuditObject aoBaseline = aoBaselineMap.get(aoBaselineNode);
				String aoRepoNode = aoBaselineNode;
        		if (ac.getRepo_name().equals(Configuration.OPENCM_DEFAULT_DIR)) {
        			aoRepoNode = DEFAULT_INSTALLATION_DIR_PREFIX + aoBaseline.getAuditInstallation().getInstallation().getVersion();
        		}
				
				if (aoRepoMap.containsKey(aoRepoNode)) {
					AuditObject aoRepo = aoRepoMap.get(aoRepoNode);
					
					// Iterate over the baseline audit values and create LocationProperties
					Iterator<AuditValue> itBaseline = aoBaseline.getAuditValues().iterator();
					while (itBaseline.hasNext()) {
						ar.addNumPropertiesAudited();
						AuditValue avBaseline = itBaseline.next();
						LocationItem locItem = new LocationItem(new Location(aoBaseline.getAuditInstallation(),avBaseline.getComponent(),avBaseline.getInstance()));
						AuditValue avRepo = aoRepo.getAuditValue(avBaseline.getComponent(), avBaseline.getInstance(), avBaseline.getProperty());
						// If the other property does not exist, include in result
						if (avRepo == null) {
							ar.addNumPropertiesDifferent();
							locItem.addLocationProperty(new LocationProperty(avBaseline.getProperty(), aoBaseline.getRepository(), avBaseline.getValue(), aoRepo.getRepository(), AuditResult.AUDIT_VALUE_MISSING));
						} else if (!avBaseline.getValue().equals(avRepo.getValue())) { 
							// If the other property exists and they are different, include in result
							ar.addNumPropertiesDifferent();
							locItem.addLocationProperty(new LocationProperty(avBaseline.getProperty(), aoBaseline.getRepository(), avBaseline.getValue(), aoRepo.getRepository(), avRepo.getValue()));
						}
						ar.addLocation(locItem);
					}
					
					// Iterate over the runtime/default audit values and create LocationProperties (if they haven't been added)
					Iterator<AuditValue> itRepo = aoRepo.getAuditValues().iterator();
					while (itRepo.hasNext()) {
						AuditValue avRepo = itRepo.next();
						AuditValue avBaseline = aoBaseline.getAuditValue(avRepo.getComponent(), avRepo.getInstance(), avRepo.getProperty());
						// If the other property does not exist, include in result - only for baseline-runtime values (not baseline-default values)
						if ((avBaseline == null) && (aoRepo.getRepository().equals(Configuration.OPENCM_RUNTIME_DIR))) {
							ar.addNumPropertiesAudited();
							ar.addNumPropertiesDifferent();
							LocationItem locItem = new LocationItem(new Location(aoBaseline.getAuditInstallation(),avRepo.getComponent(),avRepo.getInstance()));
							locItem.addLocationProperty(new LocationProperty(avRepo.getProperty(), aoBaseline.getRepository(), AuditResult.AUDIT_VALUE_MISSING, aoRepo.getRepository(), avRepo.getValue()));
							ar.addLocation(locItem);
						}
					}
					
				}
			}
		}
		if (ac.getAudit_type().equals(AuditConfiguration.AUDIT_TYPE_REFERENCE)) {
	    	// -------------------------------------------------------------------------------
	    	// Property centric: Reference Audits
	    	// -------------------------------------------------------------------------------
	    	// 	=> Locate the Reference AuditObject from collection
			AuditObject aoRef = null;
			for (int a = 0; a < aos.size(); a++) {
				AuditObject ao = aos.get(a);
				if (ao.getAuditInstallation().getInstallation().getName().equals(ac.getRepo_name())) {
					aoRef = ao;
					break;
				}
			}
			if (aoRef == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Collection :: collect :: unable to locate Reference Node in collected items: " + ac.getRepo_name());
				return null;
			}
			
			// Loop through each property of the reference installation
			for (int r = 0; r < aoRef.getAuditValues().size(); r++) {
				ar.addNumPropertiesAudited();
				AuditValue avRef = aoRef.getAuditValues().get(r);
				PropertyItem pi = new PropertyItem(avRef.getProperty());
				Location lRef = new Location(aoRef.getAuditInstallation(),avRef.getComponent(),avRef.getInstance());
				PropertyLocation plRef = new PropertyLocation(lRef,avRef.getValue());
				pi.addPropertyLocation(plRef);
				boolean includeProp = false;
				// Go through all the collected data
				for (int a = 0; a < aos.size(); a++) {
					AuditObject ao = aos.get(a);
					if (ao.getAuditInstallation().getInstallation().getName().equals(ac.getRepo_name())) {
						continue;
					}
					// For this installation, find the property
					Location l = new Location(ao.getAuditInstallation(),avRef.getComponent(),avRef.getInstance());
					AuditValue avNode = ao.getAuditValue(avRef.getComponent(), avRef.getInstance(), avRef.getProperty());
					if (avNode == null) {
						ar.addNumPropertiesDifferent();
						pi.addPropertyLocation(new PropertyLocation(l,AuditResult.AUDIT_VALUE_MISSING));
						includeProp = true;
					} else if (!avRef.getValue().equals(avNode.getValue())) {
						ar.addNumPropertiesDifferent();
						pi.addPropertyLocation(new PropertyLocation(l,avNode.getValue()));
						includeProp = true;
					}
				}
				if (includeProp) {
					ar.addPropertyItem(pi);
				}
			}
		}
        return ar;
    }

}
