package org.opencm.inventory;

import java.util.LinkedList;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class Organisation {

    private String name;
    private LinkedList<Department> departments;

    @JsonPropertyOrder({ "name", "departments" })
    
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Department> getDepartments() {
        return this.departments;
    }
    public void setDepartments(LinkedList<Department> deps) {
        this.departments = deps;
    }
    
}
