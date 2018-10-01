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
import org.opencm.security.KeyUtils;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.Node;
import org.opencm.configuration.RuntimeComponent;
import org.opencm.util.LogUtils;
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
		// [i] field:0:required action {"encrypt","decrypt"}
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	stAction = IDataUtil.getString( pipelineCursor, "action" );
		pipelineCursor.destroy(); 
		
		/*
		 * Encrypt/Decrypt the nodes.properties passwords 
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========  " + stAction + "ing the passwords in nodes.properties... ========== ");
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," Security: Update Passwords: Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("OpenCM.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM updatePasswords :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Loop through nodes
		// --------------------------------------------------------------------
		boolean changed = false;
		for (int n = 0; n < nodes.getNodes().size(); n++) {
			Node node = nodes.getNodes().get(n);
			LinkedList<RuntimeComponent> runtimeComponents = node.getRuntimeComponents();
			for (int i = 0; i < runtimeComponents.size(); i++) {
				if (stAction.equals("encrypt")) {
					if (!runtimeComponents.get(i).passwordEncrypted()) {
						changed = true;
						runtimeComponents.get(i).encryptPassword();
					}
				} else {
					if (runtimeComponents.get(i).passwordEncrypted()) {
						changed = true;
						runtimeComponents.get(i).decryptPassword();
					}
					
				}
			}
		}
		
		// --------------------------------------------------------------------
		// Store nodes to config file
		// --------------------------------------------------------------------
		if (changed) {
			nodes.writeNodes(opencmConfig);
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========  " + stAction + "ing passwords completed ========== ");
			
		// --- <<IS-END>> ---

                
	}
}

