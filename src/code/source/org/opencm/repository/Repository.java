package org.opencm.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import org.opencm.configuration.Configuration;
import org.opencm.util.Cache;
import org.opencm.util.LogUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Repository {

	public static File REPOSITORY_CONFIG_DIRECTORY 		= new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_REPOSITORY);
    public static final String REPOSITORY_CACHE_KEY		= "OPENCM_REPOSITORY";

    private LinkedList<RepoInstallation> installations = new LinkedList<RepoInstallation>();
    
    public Repository() {
    }
    
    public static Repository instantiate() {
		LogUtils.logDebug("Repository Instantiation starting .... ");
		Repository repo = (Repository) Cache.getInstance().get(REPOSITORY_CACHE_KEY);
    	if (repo != null) {
    		LogUtils.logDebug("Repository already in cache: Returning .... ");
    		return repo;
    	}

		LogUtils.logDebug("Repository not in cache - generating .... ");
		repo = new Repository();
		
    	Cache.getInstance().set(REPOSITORY_CACHE_KEY, repo);
		LogUtils.logDebug(" Repository Instantiation finishing .... ");
    	return repo;
    }
    
    public LinkedList<RepoInstallation> getInstallations() {
        return this.installations;
    }
    
    public RepoInstallation getInstallation(ArrayList<String> path) {
    	for (RepoInstallation inst : installations) {
    		if (inst.getPath().equals(path)) {
    			return inst;
    		}
    	}
        return null;
    }
    
    public RepoInstallation addInstallation(ArrayList<String> path) {
    	RepoInstallation newRepoInstallation = getRepoInstallation(path);
    	this.installations.add(newRepoInstallation);
    	Cache.getInstance().set(REPOSITORY_CACHE_KEY, this);
    	return newRepoInstallation;
    }
    
	/*
	 * Reading in repository from file system
	 * 
	 */
	public static RepoInstallation getRepoInstallation(ArrayList<String> path) {
		LogUtils.logDebug("Repository :: getInstallation :: " + path.toString());
		
		// -------------------------------------------------------
		// Determine repository
		// -------------------------------------------------------
		File repoFile = getInstallationRepositoryFile(path);
		if (repoFile == null) {
			LogUtils.logInfo("Repository :: getInstallation: repo does not exist: " + path.toString());
			return null;
		}
		
		RepoInstallation repoInstallation = null;
    	
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		repoInstallation = mapper.readValue(repoFile, RepoInstallation.class);
    	} catch (Exception e) {
    		LogUtils.logError("Repository :: getInstallation - Exception: " + e.toString());
    	}
    	
		LogUtils.logDebug("Repository :: returning installation :: " + repoInstallation.getName());
    	return repoInstallation;
		
	}
	
	public static boolean repoExists(ArrayList<String> path) {
		if (getInstallationRepositoryFile(path).exists()) {
			return true;
		}
		return false;
	}
	
    @JsonIgnore
    public static void checkDirectory() {
    	if (!REPOSITORY_CONFIG_DIRECTORY.exists()) {
    		if (!REPOSITORY_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + REPOSITORY_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }
    
	public static File getInstallationRepositoryFile(ArrayList<String> path) {
		String strPath = path.toString();
		strPath = strPath.replaceAll("\\s","").replaceAll(",","_").replaceAll("\\[","").replaceAll("\\]","");
		File repoFile = new File(REPOSITORY_CONFIG_DIRECTORY.getPath() + File.separator + strPath + ".json");
		return repoFile;
	}
    
}
