package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.Iterator;
import java.util.LinkedList;
import java.io.File;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.Configuration;
import org.opencm.util.Cache;
import org.opencm.security.KeyUtils;
import org.opencm.util.LogUtils;
import org.opencm.util.PackageUtils;
import org.opencm.inventory.*;
import org.opencm.util.MailUtils;
import org.opencm.util.JsonUtils;
import org.opencm.audit.result.AuditResult;
import java.util.Map;
import java.util.HashMap;
import org.opencm.freemarker.FMConfiguration;
import freemarker.template.Template;
// --- <<IS-END-IMPORTS>> ---

public final class util

{
	// ---( internal utility methods )---

	final static util _instance = new util();

	static util _newInstance() { return new util(); }

	static util _cast(Object o) { return (util)o; }

	// ---( server methods )---




	public static final void addToCache (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(addToCache)>> ---
		// @sigtype java 3.5
		// [i] field:0:required key
		// [i] field:0:required value
		IDataCursor pipelineCursor = pipeline.getCursor(); 
		String	stKey = IDataUtil.getString( pipelineCursor, "key" );
		String	stValue = IDataUtil.getString( pipelineCursor, "value" );
		pipelineCursor.destroy();
		Cache.getInstance().set(stKey, stValue);
			
		// --- <<IS-END>> ---

                
	}



	public static final void getConfig (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfig)>> ---
		// @sigtype java 3.5
		// [o] field:0:required cmdata_root
		// [o] field:0:required output_dir
		// [o] field:0:required inventory_type
		// [o] field:0:required debug_level
		// [o] field:0:required package_directory
		// [o] field:0:required audit_prop_directory
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory()); 
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		IDataUtil.put( pipelineCursor, "cmdata_root", opencmConfig.getCmdata_root() );
		IDataUtil.put( pipelineCursor, "output_dir", opencmConfig.getOutput_dir() );
		IDataUtil.put( pipelineCursor, "inventory_type", opencmConfig.getInventory_config().getType());
		IDataUtil.put( pipelineCursor, "debug_level", opencmConfig.getDebug_level() );
		// -----------------
		String packageConfig = PackageUtils.getPackageConfigPath();
		File pkgDir = new File(packageConfig + File.separator + "..");
		IDataUtil.put( pipelineCursor, "package_directory", pkgDir.getPath());
		IDataUtil.put( pipelineCursor, "audit_prop_directory", opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_CONFIG_DIR_AUDIT);
		
		pipelineCursor.destroy(); 
			
		// --- <<IS-END>> ---

                
	}



	public static final void getInventory (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getInventory)>> ---
		// @sigtype java 3.5
		// [o] recref:1:required Organsiation org.opencm.doc.inventory:Organisation
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
		IData[]	Organisation = new IData[orgs.size()];
		for (int o = 0; o < orgs.size(); o++) {
			Organisation org = orgs.get(o);
			Organisation[o] = IDataFactory.create();
			IDataCursor OrganisationCursor = Organisation[o].getCursor();
			IDataUtil.put( OrganisationCursor, "name", org.getName() );
			// --------------------------------------------------------------------
			// Departments
			// --------------------------------------------------------------------
			LinkedList<Department> deps = org.getDepartments();
			IData[]	Department = new IData[deps.size()];
			for (int d = 0; d < deps.size(); d++) {
				Department dep = deps.get(d);
				Department[d] = IDataFactory.create();
				IDataCursor DepartmentCursor = Department[d].getCursor();
				IDataUtil.put( DepartmentCursor, "name", dep.getName() );
				// --------------------------------------------------------------------
				// Environments
				// --------------------------------------------------------------------
				LinkedList<String> envs = dep.getEnvironments();
				IData[]	Environment = new IData[envs.size()];
				for (int e = 0; e < envs.size(); e++) {
					String env = envs.get(e);
					Environment[e] = IDataFactory.create();
					IDataCursor EnvironmentCursor = Environment[e].getCursor();
					IDataUtil.put( EnvironmentCursor, "name", env );
					// --------------------------------------------------------------------
					// Layers
					// --------------------------------------------------------------------
					LinkedList<String> layers = dep.getLayersByEnv(env);
					IData[]	Layer = new IData[layers.size()];
					for (int l = 0; l < layers.size(); l++) {
						String layer = layers.get(l);
						Layer[l] = IDataFactory.create();
						IDataCursor LayerCursor = Layer[l].getCursor();
						IDataUtil.put( LayerCursor, "name", layer );
						// ----------------------------------------------------------------
						// Servers
						// ----------------------------------------------------------------
						LinkedList<Server> servers = dep.getServers(env, layer);
						IData[]	Server = new IData[servers.size()];
						for (int s = 0; s < servers.size(); s++) {
							Server server = servers.get(s);
							Server[s] = IDataFactory.create();
							IDataCursor ServerCursor = Server[s].getCursor();
							IDataUtil.put( ServerCursor, "name", server.getName() );
							IDataUtil.put( ServerCursor, "description", server.getDescription() );
							IDataUtil.put( ServerCursor, "os", server.getOs() );
							IDataUtil.put( ServerCursor, "type", server.getType() );
							
							// ----------------------------------------------------------------
							// Installations
							// ----------------------------------------------------------------
							LinkedList<Installation> installations = server.getInstallations(env,layer,null);
							IData[]	Installation = new IData[installations.size()];
							for (int i = 0; i < installations.size(); i++) {
								Installation installation = installations.get(i);
								Installation[i] = IDataFactory.create();
								IDataCursor InstallationCursor = Installation[i].getCursor();
								IDataUtil.put( InstallationCursor, "name", installation.getName() );
								IDataUtil.put( InstallationCursor, "environment", installation.getEnvironment() );
								IDataUtil.put( InstallationCursor, "layer", installation.getLayer() );
								IDataUtil.put( InstallationCursor, "sublayer", installation.getSublayer() );
								IDataUtil.put( InstallationCursor, "version", installation.getVersion() );
								
								// ----------------------------------------------------------------
								// Runtime Components
								// ----------------------------------------------------------------
								LinkedList<RuntimeComponent> rcs = installation.getRuntimes();
								IData[]	RuntimeComponent = new IData[rcs.size()];
								for (int r = 0; r < rcs.size(); r++) {
									RuntimeComponent rc = rcs.get(r);
									RuntimeComponent[r] = IDataFactory.create();
									IDataCursor RuntimeComponentCursor = RuntimeComponent[r].getCursor();
									IDataUtil.put( RuntimeComponentCursor, "name", rc.getName() );
									IDataUtil.put( RuntimeComponentCursor, "protocol", rc.getProtocol() );
									IDataUtil.put( RuntimeComponentCursor, "port", rc.getPort() );
									IDataUtil.put( RuntimeComponentCursor, "username", rc.getUsername() );
									RuntimeComponentCursor.destroy();
									IDataUtil.put( InstallationCursor, "RuntimeComponent", RuntimeComponent );
								}
								InstallationCursor.destroy();
								IDataUtil.put( ServerCursor, "Installation", Installation );
							}
							ServerCursor.destroy();
							IDataUtil.put( LayerCursor, "Server", Server );
						}
						LayerCursor.destroy();
						IDataUtil.put( EnvironmentCursor, "Layer", Layer );
					}
					EnvironmentCursor.destroy();
					IDataUtil.put( DepartmentCursor, "Environment", Environment );
				}
				DepartmentCursor.destroy();
				IDataUtil.put( OrganisationCursor, "Department", Department );
			}
			OrganisationCursor.destroy();
			IDataUtil.put( pipelineCursor, "Organisation", Organisation );
		}
		pipelineCursor.destroy();
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," getInventory: End Service");
			
		// --- <<IS-END>> ---

                
	}



	public static final void getResultFiles (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getResultFiles)>> ---
		// @sigtype java 3.5
		// [i] field:0:required directory
		// [o] record:1:required resultFiles
		// [o] - field:0:required resultFile
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stDirectory = IDataUtil.getString( pipelineCursor, "directory" );
		pipelineCursor.destroy();
		
		// pipeline
		IDataCursor pipelineCursor_1 = pipeline.getCursor(); 
		
		try {
			java.io.File fDirectory = new java.io.File(stDirectory);
			java.io.File [] fDirectories = fDirectory.listFiles();
			java.util.LinkedList<String> lResultFiles = new java.util.LinkedList<String>();
			for(java.io.File path:fDirectories) {
				if (path.isFile() && path.getName().endsWith(".xlsx") && !path.getName().startsWith("~")) {
					lResultFiles.add(path.getName().substring(0,path.getName().lastIndexOf(".")));
				}
			}
			IData[]	iResultFiles = new IData[lResultFiles.size()];
			for (int i = 0; i < lResultFiles.size(); i++) {
				iResultFiles[i] = IDataFactory.create();
				IDataCursor dirCursor = iResultFiles[i].getCursor();
				IDataUtil.put( dirCursor, "resultFile", lResultFiles.get(i));
				dirCursor.destroy();
			}
			
			IDataUtil.put( pipelineCursor_1, "resultFiles", iResultFiles );
			
		} catch (Exception ex) {
			System.out.println("Exception: " + ex);
		}
		
		// directories
		pipelineCursor_1.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void getSubDirectories (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getSubDirectories)>> ---
		// @sigtype java 3.5
		// [i] field:0:required directory
		// [o] record:1:required directories
		// [o] - field:0:required directory
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stDirectory = IDataUtil.getString( pipelineCursor, "directory" );
		pipelineCursor.destroy();
		
		// pipeline
		IDataCursor pipelineCursor_1 = pipeline.getCursor();
		
		try {
			java.io.File fDirectory = new java.io.File(stDirectory);
			java.io.File [] fDirectories = fDirectory.listFiles();
			java.util.LinkedList<String> lDirectories = new java.util.LinkedList<String>();
			for(java.io.File path:fDirectories) {
				if (path.isDirectory()) {
					lDirectories.add(path.getName());
				}
			}
			IData[]	iDirectories = new IData[lDirectories.size()];
			for (int i = 0; i < lDirectories.size(); i++) {
				iDirectories[i] = IDataFactory.create();
				IDataCursor dirCursor = iDirectories[i].getCursor();
				IDataUtil.put( dirCursor, "directory", lDirectories.get(i));
				dirCursor.destroy();
			}
			
			IDataUtil.put( pipelineCursor_1, "directories", iDirectories );
			
		} catch (Exception ex) {
			System.out.println("Exception: " + ex);
		}
		
		// directories
		pipelineCursor_1.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void logConsole (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(logConsole)>> ---
		// @sigtype java 3.5
		// [i] field:0:required message
		IDataCursor pipelineCursor = pipeline.getCursor();
		String stMessage = IDataUtil.getString(pipelineCursor, "message");
		System.out.println(new java.sql.Timestamp(System.currentTimeMillis()).toString() + " - " + stMessage);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void refreshInventory (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(refreshInventory)>> ---
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," refreshInventory: Start Service");
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," refreshInventory: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("org.opencm.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," refreshInventory :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Refresh Inventory
		// --------------------------------------------------------------------
		Cache.getInstance().set(org.opencm.inventory.Inventory.INVENTORY_CACHE_KEY, null);
		Inventory.instantiate(opencmConfig);
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," refreshInventory: End Service");
			
			
		// --- <<IS-END>> ---

                
	}



	public static final void sendAuditResult (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(sendAuditResult)>> ---
		// @sigtype java 3.5
		// [i] field:0:required smtpPassword
		// [i] field:0:required auditName
		// [i] object:0:required auditResult
		IDataCursor pipelineCursor = pipeline.getCursor();
		String pwd = IDataUtil.getString( pipelineCursor, "smtpPassword" );
		String auditName = IDataUtil.getString( pipelineCursor, "auditName" );
		AuditResult	auditResult = (AuditResult) IDataUtil.get( pipelineCursor, "auditResult" );
		pipelineCursor.destroy();
		
		// --------------------------------------------------------------------
		// Read in Default Package Properties 
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate(); 
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," - Send Audit Result ...");
		
		if (pwd == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Send Audit Result :: SMTP Server password not passed, cannot send - Exiting.");
			return;
		}
		if (auditResult == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Send Audit Result :: No audit Result - Exiting.");
			return;
		}
		
		// --------------------------------------------------------------------
		// DEBUG
		// --------------------------------------------------------------------
		if (opencmConfig.getDebug_level().equals(Configuration.OPENCM_LOG_DEBUG)) {
			if (auditResult.getLocationItems() != null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"LocationItems: " + auditResult.getLocationItems().size());
				for (int i = 0; i < auditResult.getLocationItems().size(); i++) {
					org.opencm.audit.result.LocationItem li = auditResult.getLocationItems().get(i);
					org.opencm.audit.result.Location l = li.getLocation();
					LinkedList<org.opencm.audit.result.LocationProperty> lp = li.getLocationProperties();
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," - Location: " + l.getComponent() + l.getInstance() + " - LocProperties: " + lp.size());
					for (int p = 0; p < lp.size(); p++) {
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"   -- Location Property: " + lp.get(p).getPropertyName());
					}
				}
			}
		}
		// --------------------------------------------------------------------
		// Read in Freemarker Configuration
		// --------------------------------------------------------------------
		FMConfiguration fmc = FMConfiguration.instantiate(opencmConfig);
		if (fmc == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"sendAuditResult - Freemarker Configuration is NULL ..... ");
			return;
		}
			
		// --------------------------------------------------------------------
		// Generate the HTML email body
		// --------------------------------------------------------------------
		try {
			Template auditTemplate = null;
			/* Create a data-model */
		    Map<String, Object> root = new HashMap<String, Object>();
		    root.put("audit_name", auditName);
		    root.put("result", auditResult);
		    auditTemplate = fmc.getConfiguration().getTemplate("audit.ftlh");
		    
		    /* Merge data-model with template */
		    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		    java.io.Writer out = new java.io.OutputStreamWriter(bos);
			auditTemplate.process(root, out);
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"Audit Result: " + bos.toString());
			MailUtils.sendMessage(opencmConfig, pwd, bos.toString());
		    bos.close();
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM sendAuditResult :: " + ex);
		}
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void sleep (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(sleep)>> ---
		// @sigtype java 3.5
		// [i] field:0:required secs
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	secs = IDataUtil.getString( pipelineCursor, "secs" );
		pipelineCursor.destroy();
		Integer iSecs = 0;
		if (secs != null) {
			iSecs = new Integer(secs);
		}
		try {
			Thread.sleep(iSecs.intValue() * 1000); 
		} catch (InterruptedException ex) {
			//
		}
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---

	
	// --- <<IS-END-SHARED>> ---
}

