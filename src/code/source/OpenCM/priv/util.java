package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.util.LinkedList;
import org.opencm.configuration.Node;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.RuntimeComponent;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.Configuration;
import org.opencm.util.Cache;
import org.opencm.util.PackageUtils;
import org.opencm.security.KeyUtils;
import org.opencm.util.LogUtils;
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



	public static final void getAllNodes (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getAllNodes)>> ---
		// @sigtype java 3.5
		// [o] recref:1:required Environment OpenCM.doc.nodes:Environment
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," GetAllNodes: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("OpenCM.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," GetAllNodes :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		// --------------------------------------------------------------------
		// Environments
		// --------------------------------------------------------------------
		LinkedList<String> envs = nodes.getAllEnvironments();
		IData[]	Environment = new IData[envs.size()];
		for (int e = 0; e < envs.size(); e++) {
			// Environment
			String env = envs.get(e);
			Environment[e] = IDataFactory.create();
			IDataCursor EnvironmentCursor = Environment[e].getCursor();
			IDataUtil.put( EnvironmentCursor, "name", env );
			
			// ----------------------------------------------------------------
			// Layers
			// ----------------------------------------------------------------
			LinkedList<String> layers = nodes.getAllAssertionGroupsForEnvironment(env);
			IData[]	Layer = new IData[layers.size()];
			for (int l = 0; l < layers.size(); l++) {
				// Environment.Layer
				String layer = layers.get(l);
				Layer[l] = IDataFactory.create();
				IDataCursor LayerCursor = Layer[l].getCursor();
				IDataUtil.put( LayerCursor, "name", layer );
				
				// ----------------------------------------------------------------
				// Servers
				// ----------------------------------------------------------------
				LinkedList<String> servers = nodes.getAllFQNServerNamesByEnvAndLayer(env, layer);
				IData[]	Server = new IData[servers.size()];
				for (int s = 0; s < servers.size(); s++) {
					// Environment.Layer.Server
					String server = servers.get(s);
					Server[s] = IDataFactory.create();
					IDataCursor ServerCursor = Server[s].getCursor();
					IDataUtil.put( ServerCursor, "name", server );
					
					// ----------------------------------------------------------------
					// Nodes
					// ----------------------------------------------------------------
					LinkedList<Node> serverNodes = nodes.getNodesByEnvLayerAndFQNServer(env, layer, server);
					IData[]	Node = new IData[serverNodes.size()];
					for (int n = 0; n < serverNodes.size(); n++) {
						// Environment.Layer.Server.Node
						Node node = serverNodes.get(n);
						Node[n] = IDataFactory.create();
						IDataCursor NodeCursor = Node[n].getCursor();
						IDataUtil.put( NodeCursor, "name", node.getNode_name() );
						
						// ----------------------------------------------------------------
						// Runtime Components
						// ----------------------------------------------------------------
						LinkedList<RuntimeComponent> rcs = node.getRuntimeComponents();
						IData[]	RuntimeComponent = new IData[rcs.size()];
						for (int r = 0; r < rcs.size(); r++) {
							// Environment.Layer.Server.Node.RuntimeComponent
							RuntimeComponent rc = rcs.get(r);
							RuntimeComponent[r] = IDataFactory.create();
							IDataCursor RuntimeComponentCursor = RuntimeComponent[r].getCursor();
							IDataUtil.put( RuntimeComponentCursor, "name", rc.getName() );
							IDataUtil.put( RuntimeComponentCursor, "protocol", rc.getProtocol() );
							IDataUtil.put( RuntimeComponentCursor, "port", rc.getPort() );
							IDataUtil.put( RuntimeComponentCursor, "username", rc.getUsername() );
							RuntimeComponentCursor.destroy();
							IDataUtil.put( NodeCursor, "RuntimeComponent", RuntimeComponent );
						}
						NodeCursor.destroy();
						IDataUtil.put( ServerCursor, "Node", Node );
					}
					ServerCursor.destroy();
					IDataUtil.put( LayerCursor, "Server", Server );
				}
				LayerCursor.destroy();
				IDataUtil.put( EnvironmentCursor, "Layer", Layer );
			}
			EnvironmentCursor.destroy();
			IDataUtil.put( pipelineCursor, "Environment", Environment );
			
		}
			
		pipelineCursor.destroy();
		
		/**
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		// Environment
		IData[]	Environment = new IData[1];
		Environment[0] = IDataFactory.create();
		IDataCursor EnvironmentCursor = Environment[0].getCursor();
		IDataUtil.put( EnvironmentCursor, "name", "name" );
		
		// Environment.Layer
		IData[]	Layer = new IData[1];
		Layer[0] = IDataFactory.create();
		IDataCursor LayerCursor = Layer[0].getCursor();
		IDataUtil.put( LayerCursor, "name", "name" );
		
		// Environment.Layer.Server
		IData[]	Server = new IData[1];
		Server[0] = IDataFactory.create();
		IDataCursor ServerCursor = Server[0].getCursor();
		IDataUtil.put( ServerCursor, "name", "name" );
		
		// Environment.Layer.Server.Node
		IData[]	Node = new IData[1];
		Node[0] = IDataFactory.create();
		IDataCursor NodeCursor = Node[0].getCursor();
		IDataUtil.put( NodeCursor, "name", "name" );
		
		// Environment.Layer.Server.Node.RuntimeComponent
		IData[]	RuntimeComponent = new IData[1];
		RuntimeComponent[0] = IDataFactory.create();
		IDataCursor RuntimeComponentCursor = RuntimeComponent[0].getCursor();
		IDataUtil.put( RuntimeComponentCursor, "name", "name" );
		IDataUtil.put( RuntimeComponentCursor, "protocol", "protocol" );
		IDataUtil.put( RuntimeComponentCursor, "port", "port" );
		IDataUtil.put( RuntimeComponentCursor, "username", "username" );
		
		RuntimeComponentCursor.destroy();
		IDataUtil.put( NodeCursor, "RuntimeComponent", RuntimeComponent );
		
		NodeCursor.destroy();
		IDataUtil.put( ServerCursor, "Node", Node );
		
		ServerCursor.destroy();
		IDataUtil.put( LayerCursor, "Server", Server );
		
		LayerCursor.destroy();
		IDataUtil.put( EnvironmentCursor, "Layer", Layer );
		
		EnvironmentCursor.destroy();
		IDataUtil.put( pipelineCursor, "Environment", Environment );
		
		pipelineCursor.destroy();
		
		*/
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void getConfig (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfig)>> ---
		// @sigtype java 3.5
		// [o] field:0:required cmdata_root
		// [o] field:0:required output_dir
		// [o] field:0:required endpoint_config_type
		// [o] field:0:required debug_level
		// [o] field:0:required package_directory
		// [o] field:0:required two_node_audit_directory
		// [o] field:0:required layered_audit_directory
		// [o] field:0:required excel_output_directory
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
		IDataUtil.put( pipelineCursor, "endpoint_config_type", opencmConfig.getEndpoint_config().getType());
		IDataUtil.put( pipelineCursor, "debug_level", opencmConfig.getDebug_level() );
		// -----------------
		String packageConfig = PackageUtils.getPackageConfigPath();
		File pkgDir = new File(packageConfig + File.separator + "..");
		IDataUtil.put( pipelineCursor, "package_directory", pkgDir.getPath());
		IDataUtil.put( pipelineCursor, "two_node_audit_directory", opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_CONFIG_DIR_TWO_NODE_AUDIT);
		IDataUtil.put( pipelineCursor, "layered_audit_directory", opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_CONFIG_DIR_LAYERED_AUDIT);
		IDataUtil.put( pipelineCursor, "excel_output_directory", opencmConfig.getOutput_dir() + File.separator + Configuration.OPENCM_RESULTS_DIR_EXCEL);
		
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void getPropertyFiles (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getPropertyFiles)>> ---
		// @sigtype java 3.5
		// [i] field:0:required directory
		// [o] record:1:required propertyFiles
		// [o] - field:0:required propertyFile
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stDirectory = IDataUtil.getString( pipelineCursor, "directory" );
		pipelineCursor.destroy();
		
		// pipeline
		IDataCursor pipelineCursor_1 = pipeline.getCursor();
		
		try {
			java.io.File fDirectory = new java.io.File(stDirectory);
			java.io.File [] fDirectories = fDirectory.listFiles();
			java.util.LinkedList<String> lPropertyFiles = new java.util.LinkedList<String>();
			for(java.io.File path:fDirectories) {
				if ((path.isFile() && path.getName().endsWith(".properties"))) {
					lPropertyFiles.add(path.getName().substring(0,path.getName().lastIndexOf(".")));
				}
			}
			IData[]	iPropertyFiles = new IData[lPropertyFiles.size()];
			for (int i = 0; i < lPropertyFiles.size(); i++) {
				iPropertyFiles[i] = IDataFactory.create();
				IDataCursor dirCursor = iPropertyFiles[i].getCursor();
				IDataUtil.put( dirCursor, "propertyFile", lPropertyFiles.get(i));
				dirCursor.destroy();
			}
			
			IDataUtil.put( pipelineCursor_1, "propertyFiles", iPropertyFiles );
			
		} catch (Exception ex) {
			System.out.println("Exception: " + ex);
		}
		
		// directories
		pipelineCursor_1.destroy();
			
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

	// --- <<IS-START-SHARED>> ---

	
	// --- <<IS-END-SHARED>> ---
}

