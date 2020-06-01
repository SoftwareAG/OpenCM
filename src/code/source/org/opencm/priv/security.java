package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.InventoryConfiguration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.inventory.*;
import org.opencm.security.KeyUtils;
import org.opencm.util.LogUtils;
// --- <<IS-END-IMPORTS>> ---

public final class security

{
	// ---( internal utility methods )---

	final static security _instance = new security();

	static security _newInstance() { return new security(); }

	static security _cast(Object o) { return (security)o; }

	// ---( server methods )---




	public static final void updatePasswords (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(updatePasswords)>> ---
		// @sigtype java 3.5
		// [i] field:0:required action {"encrypt","decrypt"}
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stAction = IDataUtil.getString( pipelineCursor, "action" );
		pipelineCursor.destroy(); 
		
		/*
		 * Encrypt/Decrypt the inventory.properties passwords 
		 */ 
		
		// --------------------------------------------------------------------
		// Read in Default Package Properties 
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," updatePassword ::  with action " + stAction);
		
		if (!opencmConfig.getInventory_config().getType().equals(InventoryConfiguration.INVENTORY_CONFIG_OPENCM)) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," updatePassword ::  Inventory type is not OpenCM property file - exiting ...");
			return;
		}
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," updatePasswords :: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("org.opencm.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," updatePasswords :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.instantiate(opencmConfig);
		
		boolean changed = false;
		
		// --------------------------------------------------------------------
		// Loop through all runtime component passwords
		// --------------------------------------------------------------------
		LinkedList<Organisation> orgs = inv.getInventory();
		for (int o = 0; o < orgs.size(); o++) {
			// Organisation
			Organisation org = orgs.get(o);
			LinkedList<Department> deps = org.getDepartments();
			for (int d = 0; d < deps.size(); d++) {
				// Department
				Department dep = deps.get(d);
				LinkedList<Server> servers = dep.getServers();
				for (int s = 0; s < servers.size(); s++) {
					// Server
					Server server = servers.get(s);
					LinkedList<Installation> installations = server.getInstallations();
					for (int i = 0; i < installations.size(); i++) {
						// Installation
						Installation installation = installations.get(i);
						LinkedList<RuntimeComponent> rcs = installation.getRuntimes();
						for (int r = 0; r < rcs.size(); r++) {
							if (stAction.equals("encrypt")) {
								if (!rcs.get(r).passwordEncrypted()) {
									rcs.get(r).encryptPassword();
									changed = true;
								}
							} else {
								if (rcs.get(r).passwordEncrypted()) {
									rcs.get(r).decryptPassword();
									changed = true;
								}
							}
						}
					}
				}
			}
		}
		
		
		// --------------------------------------------------------------------
		// Store nodes to config file
		// --------------------------------------------------------------------
		if (changed) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," updatePasswords ::  " + stAction + "ing after scanning ... ");
			inv.updateInventory(opencmConfig);
		} else {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," updatePasswords ::  no need to " + stAction + " after scanning ... ");
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," updatePasswords ::  " + stAction + "ing passwords completed ... ");
			
		// --- <<IS-END>> ---

                
	}
}

