package org.opencm.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

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
import org.opencm.security.KeyUtils;
import org.opencm.util.Cache;
import org.opencm.util.LogUtils;

public class Nodes {

    public static final String  		NODES_CACHE_KEY					= "OPENCM_NODES";

	private static final String 		KEEPASS_PROPERTY_HOSTNAME 		= 	"host";
	private static final String 		KEEPASS_PROPERTY_PROTOCOL 		= 	"protocol";
	private static final String 		KEEPASS_PROPERTY_PORT 			= 	"port";

	
	private static final String 		UNDEFINED_ENVIRONMENT 			= 	"UNDEFINED";
	private static final String 		UNDEFINED_ASSERTION_GROUP 		= 	"UNDEFINED";

	private LinkedList<Node> nodes;
    
    public Nodes() {
    }
    
    public static Nodes instantiate(Configuration opencmConfig) {
    	Nodes opencmNodes = (Nodes) Cache.getInstance().get(NODES_CACHE_KEY);
    	if (opencmNodes != null) {
    		return opencmNodes;
    	}

    	if (opencmConfig.getInventory_config().getType().equals(InventoryConfiguration.INVENTORY_CONFIG_KEEPASS)) {
    		// Using Keepass as a db for endpoints
    		String keepassDb = opencmConfig.getInventory_config().getDb();
    		String opencmGroup = opencmConfig.getInventory_config().getTop_group();
        	String masterPwd = KeyUtils.getMasterPassword();
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," KeepassDB: " + keepassDb);
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Top Group: " + opencmGroup);
    		if (masterPwd == null) {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," Nodes: Master Password NULL ");
        		return null;
    		} else {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Master Pwd: ************ ");
    		}

    		opencmNodes = new Nodes();
    		try {
    			KeePassFile database = KeePassDatabase.getInstance(keepassDb).openDatabase(masterPwd);
    			Group topGroup = database.getGroupByName(opencmGroup);
    			List<Group> envGroups = topGroup.getGroups();
    			for (int e = 0; e < envGroups.size(); e++) {
    				Group envGroup = envGroups.get(e);
            		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Keepass: Processing Environment : " + envGroup.getName());
    				List<Group> layerGroups = envGroup.getGroups();
    				for (int l = 0; l < layerGroups.size(); l++) {
    					Group layerGroup = layerGroups.get(l);
                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"   Keepass: Processing Layer : " + layerGroup.getName());
    					List<Group> nodeGroups = layerGroup.getGroups();
    					for (int n = 0; n < nodeGroups.size(); n++) {
    						Group nodeGroup = nodeGroups.get(n);
                    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"     Keepass: Processing Node : " + nodeGroup.getName());
							Node node = new Node();
							node.setNode_name(nodeGroup.getName());
							node.setEnvironment(envGroup.getName());
							node.setAssertion_group(layerGroup.getName());
							// Get all components for this node
    						List<Entry> components = nodeGroup.getEntries();
    						LinkedList<RuntimeComponent> runtimeComponents = new LinkedList<RuntimeComponent>();
    						for (int c = 0; c < components.size(); c++) {
    							Entry component = components.get(c);
       	                		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"       Keepass: Processing Component : " + component.getTitle());
       	                		if (!component.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM) &&  
   	                				!component.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_CCE) &&
   	                				!component.getTitle().equals(RuntimeComponent.RUNTIME_COMPONENT_NAME_SYNCH) &&
	                				!component.getTitle().startsWith(RuntimeComponent.RUNTIME_COMPONENT_NAME_IS_PREFIX)) {
       	                			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG,"       Keepass: Ignoring Component : " + component.getTitle());
       	                			continue;
    	                		}
    	                		RuntimeComponent runtimeComponent = new RuntimeComponent();
    							if (node.getHostname() == null) {
        							node.setHostname(component.getPropertyByName(KEEPASS_PROPERTY_HOSTNAME).getValue());
    							}
    							runtimeComponent.setName(component.getTitle());
    							runtimeComponent.setProtocol(component.getPropertyByName(KEEPASS_PROPERTY_PROTOCOL).getValue());
    							runtimeComponent.setPort(component.getPropertyByName(KEEPASS_PROPERTY_PORT).getValue());
    							runtimeComponent.setUsername(component.getUsername());
    							runtimeComponent.setPassword(component.getPassword());
    							runtimeComponents.add(runtimeComponent);
    						}
    						node.setRuntimeComponents(runtimeComponents);
    						opencmNodes.addNode(node);
    					}
    				}
    			}
    					
    		} catch (Exception ex) {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Nodes - Keepass Exception: " + ex.getMessage());
    		} 
    		
    	} else {
    		// Using OpenCM nodes.properties as an inventory store
        	File opencmNodesFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_INVENTORY);
        	if (!opencmNodesFile.exists()) {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"Nodes - File not found: " + opencmNodesFile.getPath());
        		return null;
        	}
        	
        	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        	mapper.configure(Feature.ALLOW_COMMENTS, true);
        	
        	try {
        		opencmNodes = mapper.readValue(opencmNodesFile, Nodes.class);
        	} catch (Exception e) {
        		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," Nodes - Exception: " + e.toString());
        	}
    	}
    	
    	Cache.getInstance().set(NODES_CACHE_KEY, opencmNodes);
    	return opencmNodes;
    }
    
    public LinkedList<Node> getNodes() {
        return this.nodes;
    }
    public void setNodes(LinkedList<Node> nodes) {
        this.nodes = nodes;
    }
    
    public Node getNode(String nodeName) {
 		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getNode_name().equals(nodeName)) {
				return this.nodes.get(i);
			}
		}
        return null;
    }
    
    @JsonIgnore
    private void addNode(Node n) {
    	if (this.nodes == null) {
    		this.nodes = new LinkedList<Node>();
    	}
        this.nodes.add(n);
    }
    
    @JsonIgnore
    private int getSize() {
    	if (this.nodes == null) {
    		return 0;
    	}
        return this.nodes.size();
    }

    @JsonIgnore
    public LinkedList<Node> getNodesByEnvironment(String env) {
    	LinkedList<Node> envNodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getEnvironment().equals(env) && !env.equals(UNDEFINED_ENVIRONMENT) && !envNodes.contains(this.nodes.get(i))) {
				envNodes.add(this.nodes.get(i));
			}
		}
        return envNodes;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllEnvironments() {
    	LinkedList<String> envs = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			String env = this.nodes.get(i).getEnvironment();
			if (!envs.contains(env) && !env.equals(UNDEFINED_ENVIRONMENT)) {
				envs.add(env);
			}
		}
        return envs;
    }
    
    
    @JsonIgnore
    public LinkedList<String> getAllServerNamesUnqualified(String opencmEnvName) {
    	LinkedList<String> servers = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			String env = this.nodes.get(i).getEnvironment();
			if (env.equals(opencmEnvName) && !servers.contains(this.nodes.get(i).getUnqualifiedHostname())) {
				servers.add(this.nodes.get(i).getUnqualifiedHostname());
			}
    	}
        return servers;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllServerNamesUnqualifiedByEnvAndGroup(String opencmEnvName, String assGroup) {
    	LinkedList<String> servers = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			String env = this.nodes.get(i).getEnvironment();
			String grp = this.nodes.get(i).getAssertion_group();
			if (env.equals(opencmEnvName) && grp.equals(assGroup) && !servers.contains(this.nodes.get(i).getUnqualifiedHostname())) {
				servers.add(this.nodes.get(i).getUnqualifiedHostname());
			}
    	}
        return servers;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllFQNServerNamesByEnvAndLayer(String opencmEnvName, String layer) {
    	LinkedList<String> servers = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			String env = this.nodes.get(i).getEnvironment();
			String grp = this.nodes.get(i).getAssertion_group();
			if (env.equals(opencmEnvName) && grp.equals(layer) && !servers.contains(this.nodes.get(i).getHostname())) {
				servers.add(this.nodes.get(i).getHostname());
			}
    	}
        return servers;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllAssertionGroups(String nodeName) {
    	Node node = getNode(nodeName);
    	LinkedList<String> groups = new LinkedList<String>();
    	if (node != null) {
			String group = node.getAssertion_group();
			int multipleAGIdx = group.indexOf(",");
			if (multipleAGIdx > 0) {
				StringTokenizer st = new StringTokenizer(group,",");
				while (st.hasMoreTokens()) {
					String stGroupName = st.nextToken().trim();
					if (!groups.contains(stGroupName)) {
						groups.add(group);
					}
				}
			} else {
				groups.add(group);
			}
    	}
        return groups;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllAssertionGroups() {
    	LinkedList<String> groups = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			String groupName = this.nodes.get(i).getAssertion_group();
			if ((groupName != null) && !groupName.equals("") && !groupName.equals(UNDEFINED_ASSERTION_GROUP)) {
				int multipleAGIdx = groupName.indexOf(",");
				if (multipleAGIdx > 0) {
					StringTokenizer st = new StringTokenizer(groupName,",");
					while (st.hasMoreTokens()) {
						String stGroupName = st.nextToken().trim();
						if (!groups.contains(stGroupName)) {
							groups.add(stGroupName);
						}
					}
				} else if (!groups.contains(groupName)) {
					groups.add(groupName);
				}
			}
		}
    	
        return groups;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllAssertionGroupsForEnvironment(String envName) {
    	LinkedList<String> groups = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (!this.nodes.get(i).getEnvironment().equals(envName)) {
				continue;
			}
			String groupName = this.nodes.get(i).getAssertion_group();
			if ((groupName != null) && !groupName.equals("") && !groupName.equals(UNDEFINED_ASSERTION_GROUP)) {
				int multipleAGIdx = groupName.indexOf(",");
				if (multipleAGIdx > 0) {
					StringTokenizer st = new StringTokenizer(groupName,",");
					while (st.hasMoreTokens()) {
						String stGroupName = st.nextToken().trim();
						if (!groups.contains(stGroupName)) {
							groups.add(stGroupName);
						}
					}
				} else if (!groups.contains(groupName)) {
					groups.add(groupName);
				}
			}
		}
        return groups;
    }
    
    @JsonIgnore
    public LinkedList<Node> getNodesByGroup(String assertionGroup) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getAssertion_group().equals(assertionGroup)) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    @JsonIgnore
    public LinkedList<Node> getNodesByServer(String unqualifiedServerName) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getUnqualifiedHostname().equals(unqualifiedServerName) && !nodes.contains(this.nodes.get(i))) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    @JsonIgnore
    public LinkedList<Node> getNodesByEnvAndServer(String env, String unqualifiedServerName) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getEnvironment().equals(env) && this.nodes.get(i).getUnqualifiedHostname().equals(unqualifiedServerName) && !nodes.contains(this.nodes.get(i))) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    @JsonIgnore
    public LinkedList<Node> getNodesByEnvGroupAndServer(String env, String group, String unqualifiedServerName) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getEnvironment().equals(env) && this.nodes.get(i).getAssertion_group().equals(group) && this.nodes.get(i).getUnqualifiedHostname().equals(unqualifiedServerName) && !nodes.contains(this.nodes.get(i))) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    @JsonIgnore
    public LinkedList<Node> getNodesByEnvLayerAndFQNServer(String env, String layer, String fqHostname) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getEnvironment().equals(env) && this.nodes.get(i).getAssertion_group().equals(layer) && this.nodes.get(i).getHostname().equals(fqHostname) && !nodes.contains(this.nodes.get(i))) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    /**
    @JsonIgnore
    public LinkedList<Node> getNodesByGroupAndCceEnv(String assertionGroup, String env) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getAssertion_group().equals(assertionGroup) && this.nodes.get(i).getCce_environment().equals(env)) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    */
    
    @JsonIgnore
    public LinkedList<Node> getNodesByGroupAndEnv(String assertionGroup, String env) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getAssertion_group().equals(assertionGroup) && this.nodes.get(i).getEnvironment().equals(env)) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    @JsonIgnore
    public String getEnvironment(String node) {
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getNode_name().equals(node)) {
				return this.nodes.get(i).getEnvironment();
			}
		}
        return null;
    }
    
    @JsonIgnore
    public RuntimeComponent getNodeInstance(String nodeName, String rcName) {
    	Node node = getNode(nodeName);
    	if (node != null) {
			for (int i = 0; i < node.getRuntimeComponents().size(); i++) {
				RuntimeComponent runtimeComponent = node.getRuntimeComponents().get(i);
				if (runtimeComponent.getName().equals(rcName)) {
					return runtimeComponent;
				}
			}
    	}
    	return null;
    }
    
    @JsonIgnore
    public void ensureDecryptedPasswords(Configuration opencmConfig) {
    	
    	if (!opencmConfig.getInventory_config().getType().equals(InventoryConfiguration.INVENTORY_CONFIG_OPENCM)) {
    		return;
    	}
    	
		boolean needsUpdate = false;

		for (int n = 0; n < this.nodes.size(); n++) {
	    	Node node = getNode(this.nodes.get(n).getNode_name());
			for (int i = 0; i < node.getRuntimeComponents().size(); i++) {
				RuntimeComponent instance = node.getRuntimeComponents().get(i);
				if (!instance.passwordEncrypted()) {
					instance.encryptPassword();
					needsUpdate = true;
				}
			}
		}

		if (needsUpdate) {
			writeInventory(opencmConfig);
		}
    }
    
    @JsonIgnore
    public String getEncryptedPassword(String nodeName, String rcName) {
    	Node node = getNode(nodeName);
		for (int i = 0; i < node.getRuntimeComponents().size(); i++) {
			RuntimeComponent runtimeComponent = node.getRuntimeComponents().get(i);
			if (runtimeComponent.getName().equals(rcName)) {
				return KeyUtils.decrypt(runtimeComponent.getPassword());
			}
		}
        return null;
    }
    
    @JsonIgnore
    public void writeInventory(Configuration opencmConfig) {
		try {
	    	File opencmInvFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_INVENTORY);
			
			// Pick up the Comments
			HashMap<Integer,String> hmComments = new HashMap<Integer,String>();
			try(BufferedReader br = new BufferedReader(new FileReader(opencmInvFile))) {
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
			SequenceWriter sw = writer.writeValues(opencmInvFile);
			sw.write(this);
			
			sw.close();
			
			// Re-write the Comments
			LinkedList<String> nodesList = new LinkedList<String>();
			// Read in the newly updated file
			try(BufferedReader br = new BufferedReader(new FileReader(opencmInvFile))) {
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
			
			BufferedWriter bwr = new BufferedWriter(new FileWriter(opencmInvFile));
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
