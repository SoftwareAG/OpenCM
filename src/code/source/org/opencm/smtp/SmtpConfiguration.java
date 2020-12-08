package org.opencm.smtp;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.opencm.configuration.Configuration;
import org.opencm.inventory.Inventory;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.secrets.SecretsUtils;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;

public class SmtpConfiguration {
	
    public static final String 	SMTP_ENTRY_NAME			= "SMTP";
    public static final File 	SMTP_CONFIG_DIRECTORY	= new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_SMTP);
	public static final File 	SMTP_CONFIGURATION_FILE = new File(SMTP_CONFIG_DIRECTORY.getPath() + File.separator + "configuration.json");
    
    private String hostname;
    private String port;
    private String starttls;
    private String username;
    private String password;
    private String password_handle;
    private String from_email;
    private String subject;
    
    private LinkedList<Property> properties;
    private LinkedList<String> recipients;
    
    private String body;
    
    public static SmtpConfiguration instantiate() {
    	SmtpConfiguration smtpConf = null;
    	
    	checkDirectory();
		if (!SMTP_CONFIG_DIRECTORY.exists()) {
			return smtpConf;
		}
    	
    	if (!SMTP_CONFIGURATION_FILE.exists()) {
    		LogUtils.logDebug("SmtpConfiguration - File not found: " + SMTP_CONFIGURATION_FILE.getPath() + "... generating defaults.");
    		// Write defaults
    		SmtpConfiguration smtpConfig = new SmtpConfiguration();
    		saveConfiguration(smtpConfig, null, null);
    	}
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		smtpConf = mapper.readValue(SMTP_CONFIGURATION_FILE, SmtpConfiguration.class);
    	} catch (Exception e) {
    		LogUtils.logError("SmtpConfiguration - Exception: " + e.toString());
    	}
    	return smtpConf;
    }


    public String getHostname() {
        return this.hostname;
    }
    public void setHostname(String host) {
        this.hostname = host;
    }
    
    public String getPort() {
        return this.port;
    }
    public void setPort(String port) {
        this.port = port;
    }
    
    @JsonGetter("starttls")
    public String getStartTLS() {
        return this.starttls;
    }
    public void setStartTLS(String tls) {
        this.starttls = tls;
    }
    
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String name) {
        this.username = name;
    }

    public String getPassword() {
        return this.password;
    }
    public void setPassword(String pwd) {
        this.password = pwd;
    }
    
    @JsonGetter("password_handle")
    public String getPasswordHandle() {
        return this.password_handle;
    }
    public void setPasswordHandle(String pwd) {
        this.password_handle = pwd;
    }
    
    @JsonGetter("from_email")
    public String getFromEmail() {
        return this.from_email;
    }
    public void setFromEmail(String from) {
        this.from_email = from;
    }

    public String getSubject() {
        return this.subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public LinkedList<Property> getProperties() {
        return this.properties;
    }
    public void setProperties(LinkedList<Property> props) {
        this.properties = props;
    }
    
    public LinkedList<String> getRecipients() {
        return this.recipients;
    }
    public void setRecipients(LinkedList<String> recps) {
        this.recipients = recps;
    }

    @JsonIgnore
    public String getBody() {
        return this.body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    
    @JsonIgnore
    public static void checkDirectory() {
    	if (!SMTP_CONFIG_DIRECTORY.exists()) {
    		if (!SMTP_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + SMTP_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }
    
    @JsonIgnore
    public static void saveConfiguration(SmtpConfiguration smtpConfig, SecretsConfiguration secConfig, SimpleDatabase db) {
		if ((secConfig != null) && secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL) && (db != null)) {
	    	// Add/update password only if present
	    	if ((smtpConfig.getPassword() != null) && (smtpConfig.getPassword() != "")) {
	        	ArrayList<String> path = new ArrayList<String>();
	        	path.add(Inventory.INVENTORY_TOP_GROUP);
	        	db = SecretsUtils.createEntry(secConfig, db, path, SMTP_ENTRY_NAME, smtpConfig.getUsername(), smtpConfig.getPassword());
	        	smtpConfig.setPasswordHandle("/" + String.join("/", path) +  "/" + SMTP_ENTRY_NAME);
	        	smtpConfig.setPassword("");
	    	}
    	}
    	
		// Save configuration to file 
		String json = JsonUtils.convertJavaObjectToJson(smtpConfig);
		FileUtils.writeToFile(SMTP_CONFIGURATION_FILE.getPath(), json);
    }

}
