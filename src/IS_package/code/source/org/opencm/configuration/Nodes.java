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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.opencm.security.KeyUtils;
import org.opencm.util.LogUtils;

public class Nodes {
	
	private static final String 		UNDEFINED_OPENCM_ENVIRONMENT 	= 	"UNDEFINED";
	private static final String 		UNDEFINED_CCE_ENVIRONMENT 		= 	"UNDEFINED";
	private static final String 		UNDEFINED_ASSERTION_GROUP 		= 	"UNDEFINED";

	private LinkedList<Node> nodes;
    
    public Nodes() {
    }
    
    public static Nodes instantiate(Configuration opencmConfig) {
    	Nodes opencmNodes = null;
    	File opencmNodesFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_NODES_PROPS);
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
    public LinkedList<Node> getNodesByOpencmEnvironment(String env) {
    	LinkedList<Node> envNodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getOpencm_environment().equals(env) && !env.equals(UNDEFINED_OPENCM_ENVIRONMENT) && !envNodes.contains(this.nodes.get(i))) {
				envNodes.add(this.nodes.get(i));
			}
		}
        return envNodes;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllOpencmEnvironments() {
    	LinkedList<String> envs = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			String env = this.nodes.get(i).getOpencm_environment();
			if (!envs.contains(env) && !env.equals(UNDEFINED_OPENCM_ENVIRONMENT)) {
				envs.add(env);
			}
		}
        return envs;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllCceEnvironments() {
    	LinkedList<String> envs = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			String env = this.nodes.get(i).getCce_environment();
			if (!envs.contains(env) && !env.equals(UNDEFINED_CCE_ENVIRONMENT)) {
				envs.add(env);
			}
		}
        return envs;
    }
    
    @JsonIgnore
    public LinkedList<String> getAllServerNamesUnqualified(String opencmEnvName) {
    	LinkedList<String> servers = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			String env = this.nodes.get(i).getOpencm_environment();
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
			String env = this.nodes.get(i).getOpencm_environment();
			String grp = this.nodes.get(i).getAssertion_group();
			if (env.equals(opencmEnvName) && grp.equals(assGroup) && !servers.contains(this.nodes.get(i).getUnqualifiedHostname())) {
				servers.add(this.nodes.get(i).getUnqualifiedHostname());
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
    public LinkedList<String> getAllAssertionGroupsForCceEnvironment(String envName) {
    	LinkedList<String> groups = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (!this.nodes.get(i).getCce_environment().equals(envName)) {
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
    public LinkedList<String> getAllAssertionGroupsForOpenCMEnvironment(String envName) {
    	LinkedList<String> groups = new LinkedList<String>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (!this.nodes.get(i).getOpencm_environment().equals(envName)) {
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
    public LinkedList<Node> getNodesByOpencmEnvAndServer(String env, String unqualifiedServerName) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getOpencm_environment().equals(env) && this.nodes.get(i).getUnqualifiedHostname().equals(unqualifiedServerName) && !nodes.contains(this.nodes.get(i))) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    @JsonIgnore
    public LinkedList<Node> getNodesByOpencmEnvGroupAndServer(String env, String group, String unqualifiedServerName) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getOpencm_environment().equals(env) && this.nodes.get(i).getAssertion_group().equals(group) && this.nodes.get(i).getUnqualifiedHostname().equals(unqualifiedServerName) && !nodes.contains(this.nodes.get(i))) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
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
    
    @JsonIgnore
    public LinkedList<Node> getNodesByGroupAndOpencmEnv(String assertionGroup, String env) {
    	LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getAssertion_group().equals(assertionGroup) && this.nodes.get(i).getOpencm_environment().equals(env)) {
				nodes.add(this.nodes.get(i));
			}
		}
        return nodes;
    }
    
    @JsonIgnore
    public String getOpencmEnvironment(String node) {
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getNode_name().equals(node)) {
				return this.nodes.get(i).getOpencm_environment();
			}
		}
        return null;
    }
    
    @JsonIgnore
    public String getCceEnvironment(String node) {
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i).getNode_name().equals(node)) {
				return this.nodes.get(i).getCce_environment();
			}
		}
        return null;
    }

    @JsonIgnore
    public Instance getNodeInstance(String nodeName, String instanceName) {
    	Node node = getNode(nodeName);
    	if (node != null) {
			for (int i = 0; i < node.getInstances().size(); i++) {
				Instance instance = node.getInstances().get(i);
				if (instance.getName().equals(instanceName)) {
					return instance;
				}
			}
    		
    	}
    	return null;
    }
    
    @JsonIgnore
    public void ensureDecryptedPasswords(Configuration opencmConfig, String masterPwd) {
		boolean needsUpdate = false;

		for (int n = 0; n < this.nodes.size(); n++) {
	    	Node node = getNode(this.nodes.get(n).getNode_name());
			for (int i = 0; i < node.getInstances().size(); i++) {
				Instance instance = node.getInstances().get(i);
				if (!instance.passwordEncrypted()) {
					instance.encryptPassword(masterPwd);
					needsUpdate = true;
				}
			}
		}

		if (needsUpdate) {
			writeNodes(opencmConfig);
		}
    }
    
    @JsonIgnore
    public String getEncryptedPassword(String masterPwd, String nodeName, String instanceName) {
    	Node node = getNode(nodeName);
		for (int i = 0; i < node.getInstances().size(); i++) {
			Instance instance = node.getInstances().get(i);
			if (instance.getName().equals(instanceName)) {
				KeyUtils keyUtils = new KeyUtils(masterPwd);
				return keyUtils.decrypt(instance.getPassword());
			}
		}
        return null;
    }
    
    @JsonIgnore
    public void writeNodes(Configuration opencmConfig) {
		try {
	    	File opencmNodesFile = new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_NODES_PROPS);
			
			// Pick up the Comments
			HashMap<Integer,String> hmComments = new HashMap<Integer,String>();
			try(BufferedReader br = new BufferedReader(new FileReader(opencmNodesFile))) {
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
			SequenceWriter sw = writer.writeValues(opencmNodesFile);
			sw.write(this);
			
			sw.close();
			
			// Re-write the Comments
			LinkedList<String> nodesList = new LinkedList<String>();
			// Read in the newly updated file
			try(BufferedReader br = new BufferedReader(new FileReader(opencmNodesFile))) {
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
			
			BufferedWriter bwr = new BufferedWriter(new FileWriter(opencmNodesFile));
            bwr.write(sb.toString());
           
            //flush the stream
            bwr.flush();
           
            //close the stream
            bwr.close();

			// Re-read endpoints from file
			// return mapper.readValue(nodesProperties, Nodes.class);
		} catch (IOException ex) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," writeNodes - Exception: " + ex.toString());
		}

    }
    
    
}
