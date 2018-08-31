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
import org.opencm.configuration.Nodes;
import org.opencm.configuration.Node;
import org.opencm.configuration.Instance;
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
		// [i] field:0:required masterPassword
		// [i] field:0:required action {"encrypt","decrypt"}
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stMaster = IDataUtil.getString( pipelineCursor, "masterPassword" );
		String	stAction = IDataUtil.getString( pipelineCursor, "action" );
		pipelineCursor.destroy();
		
		/*
		 * Extract Service 
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
		
		// --------------------------------------------------------------------
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Loop through nodes
		// --------------------------------------------------------------------
		for (int n = 0; n < nodes.getNodes().size(); n++) {
			Node node = nodes.getNodes().get(n);
			LinkedList<Instance> instances = node.getInstances();
			for (int i = 0; i < instances.size(); i++) {
				if (stAction.equals("encrypt")) {
					if (!instances.get(i).passwordEncrypted()) {
						instances.get(i).encryptPassword(stMaster);
					}
				} else {
					if (instances.get(i).passwordEncrypted()) {
						instances.get(i).decryptPassword(stMaster);
					}
					
				}
			}
		}
		
		// --------------------------------------------------------------------
		// Store nodes to config file
		// --------------------------------------------------------------------
		nodes.writeNodes(opencmConfig);
			
		// --- <<IS-END>> ---

                
	}
}

