package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.opencm.audit.AuditUtils;
import org.opencm.audit.AuditResult;
import org.opencm.governance.*;
import org.opencm.configuration.Configuration;
import org.opencm.inventory.Inventory;
import org.opencm.util.*;
// --- <<IS-END-IMPORTS>> ---

public final class governance

{
	// ---( internal utility methods )---

	final static governance _instance = new governance();

	static governance _newInstance() { return new governance(); }

	static governance _cast(Object o) { return (governance)o; }

	// ---( server methods )---




	public static final void getConfigurationByJson (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfigurationByJson)>> ---
		// @sigtype java 3.5
		// [i] field:0:required json
		// [o] object:0:required GovernanceConfiguration
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stJson = IDataUtil.getString( pipelineCursor, "json" ); 
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Configuration Object by Json"); 
		
		GovernanceConfiguration govConfig = GovernanceConfiguration.instantiate(stJson); 
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "GovernanceConfiguration", govConfig );
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Configuration Object");
			
		// --- <<IS-END>> ---

                
	}



	public static final void getGovResultJson (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getGovResultJson)>> ---
		// @sigtype java 3.5
		// [i] field:0:required rules
		// [o] field:0:required json_response
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String stRulesJson = IDataUtil.getString( pipelineCursor, "rules" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Run Governance Audit Starting");
		
		LinkedList<GovernanceConfiguration> rulesConfig = new LinkedList<GovernanceConfiguration>(); 
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> rules = mapper.readValue(stRulesJson, new TypeReference<ArrayList<String>>() { });
			LogUtils.logDebug("Governance Rules passed: " + rules.size());
			for (String rule: rules) {
				File ruleFile = new File(GovernanceConfiguration.GOV_CONFIG_DIRECTORY + File.separator + rule + ".json");
				GovernanceConfiguration govConfig = GovernanceConfiguration.instantiate(ruleFile);
				rulesConfig.add(govConfig);
			}
		} catch (Exception e) {
			LogUtils.logError("GovernanceConfiguration - Exception: " + e.toString());
		}
		
		String jsonResponse = "";
		
		// -----------------------------------------------------
		// Run Governance Audit
		// -----------------------------------------------------
		LogUtils.logDebug("Running Governance Audit with rules config size :: " + rulesConfig.size());
		AuditResult ar = AuditUtils.performAudit(rulesConfig); 
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
		
		LogUtils.logDebug("Run Governance Audit Completed");
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void getGovResultObject (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getGovResultObject)>> ---
		// @sigtype java 3.5
		// [i] field:0:required rules
		// [o] object:0:required AuditResult
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String stRulesJson = IDataUtil.getString( pipelineCursor, "rules" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Run Governance Audit Starting");
		
		LinkedList<GovernanceConfiguration> rulesConfig = new LinkedList<GovernanceConfiguration>(); 
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> rules = mapper.readValue(stRulesJson, new TypeReference<ArrayList<String>>() { });
			LogUtils.logDebug("Governance Rules passed: " + rules.size());
			for (String rule: rules) {
				File ruleFile = new File(GovernanceConfiguration.GOV_CONFIG_DIRECTORY + File.separator + rule + ".json");
				GovernanceConfiguration govConfig = GovernanceConfiguration.instantiate(ruleFile);
				rulesConfig.add(govConfig);
			}
		} catch (Exception e) {
			LogUtils.logError("GovernanceConfiguration - Exception: " + e.toString());
		}
		
		// -----------------------------------------------------
		// Run Governance Audit
		// -----------------------------------------------------
		LogUtils.logDebug("Running Governance Audit with rules config size :: " + rulesConfig.size());
		AuditResult ar = AuditUtils.performAudit(rulesConfig); 
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "AuditResult", ar );
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Run Scheduled Audit Completed");
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void getRule (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getRule)>> ---
		// @sigtype java 3.5
		// [i] field:0:required ruleName
		// [o] field:0:required json_response
		// pipeline 
		IDataCursor pipelineCursor = pipeline.getCursor(); 
		String	stRuleName = IDataUtil.getString( pipelineCursor, "ruleName" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Template " + stRuleName);
		
		String msg = "Success";
		int rc = 0;
		
		String json = null;
		File ruleFile = new File(GovernanceConfiguration.GOV_CONFIG_DIRECTORY.getPath() + File.separator + stRuleName + ".json");
		if (!ruleFile.exists()) {
			LogUtils.logError("Get Rule: No file exists :: " + ruleFile.getPath());
			msg = "Failure: No rule file exists.";
			rc = -1;
		} else { 
			GovernanceConfiguration govConfig = GovernanceConfiguration.instantiate(ruleFile);
			if (govConfig == null) {
				rc = -1;
				msg = "Failure getting configuration ";
			} else {
				json = JsonUtils.convertJavaObjectToJson(govConfig);
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



	public static final void getRules (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getRules)>> ---
		// @sigtype java 3.5
		// [o] field:0:required json_response
		LogUtils.logDebug("Get Rules"); 
		
		String msg = "Success"; 
		int rc = 0;
		
		LinkedList<String> lTemplates = new LinkedList<String>();
		
		GovernanceConfiguration.checkDirectory();
		if (!GovernanceConfiguration.GOV_CONFIG_DIRECTORY.exists()) {
			msg = "Failure: Unable to get rules directory.";
			rc = -1;
		} else {
			try {
				File [] fDirectories = GovernanceConfiguration.GOV_CONFIG_DIRECTORY.listFiles();
				for(File path : fDirectories) {
					if ((path.isFile() && path.getName().endsWith(".json"))) {
						lTemplates.add(path.getName().substring(0,path.getName().lastIndexOf(".")));
					}
				}
			} catch (Exception ex) {
				LogUtils.logError("getRules: Exception: " + ex.getMessage());
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
		
		LogUtils.logDebug("Got Rules");
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void saveRule (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(saveRule)>> ---
		// @sigtype java 3.5
		// [i] object:0:required GovernanceConfiguration
		// [o] field:0:required json_response
		IDataCursor pipelineCursor = pipeline.getCursor(); 
		GovernanceConfiguration govConfig = (GovernanceConfiguration) IDataUtil.get( pipelineCursor, "GovernanceConfiguration" );
		
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Save Rule");
		
		// -----------------------------------------------------
		// Return information
		// -----------------------------------------------------
		String msg = "Success";
		int rc = 0;
		
		// -----------------------------------------------------
		// Save template
		// -----------------------------------------------------
		GovernanceConfiguration.checkDirectory();
		if (!GovernanceConfiguration.GOV_CONFIG_DIRECTORY.exists()) {
			rc = -1;
			msg = "Unable to get directory " + GovernanceConfiguration.GOV_CONFIG_DIRECTORY.getPath();
			
		} else {
			govConfig.save();
			LogUtils.logDebug("Rule Saved");
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

