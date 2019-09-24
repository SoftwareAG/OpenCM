package org.opencm.configuration.model;

import java.util.LinkedList;

public class Environment {

    private String environment;
    private LinkedList<String> nodes;
    private LinkedList<String> layers;
    private LinkedList<String> sublayers;
    private LinkedList<String> versions;

    public String getEnv() {
        return this.environment;
    }
    public void setEnv(String name) {
        this.environment = name;
    }

    public LinkedList<String> getNodes() {
        return this.nodes;
    }
    
    public LinkedList<String> getLayers() {
        return this.layers;
    }
    
    public LinkedList<String> getSublayers() {
        return this.sublayers;
    }
    
    public LinkedList<String> getVersions() {
        return this.versions;
    }
}
