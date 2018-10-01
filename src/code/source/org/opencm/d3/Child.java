package org.opencm.d3;

import java.util.LinkedList;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Child {

	private String name;
	private String level;
	private String url;
	private Boolean hasBaseline;
	private Boolean hasRuntime;
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
	public String getUrl() {
		return this.url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean getHasBaseline() {
		return this.hasBaseline;
	}
	public void setHasBaseline(Boolean hasBaseline) {
		this.hasBaseline = hasBaseline;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean getHasRuntime() {
		return this.hasRuntime;
	}
	public void setHasRuntime(Boolean hasRuntime) {
		this.hasRuntime = hasRuntime;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public LinkedList<Child> getChildren() {
		return this.children;
	}
	public void setChildren(LinkedList<Child> children) {
		this.children = children;
	}
	
}
