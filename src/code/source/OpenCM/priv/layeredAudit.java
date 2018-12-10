package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.model.*;
import org.opencm.inventory.Inventory;
import org.opencm.inventory.Installation;
import org.opencm.util.LogUtils;
import org.opencm.audit.env.*;
import org.opencm.audit.util.ExcelWriter;
import org.opencm.audit.configuration.Property;
import org.opencm.repository.util.RepoUtils;
// --- <<IS-END-IMPORTS>> ---

public final class layeredAudit

{
	// ---( internal utility methods )---

	final static layeredAudit _instance = new layeredAudit();

	static layeredAudit _newInstance() { return new layeredAudit(); }

	static layeredAudit _cast(Object o) { return (layeredAudit)o; }

	// ---( server methods )---




	public static final void run (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(run)>> ---
		// @sigtype java 3.5
		// [i] field:0:required properties
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	properties = IDataUtil.getString( pipelineCursor, "properties" );
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
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== Layered Audit - Starting Process ..... ============ ");
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Read in audit config Properties (specific for this audit)
		// --------------------------------------------------------------------
		AssertionConfig layeredAuditConfig = AssertionConfig.instantiate(opencmConfig,properties);
		
		// --------------------------------------------------------------------
		// Initialize
		// --------------------------------------------------------------------
		HashMap<String,AssertionGroup> assGroups = new HashMap<String,AssertionGroup>(); // Key = AssertionGroup name
		
		// --------------------------------------------------------------------
		// Process each Property Definition to Assert
		// --------------------------------------------------------------------
		for (int p = 0; p < layeredAuditConfig.getProperties().size(); p++) {
			Property propConfig = layeredAuditConfig.getProperties().get(p);
			// --------------------------------------------------------------------
			// Process each assertion group
			// --------------------------------------------------------------------
			LinkedList<String> lLayers;
			if ((layeredAuditConfig.getLayers() != null) && (layeredAuditConfig.getLayers().size() > 0)) {
				// Only process the defined groups
				lLayers = layeredAuditConfig.getLayers();
			} else {
				// Process all groups (as defined in the inventory)
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Layered Audit - No layers defined. Exiting ....");
				return;
			}
			
			// --------------------------------------------------------------------
			// For Each Layer, process the list of nodes to assert
			// --------------------------------------------------------------------
			for (int i = 0; i < lLayers.size(); i++) {
				String layer = lLayers.get(i);
				assGroups.put(layer, new AssertionGroup(layer));
				// --------------------------------------------------------------------
				// Get all nodes for this layer
				// --------------------------------------------------------------------
				LinkedList<Installation> lNodes = inv.getInstallationsByLayer(layer);
				
				// --------------------------------------------------------------------
				// Filter nodes by defined sublayers
				// --------------------------------------------------------------------
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Layered Audit - Filtering by sublayers. Before: " + lNodes.size());
				LinkedList<String> sublayers = layeredAuditConfig.getSublayers();
				if ((sublayers != null) && (sublayers.size() > 0)) {
					Iterator<Installation> it = lNodes.iterator();
					while (it.hasNext()) {
					    Installation inst = it.next();
						if ((inst.getSublayer() == null) || (!sublayers.contains(inst.getSublayer()))) {
							it.remove();
						}
					}					
				}
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Layered Audit - Filtering by sublayers. After: " + lNodes.size());
				
				// --------------------------------------------------------------------
				// Filter nodes by defined versions
				// --------------------------------------------------------------------
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Layered Audit - Filtering by versions. Before: " + lNodes.size());
				LinkedList<String> versions = layeredAuditConfig.getVersions();
				if ((versions != null) && (versions.size() > 0)) {
					Iterator<Installation> it = lNodes.iterator();
					while (it.hasNext()) {
					    Installation inst = it.next();
						if ((inst.getVersion() == null) || (!versions.contains(inst.getVersion()))) {
							it.remove();
						}
					}					
				}
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Layered Audit - Filtering by versions. After: " + lNodes.size());
				
				// --------------------------------------------------------------------
				// Filter nodes by defined audit config
				// --------------------------------------------------------------------
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Layered Audit - Filtering by audit config. Before: " + lNodes.size());
				LinkedList<Organisation> configModel = layeredAuditConfig.getAudit();
				LinkedList<Installation> configInstallations = inv.getInstallationsByConfigModel(configModel);
				Iterator<Installation> it = lNodes.iterator();
				while (it.hasNext()) {
				    Installation inst = it.next();
					if (!configInstallations.contains(inst)) {
						it.remove();
					}
				}					
				
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Layered Audit - Filtering by audit config. After: " + lNodes.size());
				
				if (lNodes.size() == 0) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"  Layered Audit - No nodes to process. Existing ... ");
					return;
				}
				
				// --------------------------------------------------------------------
				// Process
				// --------------------------------------------------------------------
				for (int n = 0; n < lNodes.size(); n++) {
					Installation opencmNode = lNodes.get(n);
					LinkedList<AssertionValue> avs = RepoUtils.getAssertionValues(opencmConfig, opencmNode, Configuration.OPENCM_RUNTIME_DIR, propConfig, layeredAuditConfig.getPropertyFilters());
					for (int v = 0; v < avs.size(); v++) {
						assGroups.get(layer).addAssertionValue(opencmConfig, opencmNode, avs.get(v), true);
					}
				}
			}
		}
		
		/***********************************
		 * The resulting hashmap contains extracted values where present
		 * However, we need to identify assertion values that are missing:
		 * 	They can either be undefined (no nodes present) or missing data
		 ***********************************/
		HashMap<String,AssertionGroup> postProcessedGroups = RepoUtils.postProcessValues(opencmConfig, inv, assGroups, layeredAuditConfig);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Layered Audit: Post Processed groups: " + postProcessedGroups.size());
		ExcelWriter ex = new ExcelWriter(opencmConfig);
		ex.writeAssertionGroups(inv, postProcessedGroups, properties, layeredAuditConfig);
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== Layered Audit - Process Completed ..... ============ ");
			
		// --- <<IS-END>> ---

                
	}
}

