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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.RuntimeComponent;
import org.opencm.inventory.Installation;
import org.opencm.util.HttpClient;
import org.opencm.util.LogUtils;
import org.opencm.security.KeyUtils;
import org.opencm.extract.spm.SpmOps;
// --- <<IS-END-IMPORTS>> ---

public final class extract

{
	// ---( internal utility methods )---

	final static extract _instance = new extract();

	static extract _newInstance() { return new extract(); }

	static extract _cast(Object o) { return (extract)o; }

	// ---( server methods )---




	public static final void createConnectivityReport (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(createConnectivityReport)>> ---
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Connectivity Report Process Started... ========== ");
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Connectivity Report : Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("org.opencm.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Connectivity Report :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Construct Current date time for report
		// --------------------------------------------------------------------
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'_'HH-mm");
		Date cDate = new Date(System.currentTimeMillis());
		String cDateTime = formatter.format(cDate);
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Ensure Encrypted passwords ...
		// --------------------------------------------------------------------
		inv.ensureEncryptedPasswords(opencmConfig);
		
		// --------------------------------------------------------------------
		// Define which nodes to extract 
		// --------------------------------------------------------------------
		LinkedList<Installation> installations = new LinkedList<Installation>();
		
		installations = inv.createInventory(opencmConfig, opencmConfig.getExtract()).getAllInstallations();
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Connectivity Report: Installations: " + installations.size());
		
		// ------------------------------------------------------
		// Create Fix Script Content
		// ------------------------------------------------------
		StringBuffer sb = new StringBuffer();
		sb.append("------------------------------------------------------" + System.lineSeparator());
		sb.append(" Connectivity Report :: " + cDateTime + System.lineSeparator());
		sb.append("------------------------------------------------------" + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		HttpClient client = new HttpClient();
		// --------------------------------------------------------------------
		// Check All nodes
		// --------------------------------------------------------------------
		for (int i = 0; i < installations.size(); i++) {
			Installation installation = installations.get(i);
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Verifying Installation: " + installation.getName());
			RuntimeComponent spmRC = installation.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
			if (spmRC == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"   --- No SPM defined ... ");
				continue;
			}
			String spmRequestURL = spmRC.getProtocol() + "://" + inv.getNodeServer(installation.getName()).getName() + ":" + spmRC.getPort() + "/spm/inventory/platform";
			client.setURL(spmRequestURL);
			client.setCredentials(spmRC.getUsername(), spmRC.getDecryptedPassword());
			try {
				client.get();
				if (client.getStatusCode() == 200) {
					sb.append("  SPM OK :: " + installation.getName() + System.lineSeparator());
				} else {
					sb.append("------------------------------------------------------" + System.lineSeparator());
					sb.append("  " + installation.getName() + " SPM --> URL: " + spmRequestURL + System.lineSeparator());
					sb.append("  HTTP Status Code = " + client.getStatusCode() + " (" + client.getResponse() + ")" + System.lineSeparator());
					sb.append("------------------------------------------------------" + System.lineSeparator());
				}
			} catch (ServiceException ex) {
				sb.append("------------------------------------------------------" + System.lineSeparator());
				sb.append("  " + installation.getName() + " SPM --> URL: " + spmRequestURL + System.lineSeparator());
				sb.append("  " + ex.toString() + System.lineSeparator());
				sb.append("------------------------------------------------------" + System.lineSeparator());
			}
			// -----------------------------------------
			// Check Integration Server connectivity
			// -----------------------------------------
			for (int c = 0; c < installation.getRuntimes().size(); c++) {
				RuntimeComponent rc = installation.getRuntimes().get(c);
				if (rc.getName().startsWith("integrationServer-")) {
					String isRequestURL = rc.getProtocol() + "://" + inv.getNodeServer(installation.getName()).getName() + ":" + rc.getPort();
					client.setURL(isRequestURL);
					client.setCredentials(rc.getUsername(), rc.getDecryptedPassword());
					try {
						client.get();
						if (client.getStatusCode() == 200) {
							sb.append("  Integration Server OK :: " + installation.getName() + " IS instance: " + rc.getName() + System.lineSeparator());
						} else {
							sb.append("------------------------------------------------------" + System.lineSeparator());
							sb.append("  " + installation.getName() + " IS instance: " + rc.getName() + " --> URL: " + isRequestURL + System.lineSeparator());
							sb.append("  HTTP Status Code = " + client.getStatusCode() + " (" + client.getResponse() + ")" + System.lineSeparator());
							sb.append("------------------------------------------------------" + System.lineSeparator());
						}
					} catch (ServiceException ex) {
						sb.append("------------------------------------------------------" + System.lineSeparator());
						sb.append("  " + installation.getName() + " IS instance: " + rc.getName() + " URL: " + isRequestURL + System.lineSeparator());
						sb.append("  " + ex.toString() + System.lineSeparator());
						sb.append("------------------------------------------------------" + System.lineSeparator());
					}
				}
				
			}
			
		}
		
		sb.append(System.lineSeparator());
		sb.append("------------------------------------------------------" + System.lineSeparator());
		
		// ------------------------------------------------------
		// Write Fix Script Content to file
		// ------------------------------------------------------
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(opencmConfig.getOutput_dir() + File.separator + "ConnectivityReport_" + cDateTime + ".txt"));
		    bwr.write(sb.toString());
		   
		    //flush the stream
		    bwr.flush();
		   
		    //close the stream
		    bwr.close();
		    
		} catch (IOException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," Connectivity Report :: Exception: " + ex.toString());
		}
		
			
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"--------------   Connectivity Report Process Completed... -------------- ");
			
		// --- <<IS-END>> ---

                
	}



	public static final void run (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(run)>> ---
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Extraction Process Started... ========== ");
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Extraction: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("org.opencm.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM extract:run :: " + ex.getMessage());
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Extraction job parameter: " + stNode);
		
		// --------------------------------------------------------------------
		// Define which nodes to extract 
		// --------------------------------------------------------------------
		LinkedList<Installation> installations = new LinkedList<Installation>();
		
		if (stNode.equals("PROPS")) {
			installations = inv.createInventory(opencmConfig, opencmConfig.getExtract()).getAllInstallations();
		} else {
			// Single node extract
			installations.add(inv.getInstallation(stNode));
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Extraction: Installations to be extracted: " + installations.size());
		
		// --------------------------------------------------------------------
		// Extract All nodes
		// --------------------------------------------------------------------
		for (int i = 0; i < installations.size(); i++) {
			Installation installation = installations.get(i);
			SpmOps spm = new SpmOps(opencmConfig, inv, installation);
			if (spm.getNode() != null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Extraction: Processing Node " + installation.getName());
				spm.extractAll();
				spm.persist(); 
			}
		}
			
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Extraction Process Completed... ========== ");
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	// --- <<IS-END-SHARED>> ---
}

