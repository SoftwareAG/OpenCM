package org.opencm.secrets;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wm.app.b2b.server.globalvariables.GlobalVariablesManager;

import org.opencm.configuration.Configuration;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;

public class SecretsConfiguration {

    public static final String 	TYPE_LOCAL					= "LOCAL";
    public static final String  GC_KEEPASS_PASSWORD			= "OPENCM_KEEPASS_PASSWORD";
    public static final String 	TYPE_VAULT					= "VAULT";
    public static final String  GC_VAULT_TOKEN				= "OPENCM_VAULT_TOKEN";
    
    public static final File 	SECRETS_CONFIG_DIRECTORY	= new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_SECRETS);
    public static final File 	SECRETS_CONFIGURATION_FILE	= new File(SECRETS_CONFIG_DIRECTORY.getPath() + File.separator + "configuration.json");
    
    public static final String 	SECRETS_LOCAL_KEEPASS_FILE	= "secrets.kdbx";
    
    private String type;
    private String keepassPassword;
    private String vaultToken;
    private String vaultURL;
    private String vaultVersion;
    
    public static SecretsConfiguration instantiate() {
    	SecretsConfiguration secConf = null;
    	
    	checkDirectory();
    	checkKeepassPassword();
    	checkVaultToken();
    	
    	if (!SECRETS_CONFIGURATION_FILE.exists()) {
    		LogUtils.logDebug("SecretsConfiguration - File not found: " + SECRETS_CONFIGURATION_FILE.getPath() + "... generating defaults.");
    		// Write defaults
    		SecretsConfiguration secretsConfig = new SecretsConfiguration();
    		secretsConfig.setType(TYPE_LOCAL);
    		saveConfiguration(secretsConfig);
    	}
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		secConf = mapper.readValue(SECRETS_CONFIGURATION_FILE, SecretsConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("SecretsConfiguration - Exception: " + e.toString());
    	}
    	return secConf;
    }

    public SecretsConfiguration() {
    }
    
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    @JsonGetter("keepass_password")
    public String getKeepassPassword() {
        return this.keepassPassword;
    }
    public void setKeepassPassword(String pwd) {
        this.keepassPassword = pwd;
    }
    
    @JsonGetter("vault_token")
    public String getVaultToken() {
        return this.vaultToken;
    }
    public void setVaultToken(String token) {
        this.vaultToken = token;
    }
    
    @JsonGetter("vault_url")
    public String getVaultURL() {
        return this.vaultURL;
    }
    public void setVaultURL(String url) {
        this.vaultURL = url;
    }
   
    @JsonGetter("vault_version")
    public String getVaultVersion() {
        return this.vaultVersion;
    }
    
    public void setVaultVersion(String version) {
        this.vaultVersion = version;
    }
    
    @JsonIgnore
    public static void checkDirectory() {
    	if (!SECRETS_CONFIG_DIRECTORY.exists()) {
    		if (!SECRETS_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + SECRETS_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }
    
    @JsonIgnore
    private static void checkKeepassPassword() {
    	// ------------------------------------------------------
		// Create Global Variable for keepass password (if it doesn't exist)
    	// ------------------------------------------------------
		try {
	    	GlobalVariablesManager.getInstance().addGlobalVariable(SecretsConfiguration.GC_KEEPASS_PASSWORD, "manage", true);
			LogUtils.logInfo("SecretsConfiguration: Default Keepass Master password created ... ");
		} catch (Exception e) {
			LogUtils.logDebug("SecretsConfiguration: Keepass Master password already exists... ");
		}
    }
    
    @JsonIgnore
    private static void checkVaultToken() {
    	// ------------------------------------------------------
		// Create Global Variable for vault token (if it doesn't exist)
    	// ------------------------------------------------------
		try {
	    	GlobalVariablesManager.getInstance().addGlobalVariable(SecretsConfiguration.GC_VAULT_TOKEN, "", true);
			LogUtils.logInfo("SecretsConfiguration: Default Vault Token created (empty string) ... ");
		} catch (Exception e) {
			LogUtils.logDebug("SecretsConfiguration: Global Variable for Vault Token already exists... ");
		}
    }
    
    @JsonIgnore
    public static void saveConfiguration(SecretsConfiguration secConfig) {
		if (secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL)) {
	    	if ((secConfig.getKeepassPassword() != null) && !secConfig.getKeepassPassword().equals("")) {
	    		try { // Update GV (assume it exists)
	    	    	GlobalVariablesManager mgr = GlobalVariablesManager.getInstance();
	    			LogUtils.logInfo("SecretsConfiguration - Updating Master Keepass password ...");
					mgr.editGlobalVariable(SecretsConfiguration.GC_KEEPASS_PASSWORD, secConfig.getKeepassPassword(), true);
	    		} catch (Exception e) {
	    			LogUtils.logFatal("SecretsConfiguration Exception: " + e.toString());
	    		}
	    		secConfig.setKeepassPassword("");
	    	}
    	}
		if (secConfig.getType().equals(SecretsConfiguration.TYPE_VAULT)) {
	    	if ((secConfig.getVaultToken() != null) && !secConfig.getVaultToken().equals("")) {
	    		try { // Update GV (assume it exists now)
	    	    	GlobalVariablesManager mgr = GlobalVariablesManager.getInstance();
	    			LogUtils.logInfo("SecretsConfiguration - Updating Vault Token ...");
	   				mgr.editGlobalVariable(SecretsConfiguration.GC_VAULT_TOKEN, secConfig.getVaultToken(), true);
	    		} catch (Exception e) {
	    			LogUtils.logFatal("SecretsConfiguration Exception: " + e.toString());
	    		}
	    		secConfig.setVaultToken("");
	    	}
		}

		String json = JsonUtils.convertJavaObjectToJson(secConfig);
		FileUtils.writeToFile(SECRETS_CONFIGURATION_FILE.getPath(), json);
    }

}
