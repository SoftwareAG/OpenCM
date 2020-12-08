package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import org.opencm.cce.configuration.CceConfiguration;
import org.opencm.configuration.Configuration;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
// --- <<IS-END-IMPORTS>> ---

public final class cce

{
	// ---( internal utility methods )---

	final static cce _instance = new cce();

	static cce _newInstance() { return new cce(); }

	static cce _cast(Object o) { return (cce)o; }

	// ---( server methods )---




	public static final void getConfigurationByJson (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfigurationByJson)>> ---
		// @sigtype java 3.5
		// [i] field:0:required json
		// [o] object:0:required CceConfiguration
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stJson = IDataUtil.getString( pipelineCursor, "json" ); 
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Configuration Object by Json"); 
		
		CceConfiguration cceConfig = CceConfiguration.instantiate(stJson); 
		
		// pipeline 
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "CceConfiguration", cceConfig );
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
		// [o] object:0:required CceConfiguration
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor(); 
		String	stTemplateName = IDataUtil.getString( pipelineCursor, "templateName" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Configuration Object from Template");
		
		File templateFile = new File(CceConfiguration.CCE_CONFIG_DIRECTORY.getPath() + File.separator + stTemplateName + ".json");
		
		if (!templateFile.exists()) {
			LogUtils.logError("Get CceConfiguration: No file exists :: " + templateFile.getPath());
			return;
		}
			
		CceConfiguration cceConfig = CceConfiguration.instantiate(templateFile); 
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "CceConfiguration", cceConfig );
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Configuration Object from Template");
					
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
		File templateFile = new File(CceConfiguration.CCE_CONFIG_DIRECTORY.getPath() + File.separator + stTemplateName + ".json");
		if (!templateFile.exists()) {
			LogUtils.logError("Get Template: No file exists :: " + templateFile.getPath());
			msg = "Failure: No template file exists.";
			rc = -1;
		} else { 
			CceConfiguration cceConfig = CceConfiguration.instantiate(templateFile);
			if (cceConfig == null) {
				rc = -1;
				msg = "Failure getting configuration ";
			} else {
				json = JsonUtils.convertJavaObjectToJson(cceConfig);
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
		
		LogUtils.logDebug("Got Template " + stTemplateName);
					
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
		
		CceConfiguration.checkDirectory();
		if (!CceConfiguration.CCE_CONFIG_DIRECTORY.exists()) {
			msg = "Failure: Unable to get templates directory.";
			rc = -1;
		} else {
			try {
				File [] fDirectories = CceConfiguration.CCE_CONFIG_DIRECTORY.listFiles();
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



	public static final void saveTemplate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(saveTemplate)>> ---
		// @sigtype java 3.5
		// [i] object:0:required CceConfiguration
		// [o] field:0:required json_response
		IDataCursor pipelineCursor = pipeline.getCursor();
		CceConfiguration cceConfig = (CceConfiguration) IDataUtil.get( pipelineCursor, "CceConfiguration" );
		
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
		CceConfiguration.checkDirectory();
		if (!CceConfiguration.CCE_CONFIG_DIRECTORY.exists()) {
			rc = -1;
			msg = "Unable to get directory " + CceConfiguration.CCE_CONFIG_DIRECTORY.getPath();
			
		} else {
			cceConfig.save();
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
}

