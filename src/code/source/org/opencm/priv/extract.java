package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.File;
import org.opencm.configuration.Configuration;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.InventoryInstallation;
import org.opencm.extract.ExtractRuntimes;
import org.opencm.extract.ExtractConfiguration;
import org.opencm.util.LogUtils;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
// --- <<IS-END-IMPORTS>> ---

public final class extract

{
	// ---( internal utility methods )---

	final static extract _instance = new extract();

	static extract _newInstance() { return new extract(); }

	static extract _cast(Object o) { return (extract)o; }

	// ---( server methods )---




	public static final void getConfigurationByJson (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfigurationByJson)>> ---
		// @sigtype java 3.5
		// [i] field:0:required json
		// [o] object:0:required ExtractConfiguration
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stJson = IDataUtil.getString( pipelineCursor, "json" ); 
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Configuration Object by Json"); 
		
		ExtractConfiguration extConfig = ExtractConfiguration.instantiate(stJson); 
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "ExtractConfiguration", extConfig );
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Configuration Object by Json");
					
		// --- <<IS-END>> ---

                
	}



	public static final void getConfigurationByTemplate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfigurationByTemplate)>> ---
		// @sigtype java 3.5
		// [i] field:0:required templateName
		// [o] object:0:required ExtractConfiguration
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stTemplateName = IDataUtil.getString( pipelineCursor, "templateName" ); 
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Configuration Object by Template");
		
		File templateFile = new File(ExtractConfiguration.EXTRACT_CONFIG_DIRECTORY.getPath() + File.separator + stTemplateName + ".json");
		
		if (!templateFile.exists()) { 
			LogUtils.logError("Get Configuration: No file exists :: " + templateFile.getPath()); 
			return;
		}
			
		ExtractConfiguration extConfig = ExtractConfiguration.instantiate(templateFile); 
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "ExtractConfiguration", extConfig );
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Configuration Object by Template");
					
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
		
		LogUtils.logDebug("Get Template"); 
		
		String msg = "Success";
		int rc = 0;
		
		String json = null;
		File templateFile = new File(ExtractConfiguration.EXTRACT_CONFIG_DIRECTORY.getPath() + File.separator + stTemplateName + ".json");
		if (!templateFile.exists()) {
			LogUtils.logError("Get Template: No file exists :: " + templateFile.getPath());
			msg = "Failure: No template file exists.";
			rc = -1;
		} else { 
			ExtractConfiguration extConfig = ExtractConfiguration.instantiate(templateFile);
			if (extConfig == null) {
				rc = -1;
				msg = "Failure getting configuration ";
			} else {
				json = JsonUtils.convertJavaObjectToJson(extConfig);
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
		LogUtils.logDebug("getTemplate : " + jResp);
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
		
		ExtractConfiguration.checkDirectory();
		if (!ExtractConfiguration.EXTRACT_CONFIG_DIRECTORY.exists()) {
			msg = "Failure: Unable to get templates directory.";
			rc = -1;
		} else {
			try {
				File [] fDirectories = ExtractConfiguration.EXTRACT_CONFIG_DIRECTORY.listFiles();
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



	public static final void performExtract (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(performExtract)>> ---
		// @sigtype java 3.5
		// [i] object:0:required ExtractConfiguration
		// [i] field:0:required keepassPassword
		// [i] field:0:required vaultToken
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		ExtractConfiguration extConfig = (ExtractConfiguration) IDataUtil.get( pipelineCursor, "ExtractConfiguration" );
		String keepassPassword = IDataUtil.getString( pipelineCursor, "keepassPassword" );
		String vaultToken = IDataUtil.getString( pipelineCursor, "vaultToken" );
		pipelineCursor.destroy(); 
		
		if (extConfig.getPaths().isEmpty()) { 
			LogUtils.logError("Extraction: No paths received "); 
			return;
		}
		
		if ((keepassPassword == null) || (vaultToken == null)) { 
			LogUtils.logError("Extraction: No secrets password received "); 
			return;
		}
		
		LogUtils.logInfo("Extraction Started");
		
		// --------------------------------------------------------------------
		// Read in Inventory 
		// --------------------------------------------------------------------
		Inventory inv = Inventory.getInstance();
		
		// -----------------------------------------------------
		// Get all Installations from this inventory based on paths
		// -----------------------------------------------------
		LinkedList<InventoryInstallation> extractInstallations = new LinkedList<InventoryInstallation>();
		Inventory.getInstallations(inv.getRootGroup(), extractInstallations, extConfig.getPaths());
		LogUtils.logDebug("Extraction: Installations to be extracted: " + extractInstallations.size());
		for (InventoryInstallation inst : extractInstallations) {
			LogUtils.logInfo("Extraction: Processing: " + inst.getName()); 
			new ExtractRuntimes(inst, keepassPassword, vaultToken); 
		}
		
		LogUtils.logInfo("Extraction Completed");
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void saveTemplate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(saveTemplate)>> ---
		// @sigtype java 3.5
		// [i] object:0:required ExtractConfiguration
		// [o] field:0:required json_response
		IDataCursor pipelineCursor = pipeline.getCursor();
		ExtractConfiguration extConfig = (ExtractConfiguration) IDataUtil.get( pipelineCursor, "ExtractConfiguration" );
		
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
		ExtractConfiguration.checkDirectory();
		if (!ExtractConfiguration.EXTRACT_CONFIG_DIRECTORY.exists()) {
			rc = -1;
			msg = "Unable to get directory " + ExtractConfiguration.EXTRACT_CONFIG_DIRECTORY.getPath();
			
		} else {
			extConfig.save();
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

