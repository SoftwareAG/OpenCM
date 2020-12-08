package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import freemarker.template.Template;
import org.opencm.audit.*;
import org.opencm.configuration.Configuration;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.secrets.SecretsUtils;
import org.opencm.smtp.SmtpConfiguration;
import org.opencm.freemarker.FMConfiguration;
import org.opencm.inventory.Inventory;
import org.opencm.util.*;
// --- <<IS-END-IMPORTS>> ---

public final class audit

{
	// ---( internal utility methods )---

	final static audit _instance = new audit();

	static audit _newInstance() { return new audit(); }

	static audit _cast(Object o) { return (audit)o; }

	// ---( server methods )---




	public static final void getAuditResultJson (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getAuditResultJson)>> ---
		// @sigtype java 3.5
		// [i] object:0:required AuditConfiguration
		// [o] field:0:required json_response
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		AuditConfiguration auditConfig = (AuditConfiguration) IDataUtil.get( pipelineCursor, "AuditConfiguration" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Run Audit Starting");
		
		String jsonResponse = "";
		
		// -----------------------------------------------------
		// Run Audit
		// -----------------------------------------------------
		AuditResult ar = AuditUtils.performAudit(auditConfig); 
		if (ar != null) {
			jsonResponse = JsonUtils.convertJavaObjectToJson(ar);
			jsonResponse = JsonUtils.addField(jsonResponse,"rc", new Integer(0).toString());
		} else {
			jsonResponse = JsonUtils.createJsonField("msg","Auditing Error - refer to log.");
			jsonResponse = JsonUtils.addField(jsonResponse,"rc", new Integer(-1).toString());
		}
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		LogUtils.logDebug(jsonResponse);
		IDataUtil.put( pipelineCursor2, "json_response", jsonResponse);
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Run Audit Completed");
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void getAuditResultObject (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getAuditResultObject)>> ---
		// @sigtype java 3.5
		// [i] object:0:required AuditConfiguration
		// [o] object:0:required AuditResult
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		AuditConfiguration auditConfig = (AuditConfiguration) IDataUtil.get( pipelineCursor, "AuditConfiguration" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Run Scheduled Audit Starting"); 
		
		// -----------------------------------------------------
		// Run Audit
		// -----------------------------------------------------
		AuditResult ar = AuditUtils.performAudit(auditConfig);
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "AuditResult", ar );
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Run Scheduled Audit Completed");
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void getConfigurationByJson (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfigurationByJson)>> ---
		// @sigtype java 3.5
		// [i] field:0:required json
		// [o] object:0:required AuditConfiguration
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stJson = IDataUtil.getString( pipelineCursor, "json" ); 
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Configuration Object by Json"); 
		
		AuditConfiguration auditConfig = AuditConfiguration.instantiate(stJson);
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "AuditConfiguration", auditConfig );
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
		// [o] object:0:required AuditConfiguration
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor(); 
		String	stTemplateName = IDataUtil.getString( pipelineCursor, "templateName" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Configuration Object from Template");
		
		File templateFile = new File(AuditConfiguration.AUDIT_CONFIG_DIRECTORY.getPath() + File.separator + stTemplateName + ".json");
		
		if (!templateFile.exists()) {
			LogUtils.logError("Get AuditConfiguration: No file exists :: " + templateFile.getPath());
			return;
		}
			
		AuditConfiguration auditConfig = AuditConfiguration.instantiate(templateFile); 
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "AuditConfiguration", auditConfig );
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
		
		LogUtils.logDebug("Get Template"); 
		
		String msg = "Success";  
		int rc = 0;
		
		String json = null;
		
		File templateFile = new File(AuditConfiguration.AUDIT_CONFIG_DIRECTORY.getPath() + File.separator + stTemplateName + ".json");
		if (!templateFile.exists()) {
			LogUtils.logError(" Get Template: No file exists :: " + templateFile.getPath());
			msg = "Failure: No template file exists.";
			rc = -1;
		} else { 
			AuditConfiguration auditConfig = AuditConfiguration.instantiate(templateFile);
			if (auditConfig == null) {
				rc = -1;
				msg = "Failure getting configuration ";
			} else {
				json = JsonUtils.convertJavaObjectToJson(auditConfig);
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
		
		AuditConfiguration.checkDirectory();
		if (!AuditConfiguration.AUDIT_CONFIG_DIRECTORY.exists()) {
			msg = "Failure: Unable to get templates directory.";
			rc = -1;
		} else {
			try {
				File [] fDirectories = AuditConfiguration.AUDIT_CONFIG_DIRECTORY.listFiles();
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
		// [i] object:0:required AuditConfiguration
		// [o] field:0:required json_response
		IDataCursor pipelineCursor = pipeline.getCursor();
		AuditConfiguration auditConfig = (AuditConfiguration) IDataUtil.get( pipelineCursor, "AuditConfiguration" );
		
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Saving Template");
		
		// -----------------------------------------------------
		// Return information
		// -----------------------------------------------------
		String msg = "Success"; 
		int rc = 0;
		
		// -----------------------------------------------------
		// Save template
		// -----------------------------------------------------
		AuditConfiguration.checkDirectory();
		if (!AuditConfiguration.AUDIT_CONFIG_DIRECTORY.exists()) {
			rc = -1;
			msg = "Unable to get directory " + AuditConfiguration.AUDIT_CONFIG_DIRECTORY.getPath();
			
		} else {
			auditConfig.save();
			LogUtils.logDebug("Template Saved");
		}
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		
		String jResp = JsonUtils.createJsonField("msg",msg);
		jResp = JsonUtils.addField(jResp,"rc", new Integer(rc).toString());
		
		IDataUtil.put( pipelineCursor2, "json_response", jResp);
		pipelineCursor.destroy(); 
					
		// --- <<IS-END>> ---

                
	}



	public static final void sendAuditResult (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(sendAuditResult)>> ---
		// @sigtype java 3.5
		// [i] object:0:required AuditResult
		// [i] field:0:required keepassPassword
		// [i] field:0:required vaultToken
		IDataCursor pipelineCursor = pipeline.getCursor();
		AuditResult	auditResult = (AuditResult) IDataUtil.get( pipelineCursor, "AuditResult" );
		String keepassPassword = IDataUtil.getString( pipelineCursor, "keepassPassword" );
		String vaultToken = IDataUtil.getString( pipelineCursor, "vaultToken" );
		pipelineCursor.destroy();
		
		LogUtils.logInfo("Sending Audit Result ... " + auditResult.getName());
		
		if (auditResult == null) {
			LogUtils.logWarning("Send Audit Result :: No audit Result - Exiting.");
			return;
		}
		
		// --------------------------------------------------------------------
		// Get SMTP Configuration from Inventory
		// --------------------------------------------------------------------
		SmtpConfiguration smtpConfig = SmtpConfiguration.instantiate();
		if (smtpConfig == null) {
			LogUtils.logError("sendAuditResult - SMTP Configuration is NULL ..... ");
			return;
		}
		
		// --------------------------------------------------------------------
		// Get SMTP Password from secret store
		// --------------------------------------------------------------------
		if ((smtpConfig.getPasswordHandle() != null) && !smtpConfig.getPasswordHandle().equals("")) {
			SecretsConfiguration secConfig = SecretsConfiguration.instantiate();
			if (secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL)) {
				secConfig.setKeepassPassword(keepassPassword);
			} else {
				secConfig.setVaultToken(vaultToken);
			}
			String pwd = SecretsUtils.getPassword(secConfig, smtpConfig.getUsername(), smtpConfig.getPasswordHandle());
			smtpConfig.setPassword(pwd);
		}
		
		// --------------------------------------------------------------------
		// Read in Freemarker Configuration
		// --------------------------------------------------------------------
		FMConfiguration fmc = FMConfiguration.instantiate();
		if (fmc == null) {
			LogUtils.logError("sendAuditResult - Freemarker Configuration is NULL ..... ");
			return;
		}
		
		// --------------------------------------------------------------------
		// Generate the HTML email body
		// --------------------------------------------------------------------
		try {
			/* Create a data-model */
		    Map<String, Object> root = new HashMap<String, Object>();
		    root.put("audit_name", auditResult.getName());
		    root.put("result", auditResult);
		    
			Template auditTemplate = fmc.getConfiguration().getTemplate("audit.ftlh");
		    
		    /* Merge data-model with template */
		    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		    java.io.Writer out = new java.io.OutputStreamWriter(bos);
			auditTemplate.process(root, out);
			
			smtpConfig.setBody(bos.toString());
			MailUtils.sendMessage(smtpConfig);
			
		    bos.close();
		} catch (Exception ex) {
			LogUtils.logError("OpenCM sendAuditResult :: " + ex);
		}
		
			
		// --- <<IS-END>> ---

                
	}
}

