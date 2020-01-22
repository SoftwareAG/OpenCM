package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.inventory.Inventory;
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
		} else if (!action.equals("refreshAll") && !action.equals("generateNode")) {
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
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Manage CCE nodes: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("OpenCM.pub.startup", "startup"), IDataFactory.create());
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
		
		if (action.equals("refreshAll")) {
			// --------------------------------------------------------------------
			// Define which installations to create
			// --------------------------------------------------------------------
			LinkedList<Installation> installations = inv.createInventory(opencmConfig, opencmConfig.getCce_mgmt_create()).getAllInstallations();
			
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," CCE Nodes: Installations to be added: " + installations.size());
			
			// -------------------------------------------------------------------- 
			// Based on each installation, create group names and populate hashmap
			// --------------------------------------------------------------------
			HashMap<String,String> cceGroups = new HashMap<String,String>();
			HashMap<Installation,String> cceNodes = new HashMap<Installation,String>();
			
			for (int i = 0; i < installations.size(); i++) {
				Installation installation = installations.get(i);
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
			
			// -------------------------------------------------------------------- 
			// Create all groups
			// --------------------------------------------------------------------
			Iterator<String> itGroups = cceGroups.keySet().iterator();
			while (itGroups.hasNext()) {
				String gAlias = itGroups.next();
				String gName = cceGroups.get(gAlias);
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," --> Creating CCE Environment (alias) :: " + gAlias);
				cceUtils.deleteEnvironment(gAlias);
				cceUtils.createEnvironment(gAlias,gName);
			}
		
			// -------------------------------------------------------------------- 
			// Create all Nodes and assign to group
			// --------------------------------------------------------------------
			Iterator<Installation> itNodes = cceNodes.keySet().iterator();
			while (itNodes.hasNext()) {
				Installation newNode = itNodes.next();
				String gAlias = cceNodes.get(newNode);
				
				Server server = inv.getNodeServer(newNode.getName());
				
				if (!newNode.getName().equals(opencmConfig.getCce_mgmt_node())) {
					cceUtils.deleteNode(newNode.getName());
				}
				RuntimeComponent spmRuntime = newNode.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
				String spmUrl = spmRuntime.getProtocol() + "://" + server.getName() + ":" + spmRuntime.getPort();
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"   - Creating Node :: " + newNode.getName());
				if (newNode.getName().equals(opencmConfig.getCce_mgmt_node())) {
					// Update the local CCE node alias to match the OpenCM CCE node alias
					cceUtils.updateNodeName("local", newNode.getName());
					if (gAlias != null) {
		    			cceUtils.assignNodeToEnv(gAlias, "local");
					}
				} else {
					cceUtils.createNode(newNode.getName(), spmUrl);
					cceUtils.setNodePassword(newNode.getName(), spmRuntime.getDecryptedPassword());
					if (gAlias != null) {
						cceUtils.assignNodeToEnv(gAlias, newNode.getName());
					}
				}
				
			}
			
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
			// Create cce group (if it doesn't exist)
			// --------------------------------------------------------------------
			// cceUtils.createEnvironment(groupAlias,groupName);
			// Above recreates the environment if it exists. I.e. removes all existing nodes within...
			
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
