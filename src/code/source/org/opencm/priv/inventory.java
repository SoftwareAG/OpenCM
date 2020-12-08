package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.util.LinkedList;
import java.util.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.opencm.audit.AuditConfiguration;
import org.opencm.configuration.Configuration;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.InventoryGroup;
import org.opencm.inventory.InventoryInstallation;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.secrets.SecretsUtils;
import org.opencm.util.Cache;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
// --- <<IS-END-IMPORTS>> ---

public final class inventory

{
	// ---( internal utility methods )---

	final static inventory _instance = new inventory();

	static inventory _newInstance() { return new inventory(); }

	static inventory _cast(Object o) { return (inventory)o; }

	// ---( server methods )---




	public static final void getInventory (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getInventory)>> ---
		// @sigtype java 3.5
		// [i] field:0:required keepassPassword
		// [i] field:0:required vaultToken
		// [o] field:0:required inventory_json
		IDataCursor pipelineCursor = pipeline.getCursor();
		String keepassPassword = IDataUtil.getString( pipelineCursor, "keepassPassword" );
		String vaultToken = IDataUtil.getString( pipelineCursor, "vaultToken" );
		pipelineCursor.destroy(); 
		
		LogUtils.logDebug("Getting Inventory");
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.getInstance();
		
		if (inv == null) {
			// --------------------------------------------------------------------
			// Refresh Inventory
			// --------------------------------------------------------------------
			LogUtils.logDebug("getInventory: Refreshing... ");
			Cache.getInstance().set(Inventory.INVENTORY_CACHE_KEY, null);
			inv = Inventory.instantiate();
		}
		
		// --------------------------------------------------------------------
		// Include Secrets if asked for
		// --------------------------------------------------------------------
		if ((keepassPassword != null) && (vaultToken != null))  {
			// Try to get the cached version:
			Inventory secretsInv = (Inventory) Cache.getInstance().get(Inventory.INVENTORY_SECRETS_CACHE_KEY);
			if (secretsInv != null) {
				LogUtils.logInfo("Inventory with secrets already in cache:  .... ");
				inv = secretsInv;
			} else {
				// include credentials
				LogUtils.logDebug("getInventory: Including Secrets into Inventory ... ");
				SecretsConfiguration secConfig = SecretsConfiguration.instantiate();
				if (secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL)) {
					secConfig.setKeepassPassword(keepassPassword);
					SimpleDatabase db = SecretsUtils.getDatabase(secConfig);
					if (db != null) {
						Inventory.includeSecrets(inv.getRootGroup(), secConfig, db);
					}
				} else {
					secConfig.setVaultToken(vaultToken);
					Inventory.includeSecrets(inv.getRootGroup(), secConfig, null);
				}
				
				// Store inventory with secrets into cache
				Cache.getInstance().set(Inventory.INVENTORY_SECRETS_CACHE_KEY, inv);
			}
		}
		
		String jsonResponse = JsonUtils.convertJavaObjectToJson(inv);
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		LogUtils.logDebug("getInventory: Got a new instance from server ... ");
		LogUtils.logDebug("getInventory: " + jsonResponse);
		
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "inventory_json", jsonResponse);
		pipelineCursor2.destroy(); 
		
		LogUtils.logDebug("Got Inventory");
			
		// --- <<IS-END>> ---

                
	}



	public static final void initInventory (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(initInventory)>> ---
		// @sigtype java 3.5
		LogUtils.logDebug("Init Inventory");
		
		// --------------------------------------------------------------------
		// Refresh Inventory
		// --------------------------------------------------------------------
		Cache.getInstance().set(Inventory.INVENTORY_CACHE_KEY, null);
		Cache.getInstance().set(Inventory.INVENTORY_SECRETS_CACHE_KEY, null);
		Inventory.instantiate();
		
		LogUtils.logInfo("Inventory Initialized");
			
		// --- <<IS-END>> ---

                
	}



	public static final void saveInventory (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(saveInventory)>> ---
		// @sigtype java 3.5
		// [i] field:0:required inventory_configuration
		// [i] field:0:required keepassPassword
		// [o] field:0:required json
		IDataCursor pipelineCursor = pipeline.getCursor();
		String inventory_configuration = IDataUtil.getString( pipelineCursor, "inventory_configuration" );
		String keepassPassword = IDataUtil.getString( pipelineCursor, "keepassPassword" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Saving Inventory Configuration .. ");
		LogUtils.logDebug("Inventory to save: " + inventory_configuration);
		Inventory inv = new Inventory();
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			inv = mapper.readValue(inventory_configuration, Inventory.class);
		} catch (Exception e) {
			LogUtils.logError("Inventory - Exception: " + e.toString());
		}
		
		
		// -----------------------------------------------------
		// Return information
		// -----------------------------------------------------
		String msg = "Success"; 
		int rc = 0;
		
		// -----------------------------------------------------
		// Check inventory directory
		// -----------------------------------------------------
		Inventory.checkDirectory(); 
		if (!Inventory.INVENTORY_CONFIG_DIRECTORY.exists()) {
			rc = -1;
			msg = "Unable to get directory " + Inventory.INVENTORY_CONFIG_DIRECTORY.getPath();
			
		}		
		
		// -----------------------------------------------------
		// Handle Passwords passed..
		// -----------------------------------------------------
		if (rc == 0) {
			SecretsConfiguration secConfig = SecretsConfiguration.instantiate();
			if (secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL)) {
				LogUtils.logDebug("Managing passwords ....");
				secConfig.setKeepassPassword(keepassPassword);
				SimpleDatabase db = SecretsUtils.getDatabase(secConfig);
				if (db != null) {
					LogUtils.logDebug("Updating Secrets in the passed Inventory ....");
					Inventory.updateSecrets(inv.getRootGroup(), secConfig, db);
					LogUtils.logDebug("Writing new secrets database ....");
					SecretsUtils.writeDatabase(secConfig, db);
					LogUtils.logDebug("Storing new Inventory ....");
					inv.saveInventory();
				} else {
					rc = -1;
					msg = "Error: Unable to get Local Keepass database.";
				}
			} else {
				LogUtils.logDebug("Storing new Inventory ....");
				inv.saveInventory();
			}
			LogUtils.logDebug("Inventory Saved");
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

