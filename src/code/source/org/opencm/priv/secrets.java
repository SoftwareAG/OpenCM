package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
// --- <<IS-END-IMPORTS>> ---

public final class secrets

{
	// ---( internal utility methods )---

	final static secrets _instance = new secrets();

	static secrets _newInstance() { return new secrets(); }

	static secrets _cast(Object o) { return (secrets)o; }

	// ---( server methods )---




	public static final void getConfiguration (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfiguration)>> ---
		// @sigtype java 3.5
		// [o] field:0:required json
		LogUtils.logDebug("Getting Secrets Configuration");
		
		String msg = "Success";
		int rc = 0;
		
		// --------------------------------------------------------------------
		// Read in Configuration
		// --------------------------------------------------------------------
		SecretsConfiguration secConfig = SecretsConfiguration.instantiate();
		if (secConfig == null) {
			LogUtils.logError("Get Secrets Configuration: NULL ");
			msg = "Failure: Error getting configuration.";
			rc = -1;
		} 
		
		String json = JsonUtils.convertJavaObjectToJson(secConfig);
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		String jResp = JsonUtils.createJsonField("rc", new Integer(rc).toString());
		if (rc < 0) {
			jResp = JsonUtils.addField(jResp,"msg",msg);
		} else {
			jResp = JsonUtils.addField(jResp,"content", json);
		}
		LogUtils.logDebug("getSecretsConfiguration: " + json);
		IDataCursor pipelineCursor = pipeline.getCursor();
		IDataUtil.put( pipelineCursor, "json", jResp);
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Got Secrets Configuration");
			
		// --- <<IS-END>> ---

                
	}



	public static final void saveConfiguration (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(saveConfiguration)>> ---
		// @sigtype java 3.5
		// [i] field:0:required secrets_configuration
		// [o] field:0:required json
		IDataCursor pipelineCursor = pipeline.getCursor();
		String secrets_configuration = IDataUtil.getString( pipelineCursor, "secrets_configuration" );
		
		LogUtils.logDebug("Saving Secrets Configuration .. ");
		
		SecretsConfiguration secConfig = new SecretsConfiguration();
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			secConfig = mapper.readValue(secrets_configuration, SecretsConfiguration.class);
		} catch (Exception e) {
			LogUtils.logError("SecretsConfiguration save - Exception: " + e.toString());
		}
		
		pipelineCursor.destroy();
		
		// -----------------------------------------------------
		// Return information
		// -----------------------------------------------------
		String msg = "Success"; 
		int rc = 0;
		
		// -----------------------------------------------------
		// Save Configuration
		// -----------------------------------------------------
		SecretsConfiguration.checkDirectory();
		if (!SecretsConfiguration.SECRETS_CONFIG_DIRECTORY.exists()) {
			rc = -1;
			msg = "Unable to get directory " + SecretsConfiguration.SECRETS_CONFIG_DIRECTORY.getPath();
		} else {
			SecretsConfiguration.saveConfiguration(secConfig);
			LogUtils.logDebug("Secrets Configuration Saved");
		}
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		
		String jResp = JsonUtils.createJsonField("msg",msg);
		jResp = JsonUtils.addField(jResp,"rc", new Integer(rc).toString());
		
		IDataUtil.put( pipelineCursor2, "json", jResp);
		pipelineCursor.destroy(); 
					
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	// --- <<IS-END-SHARED>> ---
}

