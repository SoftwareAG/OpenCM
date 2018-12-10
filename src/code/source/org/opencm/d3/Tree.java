package org.opencm.d3;

import java.util.LinkedList;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Tree {

	private String name;
	private String level;
	private String layer;
	private String env;
	private String spmURL;
	private LinkedList<Child> children;
	
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	} 

	public String getLevel() {
		return this.level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getLayer() {
		return this.layer;
	}
	public void setLayer(String layer) {
		this.layer = layer;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getEnvironment() {
		return this.env;
	}
	public void setEnvironment(String env) {
		this.env = env;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getSpmURL() {
		return this.spmURL;
	}
	public void setSpmURL(String url) {
		this.spmURL = url;
	}
	
	public LinkedList<Child> getChildren() {
		return this.children;
	}
	public void setChildren(LinkedList<Child> children) {
		this.children = children;
	}
}
