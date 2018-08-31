package org.opencm.extract.configuration;

public class ExtractInstance {

	private String instanceName;
	private String instanceInfo;
	private String instanceData;

	public ExtractInstance(String name) {
		setName(name);
	}

	public String getName() {
		return this.instanceName;
	}
	
	public void setName(String name) {
		this.instanceName = name; 
	}

	public String getInfo() {
		return this.instanceInfo;
	}
	
	public void setInfo(String info) {
		this.instanceInfo = info; 
	}

	public String getData() {
		return this.instanceData;
	}
	
	public void setData(String data) {
		this.instanceData = data; 
	}
}
