package OpenCM.priv;

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
import org.opencm.configuration.PkgConfiguration;
import org.opencm.synch.SynchConfig;
import org.opencm.util.LogUtils;
import org.opencm.util.ZipUtils;
import org.opencm.util.FileUtils;
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
		// [i] field:0:required serverName
		// [i] object:0:required contentStream
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stServerName = IDataUtil.getString( pipelineCursor, "serverName" );
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
		
		File runtimeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR);
		File serverDir = new File(runtimeDir.getPath() + File.separator + stServerName);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"synch:receive --> Receive Process Started for " + serverDir.getName() + " -- ");
		
		if (ftpis != null) {
			// --------------------------------------------------------------------
			// Remove existing runtime directory
			// --------------------------------------------------------------------
			if (serverDir.exists()) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"synch:receive --> Removing old Runtime dir: " + serverDir.getName());
				FileUtils.removeDirectory(serverDir.getPath());
			}
		
			// --------------------------------------------------------------------
			// Extracting content stream into target
			// --------------------------------------------------------------------
		    try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"synch:receive --> Extracting into : " + serverDir.getPath());
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
		// [i] field:0:required masterPassword
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stMaster = IDataUtil.getString( pipelineCursor, "masterPassword" );
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Synchronization Transfer Process Started... ========== ");
		
		// --------------------------------------------------------------------
		// Read in OpenCM Synch Properties
		// --------------------------------------------------------------------
		SynchConfig synchConfig = SynchConfig.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Ensure Encrypted password ...
		// --------------------------------------------------------------------
		synchConfig.ensureDecryptedPassword(opencmConfig, stMaster);
		
		FTPSClient ftp = new FTPSClient();
		ftp.setConnectTimeout(synchConfig.getTimeout());
		
		try {
			// --------------------------------------------------------------------
			// Open FTP Connection to target
			// --------------------------------------------------------------------
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Opening FTPS Connection to : " + synchConfig.getRemote_opencm_ftps_server() + ":" + synchConfig.getRemote_opencm_ftps_port());
			ftp.connect(synchConfig.getRemote_opencm_ftps_server(),synchConfig.getPort());
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
			
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"FTPS Connection to " + synchConfig.getRemote_opencm_ftps_server() + " successful.");
			
			// ---------------------------------
			// Login
			// ---------------------------------
			ftp.login(synchConfig.getRemote_opencm_ftps_username(), synchConfig.getDecryptedPassword(stMaster));
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"FTPS Login Refused : Reply Code = " + reply + " - " + ftp.getReplyString());
				ftp.disconnect();
				return;
			}
			
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"FTPS Login with " + synchConfig.getRemote_opencm_ftps_username() + " successful.");
			
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
			// Loop through the runtime server directories
			// --------------------------------------------------------------------
			LinkedList<String> dirs = FileUtils.getSubDirectories(runtimeDir, "*");
			for (int i = 0; i < dirs.size(); i++) {
				File serverDir = new File(runtimeDir + File.separator + dirs.get(i));
				if (!serverDir.exists() || !serverDir.isDirectory()) {
					continue;
				}
				
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," --> Zipping and Sending Server Extraction :: " + serverDir.getName());
				// ------------------------------------------------------
				// Zip up server directory
				// ------------------------------------------------------
				ByteArrayInputStream bais = new ByteArrayInputStream(ZipUtils.compress(serverDir).toByteArray());
				
				// ------------------------------------------------------
				// Send server zip file
				// ------------------------------------------------------
			    try {
				    if (!ftp.storeFile(serverDir.getName(), bais)) {
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING,"Unable to send and store the zip for " + serverDir.getName() + ":: "  + ftp.getReplyString());
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
}

