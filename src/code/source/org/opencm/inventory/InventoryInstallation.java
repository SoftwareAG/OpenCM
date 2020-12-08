package org.opencm.inventory;

import java.util.ArrayList;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.opencm.repository.Instance;
import org.opencm.repository.Property;

@JsonSerialize
public class InventoryInstallation {

	private String name;
	private String description;
    private String hostname;
    private ArrayList<String> path;
    private LinkedList<InventoryRuntime> runtimes = new LinkedList<InventoryRuntime>(); 
    private LinkedList<InventoryLB> lbs = new LinkedList<InventoryLB>();
    
    // Additional properties added to an installation
    private LinkedList<Instance> instances = new LinkedList<Instance>(); 
    private boolean repoExists = false;
    
    public InventoryInstallation() {
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }
    public void setDescription(String desc) {
        this.description = desc;
    }
    
    public String getHostname() {
        return this.hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public ArrayList<String> getPath() {
    	return this.path;
    }
    public void setPath(ArrayList<String> path) {
        this.path = path;
    }
    
    public LinkedList<InventoryRuntime> getRuntimes() {
        return this.runtimes;
    }
    public InventoryRuntime getRuntime(String name) {
    	for (InventoryRuntime rt : getRuntimes()) {
    		if (rt.getName().equals(name)) {
    			return rt;
    		}
    	}
        return null;
    }
    public void addRuntime(InventoryRuntime rt) {
        this.runtimes.add(rt);
    }
    public void setRuntimes(LinkedList<InventoryRuntime> rts) {
        this.runtimes = rts;
    }
    
    @JsonGetter("load_balancers")
    public LinkedList<InventoryLB> getLoadBalancers() {
        return this.lbs;
    }
    public void setLoadBalancers(LinkedList<InventoryLB> lbs) {
        this.lbs = lbs;
    }
    public void addLoadBalancer(InventoryLB lb) {
        this.lbs.add(lb);
    }
    
    // -------------------------------------------
    // Decorations
    // -------------------------------------------
    public LinkedList<Instance> getInstances() {
        return this.instances;
    }
    public void addProperty(String instanceName, String propertyKey, String propertyValue) {
    	for (Instance inst : getInstances()) {
    		if (inst.getName().equals(instanceName)) {
    			inst.addProperty(new Property(propertyKey,propertyValue));
    			return;
    		}
    	}
    	// No instances found: create new
    	Instance newInstance = new Instance(instanceName); 
        newInstance.addProperty(new Property(propertyKey,propertyValue));
        addInstance(newInstance);
    }
    
    private void addInstance(Instance instance) {
        this.instances.add(instance);
    }
        
    @JsonGetter("repoExists")
    public boolean repoExists() {
        return this.repoExists;
    }
    public void setRepoExists(boolean exists) {
        this.repoExists = exists;
    }

}
