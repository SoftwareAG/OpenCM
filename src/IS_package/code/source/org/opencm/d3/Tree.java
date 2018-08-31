package org.opencm.d3;

import java.util.LinkedList;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Tree {

	private String name;
	private String level;
	private String assertionGroup;
	private String cceEnv;
	private String opencmEnv;
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
	public String getAssertionGroup() {
		return this.assertionGroup;
	}
	public void setAssertionGroup(String group) {
		this.assertionGroup = group;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getCceEnvironment() {
		return this.cceEnv;
	}
	public void setCceEnvironment(String env) {
		this.cceEnv = env;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getOpencmEnvironment() {
		return this.opencmEnv;
	}
	public void setOpencmEnvironment(String env) {
		this.opencmEnv = env;
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
