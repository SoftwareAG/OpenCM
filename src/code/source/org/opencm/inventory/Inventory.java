package org.opencm.inventory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.opencm.configuration.Configuration;
import org.opencm.configuration.InventoryConfiguration;
import org.opencm.security.KeyUtils;
import org.opencm.util.Cache;
import org.opencm.util.LogUtils;

public class Inventory {

    public static final String  		INVENTORY_CACHE_KEY				= "OPENCM_INVENTORY";

	private LinkedList<Organisation> inventory;
    
    public Inventory() {
    }
    
    public static Inventory instantiate(Configuration opencmConfig) {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Inventory Instantiation starting .... ");
    	Inventory inv = (Inventory) Cache.getInstance().get(INVENTORY_CACHE_KEY);
    	if (inv != null) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Inventory already in cache: Returning .... ");
    		return inv;
    	}

		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Inventory not in cache - generating .... ");
		
    	if (opencmConfig.getInventory_config().getType().equals(InventoryConfiguration.INVENTORY_CONFIG_KEEPASS)) {
    		// Using Keepass as a db for endpoints
    		String keepassDb = opencmConfig.getInventory_config().getDb();
    		String opencmGroup = opencmConfig.getInventory_config().getTop_group();
        	String masterPwd = KeyUtils.getMasterPassword();
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," KeepassDB: " + keepassDb);
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Top Group: " + opencmGroup);
    		if (masterPwd == null) {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," Inventory: Master Password NULL ");
        		return null;
    		} else {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Master Pwd: ************ ");
    		}

    		inv = new Inventory();
    		try {
    			KeePassFile database = KeePassDatabase.getInstance(keepassDb).openDatabase(masterPwd);
    			Group topGroup = database.getGroupByName(opencmGroup);
    			// --------------------------------
    			// Organisations
    			// --------------------------------
    			List<Group> orgGroups = topGroup.getGroups();
				LinkedList<Organisation> organisations = new LinkedList<Organisation>();
    			for (int o = 0; o < orgGroups.size(); o++) {
    				Group orgGroup = orgGroups.get(o);
					Organisation organisation = new Organisation();
					// --- Department Name
					organisation.setName(orgGroup.getName());
            		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Inventory Keepass: Processing Organisation : " + orgGroup.getName());
        			// --------------------------------
        			// Operations Departments
        			// --------------------------------
    				List<Group> opGroups = orgGroup.getGroups();
            		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"   :: Departments : " + opGroups.size());
					LinkedList<Department> opDepartments = new LinkedList<Department>();
    				for (int d = 0; d < opGroups.size(); d++) {
    					Group opGroup = opGroups.get(d);
						Department opDepartment = new Department();
						// --- Department Name
						opDepartment.setName(opGroup.getName());
                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Inventory Keepass: Processing OpDepartment : " + opGroup.getName());
            			// --------------------------------
            			// Servers
            			// --------------------------------
        				List<Group> serverGroups = opGroup.getGroups();
                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"   :: Servers : " + serverGroups.size());
						LinkedList<Server> servers = new LinkedList<Server>();
        				for (int s = 0; s < serverGroups.size(); s++) {
        					Group serverGroup = serverGroups.get(s);
                    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Inventory Keepass: Processing Server : " + serverGroup.getName());
                    		
    						Entry serverMetadata = serverGroup.getEntryByTitle(Server.SERVER_METADATA_NAME);
    						if (serverMetadata == null) {
    							// Support arbitrary subgroup for servers (to better structure them). Collect list of subgroups and add to existing list
                        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"  No server metatdata - assuming subgroup... ");
    							List<Group> serverSubGroups = serverGroup.getGroups();
    							serverGroups.addAll(serverSubGroups);
    							continue;
    						}
                    		
							Server server = new Server();
							// --- Server Name
							server.setName(serverGroup.getName());
							// --- Set optional Server labels
							if (serverMetadata != null) {
								server.setDescription(serverMetadata.getPropertyByName(Server.KEEPASS_PROPERTY_DESCRIPTION).getValue());
								server.setOs(serverMetadata.getPropertyByName(Server.KEEPASS_PROPERTY_OS).getValue());
								server.setType(serverMetadata.getPropertyByName(Server.KEEPASS_PROPERTY_TYPE).getValue());
							}
                			// --------------------------------
                			// Installations
                			// --------------------------------
            				List<Group> instGroups = serverGroup.getGroups();
    						LinkedList<Installation> installations = new LinkedList<Installation>();
            				for (int i = 0; i < instGroups.size(); i++) {
            					Group instGroup = instGroups.get(i);
                        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Inventory Keepass: Processing Installation : " + instGroup.getName());
    							Installation inst = new Installation();
    							// --- Node name
    							inst.setName(instGroup.getName());
    							// --- Set optional Installation labels - logical tags
    							Entry instMetadata = instGroup.getEntryByTitle(Installation.INSTALLATION_METADATA_NAME);
    							if (instMetadata != null) {
           	                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"       Keepass: Processing Metadata .... " + instGroup.getName());
        							inst.setEnvironment(instMetadata.getPropertyByName(Installation.KEEPASS_PROPERTY_ENVIRONMENT).getValue());
        							inst.setLayer(instMetadata.getPropertyByName(Installation.KEEPASS_PROPERTY_LAYER).getValue());
        							inst.setSublayer(instMetadata.getPropertyByName(Installation.KEEPASS_PROPERTY_SUBLAYER).getValue());
        							inst.setVersion(instMetadata.getPropertyByName(Installation.KEEPASS_PROPERTY_VERSION).getValue());
    							}
    							// Get all runtimes for this installation
        						List<Entry> rcs = instGroup.getEntries();
        						LinkedList<RuntimeComponent> runtimeComponents = new LinkedList<RuntimeComponent>();
        						for (int r = 0; r < rcs.size(); r++) {
        							Entry rc = rcs.get(r);
           	                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"       Keepass: Processing Runtime : " + rc.getTitle());
           	                		if (!rc.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM) &&  
       	                				!rc.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_CCE) &&
       	                				!rc.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_SYNCH) &&
    	                				!rc.getTitle().startsWith(RuntimeComponent.RUNTIME_COMPONENT_NAME_IS_PREFIX)) {
           	                			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"       Keepass: Ignoring Runtime : " + rc.getTitle());
           	                			continue;
        	                		}
        	                		RuntimeComponent runtimeComponent = new RuntimeComponent();
        							runtimeComponent.setName(rc.getTitle());
        							runtimeComponent.setProtocol(rc.getPropertyByName(RuntimeComponent.KEEPASS_PROPERTY_PROTOCOL).getValue());
        							runtimeComponent.setPort(rc.getPropertyByName(RuntimeComponent.KEEPASS_PROPERTY_PORT).getValue());
        							runtimeComponent.setUsername(rc.getUsername());
        							runtimeComponent.setPassword(rc.getPassword());
        							runtimeComponents.add(runtimeComponent);
        						}
        						if (runtimeComponents.size() == 0) {
           	                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE,"       Keepass: No Runtime components... ");
        						}
        						inst.setRuntimes(runtimeComponents);
        						installations.add(inst);
            				}
            				server.setInstallations(installations);
    						servers.add(server);
        				}
        				opDepartment.setServers(servers);
						opDepartments.add(opDepartment);
    				}
					organisation.setDepartments(opDepartments);
					organisations.add(organisation);
    			}
    			inv.setInventory(organisations);
    					
    		} catch (Exception ex) {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Inventory - Keepass Exception: " + ex.getMessage());
    		} 
    		
    	} else {
    		// Using OpenCM inventory.properties as an inventory store
        	File invFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_INVENTORY);
        	if (!invFile.exists()) {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Inventory - File not found: " + invFile.getPath());
        		return null;
        	}
        	
        	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        	mapper.configure(Feature.ALLOW_COMMENTS, true);
        	
        	try {
        		inv = mapper.readValue(invFile, Inventory.class);
        	} catch (Exception e) {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," Inventory - Exception: " + e.toString());
        	}
    	}
    	
    	Cache.getInstance().set(INVENTORY_CACHE_KEY, inv);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Inventory Instantiation finishing .... ");
    	return inv;
    }
    
    public LinkedList<Organisation> getInventory() {
        return this.inventory;
    }
    public void setInventory(LinkedList<Organisation> inv) {
        this.inventory = inv;
    }
    
    @JsonIgnore
    public Organisation getOrganisation(String organisation) {
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			if (org.getName().equals(organisation)) {
 				return org;
 			}
		}
        return null;
    }

    @JsonIgnore
    public Department getDepartment(String organisation, String department) {
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			if (org.getName().equals(organisation)) {
 				LinkedList<Department> deps = org.getDepartments();
 		 		for (int d = 0; d < deps.size(); d++) {
 		 			Department dep = deps.get(d);
 		 			if (dep.getName().equals(department)) {
 		 				return dep;
 		 			}
 		 		}
 			}
		}
        return null;
    }

    @JsonIgnore
    public Organisation getNodeOrganisation(String nodeName) {
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> servers = opDep.getServers();
 	 	 		for (int s = 0; s < servers.size(); s++) {
 	 	 			Server srv = servers.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 					if (nodes.get(n).getName().equals(nodeName)) {
 	 						return org;
 	 					}
 	 	 	 		}
 	 	 			
 	 	 		}
 	 		}
		}
        return null;
    }

    @JsonIgnore
    public Department getNodeDepartment(String nodeName) {
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> servers = opDep.getServers();
 	 	 		for (int s = 0; s < servers.size(); s++) {
 	 	 			Server srv = servers.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 					if (nodes.get(n).getName().equals(nodeName)) {
 	 						return opDep;
 	 					}
 	 	 	 		}
 	 	 			
 	 	 		}
 	 		}
		}
        return null;
    }

    @JsonIgnore
    public Server getNodeServer(String nodeName) {
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> servers = opDep.getServers();
 	 	 		for (int s = 0; s < servers.size(); s++) {
 	 	 			Server srv = servers.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 					if (nodes.get(n).getName().equals(nodeName)) {
 	 						return srv;
 	 					}
 	 	 	 		}
 	 	 			
 	 	 		}
 	 		}
		}
        return null;
    }
    
    @JsonIgnore
    public Installation getInstallation(String nodeName) {
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> servers = opDep.getServers();
 	 	 		for (int s = 0; s < servers.size(); s++) {
 	 	 			Server srv = servers.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 					if (nodes.get(n).getName().equals(nodeName)) {
 	 						return nodes.get(n);
 	 					}
 	 	 	 		}
 	 	 			
 	 	 		}
 	 		}
		}
        return null;
    }
    
    @JsonIgnore
    private void addOrganisation(Organisation o) {
    	if (this.inventory == null) {
    		this.inventory = new LinkedList<Organisation>();
    	}
        this.inventory.add(o);
    }
    
    @JsonIgnore
    public LinkedList<String> getAllOpDepartments() {
    	LinkedList<String> opds = new LinkedList<String>();
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 				if (!opds.contains(opDep.getName())) {
 					opds.add(opDep.getName());
 				}
 	 		}
		}
        return opds;
    }
    
    @JsonIgnore
    public LinkedList<Server> getAllServers() {
    	LinkedList<Server> servers = new LinkedList<Server>();
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> srvs = opDep.getServers();
 	 	 		for (int s = 0; s < srvs.size(); s++) {
 	 	 			Server srv = srvs.get(s);
 	 				if (!servers.contains(srv)) {
 	 					servers.add(srv);
 	 				}
 	 	 		}
 	 		}
		}
        return servers;
    }
    
    @JsonIgnore
    private boolean serverExistsForEnvironment(String server, String env) {
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> srvs = opDep.getServers();
 	 	 		for (int s = 0; s < srvs.size(); s++) {
 	 	 			Server srv = srvs.get(s);
 	 	 			if (srv.getName().equals(server)) {
 	 	 	 			LinkedList<Installation> installations = srv.getInstallations();
 	 	 	 	 		for (int i = 0; i < installations.size(); i++) {
 	 	 	 	 			Installation inst = installations.get(i);
 		 	 	 			if (inst.getEnvironment().equals(env)) {
 		 	 	 				return true;
 		 	 	 			}
 	 	 	 	 		}
 	 	 			}
 	 	 		}
 	 		}
		}
        return false;
    }
    
    @JsonIgnore
    public LinkedList<Installation> getAllInstallations() {
    	LinkedList<Installation> insts = new LinkedList<Installation>();
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> srvs = opDep.getServers();
 	 	 		for (int s = 0; s < srvs.size(); s++) {
 	 	 			Server srv = srvs.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 	 	 			Installation inst = nodes.get(n);
 	 	 				if (!insts.contains(inst)) {
 	 	 					insts.add(inst);
 	 	 				}
 	 	 	 		}
 	 	 		}
 	 		}
		}
        return insts;
    }
    
    @JsonIgnore
    public LinkedList<Installation> getInstallationsByLayer(String layer) {
    	LinkedList<Installation> insts = new LinkedList<Installation>();
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> srvs = opDep.getServers();
 	 	 		for (int s = 0; s < srvs.size(); s++) {
 	 	 			Server srv = srvs.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 	 	 			Installation inst = nodes.get(n);
 	 	 				if (((inst.getLayer() != null) && inst.getLayer().equals(layer)) && !insts.contains(inst)) {
 	 	 					insts.add(inst);
 	 	 				}
 	 	 	 		}
 	 	 		}
 	 		}
		}
        return insts;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllEnvironments() {
    	LinkedList<String> envs = new LinkedList<String>();
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			LinkedList<Server> srvs = opDep.getServers();
 	 	 		for (int s = 0; s < srvs.size(); s++) {
 	 	 			Server srv = srvs.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 	 	 			Installation inst = nodes.get(s);
 	 	 				if (!envs.contains(inst.getEnvironment())) {
 	 	 					envs.add(inst.getEnvironment());
 	 	 				}
 	 	 	 		}
 	 	 		}
 	 		}
		}
        return envs;
    }
    
    @JsonIgnore
    public LinkedList<Installation> getInstallations(String organisation, String department, String environment) {
    	LinkedList<Installation> insts = new LinkedList<Installation>();
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			if ((organisation != null) && (!organisation.equals(org.getName()))) {
 				continue;
 			}
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			if ((department != null) && (!department.equals(opDep.getName()))) {
 	 				continue;
 	 			}
 	 			LinkedList<Server> srvs = opDep.getServers();
 	 	 		for (int s = 0; s < srvs.size(); s++) {
 	 	 			Server srv = srvs.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 	 	 			Installation inst = nodes.get(n);
 	 	 				if (((environment == null) || environment.equals(inst.getEnvironment())) && !insts.contains(inst)) {
 	 	 					insts.add(inst);
 	 	 				}
 	 	 	 		}
 	 	 		}
 	 		}
		}
 		
        return insts;
    }
    
    @JsonIgnore
    public LinkedList<Installation> getInstallations(String organisation, String department, String environment, String layer) {
    	LinkedList<Installation> insts = new LinkedList<Installation>();
 		for (int o = 0; o < this.inventory.size(); o++) {
 			Organisation org = this.inventory.get(o);
 			if ((organisation != null) && (!organisation.equals(org.getName()))) {
 				continue;
 			}
 			LinkedList<Department> opDeps = org.getDepartments();
 	 		for (int d = 0; d < opDeps.size(); d++) {
 	 			Department opDep = opDeps.get(d);
 	 			if ((department != null) && (!department.equals(opDep.getName()))) {
 	 				continue;
 	 			}
 	 			LinkedList<Server> srvs = opDep.getServers();
 	 	 		for (int s = 0; s < srvs.size(); s++) {
 	 	 			Server srv = srvs.get(s);
 	 	 			LinkedList<Installation> nodes = srv.getInstallations();
 	 	 	 		for (int n = 0; n < nodes.size(); n++) {
 	 	 	 			Installation inst = nodes.get(n);
 	 	 				if (((environment == null) || environment.equals(inst.getEnvironment())) && ((layer == null) || layer.equals(inst.getLayer())) && !insts.contains(inst)) {
 	 	 					insts.add(inst);
 	 	 				}
 	 	 	 		}
 	 	 		}
 	 		}
		}
 		
        return insts;
    }
    
    @JsonIgnore
    public void ensureEncryptedPasswords(Configuration opencmConfig) {
    	
    	if (!opencmConfig.getInventory_config().getType().equals(InventoryConfiguration.INVENTORY_CONFIG_OPENCM)) {
    		return;
    	}
    	
		boolean needsUpdate = false;

 		for (int o = 0; o < this.inventory.size(); o++) {
 	 		for (int d = 0; d < this.inventory.get(o).getDepartments().size(); d++) {
 	 	 		for (int s = 0; s < this.inventory.get(o).getDepartments().get(d).getServers().size(); s++) {
 	 	 	 		for (int n = 0; n < this.inventory.get(o).getDepartments().get(d).getServers().get(s).getInstallations().size(); n++) {
 	 	 	 	 		for (int r = 0; r < this.inventory.get(o).getDepartments().get(d).getServers().get(s).getInstallations().get(n).getRuntimes().size(); r++) {
 	 	 	 	 			if (!this.inventory.get(o).getDepartments().get(d).getServers().get(s).getInstallations().get(n).getRuntimes().get(r).passwordEncrypted()) {
 	 	 	 	 				this.inventory.get(o).getDepartments().get(d).getServers().get(s).getInstallations().get(n).getRuntimes().get(r).encryptPassword();
 	 	 	 	 				needsUpdate = true;
 	 	 	 	 			}
 	 	 	 	 		}
 	 	 	 		}
 	 	 		}
 	 		}
		}

		if (needsUpdate) {
			updateInventory(opencmConfig);
		}
    }
    
    @JsonIgnore
    public void updateInventory(Configuration opencmConfig) {
		try {
	    	File invFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_INVENTORY);
			
			// Pick up the Comments
			HashMap<Integer,String> hmComments = new HashMap<Integer,String>();
			try(BufferedReader br = new BufferedReader(new FileReader(invFile))) {
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
			SequenceWriter sw = writer.writeValues(invFile);
			sw.write(this);
			
			sw.close();
			
			// Re-write the Comments
			LinkedList<String> invYaml = new LinkedList<String>();
			// Read in the newly updated file
			try(BufferedReader br = new BufferedReader(new FileReader(invFile))) {
			    for(String line; (line = br.readLine()) != null; ) {
			    	if (!line.equals("---")) {
			    		invYaml.add(line);
			    	}
			    }
			}
			// Loop through the lines
			StringBuffer sb = new StringBuffer();
			int totalLines = invYaml.size() + hmComments.size();
			int nodesLinesIdx = 0;
			for (int i = 0; i < (totalLines); i++) {
		    	if (hmComments.containsKey(i)) {
		    		sb.append(hmComments.get(i) + System.lineSeparator());
		    	} else {
		    		sb.append(invYaml.get(nodesLinesIdx++) + System.lineSeparator());
		    	}
			}
			
			BufferedWriter bwr = new BufferedWriter(new FileWriter(invFile));
            bwr.write(sb.toString());
           
            //flush the stream
            bwr.flush();
           
            //close the stream
            bwr.close();

		} catch (IOException ex) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," Inventory - Exception: " + ex.toString());
		}

    }
    
    @JsonIgnore
    public boolean containsOrg(String org) {
 		for (int o = 0; o < this.inventory.size(); o++) {
 			if (this.inventory.get(o).getName().equals(org)) {
 				return true;
 			}
 			
 		}
    	
    	return false;
    }
    
    @JsonIgnore
    /*
     * Create a subset of the inventory based on a configured filter
     * 
     */
    public Inventory createInventory(Configuration opencmConfig, LinkedList<org.opencm.configuration.model.Organisation> configModel) {
    	Inventory configInv = new Inventory();

		// ---------------------------------------------
		// Nothing specified... return full inventory
		// ---------------------------------------------
    	if ((configModel == null) || (configModel.size() == 0)) {
    		return this;
    	}
    	
		// --------------------------------
		// Organisations
		// --------------------------------
		LinkedList<Organisation> newOrganisations = new LinkedList<Organisation>();
		LinkedList<Organisation> invOrganisations = getInventory();
		for (int o = 0; o < invOrganisations.size(); o++) {
			Organisation invOrganisation = invOrganisations.get(o);
			if (!filtered(invOrganisation, null, null, null, configModel)) {
				// This organisation is not part of the configured list
				continue;
			}
			Organisation newOrganisation = invOrganisation.getCopy();
			// --------------------------------
			// Departments
			// --------------------------------
			LinkedList<Department> newDepartments = new LinkedList<Department>();
			LinkedList<Department> invDepartments = invOrganisation.getDepartments();
			for (int d = 0; d < invDepartments.size(); d++) {
				Department invDepartment = invDepartments.get(d);
				if (!filtered(invOrganisation, invDepartment, null, null, configModel)) {
					// This department is not part of the configured list
					continue;
				}
				Department newDepartment = invDepartment.getCopy();
				// --------------------------------
				// Servers
				// --------------------------------
				LinkedList<Server> newServers = new LinkedList<Server>();
				LinkedList<Server> invServers = invDepartment.getServers();
				for (int s = 0; s < invServers.size(); s++) {
					Server invServer = invServers.get(s);
					if (!filtered(invOrganisation, invDepartment, invServer, null, configModel)) {
						// This server is not part of the configured list
						continue;
					}
					Server newServer = invServer.getCopy();
					// --------------------------------
					// Installations
					// --------------------------------
					LinkedList<Installation> newInstallations = new LinkedList<Installation>();
					LinkedList<Installation> invInstallations = invServer.getInstallations();
					for (int i = 0; i < invInstallations.size(); i++) {
						Installation invInst = invInstallations.get(i);
						if (!filtered(invOrganisation, invDepartment, invServer, invInst, configModel)) {
							// This installation is not part of the configured list
							continue;
						}
						newInstallations.add(invInst.getCopy());
					}
    				newServer.setInstallations(newInstallations);
					newServers.add(newServer);
				}
				newDepartment.setServers(newServers);
				newDepartments.add(newDepartment);
			}
			newOrganisation.setDepartments(newDepartments);
			newOrganisations.add(newOrganisation);
		}
		
		configInv.setInventory(newOrganisations);
    	
    	return configInv;
    }
    
    
    @JsonIgnore
    private boolean filtered(Organisation org, Department dep, Server server, Installation inst, LinkedList<org.opencm.configuration.model.Organisation> configModel) {
    	// --------------------------------
    	// Inspect Org
    	// --------------------------------
    	if ((configModel == null) || (configModel.size() == 0)) {
    		return true;
    	}
    	for (int o = 0; o < configModel.size(); o++) {
    		org.opencm.configuration.model.Organisation configOrg = configModel.get(o);
    		if (configOrg.getOrg().equals(org.getName())) {
    			if (dep == null) {
    				return true;
    			}
		    	// ----------------------------------------
		    	// Also Ok, if there are no dep configured
		    	// ----------------------------------------
		    	if ((configOrg.getDepartments() == null) || (configOrg.getDepartments().size() == 0)) {
		    		return true;
		    	}
		    	// --------------------------------
		    	// Inspect Department
		    	// --------------------------------
		    	for (int d = 0; d < configOrg.getDepartments().size(); d++) {
		    		org.opencm.configuration.model.Department configDep = configOrg.getDepartments().get(d);
		    		if (configDep.getDep().equals(dep.getName())) {
		    			if (server == null) {
		    				return true;
		    			} 
				    	// ----------------------------------------
				    	// Also Ok, if there are no env configured
				    	// ----------------------------------------
	    		    	if ((configDep.getEnvironments() == null) || (configDep.getEnvironments().size() == 0)) {
	    		    		return true;
	    		    	}
	    		    	
	    		    	// --------------------------------
	    		    	// Inspect Filters under Environment
	    		    	// --------------------------------
	    		    	for (int e = 0; e < configDep.getEnvironments().size(); e++) {
	    		    		org.opencm.configuration.model.Environment configEnv = configDep.getEnvironments().get(e);
	    		    		if (serverExistsForEnvironment(server.getName(),configEnv.getEnv())) {
    		    		    	// --------------------------------
    		    		    	// Inspect Installation
    		    		    	// --------------------------------
	    		    			if (inst == null) {
	    		    				return true;
	    		    			}
	    		    			
    		    		    	// --------------------------------
    		    		    	// If no layers filter, also ok
    		    		    	// --------------------------------
    		    		    	if ((configEnv.getLayers() == null) || (configEnv.getLayers().size() == 0)) {
    		    		    		return true;
    		    		    	}
    		    		    	
	    				    	// ----------------------------------------
	    				    	// Check Defined Filters for each layer
	    				    	// ----------------------------------------
    		    		    	for (int l = 0; l < configEnv.getLayers().size(); l++) {
			    		    		org.opencm.configuration.model.Layer configLayer = configEnv.getLayers().get(l);
    		    		    		if (configLayer.getLayer().equals(inst.getLayer())) {
    	    		    		    	// --------------------------------------
    	    		    		    	// Check additional filters under layer
    	    		    		    	// --------------------------------------
    	    		    		    	// --------------------------------
    	    		    		    	// Inspect Filtered Versions
    	    		    		    	// --------------------------------
    	    		    		    	if ((configLayer.getVersions() != null) && (configLayer.getVersions().size() > 0)) {
    	    		    		    		boolean lFilter = false;
    		    		    		    	for (int n = 0; n < configLayer.getVersions().size(); n++) {
    		    		    		    		if (configLayer.getVersions().get(n).equals(inst.getVersion())) {
    		    		    		    			lFilter = true;
    		    		    		    			break;
    		    		    		    		}
    		    		    		    	}
    		    		    		    	// Version filtered but not one of them
    		    		    		    	if (!lFilter) {
    		    		    		    		return false;
    		    		    		    	}
    	    		    		    	}
    	    		    		    	// --------------------------------
    	    		    		    	// Inspect Filtered Sublayers
    	    		    		    	// --------------------------------
    	    		    		    	if ((configLayer.getSublayers() != null) && (configLayer.getSublayers().size() > 0)) {
    	    		    		    		boolean lFilter = false;
    		    		    		    	for (int n = 0; n < configLayer.getSublayers().size(); n++) {
    		    		    		    		if (configLayer.getSublayers().get(n).equals(inst.getSublayer())) {
    		    		    		    			lFilter = true;
    		    		    		    			break;
    		    		    		    		}
    		    		    		    	}
    		    		    		    	// Sublayer filtered but not one of them
    		    		    		    	if (!lFilter) {
    		    		    		    		return false;
    		    		    		    	}
    	    		    		    	}
    	    		    		    	// --------------------------------
    	    		    		    	// Inspect Filtered Installations
    	    		    		    	// --------------------------------
    	    		    		    	if ((configLayer.getNodes() != null) && (configLayer.getNodes().size() > 0)) {
    	    		    		    		boolean lFilter = false;
    		    		    		    	for (int n = 0; n < configLayer.getNodes().size(); n++) {
    		    		    		    		if (configLayer.getNodes().get(n).equals(inst.getName())) {
    		    		    		    			lFilter = true;
    		    		    		    			break;
    		    		    		    		}
    		    		    		    	}
    		    		    		    	// Installation filtered but not one of them
    		    		    		    	if (!lFilter) {
    		    		    		    		return false;
    		    		    		    	}
    	    		    		    	}
    		    		    			
    		    		    			// All defined filters are applied and ok
           		    		    		return true;
    		    		    		}
    		    		    	}
    		    		    	
		    		    		// Installation not part of the layer
		    		    		return false;
		    		    	}
		    			}
		    		}
    			}
			}
    	}
    	
    	return false;
    
    }
    	
}
