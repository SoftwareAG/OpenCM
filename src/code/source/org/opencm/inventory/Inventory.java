package org.opencm.inventory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.io.File;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.opencm.configuration.Configuration;
import org.opencm.repository.Property;
import org.opencm.repository.Repository;
import org.opencm.repository.RepoInstallation;
import org.opencm.secrets.SecretsUtils;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.ui.*;
import org.opencm.util.Cache;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
import org.opencm.util.TimeUtils;

@JsonSerialize
public class Inventory {

	public static final File INVENTORY_CONFIG_DIRECTORY = new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_INVENTORY);
	public static final File INVENTORY_FILE 			= new File(INVENTORY_CONFIG_DIRECTORY.getPath() + File.separator + "inventory.json");
	
    public static final String 	INVENTORY_TOP_GROUP				= "OpenCM";
	public static final String 	INVENTORY_CACHE_KEY				= "OPENCM_INVENTORY";
	public static final String 	INVENTORY_SECRETS_CACHE_KEY		= "OPENCM_INVENTORY_SECRETS";
	
    private InventoryGroup rootGroup = new InventoryGroup();
    
    public Inventory() {
    }
    
    public static Inventory instantiate() {
		LogUtils.logDebug("Inventory Instantiation starting .... ");
    	Inventory inv = (Inventory) Cache.getInstance().get(INVENTORY_CACHE_KEY);
    	if (inv != null) {
    		LogUtils.logDebug("Inventory already in cache: Returning copy .... ");
    		return inv;
    	}

		LogUtils.logDebug("Inventory is not in cache - generating .... ");
		
    	if (!INVENTORY_FILE.exists()) {
    		LogUtils.logInfo("Inventory :: no Inventory present, creating new.");
    		InventoryGroup initialGroup = new InventoryGroup(INVENTORY_TOP_GROUP, null);
    		Inventory newInventory = new Inventory();
    		newInventory.setRootGroup(initialGroup);
    		return newInventory;
    	}
		
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		inv = mapper.readValue(INVENTORY_FILE, Inventory.class);
    	} catch (Exception e) {
    		LogUtils.logError("Inventory - Exception: " + e.toString());
    		return null;
    	}
    	
    	// Add additional information for UI display
    	decorateInventory(inv.getRootGroup());
    	
    	Cache.getInstance().set(INVENTORY_CACHE_KEY, inv);
		LogUtils.logDebug("Inventory Instantiation completed .... ");
		
    	return inv;
		
    }

    @JsonIgnore
    public static Inventory getInstance() {
    	return (Inventory) Cache.getInstance().get(INVENTORY_CACHE_KEY);
    }
    
    @JsonGetter("root_group")
    public InventoryGroup getRootGroup() {
        return this.rootGroup;
    }
    public void setRootGroup(InventoryGroup group) {
        this.rootGroup = group;
    }
    
    /*
     * Get all installations from an inventory
     * 
     */
    public static void getInstallations(InventoryGroup inventoryGroup, LinkedList<InventoryInstallation> installations) {
    	if (inventoryGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : inventoryGroup.getInstallations()) {
        		installations.add(inst);
    		}
    		return;
    	}
    	for (InventoryGroup group : inventoryGroup.getGroups()) {
    		getInstallations(group,installations);
    	}
    }
    
    /*
     * Get all installations from an inventory based on paths
     * 
     */
    public static void getInstallations(InventoryGroup inventoryGroup, LinkedList<InventoryInstallation> installations, LinkedList<ArrayList<String>> paths) {
    	if (inventoryGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : inventoryGroup.getInstallations()) {
        		for (ArrayList<String> path : paths) {
            		if (inst.getPath().equals(path)) {
                		installations.add(inst);
                		break;
            		}
        		}
    		}
    		return;
    	}
    	for (InventoryGroup group : inventoryGroup.getGroups()) {
    		getInstallations(group, installations, paths);
    	}
    }
    
    /*
     * Get a single installation from an inventory based on a path
     * 
     */
    public static void getInstallation(InventoryGroup inventoryGroup, LinkedList<InventoryInstallation> installation, ArrayList<String> path) {
    	if (inventoryGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : inventoryGroup.getInstallations()) {
        		if (inst.getPath().equals(path)) {
            		installation.add(inst);
        		}
    		}
    		return;
    	}
    	for (InventoryGroup group : inventoryGroup.getGroups()) {
    		getInstallation(group, installation, path);
    	}
    }
    
    /*
     * Get all OpenCM targets (for synchronization)
     * 
     */
    public static void getSynchTargets(InventoryGroup inventoryGroup, LinkedList<InventoryInstallation> installations) {
    	if (inventoryGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : inventoryGroup.getInstallations()) {
        		for (InventoryRuntime rc : inst.getRuntimes()) {
            		if (rc.getName().equals(InventoryRuntime.RUNTIME_NAME_SYNCH)) {
                		installations.add(inst);
            		}
        		}
    		}
    		return;
    	}
    	for (InventoryGroup group : inventoryGroup.getGroups()) {
    		getSynchTargets(group,installations);
    	}
    }
    
    @JsonIgnore
    public void saveInventory() {
		File savedFile = new File(INVENTORY_FILE.getPath() + "." + TimeUtils.getDateTimeFileFormat() + ".bkp");
		FileUtils.renameFile(INVENTORY_FILE, savedFile);
		// Ensure no passwords are stored...
		excludeSecrets(getRootGroup());
		String json = JsonUtils.convertJavaObjectToJson(this);
		FileUtils.writeToFile(INVENTORY_FILE.getPath(), json);
    }
    
    @JsonIgnore
    public static void updateSecrets(InventoryGroup invGroup, SecretsConfiguration secConfig, SimpleDatabase db) {
    	for (InventoryGroup group : invGroup.getGroups()) {
    		updateSecrets(group, secConfig, db);
    	}
    	if (invGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : invGroup.getInstallations()) {
    			for (InventoryRuntime rc : inst.getRuntimes()) {
    				if ((rc.getPassword() != null) && !rc.getPassword().equals("")) {
                    	LogUtils.logDebug("Inventory updateSecrets: adding/updating secret for " + inst.getName() + " -> " + rc.getName());
            			db = SecretsUtils.createEntry(secConfig, db, inst.getPath(), rc.getName(), rc.getUsername(), rc.getPassword());
            			rc.setPassword("");
    				}
    				// Update handle
        			rc.setPasswordHandle("/" + String.join("/", inst.getPath()) +  "/" + rc.getName());
    			}
    		}
    	}
    }
    
    @JsonIgnore
    public static void includeSecrets(InventoryGroup invGroup, SecretsConfiguration secConfig, SimpleDatabase db) {
    	for (InventoryGroup group : invGroup.getGroups()) {
    		includeSecrets(group, secConfig, db);
    	}
    	if (invGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : invGroup.getInstallations()) {
    			for (InventoryRuntime rc : inst.getRuntimes()) {
    				// Make an attempt to get pwd:
    				if ((rc.getPasswordHandle() != null) && !rc.getPasswordHandle().equals("")) {
                    	LogUtils.logDebug("Inventory includeSecrets: getting secret for " + inst.getName() + " -> " + rc.getName());
        				String pwd = SecretsUtils.getPassword(secConfig, rc.getUsername(), rc.getPasswordHandle());
        				if (pwd != null) {
        					rc.setPassword(pwd);
        				}
    				}
    			}
    		}
    	}
    }
    
    @JsonIgnore
    private static void excludeSecrets(InventoryGroup invGroup) {
    	for (InventoryGroup group : invGroup.getGroups()) {
    		excludeSecrets(group);
    	}
    	if (invGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : invGroup.getInstallations()) {
    			for (InventoryRuntime rc : inst.getRuntimes()) {
					rc.setPassword("");
    			}
    		}
    	}
    }
    
    @JsonIgnore
    /*
     * Adding additional runtime extraction information to the inventory passed back to UI
     */
    public static void decorateInventory(InventoryGroup invGroup) {
    	for (InventoryGroup group : invGroup.getGroups()) {
    		decorateInventory(group);
    	}
    	if (invGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : invGroup.getInstallations()) {
    			LogUtils.logDebug("decorateInventory :: " + inst.getName());
    			
            	// Does repo exist?
    			boolean repoExists = Repository.repoExists(inst.getPath());
    			inst.setRepoExists(repoExists);
    	    	if (repoExists) {
    	    		// --------------------------------------------------------------------
    	    		// Read in Repository
    	    		// --------------------------------------------------------------------
    	    		Repository repo = Repository.instantiate();
    	    		RepoInstallation repoInstallation = repo.getInstallation(inst.getPath());
    	    		if (repoInstallation == null) {
    	    			repoInstallation = repo.addInstallation(inst.getPath());
    	    		}
    	    		if (repoInstallation != null) {

    	        		// Extraction Time
    	    			LogUtils.logDebug("decorateInventory :: Extraction Time ... ");
    	        		String extractionTime = repoInstallation.getExtractTime();
    	        		if (extractionTime != null) {
    	        			inst.addProperty(RepoInstallation.INSTANCE_NAME_INFO, RepoInstallation.PROPERTY_KEY_EXTR_TIME, extractionTime);
    	        		}
    	        		
    	        		// Extraction Alias
    	    			LogUtils.logDebug("decorateInventory :: Extraction Alias ... ");
    	        		String extractionAlias = repoInstallation.getExtractAlias();
    	        		if (extractionAlias != null) {
    	        			inst.addProperty(RepoInstallation.INSTANCE_NAME_INFO, RepoInstallation.PROPERTY_KEY_EXTR_ALIAS, extractionAlias);
    	        		}
    	        		
    	        		// Installation Time
    	    			LogUtils.logDebug("decorateInventory :: Installation Time ... ");
    	        		String installTime = repoInstallation.getInstallTime();
    	        		if (installTime != null) {
    	        			inst.addProperty(RepoInstallation.INSTANCE_NAME_INFO, RepoInstallation.PROPERTY_KEY_INST_TIME, TimeUtils.formatDate01(installTime));
    	        		}
    	        		
    	        		// Installation Directory
    	    			LogUtils.logDebug("decorateInventory :: Installation Dir ... ");
    	        		String installDir = repoInstallation.getInstallationDirectory();
    	        		if (installDir != null) {
    	        			inst.addProperty(RepoInstallation.INSTANCE_NAME_INFO, RepoInstallation.PROPERTY_KEY_DIR,installDir);
    	        		}
    	        		
    	        		// Version
    	    			LogUtils.logDebug("decorateInventory :: Version ... ");
    	        		String version = repoInstallation.getVersion();
    	        		if (version != null) {
    	        			inst.addProperty(RepoInstallation.INSTANCE_NAME_INFO, RepoInstallation.PROPERTY_KEY_VERSION, version);
    	        		}

    	        		// Operating System
    	    			LogUtils.logDebug("decorateInventory :: OS ... ");
    	        		String os = repoInstallation.getOperatingSystem();
    	        		if (os != null) {
    	        			inst.addProperty(RepoInstallation.INSTANCE_NAME_INFO, RepoInstallation.PROPERTY_KEY_OPERATING_SYSTEM, os);
    	        		}
    	        		
    	        		// Physical CPUs
    	    			LogUtils.logDebug("decorateInventory :: Phys CPUs ... ");
    	        		String cpuPhysical = repoInstallation.getCpuPhysical();
    	        		if (cpuPhysical != null) {
    	        			inst.addProperty(RepoInstallation.INSTANCE_NAME_INFO, RepoInstallation.PROPERTY_KEY_CPU_PHYSICAL, cpuPhysical);
    	        		}

    	        		// Logical CPUs
    	    			LogUtils.logDebug("decorateInventory :: Logical CPUs ... ");
    	        		String cpuLogical = repoInstallation.getCpuPhysical();
    	        		if (cpuLogical != null) {
    	        			inst.addProperty(RepoInstallation.INSTANCE_NAME_INFO, RepoInstallation.PROPERTY_KEY_CPU_LOGICAL, cpuLogical);
    	        		}
    	        		
    	        		// Ports
    	    			LogUtils.logDebug("decorateInventory :: Ports ... ");
    	        		LinkedList<Port> ports = repoInstallation.getPorts();
    	        		if (ports != null) {
    	        			for (Port port : ports) {	// Instance names = "install_port_spm/install_port_is_default/etc."
    	        				inst.addProperty(port.getInstanceName(), RepoInstallation.PROPERTY_KEY_PORT_RUNTIME, port.getComponent());
    	        				inst.addProperty(port.getInstanceName(), Property.PROPERTY_NAME_ID, port.getIdentifier());
    	        				inst.addProperty(port.getInstanceName(), Property.PROPERTY_NAME_PORT_NUMBER, port.getNumber());
    	        				inst.addProperty(port.getInstanceName(), Property.PROPERTY_NAME_PORT_PROTOCOL, port.getProtocol());
    	        				inst.addProperty(port.getInstanceName(), Property.PROPERTY_NAME_PORT_ENABLED, port.getEnabled());
    	        				inst.addProperty(port.getInstanceName(), Property.PROPERTY_NAME_PORT_BINDADDRESS, port.getBindAddress());
    	        				// Alternative hostname
	                			for (InventoryRuntime rt : inst.getRuntimes()) {
	                				if (rt.getName().equals(port.getComponent()) && rt.getPort().equals(port.getNumber())) {
	                           			inst.addProperty(port.getInstanceName(), InventoryRuntime.RUNTIME_PROPERTY_ALT_HOST, rt.getAltHostname());
	                           			break;
	                				}
	                			}
    	               			// Additional Load Balancer info
	                			int lb_idx = 0;
	                			for (InventoryLB lb : inst.getLoadBalancers()) {
	                				if (lb.getPort().equals(port.getNumber())) {
	                           			inst.addProperty(port.getInstanceName(), RepoInstallation.PROPERTY_KEY_PORT_LB + "_" + lb_idx, lb.getUrl());
	                           			lb_idx++;
	                				}
	                			}
    	        			}
    	        		}
    	        		// Internal wM DB info
    	    			LogUtils.logDebug("decorateInventory :: Product DBs ... ");
    	        		LinkedList<WmDb> wMDbs = repoInstallation.getWmDbs();
    	        		if (wMDbs != null) {
    	        			for (WmDb wmdb : wMDbs) {
    	        				inst.addProperty(wmdb.getInstanceName(), Property.PROPERTY_NAME_ID, wmdb.getId());
    	        				inst.addProperty(wmdb.getInstanceName(), Property.PROPERTY_NAME_WMDB_URL, wmdb.getUrl());
    	        				inst.addProperty(wmdb.getInstanceName(), Property.PROPERTY_NAME_WMDB_USERNAME, wmdb.getUsername());
    	        			}
    	        		}
    	        		
    	        		// JDBC Connection Info
    	    			LogUtils.logDebug("decorateInventory :: JDBC Info ... ");
    	        		LinkedList<JdbcConnection> jdbcConns = repoInstallation.getJdbcConnections();
    	        		if (jdbcConns != null) {
    	        			for (JdbcConnection jdbcConn : jdbcConns) {
    	        				inst.addProperty(jdbcConn.getInstanceName(), Property.PROPERTY_NAME_ID, jdbcConn.getId());
    	        				inst.addProperty(jdbcConn.getInstanceName(), Property.PROPERTY_NAME_JDBC_ENABLED, jdbcConn.getEnabled());
    	        				inst.addProperty(jdbcConn.getInstanceName(), Property.PROPERTY_NAME_JDBC_HOSTNAME, jdbcConn.getHostname());
    	        				inst.addProperty(jdbcConn.getInstanceName(), Property.PROPERTY_NAME_JDBC_PORT, jdbcConn.getPort());
    	        				inst.addProperty(jdbcConn.getInstanceName(), Property.PROPERTY_NAME_JDBC_DATABASE, jdbcConn.getDatabase());
    	        				inst.addProperty(jdbcConn.getInstanceName(), Property.PROPERTY_NAME_JDBC_USERNAME, jdbcConn.getUsername());
    	        			}
    	        		}
    	        		
    	        		// SAP Connection Info
    	    			LogUtils.logDebug("decorateInventory :: SAP Info ... ");
    	        		LinkedList<SapConnection> sapConns = repoInstallation.getSapConnections();
    	        		if (sapConns != null) {
    	        			for (SapConnection sapConn : sapConns) {
    	        				inst.addProperty(sapConn.getInstanceName(), Property.PROPERTY_NAME_ID, sapConn.getId());
    	        				inst.addProperty(sapConn.getInstanceName(), Property.PROPERTY_NAME_SAP_ENABLED, sapConn.getEnabled());
    	        				inst.addProperty(sapConn.getInstanceName(), Property.PROPERTY_NAME_SAP_APP_SERVER, sapConn.getApplicationServer());
    	        				inst.addProperty(sapConn.getInstanceName(), Property.PROPERTY_NAME_SAP_MSG_SERVER, sapConn.getMessageServer());
    	        				inst.addProperty(sapConn.getInstanceName(), Property.PROPERTY_NAME_SAP_SYSTEM_ID, sapConn.getSystemId());
    	        				inst.addProperty(sapConn.getInstanceName(), Property.PROPERTY_NAME_SAP_CLIENT, sapConn.getClient());
    	        				inst.addProperty(sapConn.getInstanceName(), Property.PROPERTY_NAME_JDBC_USERNAME, sapConn.getUsername());
    	        			}
    	        		}
    	        		
    	        		// Messaging info
    	    			LogUtils.logDebug("decorateInventory :: Messaging ... ");
    	        		LinkedList<Messaging> msgs = repoInstallation.getMessaing();
    	        		if (msgs != null) {
    	        			for (Messaging msg : msgs) {
    	        				inst.addProperty(msg.getInstanceName(), Property.PROPERTY_NAME_ID, msg.getId());
    	        				inst.addProperty(msg.getInstanceName(), Property.PROPERTY_NAME_MSG_TYPE, msg.getType());
    	        				inst.addProperty(msg.getInstanceName(), Property.PROPERTY_NAME_MSG_URL, msg.getUrl());
    	        				inst.addProperty(msg.getInstanceName(), Property.PROPERTY_NAME_MSG_USERNAME, msg.getUsername());
    	        			}
    	        		}
    	        		
    	    			LogUtils.logDebug("decorateInventory :: Decoration Completed ... ");
    	    		}
    	    	}
    	    	
    		}
    	}
    }
    
    @JsonIgnore
    public static void checkDirectory() {
    	if (!INVENTORY_CONFIG_DIRECTORY.exists()) {
    		if (!INVENTORY_CONFIG_DIRECTORY.mkdir()) {
    			LogUtils.logError("Unable to create directory " + INVENTORY_CONFIG_DIRECTORY.getPath());
    		}
    	}
    }
    
    @JsonIgnore
    public static void writeInventory(InventoryGroup invGroup) {
    	for (InventoryGroup group : invGroup.getGroups()) {
    		writeInventory(group);
    	}
    	if (invGroup.getInstallations().size() > 0) {
    		for (InventoryInstallation inst : invGroup.getInstallations()) {
            	LogUtils.logInfo("Inventory Installation: " + inst.getName());
    		}
    	}
    }
}





