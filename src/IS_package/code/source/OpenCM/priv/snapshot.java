package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.io.IOException;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.Node;
import org.opencm.util.LogUtils;
import org.opencm.util.FileUtils;
// --- <<IS-END-IMPORTS>> ---

public final class snapshot

{
	// ---( internal utility methods )---

	final static snapshot _instance = new snapshot();

	static snapshot _newInstance() { return new snapshot(); }

	static snapshot _cast(Object o) { return (snapshot)o; }

	// ---( server methods )---




	public static final void promoteSnapshot (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(promoteSnapshot)>> ---
		// @sigtype java 3.5
		// [i] field:0:required node
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	node = IDataUtil.getString( pipelineCursor, "node" );
		pipelineCursor.destroy();
		
		
		if (node == null) {
			throw new ServiceException("Missing Node Parameter");
		} 
		
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Promoting " + node + " to Baseline... ========== ");
		
		// --------------------------------------------------------------------
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes opencmNodes = Nodes.instantiate(opencmConfig);
		
		Node opencmNode = opencmNodes.getNode(node);
		if (opencmNode == null) {
			throw new ServiceException("Node is not defined in nodes.properties: " + node);
		}
		
		String baselineDir = opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_BASELINE_DIR;
		File fBaselineDir = new File(baselineDir);
		if (!fBaselineDir.exists()) {
			throw new ServiceException("Directory Does not Exist: " + fBaselineDir.getPath());
		}
		
		String baselineServerDir = baselineDir + File.separator + opencmNode.getUnqualifiedHostname();
		File fBaselineServerDir = new File(baselineServerDir);
		if (!fBaselineServerDir.exists()) {
			FileUtils.createDirectory(baselineServerDir);
		}
		
		String baselineNodeDir = baselineServerDir + File.separator + opencmNode.getNode_name();
		File fBaselineNodeDir = new File(baselineNodeDir);
		
		String runtimeDir = opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR;
		File fRuntimeDir = new File(runtimeDir);
		if (!fRuntimeDir.exists()) {
			throw new ServiceException("Directory Does not Exist: " + fRuntimeDir.getPath());
		}
		
		String runtimeServerDir = runtimeDir + File.separator + opencmNode.getUnqualifiedHostname();
		File fRuntimeServerDir = new File(runtimeServerDir);
		if (!fRuntimeServerDir.exists()) {
			throw new ServiceException("Directory Does not Exist: " + fRuntimeServerDir.getPath());
		}
		
		String runtimeNodeDir = runtimeServerDir + File.separator + opencmNode.getNode_name();
		File fRuntimeNodeDir = new File(runtimeNodeDir);
		if (!fRuntimeNodeDir.exists()) {
			throw new ServiceException("Directory Does not Exist: " + fRuntimeNodeDir.getPath());
		}
		
		try {
			// Remove the existing baseline node directory if it exists
			if (fBaselineNodeDir.exists()) {
				org.apache.commons.io.FileUtils.deleteDirectory(fBaselineNodeDir);
			}
			
			// Copy directory from runtime to baseline
			org.apache.commons.io.FileUtils.copyDirectoryToDirectory(fRuntimeNodeDir, fBaselineServerDir);
		} catch (IOException ex) {
			throw new ServiceException("OpenCM promoteSnapshot: " + ex.toString());
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Promotion Completed... ========== ");
			
		// --- <<IS-END>> ---

                
	}
}

