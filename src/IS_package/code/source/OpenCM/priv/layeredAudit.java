package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.LinkedList;
import java.util.HashMap;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.audit.configuration.Property;
import org.opencm.configuration.Nodes;
import org.opencm.util.LogUtils;
import org.opencm.configuration.Node;
import org.opencm.audit.util.ExcelWriter;
import org.opencm.audit.util.RepoParser;
import org.opencm.audit.env.*;
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
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes opencmNodes = Nodes.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Read in audit environment config Properties (specific for this audit)
		// --------------------------------------------------------------------
		AssertionConfig envAuditConfig = AssertionConfig.instantiate(opencmConfig,properties);
		
		// --------------------------------------------------------------------
		// Initialize
		// --------------------------------------------------------------------
		HashMap<String,AssertionGroup> assGroups = new HashMap<String,AssertionGroup>(); // Key = AssertionGroup name
		
		// --------------------------------------------------------------------
		// Process each Property Definition to Assert
		// --------------------------------------------------------------------
		for (int p = 0; p < envAuditConfig.getProperties().size(); p++) {
			Property propConfig = envAuditConfig.getProperties().get(p);
			// --------------------------------------------------------------------
			// Process each assertion group
			// --------------------------------------------------------------------
			LinkedList<String> lGroups;
			if ((envAuditConfig.getAssertionGroups() != null) && (envAuditConfig.getAssertionGroups().size() > 0)) {
				// Only process the defined groups
				lGroups = envAuditConfig.getAssertionGroups();
			} else {
				// Process all groups (as defined in the nodes.properties)
				lGroups = opencmNodes.getAllAssertionGroups();
			}
			for (int i = 0; i < lGroups.size(); i++) {
				String assGroup = lGroups.get(i);
				assGroups.put(assGroup, new AssertionGroup(assGroup));
				
				// --------------------------------------------------------------------
				// For Each Group, process the list of nodes to assert
				// --------------------------------------------------------------------
				LinkedList<Node> lNodes = opencmNodes.getNodesByGroup(assGroup);
				for (int n = 0; n < lNodes.size(); n++) {
					Node opencmNode = lNodes.get(n);
					opencmNode.setRepositoryType(Configuration.OPENCM_RUNTIME_DIR);
					boolean includeEnv = true;
					if ((envAuditConfig.getEnvironments() != null) && (envAuditConfig.getEnvironments().size() > 0)) {
						// Only process nodes that are within the defined environment
						if (!envAuditConfig.getEnvironments().contains(opencmNode.getOpencm_environment())) {
							includeEnv = false;
						}
					}
					if (includeEnv) {
						LinkedList<AssertionValue> avs = RepoParser.getAssertionValues(opencmConfig, opencmNode, propConfig, envAuditConfig.getPropertyFilters());
						for (int v = 0; v < avs.size(); v++) {
							assGroups.get(assGroup).addAssertionValue(opencmConfig, opencmNode, avs.get(v), true);
						}
					}
				}
			}
		}
		
		/***********************************
		 * The resulting hashmap contains extracted values where present
		 * However, we need to identify assertion values that are missing:
		 * 	They can either be undefined (no nodes present) or missing data
		 ***********************************/
		HashMap<String,AssertionGroup> postProcessedGroups = RepoParser.postProcessValues(opencmConfig, opencmNodes, assGroups, envAuditConfig);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Post Processed groups: " + postProcessedGroups.size());
		ExcelWriter ex = new ExcelWriter(opencmConfig);
		ex.writeAssertionGroups(opencmNodes, postProcessedGroups, properties, envAuditConfig);
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== Layered Audit - Process Completed ..... ============ ");
			
		// --- <<IS-END>> ---

                
	}
}

