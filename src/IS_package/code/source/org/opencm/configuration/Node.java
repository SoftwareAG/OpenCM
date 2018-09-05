package org.opencm.configuration;

import java.util.LinkedList;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node {
	
	public static String NODE_VERSION_900 			= "V900";
	public static String NODE_VERSION_960 			= "V960";
	public static String NODE_VERSION_980 			= "V980";
	public static String NODE_VERSION_990 			= "V990";
	public static String NODE_VERSION_912 			= "V912";
	public static String NODE_VERSION_100 			= "V912";
	public static String NODE_VERSION_101 			= "V101";
	public static String NODE_VERSION_102 			= "V102";
	public static String NODE_VERSION_103 			= "V103";
	public static String NODE_VERSION_104 			= "V104";
	public static String NODE_VERSION_105 			= "V105";
	public static String NODE_VERSION_106 			= "V106";

    private String node_name;
    private String environment;
    private String hostname;
    private String assertion_group;
    private LinkedList<RuntimeComponent> runtimeComponents;
    // Added during processing
    private String repoType;
    
    public Node() {
    }

    public String getNode_name() {
        return this.node_name;
    }
    public void setNode_name(String name) {
        this.node_name = name;
    }
    public String getEnvironment() {
        return this.environment;
    }
    public void setEnvironment(String env) {
        this.environment = env;
    }
    public String getHostname() {
        return this.hostname;
    }
    
    @JsonIgnore
    public String getUnqualifiedHostname() {
		if (getHostname().indexOf(".") > 0) {
			return getHostname().substring(0,getHostname().indexOf("."));
		}
        return getHostname();
    }
    
    public void setHostname(String host) {
        this.hostname = host;
    }
    public String getAssertion_group() {
        return this.assertion_group;
    }
    public void setAssertion_group(String group) {
        this.assertion_group = group;
    }
    public LinkedList<RuntimeComponent> getRuntimeComponents() {
        return this.runtimeComponents;
    }
    public void setRuntimeComponents(LinkedList<RuntimeComponent> runtimeComponents) {
        this.runtimeComponents = runtimeComponents;
    }
    
    @JsonIgnore
    public RuntimeComponent getRuntimeComponent(String rcName) {
    	for (int i = 0; i < getRuntimeComponents().size(); i++) {
    		RuntimeComponent nodeComp = getRuntimeComponents().get(i);
    		if (nodeComp.getName().equals(rcName)) {
    			return nodeComp;
    		}
    	}
        return null;
    }
    
    @JsonIgnore
    public String getRepositoryType() {
        return this.repoType;
    }
    public void setRepositoryType(String type) {
        this.repoType = type;
    }

    @JsonIgnore
    public Node getCopy() {
    	Node node = new Node();
		node.setNode_name(getNode_name());
		node.setAssertion_group(getAssertion_group());
		node.setEnvironment(getEnvironment());
		node.setHostname(getHostname());
		node.setRuntimeComponents(getRuntimeComponents());
		return node;
    }
    
    @JsonIgnore
    public String getVersion() {
    	StringTokenizer st = new StringTokenizer(getNode_name(),"_");
    	while (st.hasMoreTokens()) {
    		String stToken = st.nextToken();
    		if ((stToken.length() == 4) && stToken.startsWith("V") && StringUtils.isNumeric(stToken.substring(1))) {
    			return stToken;
    		}
    	}
    	return null;
    }

}
