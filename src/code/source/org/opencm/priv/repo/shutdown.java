package org.opencm.priv.repo;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.repository.Repository;
import org.opencm.util.LogUtils;
// --- <<IS-END-IMPORTS>> ---

public final class shutdown

{
	// ---( internal utility methods )---

	final static shutdown _instance = new shutdown();

	static shutdown _newInstance() { return new shutdown(); }

	static shutdown _cast(Object o) { return (shutdown)o; }

	// ---( server methods )---




	public static final void run (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(run)>> ---
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," getInventory: Start Service");
		
		Repository repo = Repository.instantiate(opencmConfig);
		repo.shutdownDatabase();
		// --- <<IS-END>> ---

                
	}
}

