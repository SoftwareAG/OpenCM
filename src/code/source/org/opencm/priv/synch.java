package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.InputStream;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.opencm.configuration.Configuration;
import org.opencm.synch.SynchConfiguration;
import org.opencm.synch.SynchUtils;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.InventoryInstallation;
import org.opencm.util.LogUtils;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.Cache;
// --- <<IS-END-IMPORTS>> ---

public final class synch

{
	// ---( internal utility methods )---

	final static synch _instance = new synch();

	static synch _newInstance() { return new synch(); }

	static synch _cast(Object o) { return (synch)o; }

	// ---( server methods )---




	public static final void getConfigurationByJson (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfigurationByJson)>> ---
		// @sigtype java 3.5
		// [i] field:0:required json
		// [o] object:0:required SynchConfiguration
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stJson = IDataUtil.getString( pipelineCursor, "json" ); 
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Configuration Object by Json"); 
		
		SynchConfiguration synchConfig = SynchConfiguration.instantiate(stJson); 
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "SynchConfiguration", synchConfig );
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Configuration Object"); 
			
		// --- <<IS-END>> ---

                
	}



	public static final void getConfigurationByTemplate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfigurationByTemplate)>> ---
		// @sigtype java 3.5
		// [i] field:0:required templateName
		// [o] object:0:required SynchConfiguration
		// pipeline 
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stTemplateName = IDataUtil.getString( pipelineCursor, "templateName" );
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Get Configuration Object from Template"); 
		
		File templateFile = new File(SynchConfiguration.SYNCH_CONFIG_DIRECTORY.getPath() + File.separator + stTemplateName + ".json");
		
		if (!templateFile.exists()) {
			LogUtils.logError("Get Configuration: No file exists :: " + templateFile.getPath());
			return;
		}
			
		SynchConfiguration synchConfig = SynchConfiguration.instantiate(templateFile); 
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "SynchConfiguration", synchConfig );
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Configuration Object from Template"); 
					
		// --- <<IS-END>> ---

                
	}



	public static final void getTargets (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getTargets)>> ---
		// @sigtype java 3.5
		// [o] field:0:required json_response
		LogUtils.logDebug("Get Targets");
		
		String msg = "Success";
		int rc = 0;
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.getInstance();
		if (inv == null) {
			msg = "Inventory not available.";
			rc = -1;
			LogUtils.logError("getTargets: Inventory not available. ");
		}
		
		// -----------------------------------------------------
		// Get targets
		// -----------------------------------------------------
		LinkedList<InventoryInstallation> synchInstallations = new LinkedList<InventoryInstallation>();
		Inventory.getSynchTargets(inv.getRootGroup(), synchInstallations);
		if ((synchInstallations == null) || (synchInstallations.size() == 0)) {
			msg = "No synch targets available.";
			rc = -1;
			LogUtils.logError("No synch targets available");
		}
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		String json = JsonUtils.convertJavaObjectToJson(synchInstallations);
		String jResp = JsonUtils.createJsonField("rc", new Integer(rc).toString());
		if (rc < 0) {
			jResp = JsonUtils.addField(jResp,"msg",msg);
		} else {
			jResp = JsonUtils.addField(jResp,"content", json);
		}
		
		IDataUtil.put( pipelineCursor, "json_response", jResp);
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Got Targets");
			
		// --- <<IS-END>> ---

                
	}



	public static final void getTemplate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getTemplate)>> ---
		// @sigtype java 3.5
		// [i] field:0:required templateName
		// [o] field:0:required json_response
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stTemplateName = IDataUtil.getString( pipelineCursor, "templateName" );
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Get Template " + stTemplateName);
		
		String msg = "Success"; 
		int rc = 0;
		
		String json = null;
		File templateFile = new File(SynchConfiguration.SYNCH_CONFIG_DIRECTORY.getPath() + File.separator + stTemplateName + ".json");
		if (!templateFile.exists()) {
			LogUtils.logError("Get Template: No file exists :: " + templateFile.getPath());
			msg = "Failure: No template file exists.";
			rc = -1;
		} else { 
			SynchConfiguration synchConfig = SynchConfiguration.instantiate(templateFile);
			if (synchConfig == null) {
				rc = -1;
				msg = "Failure getting configuration ";
			} else {
				json = JsonUtils.convertJavaObjectToJson(synchConfig);
			}
		}
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		
		String jResp = JsonUtils.createJsonField("rc", new Integer(rc).toString());
		if (rc < 0) {
			jResp = JsonUtils.addField(jResp,"msg",msg);
		} else {
			jResp = JsonUtils.addField(jResp,"content", json);
		}
		IDataUtil.put( pipelineCursor2, "json_response", jResp);
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Template");
					
		// --- <<IS-END>> ---

                
	}



	public static final void getTemplates (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getTemplates)>> ---
		// @sigtype java 3.5
		// [o] field:0:required json_response
		LogUtils.logDebug("Get Templates"); 
		
		String msg = "Success"; 
		int rc = 0;
		
		LinkedList<String> lTemplates = new LinkedList<String>();
		
		SynchConfiguration.checkDirectory();
		if (!SynchConfiguration.SYNCH_CONFIG_DIRECTORY.exists()) {
			msg = "Failure: Unable to get templates directory.";
			rc = -1;
		} else {
			try {
				File [] fDirectories = SynchConfiguration.SYNCH_CONFIG_DIRECTORY.listFiles();
				for(File path : fDirectories) {
					if ((path.isFile() && path.getName().endsWith(".json"))) {
						lTemplates.add(path.getName().substring(0,path.getName().lastIndexOf(".")));
					}
				}
			} catch (Exception ex) {
				LogUtils.logError("getTemplates: Exception: " + ex.getMessage());
			}
		}
		
		String json = JsonUtils.convertJavaObjectToJson(lTemplates);
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		String jResp = JsonUtils.createJsonField("rc", new Integer(rc).toString());
		if (rc < 0) {
			jResp = JsonUtils.addField(jResp,"msg",msg);
		} else {
			jResp = JsonUtils.addField(jResp,"content", json);
		}
		IDataUtil.put( pipelineCursor2, "json_response", jResp);
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Templates");
			
		// --- <<IS-END>> ---

                
	}



	public static final void performSynch (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(performSynch)>> ---
		// @sigtype java 3.5
		// [i] object:0:required SynchConfiguration
		// [i] field:0:required keepassPassword
		// [i] field:0:required vaultToken
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		SynchConfiguration synchConfig = (SynchConfiguration) IDataUtil.get( pipelineCursor, "SynchConfiguration" );
		String keepassPassword = IDataUtil.getString( pipelineCursor, "keepassPassword" );
		String vaultToken = IDataUtil.getString( pipelineCursor, "vaultToken" );
		pipelineCursor.destroy(); 
		
		LogUtils.logInfo("Synch Send ...");  
		
		if (synchConfig.getPaths().isEmpty()) {
			LogUtils.logError("performSynch: No paths passed "); 
			return;
		}
		
		if ((keepassPassword == null) || (vaultToken == null)) { 
			LogUtils.logError("performSynch: No secrets password received "); 
			return;
		}
		
		SynchUtils.synchRepositories(synchConfig, keepassPassword, vaultToken); 
		
		LogUtils.logInfo("Synch Sent");
			
		// --- <<IS-END>> ---

                
	}



	public static final void receive (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(receive)>> ---
		// @sigtype java 3.5
		// [i] field:0:required repoFilename
		// [i] object:0:required contentStream
		IDataCursor pipelineCursor = pipeline.getCursor();
		String repoFilename = IDataUtil.getString( pipelineCursor, "repoFilename" );
		InputStream ftpis = (InputStream) IDataUtil.get( pipelineCursor, "contentStream" );
		pipelineCursor.destroy(); 
		 
		LogUtils.logInfo("Synch Receive: " + repoFilename);
		
		if (repoFilename == null) {
			LogUtils.logError("Synch receive :: No filename passed ");
			return; 
		} 
		if (ftpis == null) {
			LogUtils.logError("Synch receive :: No ftpis stream passed ");
			return; 
		} 
		
		File repoDir = new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_REPOSITORY);
		File repoInstallation = new File(repoDir.getPath() + File.separator + repoFilename);
		LogUtils.logDebug("Synch receive :: Receiving: " + repoInstallation.getName());
		
		if (ftpis != null) { 
			// -------------------------------------------------------------------- 
			// Extracting content stream into target
			// --------------------------------------------------------------------
		    try {
				LogUtils.logDebug("Synch receive ::  Extracting into : " + repoDir.getPath());
				SynchUtils.decompress(ftpis, repoDir);
				ftpis.close();
		    } catch (Exception ex) {
				LogUtils.logError("Synch receive :: Decompress Exception :: " + ex.getMessage());
		    }
		
		}
		
		LogUtils.logDebug("Synch Received");
			
		// --- <<IS-END>> ---

                
	}



	public static final void saveTemplate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(saveTemplate)>> ---
		// @sigtype java 3.5
		// [i] object:0:required SynchConfiguration
		// [o] field:0:required json_response
		IDataCursor pipelineCursor = pipeline.getCursor();
		SynchConfiguration synchConfig = (SynchConfiguration) IDataUtil.get( pipelineCursor, "SynchConfiguration" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Save Template"); 
		
		// -----------------------------------------------------
		// Validate
		// -----------------------------------------------------
		String msg = "Success"; 
		int rc = 0;
		
		// -----------------------------------------------------
		// Save template
		// -----------------------------------------------------
		SynchConfiguration.checkDirectory();
		if (!SynchConfiguration.SYNCH_CONFIG_DIRECTORY.exists()) {
			rc = -1;
			msg = "Unable to get directory " + SynchConfiguration.SYNCH_CONFIG_DIRECTORY.getPath();
			
		} else {
			synchConfig.save();
			LogUtils.logDebug("Template Saved");
		}
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		
		String jResp = JsonUtils.createJsonField("msg",msg);
		jResp = JsonUtils.addField(jResp,"rc", new Integer(rc).toString());
		
		IDataUtil.put( pipelineCursor2, "json_response", jResp);
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Saved Template");
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---

	
	// --- <<IS-END-SHARED>> ---
}

