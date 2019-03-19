package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.model.*;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.Installation;
import org.opencm.util.LogUtils;
import org.opencm.security.KeyUtils;
import org.opencm.extract.configuration.*;
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
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Ensure Encrypted passwords ...
		// --------------------------------------------------------------------
		inv.ensureEncryptedPasswords(opencmConfig);
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Extraction job parameter: " + stNode);
		
		// --------------------------------------------------------------------
		// Define which nodes to extract 
		// --------------------------------------------------------------------
		LinkedList<Installation> installations = new LinkedList<Installation>();
		
		if (stNode.equals("PROPS")) {
			// --------------------------------------------------------------------
			// Read in OpenCM Extract Properties
			// --------------------------------------------------------------------
			ExtractConfiguration extractConfig = ExtractConfiguration.instantiate(opencmConfig);
			LinkedList<Organisation> extractOrgs = extractConfig.getExtract();
			for (int o = 0; o < extractOrgs.size(); o++) {
				Organisation extractOrg = extractOrgs.get(o);
				if ((extractOrg.getDepartments() == null) || (extractOrg.getDepartments().size() == 0)) {
					// Collect all nodes defined under this Org
					LinkedList<Installation> invNodes = inv.getInstallations(extractOrg.getOrg(),null,null);
					installations = addInstallations(installations, invNodes);
				} else {
					LinkedList<Department> extractDeps = extractOrg.getDepartments();
					for (int d = 0; d < extractDeps.size(); d++) {
						Department extractDep = extractDeps.get(d);
						if ((extractDep.getEnvironments() == null) || (extractDep.getEnvironments().size() == 0)) {
							// Collect all nodes defined under this Dep
							LinkedList<Installation> invNodes = inv.getInstallations(extractOrg.getOrg(),extractDep.getDep(),null);
							installations = addInstallations(installations, invNodes);
						} else {
							LinkedList<Environment> extractEnvs = extractDep.getEnvironments();
							for (int e = 0; e < extractEnvs.size(); e++) {
								Environment extractEnv = extractEnvs.get(e);
								if ((extractEnv.getNodes() == null) || (extractEnv.getNodes().size() == 0)) {
									// Collect all nodes defined under this Env
									LinkedList<Installation> invNodes = inv.getInstallations(extractOrg.getOrg(),extractDep.getDep(),extractEnv.getEnv());
									installations = addInstallations(installations, invNodes);
								} else {
									// Add individual nodes
									LinkedList<String> extractNodes = extractEnv.getNodes();
									for (int n = 0; n < extractNodes.size(); n++) {
										installations = addInstallation(installations, inv.getInstallation(extractNodes.get(n)));
									}
								}
							}
						}
					}
				}
			}
				
		} else {
			// Single node extract
			installations.add(inv.getInstallation(stNode));
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Ready for Extraction: " + installations.size() + " to process...");
		
		// --------------------------------------------------------------------
		// Extract All nodes
		// --------------------------------------------------------------------
		for (int i = 0; i < installations.size(); i++) {
			Installation installation = installations.get(i);
			SpmOps spm = new SpmOps(opencmConfig, inv, installation);
			if (spm.getNode() != null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Extracting Node .. " + installation.getName());
				spm.extractAll();
				spm.persist(); 
			}
		}
			
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Extraction Process Completed... ========== ");
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static LinkedList<Installation> addInstallation(LinkedList<Installation> nodeList, Installation inst) {
		if ((inst != null) && !nodeList.contains(inst)) {
			nodeList.add(inst);
		}
		return nodeList;
	}
	private static LinkedList<Installation> addInstallations(LinkedList<Installation> nodeList, LinkedList<Installation> toAdd) {
		for (int i = 0; i < toAdd.size(); i++) {
			Installation inst = toAdd.get(i);
			if (!nodeList.contains(inst)) {
				nodeList.add(inst);
			}
		}
		return nodeList;
	}
		
	// --- <<IS-END-SHARED>> ---
}

