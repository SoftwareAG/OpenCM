package org.opencm.repository;

import java.util.LinkedList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opencm.extract.IntegrationServerExtractor;

public class Component {

    public static final String 	COMPONENT_NAME_OSGI_SPM					= "OSGI-SPM";
    public static final String 	COMPONENT_PREFIX_OSGI_IS				= "OSGI-IS_";
    public static final String 	COMPONENT_PREFIX_OSGI_MWS				= "OSGI-MWS_";
    public static final String 	COMPONENT_PREFIX_INTEGRATION_SERVER		= "integrationServer-";
    public static final String 	COMPONENT_PREFIX_UNIVERSAL_MESSAGING	= "Universal-Messaging-";
    public static final String 	COMPONENT_PREFIX_MY_WEBMETHODS_SERVER	= "MwsProgramFiles-";
    public static final String 	COMPONENT_PREFIX_TERRACOTTA_SERVER		= "TES-";
	
	private String componentName;
	private LinkedList<Property> properties;
	private LinkedList<Instance> instances;

	public Component() {
	}
	
	public Component(String name) {
		this.properties = new LinkedList<Property>();
		this.instances = new LinkedList<Instance>();
		setName(name);
	}

	public String getName() {
		return this.componentName;
	}
	
	public void setName(String name) {
		this.componentName = name; 
	}

	public LinkedList<Instance> getInstances() {
		return this.instances;
	}
	
    @JsonIgnore
	public void addInstance(Instance instance) {
		this.instances.add(instance); 
	}

    @JsonIgnore
	public void addInstances(LinkedList<Instance> instances) {
		this.instances.addAll(instances); 
	}
    
    @JsonIgnore
	public Instance getInstance(String instanceName) {
		for (Instance instance: getInstances() )  {
			if (instanceName.equals(instance.getName())) {
				return instance;
			}
		}
		return null; 
	}
    
    @JsonIgnore
	public boolean hasInstances() {
		if (this.instances.isEmpty())  {
			return false;
		}
		return true; 
	}
	
	public LinkedList<Property> getProperties() {
		return this.properties;
	}
	
	public void setProperties(LinkedList<Property> props) {
		this.properties = props; 
	}
	
    @JsonIgnore
	public void addProperty(Property prop) {
		this.properties.add(prop); 
	}
	
    @JsonIgnore
	public void addProperties(LinkedList<Property> props) {
		this.properties.addAll(props); 
	}
    
    @JsonIgnore
	public Property getProperty(String key) {
		for (Property prop: getProperties()) {
			if (prop.getKey().equals(key)) {
				return prop;
			}
		}
		return null;
	}
	
    @JsonIgnore
	public boolean hasInstance(String instanceName) {
    	for (Instance instance: getInstances()) {
    		if (instance.getName().equals(instanceName)) {
    			return true;
    		}
    	}
		return false; 
	}
    
    @JsonIgnore
	public LinkedList<String> getPackages() {
		LinkedList<String> packages = new LinkedList<String>();
		Instance packageInstance = getInstance(IntegrationServerExtractor.INSTANCE_IS_PACKAGES);
		if (packageInstance != null) {
			for (Property prop : packageInstance.getProperties()) {
				if (prop.getKey().endsWith(".version")) {
					packages.add(prop.getKey().substring(0, prop.getKey().lastIndexOf(".version")));
				}
			}
		}
		return packages;
	}
    
    @JsonIgnore
	public boolean hasPackage(String packageName) {
		Instance packageInstance = getInstance(IntegrationServerExtractor.INSTANCE_IS_PACKAGES);
		if (packageInstance.hasPropertyKey(packageName + ".version")) {
			return true;
		}
		return false;
	}
}
    
