package OpenCM.priv;

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
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.Node;
import org.opencm.configuration.RuntimeComponent;
import org.opencm.extract.cce.CceOps;
import org.opencm.extract.spm.SpmOps;
import org.opencm.util.LogUtils;
import org.opencm.security.KeyUtils;
// --- <<IS-END-IMPORTS>> ---

public final class cli

{
	// ---( internal utility methods )---

	final static cli _instance = new cli();

	static cli _newInstance() { return new cli(); }

	static cli _cast(Object o) { return (cli)o; }

	// ---( server methods )---




	public static final void generate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(generate)>> ---
		// @sigtype java 3.5
				
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
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Generate CLI: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("OpenCM.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM cli:generate :: " + ex.getMessage());
			}
		}
		
		// -------------------------------------------------------------------- 
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig); 
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CLI Generation - Starting Process ...... ");
		
		StringBuffer sb = new StringBuffer();
		sb.append("@ECHO OFF" + System.lineSeparator());
		sb.append("rem ############################################################################" + System.lineSeparator());
		sb.append("rem" + System.lineSeparator()); 
		sb.append("rem  Auto-generated windows batch script for generating environments and nodes" + System.lineSeparator());
		sb.append("rem" + System.lineSeparator());  
		sb.append("rem ############################################################################" + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		// Set CCE properties
		Node cceNode = nodes.getCommandCentralNode();
		if (cceNode == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CLI Generation - No Command Central Node defined.");
		}
		RuntimeComponent cceRuntimeComponent = cceNode.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_CCE);
		if (cceRuntimeComponent == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CLI Generation - No CCE Instance defined for \"local\"");
		}
		String cceURL = cceRuntimeComponent.getProtocol() + "://" + cceNode.getHostname() + ":" + cceRuntimeComponent.getPort();
		String cceUser = cceRuntimeComponent.getUsername();
		
		sb.append("SET CCE_URL=" + cceURL + System.lineSeparator());
		sb.append("SET CCE_USERNAME=" + cceUser + System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Defining CCE Environments .......... " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		LinkedList<String> environments = nodes.getAllEnvironments();
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Generating Environments ...... " + environments.size());
		for (int i = 0; i < environments.size(); i++) {
			String env = environments.get(i);
			sb.append(".\\sagcc create landscape environments alias=" + env + " name=" + env + " description=\"" + env + " Environment\" -s %CCE_URL% -u %CCE_USER%" + System.lineSeparator());
		}
		sb.append(System.lineSeparator());
		sb.append("rem ==========================================================================" + System.lineSeparator());
		sb.append("rem                        Defining CCE Nodes  .......... " + System.lineSeparator());
		sb.append("rem ==========================================================================" + System.lineSeparator());
		
		for (int i = 0; i < environments.size(); i++) {
			String env = environments.get(i);
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  - Processing Nodes for Environment: " + env);
			LinkedList<String> cceAssertionGroups = nodes.getAllAssertionGroupsForEnvironment(env);
			for (int a = 0; a < cceAssertionGroups.size(); a++) {
				String assGroup = cceAssertionGroups.get(a);
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"    Processing Layer: " + assGroup);
				sb.append("rem ----------------------------------------------------" + System.lineSeparator());
				sb.append("rem      [" + env + "] -> " + assGroup + " Layer" + System.lineSeparator());
				sb.append("rem ----------------------------------------------------" + System.lineSeparator());
				sb.append("echo \"Generating nodes for [" + env + "] -> " + assGroup + " Layer .... " + System.lineSeparator());
				LinkedList<Node> cceAssertionGroupNodes = nodes.getNodesByGroupAndEnv(assGroup, env);
				for (int n = 0; n < cceAssertionGroupNodes.size(); n++) {
					Node node = cceAssertionGroupNodes.get(n);
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"      Processing Node: " + node.getNode_name());
					RuntimeComponent nodeRuntimeComponent = node.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
					if (nodeRuntimeComponent == null) {
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CLI Generation - No SPM Instance defined for node " + node.getNode_name());
					}
					String spmURL = nodeRuntimeComponent.getProtocol() + "://" + node.getHostname() + ":" + nodeRuntimeComponent.getPort();
					sb.append(".\\sagcc create landscape nodes alias=" + node.getNode_name() + " url=" + spmURL + " -s %CCE_URL% -u %CCE_USER%" + System.lineSeparator()); 
					sb.append(".\\sagcc add landscape environments " + env + " nodes nodeAlias=" + node.getNode_name() + " -s %CCE_URL% -u %CCE_USER%" + System.lineSeparator()); 
					sb.append(System.lineSeparator());
				}
			}
		} 
		sb.append(System.lineSeparator());
		sb.append("echo \"------------------------------------------------------ " + System.lineSeparator());
		sb.append("echo \"--            Processing Completed                  -- " + System.lineSeparator());
		sb.append("echo \"------------------------------------------------------ " + System.lineSeparator());
		
		
		// Write to File
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(opencmConfig.getOutput_dir() + File.separator + Configuration.OPENCM_RESULTS_DIR_CLI + File.separator + Configuration.OPENCM_OUTPUT_CLI));
		    bwr.write(sb.toString());
		   
		    //flush the stream
		    bwr.flush();
		   
		    //close the stream
		    bwr.close();
		    
		} catch (IOException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CLI Generation - Exception: " + ex.toString());
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CLI Generation - Process Completed ...... ");
			
		// --- <<IS-END>> ---

                
	}
}

