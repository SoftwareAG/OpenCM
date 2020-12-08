package org.opencm.extract;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedList;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import com.gargoylesoftware.htmlunit.WebClient;

import org.opencm.repository.*;
import org.opencm.inventory.InventoryInstallation;
import org.opencm.inventory.InventoryRuntime;
import org.opencm.util.LogUtils;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;

public class IntegrationServerExtractor {

    private static String INTERNAL_SAP_CONNECTION_NAME		= "wm.sap.internal.cs";
	public static String INSTANCE_IS_PACKAGES 				= "IS-EXT-PACKAGES"; 
	public static String INSTANCE_JCE_INFO 					= "IS-EXT-JCE-POLICYINFO"; 
	
	public static String PACKAGE_SAP_ADAPTER				= "WmSAP";
	public static String INSTANCE_SAP 						= "IS-EXT-SAP-ADAPTERS";
	
	public static String PACKAGE_JDBC_ADAPTER				= "WmJDBCAdapter";
	public static String INSTANCE_JDBC 						= "IS-EXT-JDBC-ADAPTERS"; 
	
	public static String PACKAGE_WXCONFIG					= "WxConfig";
	public static String INSTANCE_WXCONFIG					= "IS-EXT-WXCONFIG"; 

	public static Instance extractPackages(InventoryInstallation installation, InventoryRuntime rc) {
		LogUtils.logInfo("IntegrationServerExtractor - extractPackages... ");
		
		HtmlUnitDriver driver = new HtmlUnitDriver(true) {
		    protected WebClient modifyWebClient(WebClient client) {
				WebClient modifiedClient = super.modifyWebClient(client);
				modifiedClient.getOptions().setThrowExceptionOnScriptError(false);
		        DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
		        creds.addCredentials(rc.getUsername(), rc.getPassword());
		        modifiedClient.setCredentialsProvider(creds); 
		        return modifiedClient; 
		    }
		};
		
		// -----------------------------------------------------
		// Base URL 
		// -----------------------------------------------------
		String hostname = installation.getHostname();
		if ((rc.getAltHostname() != null) && !rc.getAltHostname().equals("")) {
			hostname = rc.getAltHostname();
		}
		String baseUrl = rc.getProtocol() + "://" + hostname + ":" + rc.getPort();
		
		Instance instance = new Instance(INSTANCE_IS_PACKAGES);
		
		LinkedList<String> lPackages = new LinkedList<String>();
		try {
			
			// Set timeouts
			driver.manage().timeouts().implicitlyWait(30, java.util.concurrent.TimeUnit.SECONDS);
			// Get summary page
			driver.get(baseUrl + "/invoke/wm.server.packages/packageList");
			LogUtils.logTrace("getPackages :: " + driver.getCurrentUrl());
			LogUtils.logTrace("getPackages :: " + driver.getPageSource());
			
			// Loop through all the packages
			List<WebElement> lNodes = driver.findElements(By.xpath("//b[text()='name']"));
			if ((lNodes != null) && (lNodes.size() > 0)) {
				for (int i = 0; i < lNodes.size(); i++) {
					String packageName = lNodes.get(i).findElement(By.xpath("./following::td[1]")).getText();
					String enabled = lNodes.get(i).findElement(By.xpath("./following::td[3]")).getText();
					instance.addProperty(new Property(packageName + ".enabled",enabled));
					lPackages.add(packageName);
				}
			}
			
			// Get Package Version
			for (String packageName: lPackages) {
				LogUtils.logTrace("getPackages version for :: " + packageName);
				driver.get(baseUrl + "/invoke/wm.server.packages/packageInfo?package=" + packageName);
				String version = driver.findElement(By.xpath("//b[text()='version']/following::td")).getText();
				instance.addProperty(new Property(packageName + ".version",version));
			}
			
		} catch (Exception ex) {
			LogUtils.logWarning("IntegrationServerExtractor - setPackages() :: " + driver.getCurrentUrl());
			LogUtils.logWarning("IntegrationServerExtractor - setPackages() - Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}
		
		return instance;
	}
	
	public static Instance extractJcePolicyInfo(InventoryInstallation installation, InventoryRuntime rc) {
		LogUtils.logInfo("IntegrationServerExtractor - extractJcePolicyInfo... ");
		
		HtmlUnitDriver driver = new HtmlUnitDriver(true) {
		    protected WebClient modifyWebClient(WebClient client) {
				WebClient modifiedClient = super.modifyWebClient(client);
				modifiedClient.getOptions().setThrowExceptionOnScriptError(false);
		        DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
		        creds.addCredentials(rc.getUsername(), rc.getPassword());
		        modifiedClient.setCredentialsProvider(creds); 
		        return modifiedClient; 
		    }
		};

		// -----------------------------------------------------
		// Base URL 
		// -----------------------------------------------------
		String hostname = installation.getHostname();
		if ((rc.getAltHostname() != null) && !rc.getAltHostname().equals("")) {
			hostname = rc.getAltHostname();
		}
		String baseUrl = rc.getProtocol() + "://" + hostname + ":" + rc.getPort();

		Instance instance = new Instance(INSTANCE_JCE_INFO);

		try {
			// Set timeouts
			driver.manage().timeouts().implicitlyWait(30, java.util.concurrent.TimeUnit.SECONDS);
			// Get info page
			driver.get(baseUrl + "/invoke/wm.server.query/getSystemAttributes");

			LogUtils.logTrace("getJCE :: " + driver.getCurrentUrl());
			instance.addProperty(new Property("jce.policy.info",driver.findElement(By.xpath("//b[text()='ssl']/following::td")).getText()));
			
		} catch (Exception ex) {
			LogUtils.logWarning("IntegrationServerExtractor - setJcePolicyInfo() :: " + driver.getCurrentUrl());
			LogUtils.logWarning("IntegrationServerExtractor - setJcePolicyInfo() --> Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}

		return instance;
	}
	
	public static Instance extractSapAdapters(InventoryInstallation installation, InventoryRuntime rc) {
		LogUtils.logInfo("IntegrationServerExtractor - extractSapAdapters... ");
		
		HtmlUnitDriver driver = new HtmlUnitDriver(true) {
		    protected WebClient modifyWebClient(WebClient client) {
				WebClient modifiedClient = super.modifyWebClient(client);
				modifiedClient.getOptions().setThrowExceptionOnScriptError(false);
		        DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
		        creds.addCredentials(rc.getUsername(), rc.getPassword());
		        modifiedClient.setCredentialsProvider(creds); 
		        return modifiedClient; 
		    }
		};

		// -----------------------------------------------------
		// Base URL 
		// -----------------------------------------------------
		String hostname = installation.getHostname();
		if ((rc.getAltHostname() != null) && !rc.getAltHostname().equals("")) {
			hostname = rc.getAltHostname();
		}
		String baseUrl = rc.getProtocol() + "://" + hostname + ":" + rc.getPort();
		
		Instance instance = new Instance(INSTANCE_SAP);
		
		LinkedList<SAPConnection> sapConnections = new LinkedList<SAPConnection>();
		try {
			
			// Set timeouts
			driver.manage().timeouts().implicitlyWait(30, java.util.concurrent.TimeUnit.SECONDS);
			// Get summary page
			driver.get(baseUrl + "/invoke/pub.art.connection/listAdapterConnections?adapterTypeName=WmSAP");

			LogUtils.logTrace("extractSapAdapters :: " + driver.getCurrentUrl());
			
			// Loop through all the connectionAliases
			List<WebElement> lNodes = driver.findElements(By.xpath("//b[text()='connectionAlias']"));
			LogUtils.logDebug("extractSapAdapters :: found Nodes: " + lNodes.size());
			if ((lNodes != null) && (lNodes.size() > 0)) {
				for (int i = 0; i < lNodes.size(); i++) {
					String sapNode = lNodes.get(i).findElement(By.xpath("./following::td")).getText();
					// Ignore the default SAP connection
					if (sapNode.startsWith(INTERNAL_SAP_CONNECTION_NAME)) {
						continue;
					}
					SAPConnection sapConnection = new SAPConnection();
					sapConnection.setNodeAlias(sapNode);
					sapConnection.setPackageName(lNodes.get(i).findElement(By.xpath("./following::td[3]")).getText());
					sapConnection.setEnabled(lNodes.get(i).findElement(By.xpath("./following::td[5]")).getText());
					sapConnections.add(sapConnection);
				}
			}
			
			// Get Additional SAP Adapter Connection Info
			for (SAPConnection sapConnection: sapConnections) {
				// Get SAP Connection Details page (read-only)
				String nodeNameURLEncoded = URLEncoder.encode(sapConnection.getNodeAlias(),StandardCharsets.UTF_8.toString());
				LogUtils.logTrace("extractSapAdapters for :: " + sapConnection.getNodeAlias());
				driver.get(baseUrl + "/WmSAP/ConnNodeDetails.dsp?readOnly=true&connectionAlias=" + nodeNameURLEncoded + "&adapterTypeName=WmSAP&searchConnectionName=");
				
				String sapConnType = driver.findElement(By.xpath("//td[text()='Connection Type']/following::td")).getText();
				String sapConnAlias = driver.findElement(By.xpath("//td[text()='Connection Alias']/following::td")).getText();
				
				// Set properties now since we have the connectionAlias:
				instance.addProperty(new Property(sapConnAlias + ".packageName",sapConnection.getPackageName()));
				instance.addProperty(new Property(sapConnAlias + ".enabled",sapConnection.getEnabled()));
				instance.addProperty(new Property(sapConnAlias + ".nodeAlias",sapConnection.getNodeAlias()));
				instance.addProperty(new Property(sapConnAlias + ".type",sapConnType));

				// And now the rest
				instance.addProperty(new Property(sapConnAlias + ".username",driver.findElement(By.xpath("//td[text()='User Name']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".client",driver.findElement(By.xpath("//td[text()='Client']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".language",driver.findElement(By.xpath("//td[text()='Language']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".loadBalancing",driver.findElement(By.xpath("//td[text()='Load Balancing']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".applicationServer",driver.findElement(By.xpath("//td[text()='Application Server']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".systemNumber",driver.findElement(By.xpath("//td[text()='System Number']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".logonGroup",driver.findElement(By.xpath("//td[text()='Logon Group']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".messageServer",driver.findElement(By.xpath("//td[text()='Message Server']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".systemId",driver.findElement(By.xpath("//td[text()='System ID']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".externalRFC",driver.findElement(By.xpath("//td[text()='External RFC Server']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".programId",driver.findElement(By.xpath("//td[text()='Program ID']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".gatewayHost",driver.findElement(By.xpath("//td[text()='Gateway Host']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".gatewayService",driver.findElement(By.xpath("//td[text()='Gateway Service']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".repositoryServer",driver.findElement(By.xpath("//td[text()='Repository Server']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".sncEnabled",driver.findElement(By.xpath("//td[text()='SNC Enabled']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".sncQoS",driver.findElement(By.xpath("//td[text()='SNC Quality of Service']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".sncName",driver.findElement(By.xpath("//td[text()='SNC Name']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".sncPartner",driver.findElement(By.xpath("//td[text()='SNC Partner Name']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".sncRouting",driver.findElement(By.xpath("//td[text()='SAP Router String']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".useSapGui",driver.findElement(By.xpath("//td[text()='Use SAPGui']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".rfcTrace",driver.findElement(By.xpath("//td[text()='RFC Trace']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".logTransStatus",driver.findElement(By.xpath("//td[text()='Log transaction status']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".storeMsgBody",driver.findElement(By.xpath("//td[text()='Store message body']/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".enableConnPooling",driver.findElement(By.xpath("//td[contains(., 'Enable Connection Pooling')]/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".minPoolSize",driver.findElement(By.xpath("//td[contains(., 'Minimum Pool Size')]/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".maxPoolSize",driver.findElement(By.xpath("//td[contains(., 'Maximum Pool Size')]/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".poolIncSize",driver.findElement(By.xpath("//td[contains(., 'Pool Increment Size')]/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".blockTimeout",driver.findElement(By.xpath("//td[contains(., 'Block Timeout (msec)')]/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".expiryTimeout",driver.findElement(By.xpath("//td[contains(., 'Expire Timeout (msec)')]/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".startupRetryCount",driver.findElement(By.xpath("//td[contains(., 'Startup Retry Count')]/following::td")).getText()));
				instance.addProperty(new Property(sapConnAlias + ".startupBackoffTime",driver.findElement(By.xpath("//td[contains(., 'Startup Backoff Timeout (sec)')]/following::td")).getText()));
				
			}
			
		} catch (Exception ex) {
			LogUtils.logWarning("IntegrationServerExtractor - extractSapAdapters() :: " + driver.getCurrentUrl());
			LogUtils.logWarning("IntegrationServerExtractor - extractSapAdapters() --> Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}
		
		return instance;

	}
	
	public static Instance extractJdbcAdapters(InventoryInstallation installation, InventoryRuntime rc) {
		LogUtils.logInfo("IntegrationServerExtractor - extractJdbcAdapters... ");

		HtmlUnitDriver driver = new HtmlUnitDriver(true) {
		    protected WebClient modifyWebClient(WebClient client) {
				WebClient modifiedClient = super.modifyWebClient(client);
				modifiedClient.getOptions().setThrowExceptionOnScriptError(false);
		        DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
		        creds.addCredentials(rc.getUsername(), rc.getPassword());
		        modifiedClient.setCredentialsProvider(creds); 
		        return modifiedClient; 
		    }
		};

		// -----------------------------------------------------
		// Base URL 
		// -----------------------------------------------------
		String hostname = installation.getHostname();
		if ((rc.getAltHostname() != null) && !rc.getAltHostname().equals("")) {
			hostname = rc.getAltHostname();
		}
		String baseUrl = rc.getProtocol() + "://" + hostname + ":" + rc.getPort();

		Instance instance = new Instance(INSTANCE_JDBC);
		
		LinkedList<String> jdbcConnections = new LinkedList<String>();
		try {
			
			// Set timeouts
			driver.manage().timeouts().implicitlyWait(30, java.util.concurrent.TimeUnit.SECONDS);
			// Get summary page
			driver.get(baseUrl + "/invoke/pub.art.connection/listAdapterConnections?adapterTypeName=JDBCAdapter");

			LogUtils.logTrace("extractJdbcAdapters :: " + driver.getCurrentUrl());
			
			// Loop through all the JDBC connections
			List<WebElement> lNodes = driver.findElements(By.xpath("//b[text()='connectionAlias']"));
			LogUtils.logDebug("extractJdbcAdapters :: found Nodes: " + lNodes.size());
			if ((lNodes != null) && (lNodes.size() > 0)) {
				for (int i = 0; i < lNodes.size(); i++) {
					String connAlias = lNodes.get(i).findElement(By.xpath("./following::td")).getText();
					instance.addProperty(new Property(connAlias + ".package",lNodes.get(i).findElement(By.xpath("./following::td[3]")).getText()));
					instance.addProperty(new Property(connAlias + ".enabled",lNodes.get(i).findElement(By.xpath("./following::td[5]")).getText()));
					jdbcConnections.add(connAlias);
				}
			}
			
			// Get Additional JDBC Adapter Connection Info
			for (String jdbcConnection: jdbcConnections) {

				// Get JDBC Connection Details page (read-only)
				String nodeNameURLEncoded = URLEncoder.encode(jdbcConnection,StandardCharsets.UTF_8.toString());
				LogUtils.logTrace("IntegrationServerExtractor  extractJdbcAdapters for :: " + jdbcConnection);
				driver.get(baseUrl + "/WmART/ConnNodeDetails.dsp?readOnly=true&connectionAlias=" + nodeNameURLEncoded + "&adapterTypeName=JDBCAdapter&searchConnectionName=");
				
				instance.addProperty(new Property(jdbcConnection + ".connectionType",driver.findElement(By.xpath("//td[text()='Connection Type']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".transactionType",driver.findElement(By.xpath("//td[text()='Transaction Type']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".datasource",driver.findElement(By.xpath("//td[text()='DataSource Class']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".serverName",driver.findElement(By.xpath("//td[text()='Server Name' or text()='serverName']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".username",driver.findElement(By.xpath("//td[text()='User' or text()='user']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".database",driver.findElement(By.xpath("//td[text()='Database Name' or text()='databaseName']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".port",driver.findElement(By.xpath("//td[text()='Port Number' or text()='portNumber']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".protocol",driver.findElement(By.xpath("//td[text()='Network Protocol' or text()='networkProtocol']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".otherProperties",driver.findElement(By.xpath("//td[text()='Other Properties']/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".enableConnPooling",driver.findElement(By.xpath("//td[contains(., 'Enable Connection Pooling')]/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".minPoolSize",driver.findElement(By.xpath("//td[contains(., 'Minimum Pool Size')]/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".maxPoolSize",driver.findElement(By.xpath("//td[contains(., 'Maximum Pool Size')]/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".poolIncSize",driver.findElement(By.xpath("//td[contains(., 'Pool Increment Size')]/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".blockTimeout",driver.findElement(By.xpath("//td[contains(., 'Block Timeout (msec)')]/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".expireTimeout",driver.findElement(By.xpath("//td[contains(., 'Expire Timeout (msec)')]/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".startupRetrycount",driver.findElement(By.xpath("//td[contains(., 'Startup Retry Count')]/following::td")).getText()));
				instance.addProperty(new Property(jdbcConnection + ".startupBackoffTimeout",driver.findElement(By.xpath("//td[contains(., 'Startup Backoff Timeout (sec)')]/following::td")).getText()));
			}
			
		} catch (Exception ex) {
			LogUtils.logWarning("IntegrationServerExtractor - extractJdbcAdapters() :: " + driver.getCurrentUrl());
			LogUtils.logWarning("IntegrationServerExtractor - extractJdbcAdapters --> Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}
		
		return instance;

	}
	
	public static Instance extractWxConfig(InventoryInstallation installation, InventoryRuntime rc, LinkedList<String> packages) {
		LogUtils.logInfo("IntegrationServerExtractor - extractWxConfig... ");
		
		Instance instance = new Instance(INSTANCE_WXCONFIG);
		
		if ((packages == null) || (packages.size() == 0)) {
			LogUtils.logWarning("IntegrationServerExtractor - extractWxConfig... - No packages to query");
			return instance;
		}
		
		HtmlUnitDriver driver = new HtmlUnitDriver(true) {
		    protected WebClient modifyWebClient(WebClient client) {
				WebClient modifiedClient = super.modifyWebClient(client);
				modifiedClient.getOptions().setThrowExceptionOnScriptError(false);
		        DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
		        creds.addCredentials(rc.getUsername(), rc.getPassword());
		        modifiedClient.setCredentialsProvider(creds); 
		        return modifiedClient; 
		    }
		};

		// -----------------------------------------------------
		// Base URL 
		// -----------------------------------------------------
		String hostname = installation.getHostname();
		if ((rc.getAltHostname() != null) && !rc.getAltHostname().equals("")) {
			hostname = rc.getAltHostname();
		}
		String baseUrl = rc.getProtocol() + "://" + hostname + ":" + rc.getPort();

		
		try {
			
			// Set timeouts
			driver.manage().timeouts().implicitlyWait(0, java.util.concurrent.TimeUnit.SECONDS);
			
			for (String packageName : packages) {
				// Ignore system packages
				if (packageName.startsWith("Wm")) {
					continue;
				}
				
				LogUtils.logDebug("IntegrationServerExtractor - extractWxConfig... processing package " + packageName);
				
				// Get info page
				try {
					driver.get(baseUrl + "/invoke/wx.config.pub/getAllValues?wxConfigPkgName=" + packageName);
					LogUtils.logTrace("extractWxConfig :: " + driver.getCurrentUrl());
					String ssValue = driver.findElement(By.xpath("//b[text()='keyValuePairs']/following::td")).getText();
					if (ssValue != null) {
						instance.addProperties(getKeyValuePairs(packageName, ssValue));
					}
					
				} catch (Exception ex) {
					// Ignore since this package may not have a WxConfig setup
				}
				
			}
			
		} catch (Exception ex) {
			LogUtils.logWarning("IntegrationServerExtractor - extractWxConfig() :: " + driver.getCurrentUrl());
			LogUtils.logWarning("IntegrationServerExtractor - extractWxConfig() --> Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}
		
		return instance;

	}
	
	private static LinkedList<Property> getKeyValuePairs(String packageName, String keyValueString) {
    	LinkedList<Property> kvs = new LinkedList<Property>();
        String[] keyVals = keyValueString.split("\n");
        LinkedList<String> llKvs = new LinkedList<String>();
        for(String keyVal : keyVals) {
          String[] parts = keyVal.split("=",2);
          if (llKvs.contains(parts[0])) {
        	  // Ignore duplicate variables coming back 
        	  continue;
          }
          llKvs.add(parts[0]);
          Property ekv = new Property(packageName + "." + parts[0],parts[1]);
          kvs.add(ekv);
        }
		return kvs;
	}
	
	private static class SAPConnection {
		private String nodeAlias;
		private String connectionEnabled;
		private String packageName;
		
		public void setNodeAlias(String alias) {
			nodeAlias = alias;
		}
		public String getNodeAlias() {
			return nodeAlias;
		}
		public void setEnabled(String enabled) {
			connectionEnabled = enabled;
		}
		public String getEnabled() {
			return connectionEnabled;
		}
		public void setPackageName(String name) {
			packageName = name;
		}
		public String getPackageName() {
			return packageName;
		}
		
	}
	
}