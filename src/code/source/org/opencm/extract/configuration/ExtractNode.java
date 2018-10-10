package org.opencm.extract.configuration;

import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExtractNode {

	private String nodeAlias;
	private String extrationDate;
	private String installTime;
	private String version;
	private String extractAlias;
	private LinkedList<ExtractComponent> components;

	public ExtractNode(String alias) {
		setAlias(alias);
	}

	public String getAlias() {
		return this.nodeAlias;
	}
	
	public void setAlias(String nodeAlias) {
		this.nodeAlias = nodeAlias; 
	}

	public String getExtractionDate() {
		return this.extrationDate;
	}
	
	public void setExtractionDate(String datetime) {
		this.extrationDate = datetime; 
	}

	public String getInstallTime() {
		return this.installTime;
	}
	
	public void setInstallTime(String date) {
		this.installTime = date; 
	}

	public String getVersion() {
		return this.version;
	}
	
	public void setVersion(String ver) {
		this.version = ver; 
	}

	public String getExtractAlias() {
		return this.extractAlias;
	}
	
	public void setExtractAlias(String alias) {
		this.extractAlias = alias; 
	}
	
    @JsonIgnore
	public LinkedList<ExtractComponent> getComponents() {
		return this.components;
	}
	
    @JsonIgnore
	public void addComponent(ExtractComponent comp) {
		if (this.components == null)  {
			this.components = new LinkedList<ExtractComponent>();
		}
		this.components.add(comp); 
	}

    @JsonIgnore
	public boolean hasComponents() {
		if ((this.components == null) || (this.components.isEmpty()))  {
			return false;
		}
		return true; 
	}

}
