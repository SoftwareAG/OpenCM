package org.opencm.extract.spm;

import java.io.File;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.text.SimpleDateFormat;

import org.opencm.configuration.Configuration;
import org.opencm.configuration.Node;
import org.opencm.configuration.RuntimeComponent;
import org.opencm.extract.is.ISPackage;
import org.opencm.extract.is.JCEPolicyInfo;
import org.opencm.extract.is.JDBCAdapterConnection;
import org.opencm.extract.is.SAPAdapterConnection;
import org.opencm.extract.is.WxConfigInfo;
import org.opencm.extract.is.ScreenScraper;
import org.opencm.extract.configuration.ExtractNode;
import org.opencm.extract.configuration.ExtractComponent;
import org.opencm.extract.configuration.ExtractInstance;
import org.opencm.util.FileUtils;
import org.opencm.util.HttpClient;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;

import com.wm.app.b2b.server.ServiceException;


public class SpmOps {

	private static String SPM_PROP_FILENAME 		= "properties.json"; 
	private static String SPM_CONF_FILENAME 		= "ci-properties.json"; 

	private static String SPM_URI_PLATFORM 			= "/spm/inventory/platform";
	private static String SPM_URI_NODE_INFO			= "/spm/inventory/products/SPM"; 
	private static String SPM_URI_COMPONENT 		= "/spm/inventory/components"; 
	private static String SPM_URI_INSTANCE 			= "/spm/configuration/instances"; 
	private static String SPM_URI_PROPERTY 			= "/spm/configuration/data"; 
	
	private static String SPM_URI_PRODUCT 			= "/spm/inventory/products"; 
	private static String SPM_URI_FIXES 			= "/spm/inventory/fixes"; 
	
	private static String SPM_JSONPATH_COMPONENT 	= "/runtimeComponents/runtimeComponent"; 
	private static String SPM_JSONFIELD_COMPONENT	= "id"; 
	
	private static String SPM_JSONPATH_INSTANCE 	= "/ConfigurationInstances/ConfigurationInstance"; 
	private static String SPM_JSONFIELD_INSTANCE	= "id"; 

	public static String SPM_COMP_PRODUCT 			= "NODE-PRODUCTS"; 
	private static String SPM_JSONPATH_PRODUCT 		= "/products/product"; 
	private static String SPM_JSONFIELD_PRODUCT		= "id"; 

	private static String SPM_NODE_INSTALL 			= "/products/product/installTime"; 
	private static String SPM_NODE_VERSION 			= "/products/product/version"; 

	public static String SPM_COMP_FIXES 			= "NODE-FIXES"; 
	private static String SPM_JSONPATH_FIXES 		= "/fixes/fix"; 
	private static String SPM_JSONFIELD_FIX			= "id"; 

	public static String SPM_INST_PACKAGES 			= "IS-PACKAGES"; 
	public static String SPM_INST_JCE_INFO 			= "JCE-POLICY-INFO"; 
	public static String SPM_INST_SAP_CONNS 		= "SAP-ADAPTERS";
	public static String SPM_INST_JDBC_CONNS 		= "JDBC-ADAPTERS"; 
	public static String SPM_INST_WXCONFIG_INFO		= "WXCONFIG-INFO"; 

	private static String SPM_JSONFIELD_EXTRACT_ALIAS	= "extractAlias"; 
	
	private Configuration opencmConfig;
	private Node opencmNode;
	private ExtractNode extractNode;
	private HttpClient client;
	private String baseURL;
	
	public SpmOps(Configuration opencmConfig, Node node) {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," SpmOps - Preparing for Extraction " + node.getNode_name());
		this.opencmConfig = opencmConfig;
		this.opencmNode = node;
		
		RuntimeComponent spmRuntimeComponent = opencmNode.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
		if (spmRuntimeComponent == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," SpmOps - SPM Node cannot be retrieved from configuration:" + opencmNode.getNode_name());
			return;
		}
		this.extractNode = new ExtractNode(opencmNode.getNode_name());
		SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.extractNode.setExtractionDate(time_formatter.format(System.currentTimeMillis()));
		this.baseURL = spmRuntimeComponent.getProtocol() + "://" + opencmNode.getHostname() + ":" + spmRuntimeComponent.getPort();
		this.client = new HttpClient();
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," SpmOps - Setting Credentials .... ");
		this.client.setCredentials(spmRuntimeComponent.getUsername(), spmRuntimeComponent.getDecryptedPassword());
	}

	public ExtractNode getNode() {
		return this.extractNode;
	}

	public void extractAll() {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," SpmOps - Extracting Node " + this.extractNode.getAlias());
		try {
			// ----------------------------------
			// Get SPM platform info
			// ----------------------------------
			this.extractNode.setPlatformInfo(getPlatformInfo());
			if (this.extractNode.getPlatformInfo() == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Unable to get Platform Info, exiting ... ");
				return;
			}
	
			// ----------------------------------
			// Get relevant SPM node info
			// ----------------------------------
			String nodeJson = getNodeInfo();
			this.extractNode.setInstallTime(JsonUtils.getJsonValue(nodeJson, SPM_NODE_INSTALL));
			this.extractNode.setVersion(JsonUtils.getJsonValue(nodeJson, SPM_NODE_VERSION));
	
			// ----------------------------------
			// Loop through all SPM discovered components and instances and gets Instance data
			// ----------------------------------
			LinkedList<ExtractComponent> components = getComponents();
			if (components != null) {
				for (int c = 0; c < components.size(); c++) {
					LinkedList<ExtractInstance> instances = getInstances(components.get(c).getName());
					for (int i = 0; i < instances.size(); i++) {
						String instanceData = getInstanceData(components.get(c),instances.get(i));
						if (instanceData != null) {
							instances.get(i).setData(instanceData);
							components.get(c).addInstance(instances.get(i));
						}
					}
					this.extractNode.addComponent(components.get(c));
				}
			}
			
			// ----------------------------------
			// Extract Products
			// ----------------------------------
			ExtractComponent products = getProducts();
			if (products != null) {
				this.extractNode.addComponent(products);
			}			
			// ----------------------------------
			// Extract Fixes
			// ----------------------------------
			ExtractComponent fixes = getFixes();
			if (fixes != null) {
				this.extractNode.addComponent(fixes);
			}			
		} catch (ServiceException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Exception during extraction ... " + ex.toString());
			this.extractNode.setPlatformInfo(null);
		}
	}
	
	private String getPlatformInfo() throws ServiceException {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting PlatformInfo for " + this.extractNode.getAlias());
		this.client.setURL(this.baseURL + SPM_URI_PLATFORM);
		this.client.setJsonContent();
		client.get();
		if (client.getStatusCode() == 500) {
			throw new ServiceException(client.getStatusLine() + " for URL " + client.getURL());
		} else if (client.getStatusCode() == 406) {
			// ----------------------------
			// Try with XML or plain text
			// ----------------------------
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"HTTP 406 for " + this.client.getURL() + ", trying XML ...");
			this.client.setXmlorTextContent();
			client.get();
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Http Response Status: " + client.getStatusLine());
			client.setResponse(JsonUtils.convertToJson(client.getResponse()));
		}
		if (client.getStatusCode() != 200) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"getPlatformInfo :: " + client.getURL() + " - " + client.getStatusLine());
			return null;
		}
		
		// -----------------------------------------------------
		// Add Local OpenCM node name to platform info
		// -----------------------------------------------------
		String platformInfo = client.getResponse();
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Platform INFO: " + platformInfo);
		String localOpencmNode = opencmConfig.getSynch_local_opencm_node();
		if (localOpencmNode == null) {
			// default
			localOpencmNode = "OPENCM";
		}
		return JsonUtils.addField(platformInfo, SPM_JSONFIELD_EXTRACT_ALIAS, localOpencmNode);
	}

	private String getNodeInfo() throws ServiceException {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting NodeInfo for " + this.extractNode.getAlias());
		this.client.setURL(this.baseURL + SPM_URI_NODE_INFO);
		this.client.setJsonContent();
		client.get();
		if (client.getStatusCode() == 500) {
			throw new ServiceException(client.getStatusLine() + " for URL " + client.getURL());
		} else if (client.getStatusCode() == 406) {
			// ----------------------------
			// Try with XML or Plain Text
			// ----------------------------
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"HTTP 406 for " + this.client.getURL() + ", trying XML ...");
			this.client.setXmlorTextContent();
			client.get();
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Http Response Status: " + client.getStatusLine());
			client.setResponse(JsonUtils.convertToJson(client.getResponse()));
		}
		if (client.getResponse() == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"getNodeInfo :: Unable to get Platform info string ...");
		}
		if (client.getStatusCode() != 200) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"getNodeInfo :: " + client.getURL() + " - " + client.getStatusLine());
		}
		return client.getResponse();
	}

	private LinkedList<ExtractComponent> getComponents() throws ServiceException {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting Components for " + this.extractNode.getAlias());
		LinkedList<ExtractComponent> lComp = new LinkedList<ExtractComponent>();
		this.client.setURL(this.baseURL + SPM_URI_COMPONENT);
		this.client.setJsonContent();
		try {
			client.get();
		} catch (ServiceException ex) {
			throw new ServiceException("getComponents() " + ex.getMessage());
		}
		if (client.getStatusCode() == 500) {
			throw new ServiceException(client.getStatusLine() + " for URL " + client.getURL());
		} else if (client.getStatusCode() != 200) { 
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"OpenCM getComponents :: " + client.getURL() + " - " + client.getStatusLine());
		} else {
			JsonUtils jUtil = new JsonUtils(client.getResponse());
			HashMap<String,String> arrComp = jUtil.getArray(SPM_JSONPATH_COMPONENT, SPM_JSONFIELD_COMPONENT);
			Iterator<String> arrIterator = arrComp.keySet().iterator();
			while (arrIterator.hasNext()) {
				String compName = arrIterator.next();
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Discovered component " + compName);
				ExtractComponent spmComponent = new ExtractComponent(compName);
				spmComponent.setInfo(arrComp.get(compName));
				lComp.add(spmComponent);
			}
		}
		return lComp;
	}
	
	private LinkedList<ExtractInstance> getInstances(String componentName) throws ServiceException {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting Instances for " + componentName + " ...");
		LinkedList<ExtractInstance> lInst = new LinkedList<ExtractInstance>();
		this.client.setURL(this.baseURL + SPM_URI_INSTANCE + "/" + componentName);
		this.client.setJsonContent();
		try {
			client.get();
		} catch (ServiceException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"getInstances :: Gracefully ignoring " + componentName + " :: " + ex.getMessage());
		}
		if (client.getStatusCode() != 200) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"getInstances :: Gracefully ignoring " + componentName + " :: " + client.getStatusLine() + " for URL " + client.getURL());
		} else {
			JsonUtils jUtil = new JsonUtils(client.getResponse());
			HashMap<String,String> arrInst = jUtil.getArray(SPM_JSONPATH_INSTANCE, SPM_JSONFIELD_INSTANCE);
			Iterator<String> arrIterator = arrInst.keySet().iterator();
			while (arrIterator.hasNext()) {
				String instName = arrIterator.next();
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Discovered instance " + instName);
				ExtractInstance spmInstance = new ExtractInstance(instName);
				spmInstance.setInfo(arrInst.get(instName));
				lInst.add(spmInstance);
			}
			// ------------------------------------
			// Include IS Additional extractions 
			// ------------------------------------
			if (componentName.startsWith("integrationServer-")) {
				RuntimeComponent isRuntimeComponent = this.opencmNode.getRuntimeComponent(componentName);
				if (isRuntimeComponent == null) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING,"No Node Instance defined for " + componentName);
				} else {
					// Package information
					ExtractInstance isPkgInstance = new ExtractInstance(SPM_INST_PACKAGES);
					isPkgInstance.setInfo("{\"id\":\"" + SPM_INST_PACKAGES + "\","
										+ "\"configurationTypeId\":\"" + SPM_INST_PACKAGES + "\","
										+ "\"displayName\":\"Integration Server packages\","
										+ "\"description\":\"Extracted using custom screen scraping\","
										+ "\"runtimeComponentId\":\"" + componentName + "\"}");
					lInst.add(isPkgInstance);
					// JCE Policy information
					ExtractInstance jceInfoInstance = new ExtractInstance(SPM_INST_JCE_INFO);
					jceInfoInstance.setInfo("{\"id\":\"" + SPM_INST_JCE_INFO + "\","
										+ "\"configurationTypeId\":\"" + SPM_INST_JCE_INFO + "\","
										+ "\"displayName\":\"Integration Server JCE Policy Info\","
										+ "\"description\":\"Extracted using custom screen scraping\","
										+ "\"runtimeComponentId\":\"" + componentName + "\"}");
					lInst.add(jceInfoInstance);
					// SAP Adapter information
					ExtractInstance sapInfoInstance = new ExtractInstance(SPM_INST_SAP_CONNS);
					sapInfoInstance.setInfo("{\"id\":\"" + SPM_INST_SAP_CONNS + "\","
										+ "\"configurationTypeId\":\"" + SPM_INST_SAP_CONNS + "\","
										+ "\"displayName\":\"Integration Server SAP Adapter Info\","
										+ "\"description\":\"Extracted using custom screen scraping\","
										+ "\"runtimeComponentId\":\"" + componentName + "\"}");
					lInst.add(sapInfoInstance);
					// JDBC Adapter information
					ExtractInstance jdbcInfoInstance = new ExtractInstance(SPM_INST_JDBC_CONNS);
					jdbcInfoInstance.setInfo("{\"id\":\"" + SPM_INST_JDBC_CONNS + "\","
										+ "\"configurationTypeId\":\"" + SPM_INST_JDBC_CONNS + "\","
										+ "\"displayName\":\"Integration Server JDBC Adapter Info\","
										+ "\"description\":\"Extracted using custom screen scraping\","
										+ "\"runtimeComponentId\":\"" + componentName + "\"}");
					lInst.add(jdbcInfoInstance);
					// JDBC Adapter information
					ExtractInstance wxConfigInfoInstance = new ExtractInstance(SPM_INST_WXCONFIG_INFO);
					wxConfigInfoInstance.setInfo("{\"id\":\"" + SPM_INST_WXCONFIG_INFO + "\","
										+ "\"configurationTypeId\":\"" + SPM_INST_WXCONFIG_INFO + "\","
										+ "\"displayName\":\"Integration Server WxConfig Info\","
										+ "\"description\":\"Extracted using custom screen scraping\","
										+ "\"runtimeComponentId\":\"" + componentName + "\"}");
					lInst.add(wxConfigInfoInstance);
				}
			}
		}
		return lInst;
	}
	
	private String getInstanceData(ExtractComponent component, ExtractInstance instance) throws ServiceException {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting Configuration Data for " + component.getName() + " - " + instance.getName() + " ...");
		if (instance.getName().equals(SPM_INST_PACKAGES)) {
			return getISPackageInfo(component);
		}
		if (instance.getName().equals(SPM_INST_JCE_INFO)) {
			return getJCEPolicyInfo(component);
		}
		if (instance.getName().equals(SPM_INST_SAP_CONNS)) {
			return getSAPAdapterInfo(component);
		}
		if (instance.getName().equals(SPM_INST_JDBC_CONNS)) {
			return getJDBCAdapterInfo(component);
		}
		if (instance.getName().equals(SPM_INST_WXCONFIG_INFO)) {
			return getWxConfigInfo(component);
		}
		this.client.setURL(this.baseURL + SPM_URI_PROPERTY + "/" + component.getName() + "/" + instance.getName());
		this.client.setJsonContent();
		try {
			client.get();
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Http Response Status: " + client.getStatusLine());
			if (client.getStatusCode() == 500) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"getInstanceData :: Gracefully ignoring property ..." + instance.getName() + " - " + client.getStatusLine());
				return null;
			} else if (client.getStatusCode() == 409) {
				// Indicates that the component is in STOPPED state...
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"getInstanceData :: Gracefully ignoring property ..." + instance.getName() + " - " + client.getStatusLine());
				return null;
			} else if (client.getStatusCode() == 406) {
				// ----------------------------
				// Try with XML or plain text
				// ----------------------------
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"HTTP 406 for " + this.client.getURL() + ", trying XML ...");
				this.client.setXmlorTextContent();
				client.get();
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Http Response Status: " + client.getStatusLine());
				client.setResponse(JsonUtils.convertToJson(client.getResponse()));
			}
			if (client.getResponse() == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"getInstanceData :: Gracefully ignoring property ..." + component.getName() + "-" + instance.getName() + " - " + client.getStatusLine());
				return null;
			}
		} catch (ServiceException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"getInstanceData :: Gracefully ignoring HTTP exception :: " + component.getName() + "-" + instance.getName() + " - " + ex.getMessage());
		}
		return client.getResponse();
	}

	private String getISPackageInfo(ExtractComponent component) throws ServiceException {
		RuntimeComponent isRuntimeComponent = this.opencmNode.getRuntimeComponent(component.getName());
		ScreenScraper isScreenScraper = new ScreenScraper(this.opencmConfig, this.opencmNode.getHostname(), isRuntimeComponent);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getISPackageInfo :: " + component.getName());
		LinkedList<ISPackage> llPackages = isScreenScraper.getPackages();
		if (llPackages == null || llPackages.isEmpty()) {
			return null;
		}
		return JsonUtils.convertJavaObjectToJson(llPackages);
	}

	private String getJCEPolicyInfo(ExtractComponent component) throws ServiceException {
		RuntimeComponent isRuntimeComponent = this.opencmNode.getRuntimeComponent(component.getName());
		ScreenScraper isScreenScraper = new ScreenScraper(this.opencmConfig, this.opencmNode.getHostname(), isRuntimeComponent);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getJCEPolicyInfo :: " + component.getName());
		JCEPolicyInfo jceInfo = isScreenScraper.getJCEInfo();
		return JsonUtils.convertJavaObjectToJson(jceInfo);
	}
	
	private String getSAPAdapterInfo(ExtractComponent component) throws ServiceException {
		RuntimeComponent isRuntimeComponent = this.opencmNode.getRuntimeComponent(component.getName());
		ScreenScraper isScreenScraper = new ScreenScraper(this.opencmConfig, this.opencmNode.getHostname(), isRuntimeComponent);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getSAPAdapterInfo :: " + component.getName());
		LinkedList<SAPAdapterConnection> lladapters = isScreenScraper.getSAPAdapterConnections();
		if (lladapters == null || lladapters.isEmpty()) {
			return null;
		}
		return JsonUtils.convertJavaObjectToJson(lladapters);
	}

	private String getJDBCAdapterInfo(ExtractComponent component) throws ServiceException {
		RuntimeComponent isRuntimeComponent = this.opencmNode.getRuntimeComponent(component.getName());
		ScreenScraper isScreenScraper = new ScreenScraper(this.opencmConfig, this.opencmNode.getHostname(), isRuntimeComponent);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getJDBCAdapterInfo :: " + component.getName());
		LinkedList<JDBCAdapterConnection> llPackages = isScreenScraper.getJDBCAdapterConnections(this.opencmNode.getVersion());
		if (llPackages == null || llPackages.isEmpty()) {
			return null;
		}
		return JsonUtils.convertJavaObjectToJson(llPackages);
	}
	
	private String getWxConfigInfo(ExtractComponent component) throws ServiceException {
		RuntimeComponent isRuntimeComponent = this.opencmNode.getRuntimeComponent(component.getName());
		ScreenScraper isScreenScraper = new ScreenScraper(this.opencmConfig, this.opencmNode.getHostname(), isRuntimeComponent);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getJCEPolicyInfo :: " + component.getName());
		WxConfigInfo wxConfigInfo = isScreenScraper.getWxConfigInfo();
		if (wxConfigInfo.getKeyValues() == null) {
			return null;
		}
		return JsonUtils.convertJavaObjectToJson(wxConfigInfo);
	}
	
	private ExtractComponent getProducts() throws ServiceException {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting Products ...");
		ExtractComponent spmComponent = new ExtractComponent(SPM_COMP_PRODUCT);
		// Hard coding Product extraction information
		spmComponent.setInfo("{\"category\":\"INSTALLED_PRODUCTS\","
							+ "\"displayName\":\"Installed Products\","
							+ "\"id\":\"NODE-PRODUCTS\","
							+ "\"productId\":\"webMethods\"}");

		this.client.setURL(this.baseURL + SPM_URI_PRODUCT);
		this.client.setJsonContent();
		try {
			client.get();
		} catch (ServiceException ex) {
			throw new ServiceException("getProducts() " + ex.getMessage());
		}
		if (client.getStatusCode() == 500) {
			throw new ServiceException(client.getStatusLine() + " for URL " + client.getURL());
		} else if (client.getStatusCode() != 200) { 
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"getProducts :: " + client.getURL() + " - " + client.getStatusLine());
		} else {
			JsonUtils jUtil = new JsonUtils(client.getResponse());
			HashMap<String,String> arrInst = jUtil.getArray(SPM_JSONPATH_PRODUCT, SPM_JSONFIELD_PRODUCT);
			Iterator<String> arrIterator = arrInst.keySet().iterator();
			while (arrIterator.hasNext()) {
				String productName = arrIterator.next();
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Discovered product " + productName);
				ExtractInstance spmInstance = new ExtractInstance(productName);
				spmInstance.setInfo(arrInst.get(productName));
				spmInstance.setInfo("{\"id\":\"" + productName + "\","
						+ "\"configurationTypeId\":\"Products\","
						+ "\"displayName\":\"webMethods Products\","
						+ "\"description\":\"Extracted from SPM\","
						+ "\"runtimeComponentId\":\"NODE-PRODUCTS\"}");
				spmInstance.setData(arrInst.get(productName));
				spmComponent.addInstance(spmInstance);
			}
		}
		
		return spmComponent;
	}

	private ExtractComponent getFixes() throws ServiceException {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Getting Fixes ...");
		ExtractComponent spmComponent = new ExtractComponent(SPM_COMP_FIXES);
		// Hard coding Product extraction information
		spmComponent.setInfo("{\"category\":\"INSTALLED_FIXES\","
							+ "\"displayName\":\"Installed Fixes\","
							+ "\"id\":\"NODE-FIXES\","
							+ "\"productId\":\"webMethods\"}");

		this.client.setURL(this.baseURL + SPM_URI_FIXES);
		this.client.setJsonContent();
		try {
			client.get();
		} catch (ServiceException ex) {
			throw new ServiceException("getFixes() " + ex.getMessage());
		}
		if (client.getStatusCode() == 500) {
			throw new ServiceException(client.getStatusLine() + " for URL " + client.getURL());
		} else if (client.getStatusCode() != 200) { 
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,"getFixes :: " + client.getURL() + " - " + client.getStatusLine());
		} else {
			JsonUtils jUtil = new JsonUtils(client.getResponse());
			HashMap<String,String> arrInst = jUtil.getArray(SPM_JSONPATH_FIXES, SPM_JSONFIELD_FIX);
			Iterator<String> arrIterator = arrInst.keySet().iterator();
			while (arrIterator.hasNext()) {
				String fixName = arrIterator.next();
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Discovered Fix " + fixName);
				ExtractInstance spmInstance = new ExtractInstance(fixName);
				spmInstance.setInfo("{\"id\":\"" + fixName + "\","
						+ "\"configurationTypeId\":\"Fixes\","
						+ "\"displayName\":\"webMethods Fixes\","
						+ "\"description\":\"Extracted from SPM\","
						+ "\"runtimeComponentId\":\"NODE-FIXES\"}");

				spmInstance.setData(arrInst.get(fixName));
				spmComponent.addInstance(spmInstance);
			}
		}
		return spmComponent;
	}

	public void persist() {
		String rootPath = opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR; 
		
		if (this.extractNode.getPlatformInfo() == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Skipping Persistence for node ... " + this.extractNode.getAlias());
			return;
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Persisting Node information for " + this.extractNode.getAlias());
		// -----------------------------
		// Create Server Directory
		// -----------------------------
		String stHostName = this.opencmNode.getUnqualifiedHostname();
		String serverPath = rootPath + File.separator + stHostName;
		if (!FileUtils.createDirectory(serverPath)) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Server Dir Could not be created - skipping Persistence");
			return;
		}
		FileUtils.writeToFile(serverPath + File.separator + SPM_PROP_FILENAME, this.extractNode.getPlatformInfo());
		 
		// -----------------------------
		// Remove Directory if it exists...
		// -----------------------------
		String nodePath = serverPath + File.separator + this.opencmNode.getNode_name();
		FileUtils.removeDirectory(nodePath);
					
		// -----------------------------
		// Create Node Directory
		// -----------------------------
		if (!FileUtils.createDirectory(nodePath)) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Node Dir Could not be created - skipping Persistence");
			return;
		}
		String json = JsonUtils.convertJavaObjectToJson(this.extractNode);
		FileUtils.writeToFile(nodePath + File.separator + SPM_PROP_FILENAME, json);
		if ((this.extractNode.getAlias() != null) && this.extractNode.hasComponents()) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Number of Components:  " + this.extractNode.getComponents().size());
			for (int c = 0; c < this.extractNode.getComponents().size(); c++) {
				// -----------------------------
				// Dump all components
				// -----------------------------
				ExtractComponent component = this.extractNode.getComponents().get(c);
				if ((component.getName() != null) && component.hasInstances()) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Persisting Component " + component.getName());
					String compPath = nodePath + File.separator + component.getName();
					FileUtils.createDirectory(compPath);
					FileUtils.writeToFile(compPath + File.separator + SPM_PROP_FILENAME, component.getInfo());
					for (int i = 0; i < component.getInstances().size(); i++) {
						// -----------------------------
						// Dump all ExtractInstances
						// -----------------------------
						ExtractInstance instance = component.getInstances().get(i);
						if ((instance.getName() != null) && (instance.getData() != null)) {
							LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"Persisting Instance " + instance.getName());
							String instPath = compPath + File.separator + instance.getName();
							FileUtils.createDirectory(instPath);
							FileUtils.writeToFile(instPath + File.separator + SPM_PROP_FILENAME, instance.getInfo());
							FileUtils.writeToFile(instPath + File.separator + SPM_CONF_FILENAME, instance.getData());
						}
					}
				}
			}
		}
	}

	/** - Removed: using explicitly defined environments instead
	public static boolean isExtractionLocal(Configuration opencmConfig, String serverPath) {
		
		File serverProps = new File(serverPath + File.separator + SpmOps.SPM_PROP_FILENAME);
		if (!serverProps.exists()) {
			return false;
		}
		try {
			String extractNode = JsonUtils.getJsonValue(serverProps,"/" + SPM_JSONFIELD_EXTRACT_ALIAS);
			if ((extractNode != null) && extractNode.equals(opencmConfig.getLocal_opencm_node())) {
				return true;
			}
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"SpmOps :: isExtractionLocal :: " + ex.getMessage());
		}

		return false;
	}
	
	**/
	
}
