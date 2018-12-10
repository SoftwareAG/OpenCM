package org.opencm.inventory;

import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class Department {

    @JsonPropertyOrder({ "name", "servers" })
    
    private String name;
    private LinkedList<Server> servers;


    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Server> getServers() {
        return this.servers;
    }
    public void setServers(LinkedList<Server> servers) {
        this.servers = servers;
    }

    @JsonIgnore
    public Server getServer(String server) {
 		for (int s = 0; s < getServers().size(); s++) {
 			Server srv = getServers().get(s);
 			if (srv.getName().equals(server)) {
 				return srv;
 			}
		}
        return null;
    }
    
    @JsonIgnore
    public LinkedList<String> getEnvironments() {
    	LinkedList<String> envs = new LinkedList<String>();
    	if ((getName() == null) || (getServers() == null)) {
    		return envs;
    	}
 		for (int s = 0; s < getServers().size(); s++) {
 			Server srv = getServers().get(s);
 			LinkedList<Installation> nodes = srv.getInstallations();
 	 		for (int n = 0; n < nodes.size(); n++) {
 	 			Installation inst = nodes.get(n);
 				if ((inst.getEnvironment() != null) && !envs.contains(inst.getEnvironment())) {
 					envs.add(inst.getEnvironment());
 				}
 	 		}
		}
        return envs;
    }
    
    @JsonIgnore
    public LinkedList<String> getLayersByEnv(String env) {
    	LinkedList<String> layers = new LinkedList<String>();
    	if ((getName() == null) || (getServers() == null)) {
    		return layers;
    	}
 		for (int s = 0; s < getServers().size(); s++) {
 			Server srv = getServers().get(s);
 			LinkedList<Installation> nodes = srv.getInstallations();
 	 		for (int n = 0; n < nodes.size(); n++) {
 	 			Installation inst = nodes.get(n);
 				if ((inst.getEnvironment() != null) && (inst.getEnvironment().equals(env)) && (inst.getLayer() != null) && (!layers.contains(inst.getLayer()))) {
 					layers.add(inst.getLayer());
 				}
 	 		}
		}
        return layers;
    }
    
    @JsonIgnore
    public LinkedList<Server> getServers(String env, String layer) {
    	LinkedList<Server> servers = new LinkedList<Server>();
    	if ((getName() == null) || (getServers() == null)) {
    		return servers;
    	}
 		for (int s = 0; s < getServers().size(); s++) {
 			Server srv = getServers().get(s);
 			LinkedList<Installation> nodes = srv.getInstallations();
 	 		for (int n = 0; n < nodes.size(); n++) {
 	 			Installation inst = nodes.get(n);
 				if ((inst.getEnvironment() != null) && (inst.getEnvironment().equals(env)) && (inst.getLayer() != null) && (inst.getLayer().equals(layer))) {
 					servers.add(srv);
 					break;
 				}
 	 		}
		}
        return servers;
    }
}
