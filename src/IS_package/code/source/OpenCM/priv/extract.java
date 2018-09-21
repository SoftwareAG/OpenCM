package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.Node;
import org.opencm.util.LogUtils;
import org.opencm.security.KeyUtils;
import org.opencm.extract.configuration.ExtractProperties;
import org.opencm.extract.spm.SpmOps;
// --- <<IS-END-IMPORTS>> ---

public final class extract

{
	// ---( internal utility methods )---

	final static extract _instance = new extract();

	static extract _newInstance() { return new extract(); }

	static extract _cast(Object o) { return (extract)o; }

	// ---( server methods )---




	public static final void run (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(run)>> ---
		// @sigtype java 3.5
		// [i] field:0:required node
		IDataCursor pipelineCursor = pipeline.getCursor(); 
		String	stNode = IDataUtil.getString( pipelineCursor, "node" );
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Extraction Process Started... ========== ");
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Extraction: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("OpenCM.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM extract:run :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Ensure Encrypted passwords ...
		// --------------------------------------------------------------------
		nodes.ensureDecryptedPasswords(opencmConfig);
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Defining the Nodes for Extraction based on: " + stNode);
		
		// --------------------------------------------------------------------
		// Define which nodes to extract 
		// --------------------------------------------------------------------
		LinkedList<Node> lNodes = new LinkedList<Node>();
		if (stNode.equals("PROPS")) {
			// Use defined Properties as per config file 
			// --------------------------------------------------------------------
			// Read in OpenCM Extract Properties
			// --------------------------------------------------------------------
			ExtractProperties extractProps = ExtractProperties.instantiate(opencmConfig);
			
			if (extractProps.getExtractAll()) {
				// --------------------------------------------------------------------
				// All CCE nodes 
				// --------------------------------------------------------------------
				lNodes.addAll(nodes.getNodes());
			} else {
				if ((extractProps.getOpencm_environments() != null) && !extractProps.getOpencm_environments().isEmpty()) {
					for (int i = 0; i < extractProps.getOpencm_environments().size(); i++) { 
						String env = extractProps.getOpencm_environments().get(i);
						lNodes.addAll(nodes.getNodesByEnvironment(env));
					}
				}
				if ((extractProps.getNodes() != null) && !extractProps.getNodes().isEmpty()) {
					for (int i = 0; i < extractProps.getNodes().size(); i++) { 
						Node opencmNode = nodes.getNode(extractProps.getNodes().get(i));
						if (opencmNode == null) {
							LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"No OpenCM Node defined for .. " + extractProps.getNodes().get(i));
						} else {
							lNodes.add(opencmNode);
						}
					}
				}
			}
			
		} else {
			// Single node extract
			lNodes.add(nodes.getNode(stNode));
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Ready for Extraction: " + lNodes.size() + " to process...");
		
		// --------------------------------------------------------------------
		// Extract All nodes
		// --------------------------------------------------------------------
		for (int i = 0; i < lNodes.size(); i++) {
			Node opencmNode = lNodes.get(i);
			SpmOps spm = new SpmOps(opencmConfig, opencmNode);
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Extracting Node .. " + opencmNode.getNode_name());
			spm.extractAll();
			spm.persist(); 
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Extraction Process Completed... ========== ");
			
		// --- <<IS-END>> ---

                
	}
}

