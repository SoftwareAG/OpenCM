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
import org.opencm.inventory.*;
import org.opencm.repository.util.RepoUtils;
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




	public static final void generateFixScript (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(generateFixScript)>> ---
		// @sigtype java 3.5
		// [i] field:0:required node
				
		IDataCursor pipelineCursor = pipeline.getCursor(); 
		String	stNode = IDataUtil.getString( pipelineCursor, "node" );
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Fix Script Generation Process Started based on " + stNode);
		
		// --------------------------------------------------------------------
		// Check arguments
		// --------------------------------------------------------------------
		if (stNode == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"cce :: generateFixScript :: No node specified .. ");
			return;
		}
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"cce :: generateFixScript :: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("OpenCM.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"cce :: generateFixScript :: " + ex.getMessage());
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
		
		Installation opencmNode = inv.getInstallation(stNode);
		if (opencmNode == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"cce :: generateFixScript :: Node " + stNode + " is not defined... ");
			return;
		}
		
		// Set CCE properties
		Installation cceNode = inv.getInstallation(opencmConfig.getCce_mgmt_node());
		if (cceNode == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"cce :: generateFixScript :: No Command Central Node defined: " + opencmConfig.getCce_mgmt_node());
		}
		
		Server cceServer = inv.getNodeServer(cceNode.getName());
		
		RuntimeComponent cceRuntimeComponent = cceNode.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_CCE);
		if (cceRuntimeComponent == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"cce :: generateFixScript :: No CCE Runtime Component defined for " + RuntimeComponent.RUNTIME_COMPONENT_NAME_CCE);
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"cce :: generateFixScript :: Starting Process ...... ");
		
		// ------------------------------------------------------
		// Retrieve fix list for current node
		// ------------------------------------------------------
		LinkedList<String> fixList = RepoUtils.getFixList(opencmConfig, opencmNode);
		if ((fixList == null) || fixList.isEmpty()) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"cce :: generateFixScript :: No Fixes found ... exiting. ");
			return;
		}
		StringBuffer sbFixList = new StringBuffer();
		for (int i = 0; i < fixList.size(); i++) {
			sbFixList.append(fixList.get(i));
			if (i+1 < fixList.size()) {
				sbFixList.append(",");
			}
		}
		
		// ------------------------------------------------------
		// Create Fix Script Content
		// ------------------------------------------------------
		StringBuffer sb = new StringBuffer();
		sb.append("@ECHO OFF" + System.lineSeparator());
		sb.append("rem ############################################################################" + System.lineSeparator());
		sb.append("rem" + System.lineSeparator()); 
		sb.append("rem  Auto-generated windows batch script for generating Fix Script ... " + System.lineSeparator());
		sb.append("rem" + System.lineSeparator());  
		sb.append("rem  Fix list based on node: " + opencmNode.getName() + System.lineSeparator());  
		sb.append("rem" + System.lineSeparator());  
		sb.append("rem  Fix List:" + System.lineSeparator());  
		for (int i = 0; i < fixList.size(); i++) {
			sb.append("rem       " + fixList.get(i) + System.lineSeparator());  
		}
		sb.append("rem" + System.lineSeparator());  
		sb.append("rem ############################################################################" + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Set Appropriate Script Directory: " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("SET SCRIPT_DIR=" + opencmConfig.getOutput_dir() + File.separator + Configuration.OPENCM_RESULTS_DIR_CLI + System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Set Appropriate CCE Install Directory: " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		String installDir = RepoUtils.getInstallDir(opencmConfig,cceNode); 
		if (installDir == null) {
			installDir = "D:\\SoftwareAG\\CCE"; 
		}
		sb.append("SET CCE_INSTALL_DIR=" + installDir + System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Set Appropriate CCE Username: " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("SET CCE_USERNAME=" + cceRuntimeComponent.getUsername() + System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Set Appropriate CCE URL: " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		String cceURL = cceRuntimeComponent.getProtocol() + "://" + cceServer.getName() + ":" + cceRuntimeComponent.getPort();
		sb.append("SET CCE_URL=" + cceURL + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Set Appropriate Fix Repository Name: " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("SET FIX_REPO=REPO_FIXES_V101_MIRROR" + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Change Directory: " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("cd %CCE_INSTALL_DIR%\\CommandCentral\\client\\bin" + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Install Fixes: " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("call .\\sagcc exec provisioning fixes " + opencmNode.getName() + " %FIX_REPO% install artifacts=" + sbFixList.toString() + " -s %CCE_URL% -u %CCE_USERNAME%" + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("rem      Change Directory: " + System.lineSeparator());
		sb.append("rem --------------------------------------------------------------------" + System.lineSeparator());
		sb.append("cd %SCRIPT_DIR%" + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		sb.append(System.lineSeparator());
		sb.append("echo \"------------------------------------------------------ \"" + System.lineSeparator());
		sb.append("echo \"--         Fix Script Submitted ...                 -- \"" + System.lineSeparator());
		sb.append("echo \"------------------------------------------------------ \"" + System.lineSeparator());
		
		
		// ------------------------------------------------------
		// Write Fix Script Content to file
		// ------------------------------------------------------
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(opencmConfig.getOutput_dir() + File.separator + Configuration.OPENCM_RESULTS_DIR_CLI + File.separator + "FixScript_" + opencmNode.getName() + ".bat"));
		    bwr.write(sb.toString());
		   
		    //flush the stream
		    bwr.flush();
		   
		    //close the stream
		    bwr.close();
		    
		} catch (IOException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CLI Generation - Exception: " + ex.toString());
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," -------------- Fix Script Generation Process Completed   ------------   ");
			
		// --- <<IS-END>> ---

                
	}
}

