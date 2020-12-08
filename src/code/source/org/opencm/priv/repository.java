package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import org.opencm.repository.Repository;
import org.opencm.extract.ExtractConfiguration;
import org.opencm.repository.RepoInstallation;
import org.opencm.util.Cache;
import org.opencm.util.LogUtils;
import org.opencm.util.JsonUtils;
// --- <<IS-END-IMPORTS>> ---

public final class repository

{
	// ---( internal utility methods )---

	final static repository _instance = new repository();

	static repository _newInstance() { return new repository(); }

	static repository _cast(Object o) { return (repository)o; }

	// ---( server methods )---




	public static final void getRepository (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getRepository)>> ---
		// @sigtype java 3.5
		// [i] field:0:required path
		// [o] field:0:required repository_json
		IDataCursor pipelineCursor = pipeline.getCursor();
		String jsonPath = IDataUtil.getString( pipelineCursor, "path" );
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Get Repository");
		
		// --------------------------------------------------------------------
		// Verify parameters
		// --------------------------------------------------------------------
		if ((jsonPath == null) || jsonPath.equals("")) {
			LogUtils.logError("getRepository: path parameter missing. ");
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ArrayList<String> path = new ArrayList<String>();
		try {
			path = mapper.readValue(jsonPath, ArrayList.class); 
		} catch (Exception e) {
			LogUtils.logError("getRepository - Exception: " + e.toString());
			return;
		}
		
		LogUtils.logDebug("getRepository: Installation = " + path);
		
		// --------------------------------------------------------------------
		// Read in Repository
		// --------------------------------------------------------------------
		Repository repo = Repository.instantiate();
		
		RepoInstallation repoInstallation = repo.getInstallation(path);
		if (repoInstallation == null) {
			if (!Repository.repoExists(path)) {
				LogUtils.logError("getRepository: Runtime repo for " + path.toString() + " does not exist.");
				return;
			}
			// Go out and read from file system
			repoInstallation = repo.addInstallation(path);
		} else {
			LogUtils.logDebug("getRepository: Repo rerieved from cache :: " + path.toString() + ".");
		}
		
		// --------------------------------------------------------------------
		// Convert response to json
		// --------------------------------------------------------------------
		String jsonResponse;
		if (repoInstallation != null) {
			jsonResponse = JsonUtils.convertJavaObjectToJson(repoInstallation);
		} else {
			jsonResponse = JsonUtils.createJsonField("error", "Unable to generate Repository");
		}
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		LogUtils.logDebug("getRepository: " + jsonResponse);
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor2, "repository_json", jsonResponse);
		pipelineCursor.destroy();
		
		LogUtils.logDebug("Got Repository");
			
		// --- <<IS-END>> ---

                
	}



	public static final void initRepository (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(initRepository)>> ---
		// @sigtype java 3.5
		LogUtils.logDebug("Init Repository"); 
		
		// --------------------------------------------------------------------
		// Refresh Inventory 
		// --------------------------------------------------------------------
		Cache.getInstance().set(Repository.REPOSITORY_CACHE_KEY, null);
		Repository.instantiate();
		
		LogUtils.logDebug("Repository Initialized");
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---



	
	// --- <<IS-END-SHARED>> ---
}

