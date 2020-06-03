package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.Department;
import org.opencm.inventory.Organisation;
import org.opencm.inventory.Server;
import org.opencm.inventory.Installation;
import org.opencm.inventory.RuntimeComponent;
import org.opencm.util.CceUtils;
import org.opencm.util.LogUtils;
import org.opencm.security.KeyUtils;
// --- <<IS-END-IMPORTS>> ---

public final class cce

{
	// ---( internal utility methods )---

	final static cce _instance = new cce();

	static cce _newInstance() { return new cce(); }

	static cce _cast(Object o) { return (cce)o; }

	// ---( server methods )---




	public static final void manage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(manage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required action
		// [i] field:0:required node
				
		// Actions: 
		//			- refreshAll (removes all and generates env groups and nodes within)
		//			- generateNode (generate a single node, within a group)
		//				- also then takes node as argument
		//
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	action = IDataUtil.getString( pipelineCursor, "action" );
		String	node = IDataUtil.getString( pipelineCursor, "node" ); 
		pipelineCursor.destroy();
		
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		 
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		
		// --------------------------------------------------------------------
		// Check arguments
		// --------------------------------------------------------------------
		if (action == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM cce:manage :: No action specified .. ");
			return;
		} else if (!action.equals("cli") && !action.equals("generateNode")) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM cce:manage :: Invalid action specified .. " + action);
			return;
		} else if (action.equals("generateNode") && (node == null)) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM cce:manage :: No node passed as argument .. ");
			return;
		}
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," vizualization:generate : Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("org.opencm.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM cce:manage :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Ensure Encrypted passwords ...
		// --------------------------------------------------------------------
		inv.ensureEncryptedPasswords(opencmConfig);
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"------------ CCE nodes management - Starting Process ...... ");
		
		// -------------------------------------------------------------------- 
		// Determine group syntax
		// --------------------------------------------------------------------
		String groupSyntax = opencmConfig.getCce_mgmt_group_syntax();
		StringTokenizer st = new StringTokenizer(groupSyntax, "|");
		boolean gOrg = false;
		boolean gDep = false;
		boolean gEnv = false;
		boolean gLay = false;
		while(st.hasMoreTokens()) {
			String el = st.nextToken();
			switch (el) {
				case "ORG":
					gOrg = true;
					break;
				case "DEP":
					gDep = true;
					break;
				case "ENV":
					gEnv = true;
					break;
				case "LAYER":
					gLay = true;
					break;
				default:
					break;
			}
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," group syntax: Org? " + gOrg + " Dep? " + gDep + " Env? " + gEnv + " Layer? " + gLay);
		
		CceUtils cceUtils = new CceUtils(opencmConfig, inv);
		
		if (action.equals("cli")) {
			// --------------------------------------------------------------------
			// Define which installations to create
			// --------------------------------------------------------------------
			Inventory cceInventory = inv.createInventory(opencmConfig, opencmConfig.getCce_mgmt_create());
			LinkedList<Installation> allFilteredInstallations = cceInventory.getAllInstallations();
			
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," CCE Nodes: Installations to be added: " + allFilteredInstallations.size());
			
			// -------------------------------------------------------------------- 
			// Based on each installation, create group names and populate hashmap
			// --------------------------------------------------------------------
			HashMap<String,String> cceGroups = new HashMap<String,String>();
			HashMap<Installation,String> cceNodes = new HashMap<Installation,String>();
			
			for (int i = 0; i < allFilteredInstallations.size(); i++) {
				Installation installation = allFilteredInstallations.get(i);
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Processing " + installation.getName() + " ...");
				// -------------------------------------------------------------------- 
				// Ensure SPM runtime
				// --------------------------------------------------------------------
				RuntimeComponent spmRuntime = installation.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
				if (spmRuntime == null) {
					continue;
				}
				
				String groupAlias = "";
				String groupName = "";
				// Determine group name 
				if(gOrg) {
					groupAlias += inv.getNodeOrganisation(installation.getName()).getName();
					groupName += inv.getNodeOrganisation(installation.getName()).getName();
					if (gDep || gEnv || gLay) {
						groupAlias += "-";
						groupName += opencmConfig.getCce_mgmt_group_delim();
					}
				}
				if(gDep) {
					groupAlias += inv.getNodeDepartment(installation.getName()).getName();
					groupName += inv.getNodeDepartment(installation.getName()).getName();
					if (gEnv || gLay) {
						groupAlias += "-";
						groupName += opencmConfig.getCce_mgmt_group_delim();
					}
				}
				if(gEnv) {
					groupAlias += installation.getEnvironment();
					groupName += installation.getEnvironment();
					if (gLay) {
						groupAlias += "-";
						groupName += opencmConfig.getCce_mgmt_group_delim();
					}
				}
				if(gLay) {
					groupAlias += installation.getLayer();
					groupName += installation.getLayer();
				}
				if ((groupAlias != null) && !cceGroups.containsKey(groupAlias)) {
					cceGroups.put(groupAlias, groupName);
				}
				cceNodes.put(installation, groupAlias);
			}
			
			// ------------------------------------------------------
			// Create Fix Script Content
			// ------------------------------------------------------
			StringBuffer sb = new StringBuffer();
			sb.append("-------------------------------------------------------------------------------" + System.lineSeparator());
			sb.append(System.lineSeparator());
			sb.append("     Auto-generated CLI commands for generating CCE nodes and groups ... " + System.lineSeparator());
			sb.append(System.lineSeparator());
			sb.append("-------------------------------------------------------------------------------" + System.lineSeparator());
			sb.append(System.lineSeparator());
			sb.append("---------------------------------------------------------------" + System.lineSeparator());
			sb.append("     Create groups: " + System.lineSeparator());
			sb.append("---------------------------------------------------------------" + System.lineSeparator());
			// -------------------------------------------------------------------- 
			// Create all groups
			// --------------------------------------------------------------------
			Iterator<String> itGroups = cceGroups.keySet().iterator();
			while (itGroups.hasNext()) {
				String gAlias = itGroups.next();
				String gName = cceGroups.get(gAlias);
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," --> Creating CCE Environment (alias) :: " + gAlias);
				// cceUtils.createEnvironment(gAlias,gName);
				sb.append(".\\sagcc create landscape environments alias=" + gAlias + " name=\"" + gName + "\"" + System.lineSeparator());
			}
			sb.append(System.lineSeparator());
			sb.append("---------------------------------------------------------------" + System.lineSeparator());
			sb.append("     Create All nodes and assign to groups: " + System.lineSeparator());
			sb.append("---------------------------------------------------------------" + System.lineSeparator());
			// -------------------------------------------------------------------- 
			// Create all Nodes and assign to group
			// Structure the output based on Env - Layer
			// --------------------------------------------------------------------
			LinkedList<Organisation> orgs = cceInventory.getInventory();
			for (int o = 0; o < orgs.size(); o++) {
				LinkedList<Department> deps = orgs.get(o).getDepartments();
				for (int d = 0; d < deps.size(); d++) {
					Department dep = deps.get(d);
					LinkedList<String> envs = dep.getEnvironments();
					for (int e = 0; e < envs.size(); e++) {
						String env = envs.get(e);
						sb.append(System.lineSeparator());
						sb.append("---------------------------------------------------------------" + System.lineSeparator());
						sb.append(" Environment: " + env + System.lineSeparator());
						sb.append("---------------------------------------------------------------" + System.lineSeparator());
						LinkedList<String> layers = dep.getLayersByEnv(env);
						for (int l = 0; l < layers.size(); l++) {
							String layer = layers.get(l);
							sb.append("-----------------------" + System.lineSeparator());
							sb.append(" " + layer + " ::" + System.lineSeparator());
							sb.append("-----------------------" + System.lineSeparator());
							LinkedList<Installation> installations = cceInventory.getInstallations(orgs.get(o).getName(), deps.get(d).getName(), env, layer);
							for (int i = 0; i < installations.size(); i++) {
								Installation inst = installations.get(i);
								String gAlias = cceNodes.get(inst);
								Server server = cceInventory.getNodeServer(inst.getName());
								RuntimeComponent spmRuntime = inst.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
								String spmUrl = spmRuntime.getProtocol() + "://" + inst.getName() + ":" + spmRuntime.getPort();
								LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"   - Writing Node :: " + inst.getName());
								
								if (inst.getName().equals(opencmConfig.getCce_mgmt_node())) {
									// Ignore local CCE node alias 
								} else {
									// Create node
									// cceUtils.createNode(newNode.getName(), spmUrl);
									sb.append(".\\sagcc add landscape nodes alias=" + inst.getName() + " url=" + spmUrl + System.lineSeparator());
									// Change Password
									// cceUtils.setNodePassword(newNode.getName(), spmRuntime.getDecryptedPassword());
									sb.append(".\\sagcc add security credentials nodeAlias=" + inst.getName() + " runtimeComponentId=SPM-CONNECTION authenticationType=BASIC username=Administrator password=" + spmRuntime.getDecryptedPassword() + System.lineSeparator());
									
									// Assign to group
									if (gAlias != null) {
										// cceUtils.assignNodeToEnv(gAlias, newNode.getName());
										sb.append(".\\sagcc add landscape environments " + gAlias + " nodes nodeAlias=" + inst.getName() + System.lineSeparator());
									}
								}
								sb.append(System.lineSeparator());
							}
						}
					}
				}
			}
				
			sb.append(System.lineSeparator());
			sb.append("-------------------------------------------------------------------------------" + System.lineSeparator());
			sb.append("     End of CLI commands " + System.lineSeparator());
			sb.append("-------------------------------------------------------------------------------" + System.lineSeparator());
			
			
			// ------------------------------------------------------
			// Write Fix Script Content to file
			// ------------------------------------------------------
			try {
				BufferedWriter bwr = new BufferedWriter(new FileWriter(opencmConfig.getOutput_dir() + File.separator + Configuration.OPENCM_RESULTS_DIR_CLI + File.separator + "CCENodes.txt"));
			    bwr.write(sb.toString());
			   
			    //flush the stream
			    bwr.flush();
			   
			    //close the stream
			    bwr.close();
			    
			} catch (IOException ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CLI Generation - Exception: " + ex.toString());
			}
			
			
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," -------------- CLI Generation Process Completed   ------------   ");
			
		} else if (action.equals("generateNode")) {
			Installation installation = inv.getInstallation(node);
			if (installation == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM cce:manage :: Node " + node + " is not defined... ");
				return;
			}
			// --------------------------------------------------------------------
			// Server properties
			// --------------------------------------------------------------------
			Server server = inv.getNodeServer(installation.getName());
			
			String groupAlias = "";
			// String groupName = "";
			
			// Determine group name 
			if(gOrg) {
				groupAlias += inv.getNodeOrganisation(installation.getName()).getName();
				// groupName += inv.getNodeOrganisation(installation.getName()).getName();
				if (gDep || gEnv || gLay) {
					groupAlias += "-";
					// groupName += opencmConfig.getCce_mgmt_group_delim();
				}
			}
			if(gDep) {
				groupAlias += inv.getNodeDepartment(installation.getName()).getName();
				// groupName += inv.getNodeDepartment(installation.getName()).getName();
				if (gEnv || gLay) {
					groupAlias += "-";
					// groupName += opencmConfig.getCce_mgmt_group_delim();
				}
			}
			if(gEnv) {
				groupAlias += installation.getEnvironment();
				// groupName += installation.getEnvironment();
				if (gLay) {
					groupAlias += "-";
					// groupName += opencmConfig.getCce_mgmt_group_delim();
				}
			}
			if(gLay) {
				groupAlias += installation.getLayer();
				// groupName += installation.getLayer();
			}
			
			// --------------------------------------------------------------------
			// Delete if exists
			// --------------------------------------------------------------------
			cceUtils.deleteNode(installation.getName());
			
			RuntimeComponent rc = installation.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
			String spmUrl = rc.getProtocol() + "://" + server.getName() + ":" + rc.getPort();
			cceUtils.createNode(installation.getName(), spmUrl);
			cceUtils.setNodePassword(installation.getName(), rc.getDecryptedPassword());
			cceUtils.assignNodeToEnv(groupAlias, installation.getName());
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"------------ CCE nodes management - Process Completed ...... ");
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	// --- <<IS-END-SHARED>> ---
}

