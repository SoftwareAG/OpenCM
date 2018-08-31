package org.opencm.extract.configuration;

import java.util.LinkedList;

public class ExtractComponent {

	private String componentName;
	private String componentInfo;
	private LinkedList<ExtractInstance> instances;

	public ExtractComponent(String name) {
		setName(name);
	}

	public String getName() {
		return this.componentName;
	}
	
	public void setName(String name) {
		this.componentName = name; 
	}

	public String getInfo() {
		return this.componentInfo;
	}
	
	public void setInfo(String info) {
		this.componentInfo = info; 
	}

	public LinkedList<ExtractInstance> getInstances() {
		return this.instances;
	}
	
	public void addInstance(ExtractInstance instance) {
		if (this.instances == null)  {
			this.instances = new LinkedList<ExtractInstance>();
		}
		this.instances.add(instance); 
	}

	public boolean hasInstances() {
		if ((this.instances == null) || (this.instances.isEmpty()))  {
			return false;
		}
		return true; 
	}
}
