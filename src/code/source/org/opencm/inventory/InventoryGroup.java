package org.opencm.inventory;

import java.util.ArrayList;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class InventoryGroup {

	private String name;
    private ArrayList<String> path;
    private LinkedList<InventoryGroup> groups = new LinkedList<InventoryGroup>();
    private LinkedList<InventoryInstallation> installations = new LinkedList<InventoryInstallation>(); 
    
    private InventoryGroup parent;
    
    public InventoryGroup() {
    }
    
    public InventoryGroup(String name, InventoryGroup parent) {
    	setName(name);
    	setParent(parent);
    	setPath();
    }
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getPath() {
    	return this.path;
    }
    public void setPath() {
    	ArrayList<String> path = new ArrayList<String>(); 
    	if (getParent() != null) {
    		path = getParent().getPath();
    	}
		path.add(getName());
    	setPath(new ArrayList<String>(path));
    }
    public void setPath(ArrayList<String> path) {
        this.path = path;
    }
    
    @JsonIgnore
    public InventoryGroup getParent() {
        return this.parent;
    }
    public void setParent(InventoryGroup group) {
        this.parent = group;
    }
    
    public LinkedList<InventoryGroup> getGroups() {
        return this.groups;
    }
    public void addGroup(InventoryGroup group) {
        this.groups.add(group);
    }
    public InventoryGroup findGroup(ArrayList<String> path) {
    	for (InventoryGroup group : this.groups) {
    		if (group.getPath().equals(path)) {
    			return group;
    		}
    	}
        return null;
    }
    public void removeGroup(InventoryGroup group) {
		this.groups.remove(group);
    }
    public boolean groupExists(InventoryGroup group) {
		if (this.groups.contains(group)) {
			return true;
		}
		return false;
    }
    public void setGroups(LinkedList<InventoryGroup> groups) {
        this.groups = groups;
    }
    
    public LinkedList<InventoryInstallation> getInstallations() {
        return this.installations;
    }
    public InventoryInstallation getInstallation(String name) {
    	for (InventoryInstallation inst : getInstallations()) {
    		if (inst.getName().equals(name)) {
    			return inst;
    		}
    	}
        return null;
    }
    public void addInstallation(InventoryInstallation inst) {
        this.installations.add(inst);
    }
    public void setInstallations(LinkedList<InventoryInstallation> installations) {
        this.installations = installations;
    }

}
