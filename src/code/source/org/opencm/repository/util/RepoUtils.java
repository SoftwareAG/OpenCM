package org.opencm.repository.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.opencm.audit.assertion.AssertionStore;
import org.opencm.audit.configuration.Property;
import org.opencm.audit.configuration.PropertyFilter;
import org.opencm.audit.env.AssertionConfig;
import org.opencm.audit.env.AssertionEnvironment;
import org.opencm.audit.env.AssertionGroup;
import org.opencm.audit.env.AssertionProperty;
import org.opencm.audit.env.AssertionValue;
import org.opencm.audit.env.AssertionValuePair;
import org.opencm.audit.util.JsonParser;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.Node;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.RuntimeComponent;
import org.opencm.extract.spm.SpmOps;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;

public class RepoUtils {
	
	
	public final static String INTEGRATION_SERVER_PREFIX	= "integrationServer";
	public final static String UNIVERSAL_MESSAGING_PREFIX	= "Universal-Messaging";
	
	public final static String ASSERTION_UNDEFINED_NODE			= "NO_OPENCM_NODE_DEFINED";
	public final static String ASSERTION_MISSING_COMPONENT		= "MISSING_COMPONENT";
	public final static String ASSERTION_MISSING_DATA 			= "NO_DATA_FOUND";
	public final static String ASSERTION_FILTERED_DATA			= "DATA_FILTERED";
	public final static String ASSERTION_DEFAULT_VALUE_MISSING	= "N/A";

	

	public static LinkedList<String> getFixList(Configuration opencmConfig, Node opencmNode) {
		LinkedList<String> fixList = new LinkedList<String>();
		File fixesDirectory = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR + File.separator + opencmNode.getUnqualifiedHostname() + File.separator + opencmNode.getNode_name() + File.separator + SpmOps.SPM_COMP_FIXES);
		if (!fixesDirectory.exists()) {
			return fixList;
		}
		try {
			File[] fixDirs = fixesDirectory.listFiles();
			for (int i = 0; i < fixDirs.length; i++) {
				File fixDir = fixDirs[i];
				if (fixDir.isDirectory()) {
					File jsonFixInfo = new File(fixDir.getPath() + File.separator + SpmOps.SPM_CONF_FILENAME);
					if (jsonFixInfo.exists()) {
						String fixName = JsonUtils.getJsonValue(jsonFixInfo,"/fixName");
						String fixVersion = JsonUtils.getJsonValue(jsonFixInfo,"/version");
						fixList.add(fixName + "_" + fixVersion);
					}
				}
				
			}
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"RepoUtils :: getFixList :: " + ex.getMessage());
		}

		return fixList;
	}
	
	public static String getInstallDir(Configuration opencmConfig, Node opencmNode) {
		File spmDirectory = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR + File.separator + opencmNode.getUnqualifiedHostname() + File.separator + opencmNode.getNode_name() + File.separator + SpmOps.SPM_COMP_NAME);
		if (!spmDirectory.exists()) {
			return null;
		}
		
		File jsonSpmInstallInfo = new File(spmDirectory.getPath() + File.separator + SpmOps.SPM_INST_INSTALL_DIR + File.separator + SpmOps.SPM_CONF_FILENAME);
		if (!jsonSpmInstallInfo.exists()) {
			return null;
		}
		
		try {
			return JsonUtils.getJsonValue(jsonSpmInstallInfo,"/plain-text-property");
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"RepoUtils :: getInstallDir :: " + ex.getMessage());
		}

		return null;
	}
	
	/*
	 * Based on a single Node and a single Property configuration, retrieve all values that are associated with it
	 * 
	 */
	public static LinkedList<AssertionValue> getAssertionValues(Configuration opencmConfig, Node opencmNode, Property propConfig, LinkedList<PropertyFilter> filters) {
		// --------------------------------------------------------------------
		// Initialize
		// --------------------------------------------------------------------
		LinkedList<AssertionValue> values = new LinkedList<AssertionValue>();

		// ------------------------------------------------------------
		// Get assertion values (from default reference, baseline or runtime)
		// ------------------------------------------------------------
		File nodeDirectory;
		if (opencmNode.getRepositoryType().equals(Configuration.OPENCM_DEFAULT_DIR)) {
			nodeDirectory = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_DEFAULT_DIR + File.separator + Configuration.OPENCM_REFERENCE_DIR_PREFIX + opencmNode.getNode_name());
		} else {
			nodeDirectory = new File(opencmConfig.getCmdata_root()  + File.separator + opencmNode.getRepositoryType() + File.separator + opencmNode.getUnqualifiedHostname() + File.separator + opencmNode.getNode_name());
		}

		if ((nodeDirectory != null) && (nodeDirectory.exists())) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  - getAssertionValues from " + nodeDirectory.getPath());
			LinkedList<AssertionValue> assList = getInstancePaths(opencmConfig, opencmNode, propConfig, nodeDirectory.getPath());
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"  - getAssertionValues getInstancePaths Size: " + assList.size());
			for (int i = 0; i < assList.size(); i++) {
				LinkedList<AssertionValue> assValues = getValues(opencmConfig, assList.get(i), propConfig, filters);
				if ((assValues != null) && (assValues.size() > 0)) {
					values.addAll(assValues);
				}
			}
		}
		return values; 
	}


	/**
	 * Get all the Instance paths for a single property/node combination based on the Property configuration
	 * Returned as a list of AssertionValues (need to keep track of Component and Instance names)
	 * 
	 */
	private static LinkedList<AssertionValue> getInstancePaths(Configuration opencmConfig, Node opencmNode, Property propConfig, String nodeDir) {
		LinkedList<AssertionValue> assList = new LinkedList<AssertionValue>();

		if (propConfig.getComponent().equals(AssertionStore.ANY_ASSERTION_KEYWORD) && propConfig.getInstance().equals(AssertionStore.ANY_ASSERTION_KEYWORD)) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL, "Parser :: Invalid Configuration: Component and Instance cannot both be " + AssertionStore.ANY_ASSERTION_KEYWORD);
			return assList;
		}
		
		// -------------------------------------------------------
		// Determine what configuration properties to assert
		// (Ability to use ANY as a keyword for choosing components or instances)
		// Note:  ignore if the directory does not exist (could be any installation type)
		// -------------------------------------------------------
		if (!propConfig.getComponent().equals(AssertionStore.ANY_ASSERTION_KEYWORD) && !propConfig.getInstance().equals(AssertionStore.ANY_ASSERTION_KEYWORD)) {
			// Might have multiple component directories... e.g. using wildcard in component name
			LinkedList<String> nodeCompDirs = FileUtils.getSubDirectories(nodeDir, propConfig.getComponent());
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"    - getInstancePaths Prop Component: " + propConfig.getComponent() + " Size of dirs: " + nodeCompDirs.size());
			for (int i = 0; i < nodeCompDirs.size(); i++) {
				String nodeCompDir = nodeCompDirs.get(i);
				LinkedList<String> nodeCompInstDirs = FileUtils.getSubDirectories(nodeDir + File.separator + nodeCompDir, propConfig.getInstance());
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"      - getInstancePaths Prop Instance: " + propConfig.getInstance() + " Size of dirs: " + nodeCompInstDirs.size());
				for (int j = 0; j < nodeCompInstDirs.size(); j++) {
					// Might have multiple instance directories... e.g. using wildcard in instance name
					String nodeCompInstDir = nodeCompInstDirs.get(j);
					File propFile = new File(nodeDir + File.separator + nodeCompDir + File.separator + nodeCompInstDir + File.separator + "ci-properties.json");
					if (propFile.exists()) {
						assList.add(new AssertionValue(opencmNode.getNode_name(),nodeCompDir,nodeCompInstDir,propFile));
					}
				}
				
			}
		} else if (propConfig.getComponent().equals(AssertionStore.ANY_ASSERTION_KEYWORD)) {
			// ------------------------------------------------------------
			// Get All Component Directories and match with configured Instance name (wildcard and lists allowed)
			// ------------------------------------------------------------
			File [] fCompDirectories = new File(nodeDir).listFiles();
			for (File compPath:fCompDirectories) {
				if (compPath.isDirectory()) {
					File dir = new File(nodeDir + File.separator + compPath.getName());
					File [] fInstanceDirectories = dir.listFiles();
					for (File instPath:fInstanceDirectories) {
						if (instPath.isDirectory() && matches(instPath.getName(), propConfig.getInstance())) {
							File propFile = new File(nodeDir + File.separator + compPath.getName() + File.separator + instPath.getName() + File.separator + "ci-properties.json");
							if (propFile.exists()) {
								assList.add(new AssertionValue(opencmNode.getNode_name(),compPath.getName(),instPath.getName(),propFile));
							}
						}
					}

				}
			}
		} else if (propConfig.getInstance().equals(AssertionStore.ANY_ASSERTION_KEYWORD)) {
			LinkedList<String> nodeCompDirs = FileUtils.getSubDirectories(nodeDir, propConfig.getComponent());
			for (int i = 0; i < nodeCompDirs.size(); i++) {
				String nodeCompDir = nodeCompDirs.get(i);
				File componentDir = new File(nodeDir + File.separator + nodeCompDir);
				if (componentDir.exists()) {
					File [] fInstanceDirectories = componentDir.listFiles();
					for(File instPath:fInstanceDirectories) {
						if (instPath.isDirectory()) {
							File propFile = new File(nodeDir + File.separator + nodeCompDir + File.separator + instPath.getName() + File.separator + "ci-properties.json");
							if (propFile.exists()) {
								assList.add(new AssertionValue(opencmNode.getNode_name(),nodeCompDir,instPath.getName(),propFile));
							}
						}
					}
				}
			}
		}
		
		return assList;

	}

	/**
	 * Method to retrieve the AssertionValues from the property file.
	 * A single property file may include multiple properties (e.g. COMMON-SYSPROPS)
	 * or a single property (e.g. a Fix version). The passed AssertionValue holds the component name, instance name and
	 * path to the actual json property file but the this method may result in passing back multiple AssertionValues.
	 */
	private static LinkedList<AssertionValue> getValues(Configuration opencmConfig, AssertionValue av, Property propConfig, LinkedList<PropertyFilter> filters) {
		LinkedList<AssertionValue> assValues = new LinkedList<AssertionValue>();
		
		// -----------------------------------------------------
		// Support multiple keys (comma-delimited)
		// -----------------------------------------------------
		LinkedList<String> keyElements = new LinkedList<String>();
		String key = propConfig.getKey();
		while (true) {
			String parsedKey = parseKey(key);
			if (parsedKey == null) {
				break;
			}
			keyElements.add(parsedKey);
			key = key.substring(parsedKey.length()).trim();
			if (key.startsWith(",")) {
				key = key.substring(key.indexOf(",") + 1).trim();
			}
		}

		try {
			for (int i = 0; i < keyElements.size(); i++) {
				JsonParser jp = new JsonParser(opencmConfig, av.getPropertyFile().getPath(), keyElements.get(i));
				HashMap<String,String> jsonValues = jp.getProperties();
				// -----------------------------------------------------
				// Parse Keys and Values from the json property file
				// -----------------------------------------------------
				if (jsonValues != null) {
					java.util.Iterator<String> it = jsonValues.keySet().iterator();
					while (it.hasNext()) {
						String propKey = it.next();
						// ------------------------------------------------
						// Apply Configuration Filters
						// ------------------------------------------------
						boolean filtered = false;
						if (filters != null) {
							for (int f = 0; f < filters.size(); f++) {
								// Node
								if (filters.get(f).getNodeAlias().equals(AssertionStore.ANY_ASSERTION_KEYWORD) || filters.get(f).getNodeAlias().equals(av.getNode())) {
									// Component
									if (matches(av.getComponent(), filters.get(f).getComponent())) {
										// Instance
										if (matches(av.getInstance(), filters.get(f).getInstance())) {
											// Key
											if (matches(propKey, filters.get(f).getKey())) {
												filtered = true;
												break;
											}
										}
									}
								}
							}
						}
						AssertionValue avNew = new AssertionValue(av.getNode(),av.getComponent(),av.getInstance(),propKey, jsonValues.get(propKey), av.getPropertyFile(), filtered); 
						assValues.add(avNew);
					}  
				}
			}
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"getValues :: Exception: " + ex.toString());
		}
		return assValues;
		
	}

	/**
	 * Based on a Hashmap of assertion groups, identify missing (ABSENT) values 
	 * This is based on layered audits where the total amount of property values for all installations are padded 
	 * -----------------------------------------------------------
	 * 			ENV 01		ENV 02		ENV 03
	 * -----------------------------------------------------------
	 * prop 	Value01		Value01		Value01
	 * 			Value02		Missing		Value02
	 * 			Undefined	Value03		Value03
	 * 
	 * (Padded with "Undefined" and "Missing")
	 */
	public static HashMap<String,AssertionGroup> postProcessValues(Configuration opencmConfig, Nodes opencmNodes, HashMap<String,AssertionGroup> assGroups, AssertionConfig envAuditConfig) {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Parser - postProcessValues ........... ");
        LinkedList<String> definedEnvironments = opencmNodes.getAllEnvironments();

        // Ignore environments if not explicitly defined in the env audit properties
		if ((envAuditConfig.getEnvironments() != null) && (envAuditConfig.getEnvironments().size() > 0)) {
			LinkedList<String> someEnvs = new LinkedList<String>();
			for (int i = 0; i < definedEnvironments.size(); i++) {
				String env = definedEnvironments.get(i);
				// Only process nodes that are within the defined environment
				if (envAuditConfig.getEnvironments().contains(env)) {
					someEnvs.add(env);
				}
			}
			definedEnvironments = someEnvs;
		}
        
        HashMap<String,AssertionGroup> postProcessedAGs = assGroups;
        
	    Iterator<String> itGroups = assGroups.keySet().iterator();
	    while (itGroups.hasNext()) {
	    	AssertionGroup ag = assGroups.get(itGroups.next());
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," - postProcess Assertion Group: " + ag.getAssertionGroup());
	        HashMap<String,AssertionProperty> aps = ag.getAssertionProperties();
		    Iterator<String> itProps = aps.keySet().iterator();
		    while (itProps.hasNext()) {
				AssertionProperty ap = aps.get(itProps.next());
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"    - postProcess Property: " + ap.getPropertyName());
				
				// -----------------------------------------------------------------
				// Determine if this property has multiple values per node
				// This is used to pad properly when dealing with e.g. IS or UM configurations
				// -----------------------------------------------------------------
				boolean instanceServerComponent = false;
				String firstEnv = ap.getAssertionEnvironments().keySet().iterator().next();
				if (ap.getAssertionEnvironments().get(firstEnv).getValues().get(0).getComponent() != null) {
					if (ap.getAssertionEnvironments().get(firstEnv).getValues().get(0).getComponent().startsWith(INTEGRATION_SERVER_PREFIX) || ap.getAssertionEnvironments().get(firstEnv).getValues().get(0).getComponent().startsWith(UNIVERSAL_MESSAGING_PREFIX)) {
						instanceServerComponent = true;
					}
				}
				
				// -----------------------------------------------------------------
				// Find out the highest number of defined nodes for this property
				// Used to "Pad" the missing AVs due to non-defined nodes or missing values
				// -----------------------------------------------------------------
				int maxValuesSize = 0;

				// -- Retrieved Data
			    Iterator<String> itEnvs = ap.getAssertionEnvironments().keySet().iterator();
			    while (itEnvs.hasNext()) {
			    	int envSize = ap.getAssertionEnvironments().get(itEnvs.next()).getValues().size();
			    	if (envSize > maxValuesSize) {
			    		maxValuesSize = envSize; 
			    	}
			    }
			    
				// -- Defined Nodes and potentially its instances
        		for (int i = 0; i < definedEnvironments.size(); i++) {
			    	LinkedList<Node> tmpNodes = opencmNodes.getNodesByGroupAndEnv(ag.getAssertionGroup(), definedEnvironments.get(i));
			    	if (!instanceServerComponent && (tmpNodes.size() > maxValuesSize)) {
			    		maxValuesSize = tmpNodes.size(); 
			    	} else {
			    		int totalInstances = 0;
			    		for (int t = 0; t < tmpNodes.size(); t++) {
			    			LinkedList<RuntimeComponent> tmpRuntimeComponents = tmpNodes.get(t).getRuntimeComponents();
			    			for (int t2 = 0; t2 < tmpRuntimeComponents.size(); t2++) {
			    				if (tmpRuntimeComponents.get(t2).getName().startsWith(INTEGRATION_SERVER_PREFIX) || tmpRuntimeComponents.get(t2).getName().startsWith(UNIVERSAL_MESSAGING_PREFIX)) {
					    			totalInstances++;
			    				}
			    			}
			    		}
				    	if (totalInstances > maxValuesSize) {
				    		maxValuesSize = totalInstances; 
				    	}
			    	}
			    }

				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"      - Max Value Size: " + maxValuesSize);
				
				// -----------------------------------------------------------------
				// Loop through all the defined environments 
				// -----------------------------------------------------------------
		        for (int envIdx = 0; envIdx < definedEnvironments.size(); envIdx++) {
		        	String definedEnv = definedEnvironments.get(envIdx);

	        		// Get all the defined nodes for this environment and assertion group
	        		LinkedList<Node> definedNodes = opencmNodes.getNodesByGroupAndEnv(ag.getAssertionGroup(),definedEnv);

		        	if (ap.getAssertionEnvironments().get(definedEnv) == null) {
						// ------------------------------------------------------------
		        		// This part covers the situation where we have no runtime data, padding up to max rows required
		        		// Either we have defined nodes for this environment or not
		        		// ------------------------------------------------------------
		        		AssertionEnvironment ae = new AssertionEnvironment(definedEnv);
		        		
		        		if (definedNodes.size() == 0) {
			        		// No Runtime Data Present and no nodes defined: pad with AVs (UNDEFINED NODES)
			        		for (int i = 0; i < maxValuesSize; i++) {
			        			AssertionValue av = new AssertionValue(ASSERTION_UNDEFINED_NODE);
			        			ae.addAssertionValue(av);
			        		}
			        		
		        		} else {
			        		// No Runtime Data Present but some nodes defined: pad with AVs  (MISSING DATA)
			        		int currentSize = 0;
			        		
			        		for (int i = 0; i < definedNodes.size(); i++) {
			        			Node opencmNode = definedNodes.get(i);
				        		if (instanceServerComponent) {
				        			// For Integration Server properties, we want to also add the instance name (opencm component name)
					        		for (int n = 0; n < opencmNode.getRuntimeComponents().size(); n++) {
					        			RuntimeComponent opencmRuntimeComponent = opencmNode.getRuntimeComponents().get(n);
					        			if (opencmRuntimeComponent.getName().startsWith(INTEGRATION_SERVER_PREFIX) || opencmRuntimeComponent.getName().startsWith(UNIVERSAL_MESSAGING_PREFIX)) {
					        				if (!ae.componentExists(opencmNode.getNode_name(), opencmRuntimeComponent.getName())) {
							        			AssertionValue av = new AssertionValue(ASSERTION_MISSING_DATA);
							        			av.setNode(opencmNode.getNode_name());
							        			av.setComponent(opencmRuntimeComponent.getName());
							        			ae.addAssertionValue(av);
							        			currentSize++;
					        				}
					        			}
					        		}
				        		} else {
				        			// E.g. Fix levels
				        			AssertionValue av = new AssertionValue(ASSERTION_MISSING_DATA);
				        			av.setNode(opencmNode.getNode_name());
				        			ae.addAssertionValue(av);
				        			currentSize++;
				        		}
			        		}
			        		
			        		// Pad remaining with undefined nodes information...
			        		if (currentSize < maxValuesSize) {
				        		for (int i = currentSize; i < maxValuesSize; i++) {
				        			AssertionValue av = new AssertionValue(ASSERTION_UNDEFINED_NODE);
				        			ae.addAssertionValue(av);
				        		}
			        		}
			        		
		        		}
		        		
		        		// Add environment to the new HashMap
		        		postProcessedAGs.get(ag.getAssertionGroup()).getAssertionProperties().get(ap.getPropertyName()).addAssertionEnvironment(ae);
		        		
		        	} else {
						// ------------------------------------------------------------
		        		// This part covers the situation where we have runtime data
		        		// Some (if not all) nodes should exist, pad the ones that should exist with AV's (MISSING DATA)
		        		// ------------------------------------------------------------
		        		AssertionEnvironment ae = ap.getAssertionEnvironments().get(definedEnv);
		        		int currentSize = ae.getValues().size();
		        		if (currentSize < maxValuesSize) {
			        		for (int i = 0; i < definedNodes.size(); i++) {
			        			Node opencmNode = definedNodes.get(i);
				        		if (instanceServerComponent) {
					        		for (int n = 0; n < opencmNode.getRuntimeComponents().size(); n++) {
					        			RuntimeComponent opencmRuntimeComponent = opencmNode.getRuntimeComponents().get(n);
					        			if (opencmRuntimeComponent.getName().startsWith(INTEGRATION_SERVER_PREFIX) || opencmRuntimeComponent.getName().startsWith(UNIVERSAL_MESSAGING_PREFIX)) {
					        				if (!ae.componentExists(opencmNode.getNode_name(), opencmRuntimeComponent.getName())) {
							        			AssertionValue av = new AssertionValue(ASSERTION_MISSING_DATA);
							        			av.setNode(opencmNode.getNode_name());
							        			av.setComponent(opencmRuntimeComponent.getName());
							        			postProcessedAGs.get(ag.getAssertionGroup()).getAssertionProperties().get(ap.getPropertyName()).addAssertionValue(opencmNode,av);
							        			currentSize++;
					        				}
					        			}
					        		}
				        		} else if (!ae.nodeExists(opencmNode.getNode_name())) {
				        			AssertionValue av = new AssertionValue(ASSERTION_MISSING_DATA);
				        			av.setNode(opencmNode.getNode_name());
				        			postProcessedAGs.get(ag.getAssertionGroup()).getAssertionProperties().get(ap.getPropertyName()).addAssertionValue(opencmNode,av);
				        			currentSize++;
				        		}
			        		}
		        		}

		        		// Pad remaining with undefined nodes information... if needed:  (UNDEFINED NODES)
		        		if (currentSize < maxValuesSize) {
			        		for (int i = currentSize; i < maxValuesSize; i++) {
			        			AssertionValue av = new AssertionValue(ASSERTION_UNDEFINED_NODE);
			        			postProcessedAGs.get(ag.getAssertionGroup()).getAssertionProperties().get(ap.getPropertyName()).getAssertionEnvironments().get(definedEnv).addAssertionValue(av);
			        		}
		        		}
		        		
		        	}
		        	
		        }

		    }
	    	
	    }
			
		return postProcessedAGs;
	}


	 /*
	  * Helper to support lists and wild cards in component and instance names in the property files (property definitions and filters).
	  * Key can be a list of elements, and each element can contain the "*" wild card character (e.g. "Comp*Name_01,Component_02*", etc.)
	  * Wild cards are then converted to regexp and matched against the name passed.
	  *  
	  */
	 
	public static boolean matches(String name, String key) {
		if (key.equals(AssertionStore.ANY_ASSERTION_KEYWORD)) {
			return true;
		}
		LinkedList<String> keyElements = new LinkedList<String>();
		int multipleElementsIdx = key.indexOf(",");
		if (multipleElementsIdx > 0) {
			StringTokenizer st = new StringTokenizer(key,",");
			while (st.hasMoreTokens()) {
				keyElements.add(st.nextToken().trim());
			}
			
		} else {
			keyElements.add(key);
		}
			
		for (int i = 0; i < keyElements.size(); i++) {
			String stElement = Configuration.convertPropertyKey(keyElements.get(i));
			if (name.matches(stElement.toString())) {
				return true;
			}
			
		}
		return false;
	}
	 
	/*
	 * Based on assertion values from two nodes, match and create pairs that should be asserted
	 */
	public static LinkedList<AssertionValuePair> createAssertionValuePairs(LinkedList<AssertionValue> avs01, LinkedList<AssertionValue> avs02, boolean ignoreAbsentValuesFor01, boolean ignoreAbsentValuesFor02) {
		LinkedList<AssertionValuePair> avsPairs = new LinkedList<AssertionValuePair>();
		
		// Loop through the avs01 values
		for (int i = 0; i < avs01.size(); i++) {
			AssertionValue av01 = avs01.get(i);
			if (av01.isFiltered()) {
				continue;
			}
			for (int j = 0; j < avs02.size(); j++) {
				AssertionValue av02 = avs02.get(j);
				if (av01.isMatchedWith(av02)) {
					av02.setMatched(true);
					avsPairs.add(new AssertionValuePair(av01,av02));
				}
			}
			if (!av01.isMatched() && !ignoreAbsentValuesFor01) {
				avsPairs.add(new AssertionValuePair(av01,null));
			}
		}
		
		// Loop through the non-matched avs02 values
		if (!ignoreAbsentValuesFor02) {
			for (int i = 0; i < avs02.size(); i++) {
				AssertionValue av02 = avs02.get(i);
				if (!av02.isFiltered() && !av02.isMatched()) {
					avsPairs.add(new AssertionValuePair(null,av02));
				}
			}
		}
		return avsPairs;
		
	}

	/*
	 * Removes the instance names (IS instance, MWS instance, UM realm name, etc.)
	 * Replaces the instance name with search pattern, used for getting subdirectories 
	 * using the matches expression
	 */
	public static String normalizeComponentName(String name) {
		if (name.startsWith("integrationServer-")) {
			return name.substring(0,name.indexOf("-")) + ".*";
		}
		if (name.startsWith("Universal-Messaging-")) {
			return name.substring(0,name.indexOf("-",10)) + ".*";	// Multiple "-" in component name and there might be more in the realm name
		}
		if (name.startsWith("MwsProgramFiles-")) {
			return name.substring(0,name.indexOf("-")) + ".*";
		}
		if (name.startsWith("TaskEngineRuntime-")) {
			return name.substring(0,name.indexOf("-")) + ".*";
		}
		if (name.startsWith("TES-")) {
			return name.substring(0,name.indexOf("-")) + ".*";
		}
		if (name.startsWith("OSGI-IS_")) {
			if (name.endsWith("-EventRouting") || name.endsWith("-NERV") || name.endsWith("-WmBusinessRules") || name.endsWith("-WmMonitor") || name.endsWith("-WmTaskClient")) {
				return name.substring(0,(name.indexOf("_") + 1)) + ".*." + name.substring(name.lastIndexOf("-") + 1);
			} else {
				return name.substring(0,name.indexOf("_")) + ".*";
			}
		}
		if (name.startsWith("OSGI-MWS_")) {
			if (name.endsWith("-EventRouting")) {
				return name.substring(0,(name.indexOf("_") + 1)) + ".*." + name.substring(name.lastIndexOf("-") + 1);
			} else {
				return name.substring(0,name.indexOf("_")) + ".*";
			}
		}
		
		return name;
	}
    
	/*
	 * Converts a component name to a default name (used to locate default values
	 */
	public static String convertComponentNameToDefault(String name) {
		if (name.startsWith("integrationServer-")) {
			return name.substring(0,name.indexOf("-")+1) + "default";
		}
		if (name.startsWith("Universal-Messaging-")) {
			return name.substring(0,name.indexOf("-",10)+1) + "umserver";	
		}
		if (name.startsWith("MwsProgramFiles-")) {
			return name.substring(0,name.indexOf("-")+1) + "default";
		}
		if (name.startsWith("TaskEngineRuntime-")) {
			return name.substring(0,name.indexOf("-")+1) + "default";
		}
		if (name.startsWith("TES-")) {
			return name.substring(0,name.indexOf("-")+1) + "default";
		}
		if (name.startsWith("OSGI-IS_")) {
			if (name.endsWith("-EventRouting") || name.endsWith("-NERV") || name.endsWith("-WmBusinessRules") || name.endsWith("-WmMonitor") || name.endsWith("-WmTaskClient")) {
				return name.substring(0,(name.indexOf("_") + 1)) + "default" + name.substring(name.lastIndexOf("-") + 1);
			} else {
				return name.substring(0,name.indexOf("_")+1) + "default";
			}
		}
		if (name.startsWith("OSGI-MWS_")) {
			if (name.endsWith("-EventRouting")) {
				return name.substring(0,(name.indexOf("_") + 1)) + "default" + name.substring(name.lastIndexOf("-") + 1);
			} else {
				return name.substring(0,name.indexOf("_")+1) + "default";
			}
		}
		
		return name;
	}
	
	/*
	 * Gets a default value, based on an AssertionValue Object
	 * If nothing found, return null
	 */
	public static String getDefaultValue(Configuration opencmConfig, AssertionValue av) {
		// --------------------------------------------------------------------
		// Identify wM version from runtime node
		// --------------------------------------------------------------------
		String stVersion = null;
		StringTokenizer st = new StringTokenizer(av.getNode(),"_");
		while (st.hasMoreTokens()) {
			String stToken = st.nextToken();
			if ((stToken.length() == 4) && stToken.startsWith("V")) {
				stVersion = stToken;
			}
		}
		
		String defaultRootDir = opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_DEFAULT_DIR + File.separator + Configuration.OPENCM_REFERENCE_DIR_PREFIX + stVersion + File.separator;
		File ciPropFile = new File(defaultRootDir + convertComponentNameToDefault(av.getComponent()) + File.separator + av.getInstance() + File.separator + "ci-properties.json");
		if (!ciPropFile.exists()) {
			return null;
		}
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"getDefaultValue :: Property file: " + ciPropFile.getPath());
		try {
			HashMap<String,String> jsonValues = new JsonParser(opencmConfig, ciPropFile.getPath(), av.getKey()).getProperties();
			if (jsonValues == null || jsonValues.size() > 1) {
				return null;
			}
			return jsonValues.get(av.getKey());
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"getDefaultValue :: Exception: " + ex.toString());
		}
		return null;
	}

	private static String parseKey(String inKey) {
		// Verify that we don't have dynamic keys to deal with... e.g. /[key01,key02],/[key01,key02]
		int dynamicKeysStartIdx = inKey.indexOf("[");
		int dynamicKeysEndIdx = inKey.indexOf("]");
		int multipleKeysIdx = inKey.indexOf(",");
		if ((dynamicKeysStartIdx > 0) && (dynamicKeysEndIdx > 0) && (multipleKeysIdx > dynamicKeysStartIdx)) {
			return inKey.substring(0,dynamicKeysEndIdx + 1).trim();
		}
		if (multipleKeysIdx > 0) {
			return inKey.substring(0,multipleKeysIdx).trim();
		}
		if (!inKey.trim().equals("")) {
			return inKey; 
		}
		return null;
	}

	/** - Removed: using explicitly defined environments instead
	public static boolean isExtractionLocal(Configuration opencmConfig, String serverPath) {
		
		File serverProps = new File(serverPath + File.separator + SpmOps.SPM_PROP_FILENAME);
		if (!serverProps.exists()) {
			return false;
		}
		try {
			String extractNode = JsonUtils.getJsonValue(serverProps,"/" + SPM_JSONFIELD_EXTRACT_ALIAS);
			if ((extractNode != null) && extractNode.equals(opencmConfig.getLocal_opencm_node())) {
				return true;
			}
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"SpmOps :: isExtractionLocal :: " + ex.getMessage());
		}

		return false;
	}
	
	**/

}
