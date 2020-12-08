package org.opencm.synch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPReply;

import org.opencm.inventory.Inventory;
import org.opencm.inventory.InventoryRuntime;
import org.opencm.inventory.InventoryInstallation;
import org.opencm.secrets.SecretsUtils;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.repository.Repository;
import org.opencm.util.LogUtils;

public class SynchUtils {

    public static final int 	SYNCH_TIMEOUT			= 3000;
    public static final int 	SYNCH_BUFFER_SIZE		= 1000;
    public static final String	SYNCH_TARGET_NAMESPACE	= "/ns/org/opencm/pub/synch/receive";
	
    public static void synchRepositories(SynchConfiguration synchConfig, String keepassPwd, String vaultToken) {
    	
		LogUtils.logTrace("synchRepository -> Process Starting ...");
		
		// --------------------------------------------------------------------
		// Get Secrets Configuration 
		// --------------------------------------------------------------------
		SecretsConfiguration secConfig = SecretsConfiguration.instantiate();
		if (secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL)) {
			secConfig.setKeepassPassword(keepassPwd);
		} else {
			secConfig.setVaultToken(vaultToken);
		}
		
		// --------------------------------------------------------------------
		// Get Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.getInstance();
		
		// --------------------------------------------------------------------
		// Locate Synch Runtime Component for target OpenCM
		// --------------------------------------------------------------------
		LinkedList<InventoryInstallation> synchTargets = new LinkedList<InventoryInstallation>();
		Inventory.getInstallation(inv.getRootGroup(), synchTargets, synchConfig.getTargetPath()); 
		if (synchTargets.size() != 1) {
			LogUtils.logError("synchRepositories - :: No Target Synch Node defined.");
			return;
		}
		InventoryInstallation synchTarget = synchTargets.getFirst();
		
		InventoryRuntime synchRuntime = synchTarget.getRuntime(InventoryRuntime.RUNTIME_NAME_SYNCH);
		if (synchRuntime == null) {
			LogUtils.logError("synchRepositories :: No OpenCM Synch Component for FTPS defined.");
			return;
		}
		
		// -----------------------------------------------------
		// Get all Installations from this inventory
		// -----------------------------------------------------
		LinkedList<InventoryInstallation> synchInstallations = new LinkedList<InventoryInstallation>();
		Inventory.getInstallations(inv.getRootGroup(), synchInstallations, synchConfig.getPaths());
		LogUtils.logInfo("synchRepositories :: Installations to be synchronized: " + synchInstallations.size());
		if (synchInstallations.size() == 0) {
			LogUtils.logWarning("synchRepositories :: No Installations to synch... exiting.");
			return;
		}
		
		// -----------------------------------------------------
		// Prepare sending
		// -----------------------------------------------------
		FTPSClient ftp = new FTPSClient();
		
		// --------------------------------------------------------------------
		// Set Timeout
		// --------------------------------------------------------------------
		ftp.setConnectTimeout(SYNCH_TIMEOUT);
		
		try {
			// --------------------------------------------------------------------
			// Open FTP Connection to target
			// --------------------------------------------------------------------
			LogUtils.logDebug("synchRepository :: Opening Connection to : " + synchTarget.getHostname() + " on port " + synchRuntime.getPort());
			ftp.connect(synchTarget.getHostname(), new Integer(synchRuntime.getPort()).intValue());
			LogUtils.logDebug("synchRepository :: Reply: " + ftp.getReplyString());
		
			// ---------------------------------
			// Server connection
			// ---------------------------------
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				LogUtils.logError("synchRepository :: FTPS Refused Connection : Reply Code = " + reply + " - " + ftp.getReplyString());
				ftp.disconnect();
				return; 
			}
			
			LogUtils.logDebug("synchRepository :: FTPS Connection to " + synchTarget.getHostname() + " successful.");
			
			// ---------------------------------
			// Login
			// ---------------------------------
			ftp.login(synchRuntime.getUsername(), SecretsUtils.getPassword(secConfig, synchRuntime.getUsername(), synchRuntime.getPasswordHandle()));
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				LogUtils.logError("synchRepository :: FTPS Login Refused : Reply Code = " + reply + " - " + ftp.getReplyString());
				ftp.disconnect();
				return;
			}
			
			LogUtils.logDebug("synchRepository :: FTPS Login with " + synchRuntime.getUsername() + " successful.");
			
			ftp.setBufferSize(SYNCH_BUFFER_SIZE);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();
			ftp.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
			
			// --------------------------------------------------------------------
			// Change directory to target receive service
			// --------------------------------------------------------------------
			if (!ftp.changeWorkingDirectory(SYNCH_TARGET_NAMESPACE)) {
				LogUtils.logError("synchRepository :: Unable to change into " + SYNCH_TARGET_NAMESPACE + " directory ... Exiting.");
				ftp.disconnect();
				return;
			}
		
			// --------------------------------------------------------------------
			// Loop through installations
			// --------------------------------------------------------------------
			for (InventoryInstallation installation : synchInstallations) {
		    	File repoFile = Repository.getInstallationRepositoryFile(installation.getPath()); 
				if (!repoFile.exists()) {
					continue;
				}
				// ------------------------------------------------------
				// Zip up Repo Installation file
				// ------------------------------------------------------
				ByteArrayInputStream bais = new ByteArrayInputStream(compress(repoFile).toByteArray());
				
				// ------------------------------------------------------
				// Send repo installation as zip file
				// ------------------------------------------------------
			    try {
				    if (!ftp.storeFile(repoFile.getName(), bais)) {
						LogUtils.logError("synchRepository :: Unable to send and store the zip for " + repoFile.getName() + ":: "  + ftp.getReplyString());
				    }
			    } catch (Exception ex) {
					LogUtils.logError("synchRepository :: FTP storeFile Exception :: " + ex.getMessage());
			    }
			    
			    bais.close();
			}

			ftp.logout();
		
		} catch (IOException e) {
			LogUtils.logError("synchRepository :: FTPS Connection Exception: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
					LogUtils.logError("synchRepository :: IOException: " + ioe.getMessage());
				}
			}
		}
		
    }

    public static ByteArrayOutputStream compress(File infFile) throws IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ZipOutputStream zos = new ZipOutputStream(baos); 
    	try { 
    		
			ZipEntry anEntry = new ZipEntry(infFile.getName()); 
			zos.putNextEntry(anEntry); 
			
    		byte[] readBuffer = new byte[2156]; 
    		int bytesIn = 0; 
    		
			FileInputStream fis = new FileInputStream(infFile); 
			while((bytesIn = fis.read(readBuffer)) != -1) { 
				zos.write(readBuffer, 0, bytesIn); 
			}
			
   			fis.close(); 
    	    zos.close(); 
    	} catch (Exception ex) { 
			LogUtils.logError("Synch zip compress :: Exception: " + ex.toString());
    	}
    	baos.close();
    	return baos;
    }
    
    public static void decompress(InputStream inStream, File outdir) throws IOException {
        byte[] buffer = new byte[1024];
    	
    	ZipInputStream zis = new ZipInputStream(inStream);
    	
    	ZipEntry ze = zis.getNextEntry();
   		
    	while (ze != null) {
    		String fileName = ze.getName();
    		File newFile = new File(outdir + File.separator + fileName);
    		FileOutputStream fos = new FileOutputStream(newFile);             

    		int len;
           
    		while ((len = zis.read(buffer)) > 0) {
    			fos.write(buffer, 0, len);
    		}
    		fos.close();   
    		ze = zis.getNextEntry();
    	}
   	
    	zis.closeEntry();
    	zis.close();
    }

}
