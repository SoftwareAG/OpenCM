package org.opencm.configuration.model;

import java.util.LinkedList;

public class Environment {

    private String environment;
    private LinkedList<Layer> layers;

    public String getEnv() {
        return this.environment;
    }
    public void setEnv(String name) {
        this.environment = name;
    }

    public LinkedList<Layer> getLayers() {
        return this.layers;
    }
    
}
