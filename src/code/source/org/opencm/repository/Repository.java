package org.opencm.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Properties;

import org.opencm.configuration.Configuration;
import org.opencm.util.Cache;
import org.opencm.util.LogUtils;

import org.opencm.repository.db.*;

public class Repository {

    public static String REPOSITORY_CACHE_KEY		= "OPENCM_REPOSITORY_DB_KEY";
    public static String OPENCM_REPO_NAME 			= "OPENCM_REPOSITORY_DB";
    public static String OPENCM_REPO_PROTOCOL		= "jdbc:derby:";
    public static String OPENCM_REPO_USER 			= "opencm_repo_user";
    public static String OPENCM_REPO_USER_PWD		= "manage";
    
    private Connection connection;
    private Configuration opencmConfig;
    
    public Repository() {
    	
    }
   	
    public static Repository instantiate(Configuration opencmConfig) {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"org.opencm.repository.Repository: instantiate() - Instantiation repository " + OPENCM_REPO_NAME + ".... ");
    	Repository repo = (Repository) Cache.getInstance().get(REPOSITORY_CACHE_KEY);
    	if (repo != null) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"org.opencm.repository.Repository: instantiate() - Repository already in cache: Returning .... ");
    		return repo;
    	}

		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"org.opencm.repository.Repository: instantiate() - Repository not in cache - generating .... ");
   		
   		repo = new Repository();
   		repo.setConfiguration(opencmConfig);
    	repo.openConnection();

    	Cache.getInstance().set(REPOSITORY_CACHE_KEY, repo);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"org.opencm.repository.Repository: instantiate() - Repository Instantiation finished .... ");
    	return repo;
    }
    
    public void openConnection() {
    	if (this.connection == null) {
			try {
				Properties props = new Properties();
				props.put("user", OPENCM_REPO_USER);
				props.put("password", OPENCM_REPO_USER_PWD);
		        this.connection = DriverManager.getConnection(OPENCM_REPO_PROTOCOL + OPENCM_REPO_NAME + ";create=true", props);
				LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_INFO,"org.opencm.repository.Repository: setConnection - Connected to database " + OPENCM_REPO_NAME);
				
			} catch (Exception ex) {
				LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.Repository: setConnection Exception: " + ex.getMessage());
			}
    	}
    }
    
    public void closeConnection() {
		try {
            if (this.connection != null) {
            	this.connection.close();
            	this.connection = null;
            }
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_INFO,"org.opencm.repository.Repository: closeConnection - Connection to database closed: " + OPENCM_REPO_NAME);
		} catch (SQLException ex) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.Repository: closeConnection Exception: " + ex.getMessage());
		}

    }
    
    public void shutdownDatabase() {
		try {
	        DriverManager.getConnection(OPENCM_REPO_PROTOCOL + OPENCM_REPO_NAME + ";shutdown=true");
        	this.connection = null;
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_INFO,"org.opencm.repository.Repository: shutdownDatabase - Database " + OPENCM_REPO_NAME + " shut down.");
		} catch (SQLException ex) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"org.opencm.repository.Repository: " + ex.getMessage());
		}

    }
    
    private void setConfiguration(Configuration opencmConfig) {
        this.opencmConfig = opencmConfig;
    }
    
    public void createSchemas() {
    	
    	if (this.connection == null) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.Repository: createSchema: Connection to DB is not available.");
    		return;
    	}
    	
    	if (Organisation.exists(opencmConfig, this.connection)) {
			LogUtils.log(this.opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"org.opencm.repository.Repository: createSchema: Schemas already exist.");
    		return;
    	} else {
    		Organisation.create(opencmConfig, this.connection);
    	}

    }
    
}
