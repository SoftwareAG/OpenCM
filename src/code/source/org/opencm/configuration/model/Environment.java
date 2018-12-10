package org.opencm.configuration.model;

import java.util.LinkedList;

public class Environment {

    private String environment;
    private LinkedList<String> nodes;

    public String getEnv() {
        return this.environment;
    }
    public void setEnv(String name) {
        this.environment = name;
    }

    public LinkedList<String> getNodes() {
        return this.nodes;
    }
    
}
