package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.Configuration;
import org.opencm.util.PackageUtils;
// --- <<IS-END-IMPORTS>> ---

public final class util

{
	// ---( internal utility methods )---

	final static util _instance = new util();

	static util _newInstance() { return new util(); }

	static util _cast(Object o) { return (util)o; }

	// ---( server methods )---




	public static final void getConfig (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getConfig)>> ---
		// @sigtype java 3.5
		// [o] field:0:required cmdata_root
		// [o] field:0:required output_dir
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

