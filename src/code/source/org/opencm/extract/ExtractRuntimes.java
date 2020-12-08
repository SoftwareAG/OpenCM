package org.opencm.extract;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;

import org.opencm.repository.*;
import org.opencm.inventory.InventoryInstallation;
import org.opencm.inventory.InventoryRuntime;
import org.opencm.secrets.SecretsUtils;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonParser;
import org.opencm.util.HttpClient;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;

import com.wm.app.b2b.server.ServiceException;

public class ExtractRuntimes {
	
	public static String EXTRACT_TEMPLATE_FILENAME 	= "extractions.json"; 
	
	public static String SPM_PROP_FILENAME 			= "properties.json"; 
	public static String SPM_CONF_FILENAME 			= "ci-properties.json"; 

	private static String SPM_URI_PLATFORM 			= "/spm/inventory/platform";
	private static String SPM_URI_SPM				= "/spm/inventory/products/SPM"; 
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

	public static String SPM_COMP_FIXES 			= "NODE-FIXES"; 
	private static String SPM_JSONPATH_FIXES 		= "/fixes/fix"; 
	private static String SPM_JSONFIELD_FIX			= "id"; 

	public static String SPM_COMP_NAME 				= "OSGI-SPM"; 
	public static String SPM_INST_INSTALL_DIR		= "SAG-INSTALL-DIR"; 

	private RepoInstallation repoInstallation;
	private InventoryInstallation inventoryInstallation;
	private SecretsConfiguration secConfig;
	
	public ExtractRuntimes(InventoryInstallation inventoryInstallation, String keepassPwd, String vaultToken) {
		this.inventoryInstallation = inventoryInstallation;
		this.secConfig = SecretsConfiguration.instantiate();
		if (secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL)) {
			this.secConfig.setKeepassPassword(keepassPwd);
		} else {
			this.secConfig.setVaultToken(vaultToken);
		}
		
		// -----------------------------------------------------
		// Create new Repository Installation instance
		// -----------------------------------------------------
		this.repoInstallation = new RepoInstallation(inventoryInstallation.getPath(), inventoryInstallation.getName());
		SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.repoInstallation.setExtractTime(time_formatter.format(System.currentTimeMillis()));
		
		// -----------------------------------------------------
		// Add Local OpenCM node as "extractor" 
		// -----------------------------------------------------
		this.repoInstallation.setExtractAlias("OPENCM");
		
		// -----------------------------------------------------
		// Retrieve Password for SPM runtime 
		// -----------------------------------------------------
		InventoryRuntime spm = inventoryInstallation.getRuntime(InventoryRuntime.RUNTIME_NAME_SPM);
		if (spm == null) {
			LogUtils.logError("ExtractInstallation - No SPM Runtime defined for " + inventoryInstallation.getName());
			return;
		}
		
		String spmPassword = null;
		if ((spm.getPasswordHandle() == null) || spm.getPasswordHandle().equals("")) {
			LogUtils.logDebug("ExtractInstallation - Default SPM Password used...");
			spmPassword = "manage";
		} else {
			spmPassword = SecretsUtils.getPassword(secConfig, spm.getUsername(), spm.getPasswordHandle());
		}
		if (spmPassword == null) {
			LogUtils.logError("ExtractInstallation - No SPM Password defined for " + spm.getPasswordHandle());
			return;
		} else {
			LogUtils.logDebug("ExtractInstallation - SPM Password retrieved: " + spmPassword);
		}
		spm.setPassword(spmPassword);
		
		// -----------------------------------------------------
		// Initialize http client 
		// -----------------------------------------------------
		HttpClient client = new HttpClient();
		LogUtils.logDebug("ExtractInstallation - Setting Credentials .... ");
		client.setCredentials(spm.getUsername(), spm.getPassword());
		
		// -----------------------------------------------------
		// Base URL 
		// -----------------------------------------------------
		String hostname = inventoryInstallation.getHostname();
		if ((spm.getAltHostname() != null) && !spm.getAltHostname().equals("")) {
			hostname = spm.getAltHostname();
		}
		String baseUrl = spm.getProtocol() + "://" + hostname + ":" + spm.getPort();
		if (runExtract(baseUrl, client)) {
			persist();
		}
	}

	
	private boolean runExtract(String baseURL, HttpClient client) {
		LogUtils.logDebug("ExtractInstallation - Extracting Node " + this.repoInstallation.getName());

		// ----------------------------------
		// Get SPM platform info
		// ----------------------------------
		if (!setPlatformInfo(baseURL, client)) {
			LogUtils.logWarning("ExtractInstallation - Unable to get Platform Info, skipping installation ... ");
			return false;
		}
		
		// ----------------------------------
		// Get relevant SPM node info
		// ----------------------------------
		setNodeInfo(baseURL, client);

		// ----------------------------------
		// Loop through all SPM discovered components and instances and gets Instance data
		// ----------------------------------
		setComponents(baseURL, client);
		
		// ----------------------------------
		// Extract Products
		// ----------------------------------
		setProducts(baseURL, client);

		// ----------------------------------
		// Extract Fixes
		// ----------------------------------
		setFixes(baseURL, client);

		return true;
	}
	
	private boolean setPlatformInfo(String baseURL, HttpClient client) {

		LogUtils.logDebug("ExtractInstallation - Getting Platform Info for: " + this.repoInstallation.getName());
		
		client.setURL(baseURL + SPM_URI_PLATFORM);
		String response = httpInvoke(client);
		if (response == null) {
			return false;
		}
		
		this.repoInstallation.addProperties(JsonParser.getProperties(client.getResponse()));
		return true;
	}

	private boolean setNodeInfo(String baseURL, HttpClient client) {
		
		LogUtils.logTrace("ExtractInstallation - setNodeInfo for " + this.repoInstallation.getName());
		
		client.setURL(baseURL + SPM_URI_SPM);
		
		String response = httpInvoke(client);
		if (response == null) {
			return false;
		}
		
		this.repoInstallation.addProperties(JsonParser.getProperties(client.getResponse()));
		return true;
	}

	private void setComponents(String baseURL, HttpClient client) {
		
		LogUtils.logTrace("ExtractInstallation - Setting Components for " + this.repoInstallation.getName());
		
		client.setURL(baseURL + SPM_URI_COMPONENT);
		String response = httpInvoke(client);
		if (response == null) {
			return;
		}
		
		HashMap<String,LinkedList<Property>> hm = JsonParser.getArray(response, SPM_JSONPATH_COMPONENT, SPM_JSONFIELD_COMPONENT);
		Iterator<String> arrIterator = hm.keySet().iterator();
		while (arrIterator.hasNext()) {
			String compName = arrIterator.next();
			LogUtils.logTrace("ExtractInstallation - Discovered component " + compName);
			Component component = new Component(compName);

			component.addProperties(hm.get(compName));
			
			setInstances(baseURL, client, component);
			this.repoInstallation.addComponent(component);
		}
		
	}
	
	private void setInstances(String baseURL, HttpClient client, Component component) {
		
		LogUtils.logTrace("Getting Instances for " + component.getName() + " ...");
		
		client.setURL(baseURL + SPM_URI_INSTANCE + "/" + component.getName());
		String response = httpInvoke(client);
		if (response == null) {
			return;
		}
		
		HashMap<String,LinkedList<Property>> hm = JsonParser.getArray(response, SPM_JSONPATH_INSTANCE, SPM_JSONFIELD_INSTANCE);
			
		Iterator<String> arrIterator = hm.keySet().iterator();
		while (arrIterator.hasNext()) {
			String instName = arrIterator.next();
			LogUtils.logTrace("Discovered instance " + instName);
			Instance instance = new Instance(instName);
			instance.addProperties(hm.get(instName));
			
			setProperties(baseURL, client, component, instance);
			component.addInstance(instance);
		}
		
		// ------------------------------------
		// Include IS Additional extractions 
		// ------------------------------------
		if (!component.getName().startsWith(InventoryRuntime.RUNTIME_NAME_IS_PREFIX)) {
			return;
		}
		
		InventoryRuntime isRuntime = this.inventoryInstallation.getRuntime(component.getName());
		if (isRuntime == null) {
			LogUtils.logWarning("No Installation Runtime defined for " + component.getName());
			return;
		}
		
		// -----------------------------------------------------
		// Retrieve Password for IS runtime 
		// -----------------------------------------------------
		String isPassword = null;
		if ((isRuntime.getPasswordHandle() == null) || isRuntime.getPasswordHandle().equals("")) {
			LogUtils.logDebug("ExtractInstallation - Default IS Password used...");
			isPassword = "manage";
		} else {
			isPassword = SecretsUtils.getPassword(this.secConfig, isRuntime.getUsername(), isRuntime.getPasswordHandle());
		}
		if (isPassword == null) {
			LogUtils.logWarning("ExtractInstallation - No IS Password defined for " + isRuntime.getPasswordHandle());
			return;
		}
		isRuntime.setPassword(isPassword);

		component.addInstance(IntegrationServerExtractor.extractPackages(this.inventoryInstallation, isRuntime));
		component.addInstance(IntegrationServerExtractor.extractJcePolicyInfo(this.inventoryInstallation, isRuntime));

		if (component.hasPackage(IntegrationServerExtractor.PACKAGE_JDBC_ADAPTER)) {
			component.addInstance(IntegrationServerExtractor.extractJdbcAdapters(this.inventoryInstallation, isRuntime));
		} else {
			LogUtils.logDebug("ExtractInstallation - Skipping JDBC Adapter since no package exists.. ");
		}
		
		if (component.hasPackage(IntegrationServerExtractor.PACKAGE_SAP_ADAPTER)) {
			component.addInstance(IntegrationServerExtractor.extractSapAdapters(this.inventoryInstallation, isRuntime));
		} else {
			LogUtils.logDebug("ExtractInstallation - Skipping SAP Adapter since no package exists.. ");
		}
		
		if (component.hasPackage(IntegrationServerExtractor.PACKAGE_WXCONFIG)) {
			LinkedList<String> packages = component.getPackages();
			component.addInstance(IntegrationServerExtractor.extractWxConfig(this.inventoryInstallation, isRuntime, packages));
		} else {
			LogUtils.logDebug("ExtractInstallation - Skipping WxConfig since no package exists.. ");
		}
		
	}
	
	private void setProperties(String baseURL, HttpClient client, Component component, Instance instance) {
		LogUtils.logTrace("Getting Properties for " + instance.getName() + " ...");

		client.setURL(baseURL + SPM_URI_PROPERTY + "/" + component.getName() + "/" + instance.getName());
		String response = httpInvoke(client);
		if (response == null) {
			return;
		}
		
		// Construct Properties out of the json response
		instance.addProperties(JsonParser.getProperties(client.getResponse()));
			
	}

	private void setProducts(String baseURL, HttpClient client) {
		LogUtils.logTrace("Getting Products ...");
		Component component = new Component(SPM_COMP_PRODUCT);
		component.addProperty(new Property("id","PRODUCTS"));
		
		client.setURL(baseURL + SPM_URI_PRODUCT);
		String response = httpInvoke(client);
		if (response == null) {
			return;
		}
		
		HashMap<String,LinkedList<Property>> hm = JsonParser.getArray(response, SPM_JSONPATH_PRODUCT, SPM_JSONFIELD_PRODUCT);
		Iterator<String> arrIterator = hm.keySet().iterator();
		while (arrIterator.hasNext()) {
			String productName = arrIterator.next();
			LogUtils.logTrace("Discovered product " + productName);
			Instance instance = new Instance(productName);
			instance.addProperties(hm.get(productName));
			component.addInstance(instance);
		}
		
		this.repoInstallation.addComponent(component);
		
	}

	private void setFixes(String baseURL, HttpClient client) {
		LogUtils.logTrace("Getting Fixes ...");
		
		Component component = new Component(SPM_COMP_FIXES);
		component.addProperty(new Property("id","FIXES"));

		client.setURL(baseURL + SPM_URI_FIXES);
		String response = httpInvoke(client);
		if (response == null) {
			return;
		}
		
		HashMap<String,LinkedList<Property>> hm = JsonParser.getArray(response, SPM_JSONPATH_FIXES, SPM_JSONFIELD_FIX);
		Iterator<String> arrIterator = hm.keySet().iterator();
		while (arrIterator.hasNext()) {
			String fixName = arrIterator.next();
			LogUtils.logTrace("Discovered Fix " + fixName);
			Instance instance = new Instance(fixName);
			instance.addProperties(hm.get(fixName));
			component.addInstance(instance);
		}

		this.repoInstallation.addComponent(component);
	}

	private void persist() {
		LogUtils.logTrace("Persisting data for " + this.repoInstallation.getName());
		
		// Check Repository directory (that it exists)
    	Repository.checkDirectory();; 
    	
		// Get Filename:
    	File repoFile = Repository.getInstallationRepositoryFile(this.inventoryInstallation.getPath()); 
    	
    	// Sort the properties alphanumerically
		Collections.sort(this.repoInstallation.getProperties(), Comparator.comparing(obj -> obj.getKey()));
    	for (Component component: this.repoInstallation.getComponents()) {
    		Collections.sort(component.getProperties(), Comparator.comparing(obj -> obj.getKey()));
        	for (Instance instance: component.getInstances()) {
        		Collections.sort(instance.getProperties(), Comparator.comparing(obj -> obj.getKey()));
        	}
    	}
		String json = JsonUtils.convertJavaObjectToJson(this.repoInstallation);
		FileUtils.writeToFile(repoFile.getPath(), json);

	}
	
	private static String httpInvoke(HttpClient client) {

		// -------------------
		//  Invoke
		// -------------------
		try {
			client.setJsonContent();
			client.get();

			// -------------------
			//  Check response
			// -------------------
			if (client.getStatusCode() == 500) {
				LogUtils.logWarning("ExtractInstallation - httpInvoke :: " + client.getStatusLine() + " for URL " + client.getURL());
				return null;
			} else if (client.getStatusCode() == 406) {
				// ----------------------------
				// Try with XML or plain text
				// ----------------------------
				LogUtils.logTrace("ExtractInstallation - httpInvoke - 406 for " + client.getURL() + ", trying XML ...");
				client.setXmlorTextContent();
				client.get();
				LogUtils.logTrace("ExtractInstallation - httpInvoke - Response Status: " + client.getStatusLine());
				client.setResponse(JsonUtils.convertToJson(client.getResponse()));
			}
			if (client.getStatusCode() != 200) {
				LogUtils.logTrace("ExtractInstallation - httpInvoke - :: Gracefully ignoring " + client.getURL() + " - " + client.getStatusLine());
				return null;
			}
			
		} catch (ServiceException ex) {
			LogUtils.logWarning("ExtractInstallation - httpInvoke - Exception for " + client.getURL() + " :: " + ex.getMessage());
			return null;
		}
		
		return client.getResponse();
	}
	
}
