package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.Node;
import org.opencm.configuration.RuntimeComponent;
import org.opencm.extract.spm.SpmOps;
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
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig); 
		
		if (action.equals("generateNode") && (node != null)) {
			Node newNode = nodes.getNode(node);
			if (newNode == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM cce:manage :: Node " + node + " is not defined... ");
				return;
			}
		}
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CCE nodes management - Starting Process ...... ");
		
		CceUtils cceUtils = new CceUtils(opencmConfig,nodes);
		if (action.equals("refreshAll")) {
			// -------------------------------------------------------------------- 
			// Based on the Defined environments in config, delete/create nodes
			// --------------------------------------------------------------------
			LinkedList<String> createEnvs = opencmConfig.getCce_mgmt_create_envs();
			for (int i = 0; i < createEnvs.size(); i++) {
				String envName = createEnvs.get(i);
				
				if (opencmConfig.getCce_mgmt_append_layer()) {
					LinkedList<String> envLayers = nodes.getAllAssertionGroupsForEnvironment(envName);
					for (int l = 0; l < envLayers.size(); l++) {
						String layerName = envLayers.get(l);
						String cceEnvAlias = envName +  "-" + layerName;
						String cceEnvName = envName +  " :: " + layerName;
						if (opencmConfig.getCce_mgmt_prepend_sequence()) {
							cceEnvName = String.format("%02d", i) + " " + cceEnvName;
						}
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," --> Creating CCE Environment :: " + cceEnvAlias);
						cceUtils.deleteEnvironment(cceEnvAlias);
						cceUtils.createEnvironment(cceEnvAlias,cceEnvName);
						LinkedList<Node> opencmNodes = nodes.getNodesByGroupAndEnv(layerName, envName);
						for (int n = 0; n < opencmNodes.size(); n++) {
							generateNode(opencmConfig,cceUtils,opencmNodes.get(n),cceEnvAlias);
						}
					}
				} else {
					String cceEnvAlias = envName;
					String cceEnvName = envName;
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," --> Creating CCE Environment :: " + cceEnvAlias);
					if (opencmConfig.getCce_mgmt_prepend_sequence()) {
						cceEnvName = String.format("%02d", i) + " " + cceEnvName;
					}
					cceUtils.deleteEnvironment(cceEnvAlias);
					cceUtils.createEnvironment(cceEnvAlias,cceEnvName);
					LinkedList<Node> opencmNodes = nodes.getNodesByEnvironment(envName);
					for (int n = 0; n < opencmNodes.size(); n++) {
						generateNode(opencmConfig,cceUtils,opencmNodes.get(n),cceEnvAlias);
					}
				}
			}
			
		} else if (action.equals("generateNode")) {
			Node newNode = nodes.getNode(node);
			cceUtils.deleteNode(newNode.getNode_name());
			RuntimeComponent spmRuntime = newNode.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
			String spmUrl = spmRuntime.getProtocol() + "://" + newNode.getHostname() + ":" + spmRuntime.getPort();
			cceUtils.createNode(newNode.getNode_name(), spmUrl);
			String cceEnvAlias = newNode.getEnvironment();
			
			if (opencmConfig.getCce_mgmt_prepend_sequence()) {
				if (opencmConfig.getCce_mgmt_append_layer()) {
					cceEnvAlias = cceEnvAlias + "-" + newNode.getAssertion_group();
				}
			}
			cceUtils.assignNodeToEnv(cceEnvAlias, newNode.getNode_name());
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CCE nodes management - Process Completed ...... ");
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static void generateNode(Configuration opencmConfig, CceUtils cceUtils, Node opencmNode, String envAlias) {
		if (!opencmNode.getNode_name().equals(opencmConfig.getCce_mgmt_node())) {
			cceUtils.deleteNode(opencmNode.getNode_name());
		}
		RuntimeComponent spmRuntime = opencmNode.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
		String spmUrl = spmRuntime.getProtocol() + "://" + opencmNode.getHostname() + ":" + spmRuntime.getPort();
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"   - Creating Node :: " + opencmNode.getNode_name());
		if (opencmNode.getNode_name().equals(opencmConfig.getCce_mgmt_node())) {
			// Update the local CCE node alias to match the OpenCM CCE node alias
			cceUtils.updateNodeName("local", opencmNode.getNode_name());
			cceUtils.assignNodeToEnv(envAlias, "local");
		} else {
			cceUtils.createNode(opencmNode.getNode_name(), spmUrl);
			cceUtils.assignNodeToEnv(envAlias, opencmNode.getNode_name());
		}
		
	}
	// --- <<IS-END-SHARED>> ---
}

