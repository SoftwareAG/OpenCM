package org.opencm.extract.cce;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.opencm.configuration.Configuration;
import org.opencm.configuration.Node;
import org.opencm.configuration.RuntimeComponent;
import org.opencm.util.HttpClient;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
import com.wm.app.b2b.server.ServiceException;

public class CceOps {
	
	public static String CCE_INSTANCE_NAME 	= "CCE";
	
	private static String CCE_URI_ENVIRONMENTS 	= "/cce/landscape/environments";
	private static String CCE_URI_NODES			= "/cce/landscape/nodes"; 

	private static String CCE_JSONPATH_ENVIRONMENT 	= "/environment"; 
	private static String CCE_JSONFIELD_ENVIRONMENT	= "@alias"; 

	private static String CCE_JSONPATH_NODE 	= "/node"; 
	private static String CCE_JSONFIELD_NODE	= "@alias";

	private static String CCE_JSONPATH_ENVNODE 	= "/nodeAlias"; 

	private Configuration opencmConfig;
	private HttpClient client;
	private String baseURL;

	public CceOps(Configuration opencmConfig, Node node) {
		RuntimeComponent cceInstance = node.getRuntimeComponent(CCE_INSTANCE_NAME);
		this.opencmConfig = opencmConfig;
		this.baseURL = cceInstance.getProtocol() + "://" + node.getHostname() + ":" + cceInstance.getPort();
		this.client = new HttpClient();
		this.client.setCredentials(cceInstance.getUsername(), cceInstance.getDecryptedPassword());
		this.client.setJsonContent();
	}

	public LinkedList<String> getEnvironments() {
		LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting Environments .... ");
		LinkedList<String> lEnv = new LinkedList<String>();
		this.client.setURL(this.baseURL + CCE_URI_ENVIRONMENTS);
		this.client.setJsonContent();
		try {
			client.get();
			if (client.getStatusCode() != 200) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"OpenCM getEnvironments :: " + client.getURL() + " - " + client.getStatusLine());
			}
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Response: " + client.getResponse());
			JsonUtils jUtil = new JsonUtils(client.getResponse());
			HashMap<String,String> arrComp = jUtil.getArray(CCE_JSONPATH_ENVIRONMENT, CCE_JSONFIELD_ENVIRONMENT);
			Iterator<String> arrIterator = arrComp.keySet().iterator();
			while (arrIterator.hasNext()) {
				String envName = arrIterator.next();
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Discovered environment " + envName);
				lEnv.add(envName);
			}
		} catch (ServiceException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM getEnvironments :: " + ex.toString());
		}
		return lEnv;
	}

	public LinkedList<String> getAllNodes() {
		LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting All nodes ... ");
		this.client.setURL(this.baseURL + CCE_URI_NODES);
		LinkedList<String> lNodes = new LinkedList<String>();
		this.client.setJsonContent();
		try {
			client.get();
			if (client.getStatusCode() != 200) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"OpenCM getNodes(env) :: " + client.getURL() + " - " + client.getStatusLine());
			}
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Response: " + client.getResponse());
			JsonUtils jUtil = new JsonUtils(client.getResponse());
			HashMap<String,String> arrNodes = jUtil.getArray(CCE_JSONPATH_NODE, CCE_JSONFIELD_NODE);
			Iterator<String> arrIterator = arrNodes.keySet().iterator();
			while (arrIterator.hasNext()) {
				String nodeName = arrIterator.next();
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Discovered node " + nodeName);
				lNodes.add(nodeName);
			}
		} catch (ServiceException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM getNodes :: " + ex.toString());
		}
		return lNodes;
	}
	
	public LinkedList<String> getEnvironmentNodes(String environmentName) {
		LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting Nodes based on " + environmentName + " ...");
		this.client.setURL(this.baseURL + CCE_URI_ENVIRONMENTS + "/" + environmentName);
		LinkedList<String> lNodes = new LinkedList<String>();
		this.client.setJsonContent();
		try {
			client.get();
			if (client.getStatusCode() != 200) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"OpenCM getNodes(env) :: " + client.getURL() + " - " + client.getStatusLine());
			}
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Response: " + client.getResponse());
			JsonUtils jUtil = new JsonUtils(client.getResponse());
			lNodes = jUtil.getEnvironmentNodesArray(CCE_JSONPATH_ENVNODE);
		} catch (ServiceException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM getNodes :: " + ex.toString());
		}
		return lNodes;
	}

}