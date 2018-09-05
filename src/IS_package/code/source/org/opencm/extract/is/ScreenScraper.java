package org.opencm.extract.is;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedList;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import com.gargoylesoftware.htmlunit.WebClient;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.RuntimeComponent;
import org.opencm.configuration.Node;
import org.opencm.util.LogUtils;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;

public class ScreenScraper {

    private static String INTERNAL_SAP_CONNECTION_NAME		= "wm.sap.internal.cs";

	private Configuration opencmConfig;
	private HtmlUnitDriver driver;
	private String baseURL;

	public ScreenScraper(Configuration opencmConfig, String serverName, RuntimeComponent isRuntimeComponent) {
		this.opencmConfig = opencmConfig;
		this.driver = new HtmlUnitDriver(true) {
		    protected WebClient modifyWebClient(WebClient client) {
				WebClient modifiedClient = super.modifyWebClient(client);
				modifiedClient.getOptions().setThrowExceptionOnScriptError(false);
		        DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
		        creds.addCredentials(isRuntimeComponent.getUsername(), isRuntimeComponent.getDecryptedPassword());
		        modifiedClient.setCredentialsProvider(creds); 
		        return modifiedClient; 
		    }
		};
		this.baseURL = isRuntimeComponent.getProtocol() + "://" + serverName + ":" + isRuntimeComponent.getPort();
	}
	
	public LinkedList<ISPackage> getPackages() {
		LinkedList<ISPackage> isPackages = new LinkedList<ISPackage>();
		try {
			
			// Set timeouts
			this.driver.manage().timeouts().implicitlyWait(30, java.util.concurrent.TimeUnit.SECONDS);
			// Get summary page
			this.driver.get(this.baseURL + "/invoke/wm.server.packages/packageList");

			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getPackages :: " + this.driver.getCurrentUrl());
			
			// Loop through all the packages
			List<WebElement> lNodes = this.driver.findElements(By.xpath("//b[text()='name']"));
			if ((lNodes != null) && (lNodes.size() > 0)) {
				for (int i = 0; i < lNodes.size(); i++) {
					ISPackage isPkg = new ISPackage(lNodes.get(i).findElement(By.xpath("./following::td[1]")).getText());
					isPkg.setEnabled(lNodes.get(i).findElement(By.xpath("./following::td[3]")).getText());
					isPackages.add(isPkg);
				}
			}
			
			// Get Package Version
			for (int i = 0; i < isPackages.size(); i++) {
				ISPackage isPkg = isPackages.get(i);
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getPackages version for :: " + isPkg.getPackageName());
				driver.get(this.baseURL + "/invoke/wm.server.packages/packageInfo?package=" + isPkg.getPackageName());
				isPkg.setVersion(driver.findElement(By.xpath("//b[text()='version']/following::td")).getText());
				isPackages.set(i, isPkg);
			}
			
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"ScreenScraper.getISPackages() :: " + driver.getCurrentUrl());
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"  --> Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}
		return isPackages;
	}
	
	public JCEPolicyInfo getJCEInfo() {
		JCEPolicyInfo jceInfo = new JCEPolicyInfo();
		try {
			// Set timeouts
			this.driver.manage().timeouts().implicitlyWait(30, java.util.concurrent.TimeUnit.SECONDS);
			// Get info page
			this.driver.get(this.baseURL + "/invoke/wm.server.query/getSystemAttributes");

			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getJCE :: " + this.driver.getCurrentUrl());
			
			jceInfo.setKey("JCE_POLICY_INFO");
			jceInfo.setValue(this.driver.findElement(By.xpath("//b[text()='ssl']/following::td")).getText());
			
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"ScreenScraper.getJCE() :: " + driver.getCurrentUrl());
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"  --> Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}
		return jceInfo;
	}

	public LinkedList<SAPAdapterConnection> getSAPAdapterConnections() {
		LinkedList<SAPAdapterConnection> sapConnections = new LinkedList<SAPAdapterConnection>();
		try {
			
			// Set timeouts
			this.driver.manage().timeouts().implicitlyWait(30, java.util.concurrent.TimeUnit.SECONDS);
			// Get summary page
			this.driver.get(this.baseURL + "/invoke/pub.art.connection/listAdapterConnections?adapterTypeName=WmSAP");

			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getSAPAdapterConnections :: " + this.driver.getCurrentUrl());
			
			// Loop through all the packages
			List<WebElement> lNodes = driver.findElements(By.xpath("//b[text()='connectionAlias']"));
			if ((lNodes != null) && (lNodes.size() > 0)) {
				for (int i = 0; i < lNodes.size(); i++) {
					SAPAdapterConnection sapConn = new SAPAdapterConnection();
					sapConn.setNodeName(lNodes.get(i).findElement(By.xpath("./following::td")).getText());
					// Ignore the default SAP connection
					if (sapConn.getNodeName().startsWith(INTERNAL_SAP_CONNECTION_NAME)) {
						continue;
					}
					sapConn.setPackageName(lNodes.get(i).findElement(By.xpath("./following::td[3]")).getText());
					sapConn.setEnabled(lNodes.get(i).findElement(By.xpath("./following::td[5]")).getText());
					sapConnections.add(sapConn);
				}
			}
			
			// Get Additional SAP Adapter Connection Info
			for (int i = 0; i < sapConnections.size(); i++) {
				SAPAdapterConnection sapConn = sapConnections.get(i);
				// Get SAP Connection Details page (read-only)
				String nodeNameURLEncoded = URLEncoder.encode(sapConn.getNodeName(),StandardCharsets.UTF_8.toString());
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  getSAPAdapterConnections for :: " + sapConn.getNodeName());
				driver.get(this.baseURL + "/WmSAP/ConnNodeDetails.dsp?readOnly=true&connectionAlias=" + nodeNameURLEncoded + "&adapterTypeName=WmSAP&searchConnectionName=");
				sapConn.setConnectionType(driver.findElement(By.xpath("//td[text()='Connection Type']/following::td")).getText());
				sapConn.setConnectionAlias(driver.findElement(By.xpath("//td[text()='Connection Alias']/following::td")).getText());
				sapConn.setUsername(driver.findElement(By.xpath("//td[text()='User Name']/following::td")).getText());
				sapConn.setClient(driver.findElement(By.xpath("//td[text()='Client']/following::td")).getText());
				sapConn.setLanguage(driver.findElement(By.xpath("//td[text()='Language']/following::td")).getText());
				sapConn.setLoadBalancing(driver.findElement(By.xpath("//td[text()='Load Balancing']/following::td")).getText());
				sapConn.setApplicationServer(driver.findElement(By.xpath("//td[text()='Application Server']/following::td")).getText());
				sapConn.setSystemNumber(driver.findElement(By.xpath("//td[text()='System Number']/following::td")).getText());
				sapConn.setLogonGroup(driver.findElement(By.xpath("//td[text()='Logon Group']/following::td")).getText());
				sapConn.setMessageServer(driver.findElement(By.xpath("//td[text()='Message Server']/following::td")).getText());
				sapConn.setSystemId(driver.findElement(By.xpath("//td[text()='System ID']/following::td")).getText());
				sapConn.setExternalRFCServer(driver.findElement(By.xpath("//td[text()='External RFC Server']/following::td")).getText());
				sapConn.setProgramId(driver.findElement(By.xpath("//td[text()='Program ID']/following::td")).getText());
				sapConn.setGatewayHost(driver.findElement(By.xpath("//td[text()='Gateway Host']/following::td")).getText());
				sapConn.setGatewayService(driver.findElement(By.xpath("//td[text()='Gateway Service']/following::td")).getText());
				sapConn.setRepositoryServer(driver.findElement(By.xpath("//td[text()='Repository Server']/following::td")).getText());
				sapConn.setSncEnabled(driver.findElement(By.xpath("//td[text()='SNC Enabled']/following::td")).getText());
				sapConn.setSncQualityOfService(driver.findElement(By.xpath("//td[text()='SNC Quality of Service']/following::td")).getText());
				sapConn.setSncName(driver.findElement(By.xpath("//td[text()='SNC Name']/following::td")).getText());
				sapConn.setSncPartner(driver.findElement(By.xpath("//td[text()='SNC Partner Name']/following::td")).getText());
				sapConn.setSncRoutingString(driver.findElement(By.xpath("//td[text()='SAP Router String']/following::td")).getText());
				sapConn.setUseSAPGui(driver.findElement(By.xpath("//td[text()='Use SAPGui']/following::td")).getText());
				sapConn.setRfcTrace(driver.findElement(By.xpath("//td[text()='RFC Trace']/following::td")).getText());
				sapConn.setLogTransactionStatus(driver.findElement(By.xpath("//td[text()='Log transaction status']/following::td")).getText());
				sapConn.setStoreMessageBody(driver.findElement(By.xpath("//td[text()='Store message body']/following::td")).getText());
				sapConn.setEnableConnectionPooling(driver.findElement(By.xpath("//td[contains(., 'Enable Connection Pooling')]/following::td")).getText());
				sapConn.setMinimumPoolSize(driver.findElement(By.xpath("//td[contains(., 'Minimum Pool Size')]/following::td")).getText());
				sapConn.setMaximumPoolSize(driver.findElement(By.xpath("//td[contains(., 'Maximum Pool Size')]/following::td")).getText());
				sapConn.setPoolIncrementSize(driver.findElement(By.xpath("//td[contains(., 'Pool Increment Size')]/following::td")).getText());
				sapConn.setBlockTimeout(driver.findElement(By.xpath("//td[contains(., 'Block Timeout (msec)')]/following::td")).getText());
				sapConn.setExpireTimeout(driver.findElement(By.xpath("//td[contains(., 'Expire Timeout (msec)')]/following::td")).getText());
				sapConn.setStartupRetryCount(driver.findElement(By.xpath("//td[contains(., 'Startup Retry Count')]/following::td")).getText());
				sapConn.setStartupBackoffTimeout(driver.findElement(By.xpath("//td[contains(., 'Startup Backoff Timeout (sec)')]/following::td")).getText());
				
				sapConnections.set(i, sapConn);
			}
			
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"ScreenScraper.getSAPAdapterConnections() :: " + driver.getCurrentUrl());
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"  --> Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}
		return sapConnections;
	}

	public LinkedList<JDBCAdapterConnection> getJDBCAdapterConnections(String version) {
		LinkedList<JDBCAdapterConnection> jdbcConnections = new LinkedList<JDBCAdapterConnection>();
		try {
			
			// Set timeouts
			this.driver.manage().timeouts().implicitlyWait(30, java.util.concurrent.TimeUnit.SECONDS);
			// Get summary page
			this.driver.get(this.baseURL + "/invoke/pub.art.connection/listAdapterConnections?adapterTypeName=JDBCAdapter");

			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"getJDBCAdapterConnections :: " + this.driver.getCurrentUrl());
			
			// Loop through all the JDBC connections
			List<WebElement> lNodes = driver.findElements(By.xpath("//b[text()='connectionAlias']"));
			if ((lNodes != null) && (lNodes.size() > 0)) {
				for (int i = 0; i < lNodes.size(); i++) {
					JDBCAdapterConnection jdbcConn = new JDBCAdapterConnection();
					jdbcConn.setConnectionAlias(lNodes.get(i).findElement(By.xpath("./following::td")).getText());
					jdbcConn.setPackageName(lNodes.get(i).findElement(By.xpath("./following::td[3]")).getText());
					jdbcConn.setConnectionState(lNodes.get(i).findElement(By.xpath("./following::td[5]")).getText());
					jdbcConnections.add(jdbcConn);
				}
			}
			
			// Get Additional JDBC Adapter Connection Info
			for (int i = 0; i < jdbcConnections.size(); i++) {
				JDBCAdapterConnection jdbcConn = jdbcConnections.get(i);
				// Get JDBC Connection Details page (read-only)
				String nodeNameURLEncoded = URLEncoder.encode(jdbcConn.getConnectionAlias(),StandardCharsets.UTF_8.toString());
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  getSAPAdapterConnections for :: " + jdbcConn.getConnectionAlias());
				driver.get(this.baseURL + "/WmART/ConnNodeDetails.dsp?readOnly=true&connectionAlias=" + nodeNameURLEncoded + "&adapterTypeName=JDBCAdapter&searchConnectionName=");
				jdbcConn.setConnectionType(driver.findElement(By.xpath("//td[text()='Connection Type']/following::td")).getText());
				jdbcConn.setTransactionType(driver.findElement(By.xpath("//td[text()='Transaction Type']/following::td")).getText());
				jdbcConn.setDataSourceClass(driver.findElement(By.xpath("//td[text()='DataSource Class']/following::td")).getText());
				if (version.equals(Node.NODE_VERSION_900) || version.equals(Node.NODE_VERSION_960) || version.equals(Node.NODE_VERSION_980) || version.equals(Node.NODE_VERSION_990)) {
					jdbcConn.setServerName(driver.findElement(By.xpath("//td[text()='serverName']/following::td")).getText());
					jdbcConn.setUser(driver.findElement(By.xpath("//td[text()='user']/following::td")).getText());
					jdbcConn.setDatabaseName(driver.findElement(By.xpath("//td[text()='databaseName']/following::td")).getText());
					jdbcConn.setPortNumber(driver.findElement(By.xpath("//td[text()='portNumber']/following::td")).getText());
					jdbcConn.setNetworkProtocol(driver.findElement(By.xpath("//td[text()='networkProtocol']/following::td")).getText());
				} else {
					jdbcConn.setServerName(driver.findElement(By.xpath("//td[text()='Server Name']/following::td")).getText());
					jdbcConn.setUser(driver.findElement(By.xpath("//td[text()='User']/following::td")).getText());
					jdbcConn.setDatabaseName(driver.findElement(By.xpath("//td[text()='Database Name']/following::td")).getText());
					jdbcConn.setPortNumber(driver.findElement(By.xpath("//td[text()='Port Number']/following::td")).getText());
					jdbcConn.setNetworkProtocol(driver.findElement(By.xpath("//td[text()='Network Protocol']/following::td")).getText());
				}
				jdbcConn.setOtherProperties(driver.findElement(By.xpath("//td[text()='Other Properties']/following::td")).getText());
				jdbcConn.setEnableConnectionPooling(driver.findElement(By.xpath("//td[contains(., 'Enable Connection Pooling')]/following::td")).getText());
				jdbcConn.setMinimumPoolSize(driver.findElement(By.xpath("//td[contains(., 'Minimum Pool Size')]/following::td")).getText());
				jdbcConn.setMaximumPoolSize(driver.findElement(By.xpath("//td[contains(., 'Maximum Pool Size')]/following::td")).getText());
				jdbcConn.setPoolIncrementSize(driver.findElement(By.xpath("//td[contains(., 'Pool Increment Size')]/following::td")).getText());
				jdbcConn.setBlockTimeout(driver.findElement(By.xpath("//td[contains(., 'Block Timeout (msec)')]/following::td")).getText());
				jdbcConn.setExpireTimeout(driver.findElement(By.xpath("//td[contains(., 'Expire Timeout (msec)')]/following::td")).getText());
				jdbcConn.setStartupRetryCount(driver.findElement(By.xpath("//td[contains(., 'Startup Retry Count')]/following::td")).getText());
				jdbcConn.setStartupBackoffTimeout(driver.findElement(By.xpath("//td[contains(., 'Startup Backoff Timeout (sec)')]/following::td")).getText());
				
				jdbcConnections.set(i, jdbcConn);
			}
			
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"ScreenScraper.getJDBCAdapterConnections() :: " + driver.getCurrentUrl());
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"  --> Exception :: " + ex.getMessage());
		} finally {
			driver.quit();
		}
		return jdbcConnections;
	}
	
}