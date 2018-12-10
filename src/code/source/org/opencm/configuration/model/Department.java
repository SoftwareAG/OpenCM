package org.opencm.configuration.model;

import java.util.LinkedList;

public class Department {

    private String op_dep_name;
    private LinkedList<Environment> environments;

    public String getDep() {
        return this.op_dep_name;
    }
    public void setDep(String name) {
        this.op_dep_name = name;
    }
	
    public LinkedList<Environment> getEnvironments() {
        return this.environments;
    }

}
