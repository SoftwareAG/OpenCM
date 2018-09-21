	package org.opencm.util;

	import java.util.HashMap;
	import java.util.Iterator;
	import java.util.LinkedList;

	import org.opencm.configuration.Configuration;
	import org.opencm.configuration.Node;
	import org.opencm.configuration.Nodes;
	import org.opencm.configuration.RuntimeComponent;

	public class CceUtils {
		
		private static String CCE_URI_NODES 			= "/cce/landscape/nodes";
		private static String CCE_URI_ENVIRONMENTS 		= "/cce/landscape/environments";
		private static String CCE_URI_ASSIGN_NODE 		= "/nodes";
		private static String CCE_PARAM_URL 			= "url";
		private static String CCE_PARAM_ALIAS 			= "alias";
		private static String CCE_PARAM_NODE_NAME		= "name";
		private static String CCE_PARAM_NODE_ALIAS 		= "nodeAlias";

		private static String CCE_JSONPATH_ENVIRONMENT 	= "/environment"; 
		private static String CCE_JSONFIELD_ENVIRONMENT	= "@alias"; 

		// private static String CCE_JSONPATH_NODE 	= "/node"; 
		// private static String CCE_JSONFIELD_NODE	= "@alias";

		private static String CCE_JSONPATH_ENVNODE 	= "/nodeAlias"; 

		private Configuration opencmConfig;
		private HttpClient client;
		private String baseURL;
		private Node cceNode;
		private RuntimeComponent cceRuntimeComponent;

		public CceUtils(Configuration opencmConfig, Nodes opencmNodes) {
			this.opencmConfig = opencmConfig;
			this.cceNode = opencmNodes.getNode(opencmConfig.getCce_mgmt_node());
			this.cceRuntimeComponent = cceNode.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_CCE);
			this.baseURL = cceRuntimeComponent.getProtocol() + "://" + cceNode.getHostname() + ":" + cceRuntimeComponent.getPort();
			this.client = new HttpClient();
			this.client.setCredentials(cceRuntimeComponent.getUsername(), cceRuntimeComponent.getDecryptedPassword());
		}
		
		public void createNode(String nodeAlias, String url) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: createNode :: " + nodeAlias);
			this.client.setURL(this.baseURL + CCE_URI_NODES);
			this.client.addParameter(CCE_PARAM_ALIAS, nodeAlias);
			this.client.addParameter(CCE_PARAM_URL, url);
			try {
				client.post();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: createNode :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: createNode :: Response: " + client.getResponse());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: createNode :: " + ex.toString());
			}
			this.client.flushParameters();
		}

		public void assignNodeToEnv(String envName, String nodeAlias) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: assignNodeToEnv :: " + envName);
			String url = this.baseURL + CCE_URI_ENVIRONMENTS + "/" + envName + CCE_URI_ASSIGN_NODE;
			this.client.setURL(url);
			this.client.addParameter(CCE_PARAM_NODE_ALIAS, nodeAlias);
			try {
				client.post();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: assignNodeToEnv :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: assignNodeToEnv :: Response: " + client.getResponse());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: assignNodeToEnv :: " + ex.toString());
			}
			
			this.client.flushParameters();
		}
		
		public void updateNodeName(String nodeAlias, String nodeName) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: updateNodeName :: " + nodeName);
			this.client.setURL(this.baseURL + CCE_URI_NODES + "/" + nodeAlias);
			this.client.addParameter(CCE_PARAM_NODE_NAME, nodeName);
			try {
				client.put();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: updateNodeName :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: updateNodeName :: Response: " + client.getResponse());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: updateNodeName :: " + ex.toString());
			}
			this.client.flushParameters();
			
		}
		
		public void createEnvironment(String envName) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: createEnvironment :: " + envName);
			this.client.setURL(this.baseURL + CCE_URI_ENVIRONMENTS);
			this.client.addParameter(CCE_PARAM_ALIAS, envName);
			try {
				client.post();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: createEnvironment :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: createEnvironment :: Response: " + client.getResponse());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: createEnvironment :: " + ex.toString());
			}
			this.client.flushParameters();
		}

		public void deleteEnvironment(String envName) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: deleteEnvironment :: " + envName);
			this.client.setURL(this.baseURL + CCE_URI_ENVIRONMENTS + "/" + envName);
			try {
				client.delete();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: deleteEnvironment :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: deleteEnvironment :: Response: " + client.getResponse());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: deleteEnvironment :: " + ex.toString());
			}
		}
		
		public void deleteAllEnvironments() {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: deleteAllEnvironments :: ");
			this.client.setURL(this.baseURL + CCE_URI_ENVIRONMENTS);
			try {
				client.delete();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: deleteAllEnvironments :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: deleteAllEnvironments :: Response: " + client.getResponse());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: deleteAllEnvironments :: " + ex.toString());
			}
		}
		
		public void deleteAllNodes() {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: deleteAllNodes :: ");
			this.client.setURL(this.baseURL + CCE_URI_NODES);
			try {
				client.delete();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: deleteNode :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: deleteNode :: Response: " + client.getResponse());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: deleteNode :: " + ex.toString());
			}
		}
		
		public void deleteNode(String nodeAlias) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: deleteNode :: " + nodeAlias);
			this.client.setURL(this.baseURL + CCE_URI_NODES + "/" + nodeAlias);
			try {
				client.delete();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: deleteNode :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: deleteNode :: Response: " + client.getResponse());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: deleteNode :: " + ex.toString());
			}
		}

		public LinkedList<String> getEnvironments() {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"CceUtils :: getEnvironments :: ");
			LinkedList<String> lEnv = new LinkedList<String>();
			this.client.setURL(this.baseURL + CCE_URI_ENVIRONMENTS);
			this.client.setJsonContent();
			try {
				client.get();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: getEnvironments ::  " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Response: " + client.getResponse());
				JsonUtils jUtil = new JsonUtils(client.getResponse());
				HashMap<String,String> arrComp = jUtil.getArray(CCE_JSONPATH_ENVIRONMENT, CCE_JSONFIELD_ENVIRONMENT);
				Iterator<String> arrIterator = arrComp.keySet().iterator();
				while (arrIterator.hasNext()) {
					String envName = arrIterator.next();
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: getEnvironments :: Discovered environment " + envName);
					lEnv.add(envName);
				}
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: getEnvironments :: " + ex.toString());
			}
			return lEnv;
			
		}
		
		public LinkedList<String> getEnvironmentNodes(String envName) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: getEnvironmentNodes :: Getting Nodes based on " + envName + " ...");
			this.client.setURL(this.baseURL + CCE_URI_ENVIRONMENTS + "/" + envName);
			LinkedList<String> lNodes = new LinkedList<String>();
			this.client.setJsonContent();
			try {
				client.get();
				if (client.getStatusCode() != 200) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"CceUtils :: getEnvironmentNodes :: " + client.getURL() + " - " + client.getStatusLine());
				}
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"CceUtils :: getEnvironmentNodes :: Response: " + client.getResponse());
				JsonUtils jUtil = new JsonUtils(client.getResponse());
				lNodes = jUtil.getEnvironmentNodesArray(CCE_JSONPATH_ENVNODE);
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"CceUtils :: getEnvironmentNodes :: " + ex.toString());
			}
			return lNodes;
		}
}