package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.inventory.*;
import org.opencm.util.LogUtils;
import org.opencm.util.JsonUtils;
import org.opencm.security.KeyUtils;
import java.util.Date;
import java.text.SimpleDateFormat;
// --- <<IS-END-IMPORTS>> ---

public final class inventory

{
	// ---( internal utility methods )---

	final static inventory _instance = new inventory();

	static inventory _newInstance() { return new inventory(); }

	static inventory _cast(Object o) { return (inventory)o; }

	// ---( server methods )---




	public static final void generateReport (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(generateReport)>> ---
		// @sigtype java 3.5
				
		// --------------------------------------------------------------------
		// Read in Default Package Properties 
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		 
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Inventory Report Generation Process Started ....");
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," inventory :: generateReport :: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("OpenCM.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," inventory :: generateReport :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.instantiate(opencmConfig);
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," inventory :: generateReport :: Starting Process ...... ");
		
		// --------------------------------------------------------------------
		// Construct Current date time for report
		// --------------------------------------------------------------------
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'_'HH-mm");
		Date cDate = new Date(System.currentTimeMillis());
		String cDateTime = formatter.format(cDate);
		
		// ------------------------------------------------------
		// Create Fix Script Content
		// ------------------------------------------------------
		StringBuffer sb = new StringBuffer();
		sb.append("------------------------------------------------------" + System.lineSeparator());
		sb.append(" Inventory Report :: " + cDateTime + System.lineSeparator());
		sb.append("------------------------------------------------------" + System.lineSeparator());
		sb.append(System.lineSeparator());
		
		LinkedList<Organisation> lOrgs = inv.getInventory();
		for (int o = 0; o < lOrgs.size(); o++) {
			Organisation lOrg = lOrgs.get(o);
			if ((lOrg.getDepartments() != null) && (lOrg.getDepartments().size() > 0)) {
				sb.append("------------------------------------------------------" + System.lineSeparator());
				sb.append(" Organsiation: " + lOrg.getName() + System.lineSeparator());
				sb.append("------------------------------------------------------" + System.lineSeparator());
				LinkedList<Department> lDeps = lOrg.getDepartments();
				for (int d = 0; d < lDeps.size(); d++) {
					Department lDep = lDeps.get(d);
					if ((lDep.getServers() != null) && (lDep.getServers().size() > 0)) {
						sb.append("  ------------------------------------------------------" + System.lineSeparator());
						sb.append("   Department: " + lDep.getName() + System.lineSeparator());
						sb.append("  ------------------------------------------------------" + System.lineSeparator());
						LinkedList<Server> lServers = lDep.getServers();
						for (int s = 0; s < lServers.size(); s++) {
							Server lServer = lServers.get(s);
							if ((lServer.getInstallations() != null) && (lServer.getInstallations().size() > 0)) {
								LinkedList<Installation> lInstallations = lServer.getInstallations();
								for (int i = 0; i < lInstallations.size(); i++) {
									Installation lInst = lInstallations.get(i);
									if ((lInst.getRuntimes() != null) && (lInst.getRuntimes().size() > 0)) {
										LinkedList<RuntimeComponent> lRuntimes = lInst.getRuntimes();
										for (int r = 0; r < lRuntimes.size(); r++) {
											RuntimeComponent lRuntime = lRuntimes.get(r);
											sb.append("  --> [" + lInst.getName() +  " - " + lRuntime.getName() +"] :: " + lServer.getName() + " " + lRuntime.getProtocol() + " " + lRuntime.getPort() + " " + System.lineSeparator());
										}
									}
								}
							}
						}
						
					}
				}
			}
		}
		
		sb.append(System.lineSeparator());
		sb.append("------------------------------------------------------" + System.lineSeparator());
		
		// ------------------------------------------------------
		// Write Fix Script Content to file
		// ------------------------------------------------------
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(opencmConfig.getOutput_dir() + File.separator + "InventoryReport_" + cDateTime + ".txt"));
		    bwr.write(sb.toString());
		   
		    //flush the stream
		    bwr.flush();
		   
		    //close the stream
		    bwr.close();
		    
		} catch (IOException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," inventory :: generateReport - Exception: " + ex.toString());
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," -------------- Inventory Report Generation Process Completed   ------------   ");
			
		// --- <<IS-END>> ---

                
	}



	public static final void getInventory (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getInventory)>> ---
		// @sigtype java 3.5
		// [o] field:0:required inventory_json
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," getInventory: Start Service");
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," getInventory: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("org.opencm.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," getInventory :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.instantiate(opencmConfig);
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		// --------------------------------------------------------------------
		// Organisations
		// --------------------------------------------------------------------
		LinkedList<Organisation> orgs = inv.getInventory();
		String jsonResponse = JsonUtils.convertJavaObjectToJson(orgs);
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE, "getInventory: " + jsonResponse);
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "inventory_json", jsonResponse);
		pipelineCursor.destroy(); 
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," getInventory: End Service");
			
		// --- <<IS-END>> ---

                
	}
}

