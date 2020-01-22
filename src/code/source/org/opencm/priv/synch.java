package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.LinkedList;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPReply;
import org.opencm.configuration.Configuration;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.Server;
import org.opencm.inventory.Installation;
import org.opencm.inventory.RuntimeComponent;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.util.LogUtils;
import org.opencm.util.ZipUtils;
import org.opencm.util.FileUtils;
import org.opencm.security.KeyUtils;
// --- <<IS-END-IMPORTS>> ---

public final class synch

{
	// ---( internal utility methods )---

	final static synch _instance = new synch();

	static synch _newInstance() { return new synch(); }

	static synch _cast(Object o) { return (synch)o; }

	// ---( server methods )---




	public static final void receive (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(receive)>> ---
		// @sigtype java 3.5
		// [i] field:0:required node
		// [i] object:0:required contentStream
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stNodeName = IDataUtil.getString( pipelineCursor, "node" );
		InputStream ftpis = (InputStream) IDataUtil.get( pipelineCursor, "contentStream" );
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
		
		if (stNodeName == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"synch:receive --> No node name passed ");
			return;
		} 
		if (ftpis == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"synch:receive --> No ftpis stream passed ");
			return; 
		} 
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Synch:receive : Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("org.opencm.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM synch:receive :: " + ex.getMessage());
			}
		}
		
		File runtimeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR);
		File nodeDir = new File(runtimeDir.getPath() + File.separator + stNodeName);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"synch:receive --> Receive Process Started for " + nodeDir.getName() + " -- ");
		
		if (ftpis != null) {
			// --------------------------------------------------------------------
			// Remove existing runtime directory
			// --------------------------------------------------------------------
			if (nodeDir.exists()) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"synch:receive --> Removing old Runtime dir: " + nodeDir.getName());
				FileUtils.removeDirectory(nodeDir.getPath());
			} 
		
			// -------------------------------------------------------------------- 
			// Extracting content stream into target
			// --------------------------------------------------------------------
		    try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"synch:receive --> Extracting into : " + nodeDir.getPath());
				ZipUtils.decompress(ftpis, runtimeDir);
				ftpis.close();
		    } catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Synch Receive Decompress Exception :: " + ex.getMessage());
		    }
		    
		
		}
			
		// --- <<IS-END>> ---

                
	}



	public static final void send (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(send)>> ---
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Synchronization Transfer Process Started... ========== ");
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Synch:send : Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("org.opencm.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM synch:send :: " + ex.getMessage());
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
		
		// --------------------------------------------------------------------
		// Locate Synch Runtime Component for target OpenCM
		// --------------------------------------------------------------------
		Installation synchNode = inv.getInstallation(opencmConfig.getSynch_target_opencm_node());
		if (synchNode == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Synch - Send :: No OpenCM Target Synch Node defined.");
			return;
		}
		RuntimeComponent synchRuntimeComponent = synchNode.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SYNCH);
		if (synchRuntimeComponent == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Synch - Send :: No OpenCM Synch Runtime Component for FTPS defined.");
			return;
		}
		
		// --------------------------------------------------------------------
		// Identify Synch Target server
		// --------------------------------------------------------------------
		Server serverTarget = inv.getNodeServer(synchNode.getName());
		
		FTPSClient ftp = new FTPSClient();
		
		// --------------------------------------------------------------------
		// Set Timeout
		// --------------------------------------------------------------------
		int tout = 3000;
		String stTimeout = opencmConfig.getSynch_ftps_timeout_ms();
		if (stTimeout != null) {
			tout = new Integer(stTimeout).intValue();
		}
		ftp.setConnectTimeout(tout);
		
		try {
			// --------------------------------------------------------------------
			// Open FTP Connection to target
			// --------------------------------------------------------------------
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Opening FTPS Connection to : " + serverTarget.getName() + ":" + synchRuntimeComponent.getPort());
			ftp.connect(serverTarget.getName(), new Integer(synchRuntimeComponent.getPort()).intValue());
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,ftp.getReplyString());
		
			// ---------------------------------
			// Server connection
			// ---------------------------------
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"FTPS Refused Connection : Reply Code = " + reply + " - " + ftp.getReplyString());
				ftp.disconnect();
				return; 
			}
			
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"FTPS Connection to " + serverTarget.getName() + " successful.");
			
			// ---------------------------------
			// Login
			// ---------------------------------
			ftp.login(synchRuntimeComponent.getUsername(), synchRuntimeComponent.getDecryptedPassword());
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"FTPS Login Refused : Reply Code = " + reply + " - " + ftp.getReplyString());
				ftp.disconnect();
				return;
			}
			
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"FTPS Login with " + synchRuntimeComponent.getUsername() + " successful.");
			
			ftp.setBufferSize(1000);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();
			ftp.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
			
			// --------------------------------------------------------------------
			// Change directory to target receive service
			// --------------------------------------------------------------------
			if (!ftp.changeWorkingDirectory("/ns/OpenCM/pub/synch/receive")) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Unable to change into /ns/OpenCM/pub/synch/receive directory ... Exiting.");
				ftp.disconnect();
				return;
			}
		
			String runtimeDir =  opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR;
		
			// --------------------------------------------------------------------
			// Define which installations to synch 
			// --------------------------------------------------------------------
			LinkedList<Installation> installations = inv.createInventory(opencmConfig, opencmConfig.getSynch()).getAllInstallations();
		
			// --------------------------------------------------------------------
			// Based on defined nodes to synch, loop through each node
			// --------------------------------------------------------------------
			for (int i = 0; i < installations.size(); i++) {
				Installation installation = installations.get(i);
		
				File nodeDir = new File(runtimeDir + File.separator + installation.getName());
				if (!nodeDir.exists() || !nodeDir.isDirectory()) {
					continue;
				}
				
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," --> Zipping and Sending Nodes Extraction :: " + installation.getName());
				
				// ------------------------------------------------------
				// Zip up Nodes directory
				// ------------------------------------------------------
				ByteArrayInputStream bais = new ByteArrayInputStream(ZipUtils.compress(nodeDir).toByteArray());
				
				// ------------------------------------------------------
				// Send node zip file
				// ------------------------------------------------------
			    try {
				    if (!ftp.storeFile(installation.getName(), bais)) {
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING,"Unable to send and store the zip for " + installation.getName() + ":: "  + ftp.getReplyString());
				    }
			    } catch (Exception ex) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"FTP storeFile Exception :: " + ex.getMessage());
			    }
			    
			    bais.close();
				
			}
		
			
			ftp.logout();
		
		} catch (IOException e) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"FTPS Connection Exception: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					// 
				}
			}
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Synchronization Transfer Process Completed... ========== ");
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static LinkedList<Installation> addInstallation(LinkedList<Installation> nodeList, Installation inst) {
		if ((inst != null) && !nodeList.contains(inst)) {
			nodeList.add(inst);
		}
		return nodeList;
	}
	private static LinkedList<Installation> addInstallations(LinkedList<Installation> nodeList, LinkedList<Installation> toAdd) {
		for (int i = 0; i < toAdd.size(); i++) {
			Installation inst = toAdd.get(i);
			if (!nodeList.contains(inst)) {
				nodeList.add(inst);
			}
		}
		return nodeList;
	}
		
	// --- <<IS-END-SHARED>> ---
}

