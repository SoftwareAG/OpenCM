package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.fasterxml.jackson.databind.ObjectMapper;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.opencm.secrets.SecretsUtils;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.smtp.SmtpConfiguration;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
// --- <<IS-END-IMPORTS>> ---

public final class smtp

{
	// ---( internal utility methods )---

	final static smtp _instance = new smtp();

	static smtp _newInstance() { return new smtp(); }

	static smtp _cast(Object o) { return (smtp)o; }

	// ---( server methods )---




	public static final void getConfiguration (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfiguration)>> ---
		// @sigtype java 3.5
		// [o] field:0:required json
		LogUtils.logDebug("Getting Smtp Configuration");
		
		String msg = "Success";
		int rc = 0;
		
		// --------------------------------------------------------------------
		// Read in Configuration
		// --------------------------------------------------------------------
		SmtpConfiguration smtpConfig = SmtpConfiguration.instantiate();
		if (smtpConfig == null) {
			LogUtils.logError("Get Smtp Configuration: NULL ");
			msg = "Failure: Error getting configuration.";
			rc = -1;
		} 
		
		String json = JsonUtils.convertJavaObjectToJson(smtpConfig);
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		String jResp = JsonUtils.createJsonField("rc", new Integer(rc).toString());
		if (rc < 0) {
			jResp = JsonUtils.addField(jResp,"msg",msg);
		} else {
			jResp = JsonUtils.addField(jResp,"content", json);
		}
		LogUtils.logDebug("getSmtpConfiguration: " + json);
		IDataCursor pipelineCursor = pipeline.getCursor();
		IDataUtil.put( pipelineCursor, "json", jResp);
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Got Smtp Configuration");
			
		// --- <<IS-END>> ---

                
	}



	public static final void saveConfiguration (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(saveConfiguration)>> ---
		// @sigtype java 3.5
		// [i] field:0:required smtp_configuration
		// [i] field:0:required keepassPassword
		// [o] field:0:required json
		IDataCursor pipelineCursor = pipeline.getCursor();
		String smtp_configuration = IDataUtil.getString( pipelineCursor, "smtp_configuration" );
		String keepassPassword = IDataUtil.getString( pipelineCursor, "keepassPassword" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Saving Smtp Configuration .. "); 
		
		SmtpConfiguration smtpConfig = new SmtpConfiguration();
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			smtpConfig = mapper.readValue(smtp_configuration, SmtpConfiguration.class);
		} catch (Exception e) {
			LogUtils.logError("SmtpConfiguration save - Exception: " + e.toString());
		}
		
		// -----------------------------------------------------
		// Return information
		// -----------------------------------------------------
		String msg = "Success"; 
		int rc = 0;
		
		// -----------------------------------------------------
		// Get local keepass database ...
		// -----------------------------------------------------
		SecretsConfiguration secConfig = SecretsConfiguration.instantiate();
		if (secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL)) {
			LogUtils.logDebug("Managing SMTP password ....");
			secConfig.setKeepassPassword(keepassPassword);
			SimpleDatabase db = SecretsUtils.getDatabase(secConfig);
			if (db == null) {
				rc = -1;
				msg = "Error: Unable to get Local Keepass database.";
			} else {
				LogUtils.logDebug("Saving Configuration ....");
				SmtpConfiguration.saveConfiguration(smtpConfig, secConfig, db); 
				SecretsUtils.writeDatabase(secConfig, db);
			}
		} else {
			LogUtils.logDebug("Saving Configuration ....");
			SmtpConfiguration.saveConfiguration(smtpConfig, secConfig, null);
		}
		
		LogUtils.logDebug("Configuration Saved");
		
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

