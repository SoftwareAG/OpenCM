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
    	Inventory inv = (Inventory) Cache.getInstance().get(INVENTORY_CACHE_KEY);
    	if (inv != null) {
    		return inv;
    	}

    	if (opencmConfig.getInventory_config().getType().equals(InventoryConfiguration.INVENTORY_CONFIG_KEEPASS)) {
    		// Using Keepass as a db for endpoints
    		String keepassDb = opencmConfig.getInventory_config().getDb();
    		String opencmGroup = opencmConfig.getInventory_config().getTop_group();
        	String masterPwd = KeyUtils.getMasterPassword();
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," KeepassDB: " + keepassDb);
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Top Group: " + opencmGroup);
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
            		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Inventory Keepass: Processing Organisation : " + orgGroup.getName());
        			// --------------------------------
        			// Operations Departments
        			// --------------------------------
    				List<Group> opGroups = orgGroup.getGroups();
            		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"   :: Departments : " + opGroups.size());
					LinkedList<Department> opDepartments = new LinkedList<Department>();
    				for (int d = 0; d < opGroups.size(); d++) {
    					Group opGroup = opGroups.get(d);
						Department opDepartment = new Department();
						// --- Department Name
						opDepartment.setName(opGroup.getName());
                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Inventory Keepass: Processing OpDepartment : " + opGroup.getName());
            			// --------------------------------
            			// Servers
            			// --------------------------------
        				List<Group> serverGroups = opGroup.getGroups();
                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"   :: Servers : " + serverGroups.size());
						LinkedList<Server> servers = new LinkedList<Server>();
        				for (int s = 0; s < serverGroups.size(); s++) {
        					Group serverGroup = serverGroups.get(s);
                    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Inventory Keepass: Processing Server : " + serverGroup.getName());
							Server server = new Server();
							// --- Server Name
							server.setName(serverGroup.getName());
							// --- Set optional Server labels
							Entry serverMetadata = serverGroup.getEntryByTitle(Server.SERVER_METADATA_NAME);
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
                        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Inventory Keepass: Processing Installation : " + instGroup.getName());
    							Installation inst = new Installation();
    							// --- Node name
    							inst.setName(instGroup.getName());
    							// --- Set optional Installation labels - logical tags
    							Entry instMetadata = instGroup.getEntryByTitle(Installation.INSTALLATION_METADATA_NAME);
    							if (instMetadata != null) {
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
           	                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"       Keepass: Processing Runtime : " + rc.getTitle());
           	                		if (!rc.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM) &&  
       	                				!rc.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_CCE) &&
       	                				!rc.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_SYNCH) &&
    	                				!rc.getTitle().startsWith(RuntimeComponent.RUNTIME_COMPONENT_NAME_IS_PREFIX)) {
           	                			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"       Keepass: Ignoring Runtime : " + rc.getTitle());
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
    public LinkedList<Installation> getInstallationsByConfigModel(LinkedList<org.opencm.configuration.model.Organisation> model) {
    	LinkedList<Installation> insts = new LinkedList<Installation>();
 		for (int o = 0; o < model.size(); o++) {
 			String modelOrg = model.get(o).getOrg();
 			if ((model.get(o).getDepartments() == null) || (model.get(o).getDepartments().size() == 0)) {
 				insts.addAll(getInstallations(modelOrg, null, null));
 			} else {
 		 		for (int d = 0; d < model.get(o).getDepartments().size(); d++) {
 		 			String modelDep = model.get(o).getDepartments().get(d).getDep();
 		 			if ((model.get(o).getDepartments().get(d).getEnvironments() == null) || (model.get(o).getDepartments().get(d).getEnvironments().size() == 0)) {
 		 				insts.addAll(getInstallations(modelOrg, modelDep, null));
 		 			} else {
 		 		 		for (int e = 0; e < model.get(o).getDepartments().get(d).getEnvironments().size(); e++) {
 		 		 			String modelEnv = model.get(o).getDepartments().get(d).getEnvironments().get(e).getEnv();
 		 		 			if ((model.get(o).getDepartments().get(d).getEnvironments().get(e).getNodes() == null) || (model.get(o).getDepartments().get(d).getEnvironments().get(e).getNodes().size() == 0)) {
 		 		 				insts.addAll(getInstallations(modelOrg, modelDep, modelEnv));
 		 		 			} else {
 		 		 		 		for (int n = 0; n < model.get(o).getDepartments().get(d).getEnvironments().get(e).getNodes().size(); n++) {
 		 		 		 			insts.add(getInstallation(model.get(o).getDepartments().get(d).getEnvironments().get(e).getNodes().get(n)));
 		 		 		 		}
 		 		 			}
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
    
}
