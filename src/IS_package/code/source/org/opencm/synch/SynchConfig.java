package org.opencm.synch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.opencm.configuration.Configuration;
import org.opencm.security.KeyUtils;
import org.opencm.util.LogUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class SynchConfig {
	
	public static final String		ENCRYPT_PREFIX		= "ENCRYPTED::";
	
	private String ftps_server;
	private String ftps_port;
	private String ftps_timeout_ms;
	private String ftps_username;
	private String ftps_password;


    public static SynchConfig instantiate(Configuration opencmConfig) {
    	
		File synchConfigFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_SYNCH_PROPS);
		if (!synchConfigFile.exists()) {
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_CRITICAL,"Unable to access OpenCM synch configuration file: " + synchConfigFile.getPath());
		}

		ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); 
		try {
			return mapper.readValue(synchConfigFile, SynchConfig.class);
		} catch (Exception e) {
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_CRITICAL,"OpenCM Properties Exception: " + e.toString());
		}
		return null;
    }
    
    
    public String getRemote_opencm_ftps_server() {
        return this.ftps_server;
    }
    public void setRemote_opencm_ftps_server(String server) {
        this.ftps_server = server;
    }

    public String getRemote_opencm_ftps_port() {
        return this.ftps_port;
    }
    @JsonIgnore
    public int getPort() {
		return new Integer(getRemote_opencm_ftps_port()).intValue();
    }
    public void setRemote_opencm_ftps_port(String port) {
        this.ftps_port = port;
    }

    public String getRemote_opencm_ftps_timeout_ms() {
        return this.ftps_timeout_ms;
    }
    @JsonIgnore
    public int getTimeout() {
		return new Integer(getRemote_opencm_ftps_timeout_ms()).intValue();
    }
    public void setRemote_opencm_ftps_timeout_ms(String timeout_ms) {
        this.ftps_timeout_ms = timeout_ms;
    }

    public String getRemote_opencm_ftps_username() {
        return this.ftps_username;
    }
    public void setRemote_opencm_ftps_username(String username) {
        this.ftps_username = username;
    }

    public String getRemote_opencm_ftps_password() {
        return this.ftps_password;
    }
    public void setRemote_opencm_ftps_password(String password) {
        this.ftps_password = password;
    }

    @JsonIgnore
    public String getDecryptedPassword(String masterPwd) {
		KeyUtils keyUtils = new KeyUtils(masterPwd);
		String passwd = getRemote_opencm_ftps_password();
		passwd = passwd.substring(12);
		passwd = passwd.substring(0, passwd.lastIndexOf("]"));
		return keyUtils.decrypt(passwd);
    }
    
    public boolean passwordEncrypted() {
    	if (getRemote_opencm_ftps_password().startsWith("[" + ENCRYPT_PREFIX)) {
    		return true;
    	}
    	return false;
    }
    
    public void decryptPassword(String masterPwd) {
		KeyUtils keyUtils = new KeyUtils(masterPwd);
		String passwd = getRemote_opencm_ftps_password();
		passwd = passwd.substring(12);
		passwd = passwd.substring(0, passwd.lastIndexOf("]"));
		setRemote_opencm_ftps_password(keyUtils.decrypt(passwd));
    }
    
    public void encryptPassword(String masterPwd) {
		KeyUtils keyUtils = new KeyUtils(masterPwd);
		setRemote_opencm_ftps_password("[" + ENCRYPT_PREFIX + keyUtils.encrypt(getRemote_opencm_ftps_password()) + "]");
    }

    @JsonIgnore
    public void ensureDecryptedPassword(Configuration opencmConfig, String masterPwd) {

		if (!passwordEncrypted()) {
			encryptPassword(masterPwd);
			rewriteConfig(opencmConfig);
		}

    }
    
    @JsonIgnore
    public void rewriteConfig(Configuration opencmConfig) {
		try {
			File synchConfigFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_SYNCH_PROPS);
			
			// Pick up the Comments
			HashMap<Integer,String> hmComments = new HashMap<Integer,String>();
			try(BufferedReader br = new BufferedReader(new FileReader(synchConfigFile))) {
				int idx = 0;
			    for(String line; (line = br.readLine()) != null; ) {
			        if (line.trim().startsWith("#") || line.trim().equals("")) {	// comment or blank lines
			        	hmComments.put(idx, line);
			        }
			        idx++;
			    }
			}
			
			// Write updated nodes to the property file
	    	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			ObjectWriter writer = mapper.writer();
			SequenceWriter sw = writer.writeValues(synchConfigFile);
			sw.write(this);
			
			sw.close();
			
			// Re-write the Comments
			LinkedList<String> nodesList = new LinkedList<String>();
			// Read in the newly updated file
			try(BufferedReader br = new BufferedReader(new FileReader(synchConfigFile))) {
			    for(String line; (line = br.readLine()) != null; ) {
			    	if (!line.equals("---")) {
				    	nodesList.add(line);
			    	}
			    }
			}
			// Loop through the lines
			StringBuffer sb = new StringBuffer();
			int totalLines = nodesList.size() + hmComments.size();
			int nodesLinesIdx = 0;
			for (int i = 0; i < (totalLines); i++) {
		    	if (hmComments.containsKey(i)) {
		    		sb.append(hmComments.get(i) + System.lineSeparator());
		    	} else {
		    		sb.append(nodesList.get(nodesLinesIdx++) + System.lineSeparator());
		    	}
			}
			
			BufferedWriter bwr = new BufferedWriter(new FileWriter(synchConfigFile));
            bwr.write(sb.toString());
           
            //flush the stream
            bwr.flush();
           
            //close the stream
            bwr.close();

		} catch (IOException ex) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," writeNodes - Exception: " + ex.toString());
		}

    }
    
}
