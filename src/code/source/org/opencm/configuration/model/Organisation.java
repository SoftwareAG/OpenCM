package org.opencm.configuration.model;

import java.util.LinkedList;

public class Organisation {

    private String org_name;
    private LinkedList<Department> departments;

    public String getOrg() {
        return this.org_name;
    }
    public void setOrg(String name) {
        this.org_name = name;
    }

    public LinkedList<Department> getDepartments() {
        return this.departments;
    }
    
}
