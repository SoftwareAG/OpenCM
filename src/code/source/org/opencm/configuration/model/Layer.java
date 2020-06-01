package org.opencm.configuration.model;

import java.util.LinkedList;

public class Layer {

    private String layer;
    private LinkedList<String> sublayers;
    private LinkedList<String> versions;
    private LinkedList<String> nodes;

    public String getLayer() {
        return this.layer;
    }
    
    public void setLayer(String name) {
        this.layer = name;
    }

    public LinkedList<String> getSublayers() {
        return this.sublayers;
    }
    
    public LinkedList<String> getVersions() {
        return this.versions;
    }

    public LinkedList<String> getNodes() {
        return this.nodes;
    }
    
}
