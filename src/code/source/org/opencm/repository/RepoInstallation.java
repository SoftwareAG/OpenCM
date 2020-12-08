package org.opencm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.opencm.extract.IntegrationServerExtractor;

import org.opencm.ui.Port;
import org.opencm.ui.WmDb;
import org.opencm.util.LogUtils;
import org.opencm.ui.JdbcConnection;
import org.opencm.ui.SapConnection;
import org.opencm.ui.Messaging;

public class RepoInstallation {

    public static final String 	INSTANCE_NAME_INFO				= "install_info";
    public static final String 	INSTANCE_NAME_PORT				= "install_port";
    public static final String 	INSTANCE_NAME_WMDB				= "install_wmdb";
    public static final String 	INSTANCE_NAME_JDBC				= "install_jdbc";
    public static final String 	INSTANCE_NAME_SAP				= "install_sap";
    public static final String 	INSTANCE_NAME_MESSAGING			= "install_msg";
    
    public static final String 	PROPERTY_KEY_DIR				= "install_dir";
    public static final String 	PROPERTY_KEY_VERSION			= "install_version";
    public static final String 	PROPERTY_KEY_INST_TIME			= "install_time";
    public static final String 	PROPERTY_KEY_EXTR_TIME			= "extract_time";
    public static final String 	PROPERTY_KEY_EXTR_ALIAS			= "extract_alias";
    public static final String 	PROPERTY_KEY_OPERATING_SYSTEM	= "install_os";
    public static final String 	PROPERTY_KEY_CPU_PHYSICAL		= "install_cpu_physical";
    public static final String 	PROPERTY_KEY_CPU_LOGICAL		= "install_cpu_logical";
    public static final String 	PROPERTY_KEY_PORT_RUNTIME		= "install_port_runtime";
    public static final String 	PROPERTY_KEY_PORT_LB			= "install_port_lb";
    
    public static final String 	PROPERTY_KEY_SUFFIX_PORT		= ".port";
    public static final String 	PROPERTY_KEY_SUFFIX_HOSTNAME	= ".serverName";
    public static final String 	PROPERTY_KEY_SUFFIX_USERNAME	= ".username";
    public static final String 	PROPERTY_KEY_SUFFIX_DATABASE	= ".database";
    public static final String 	PROPERTY_KEY_SUFFIX_ENABLED		= ".enabled";

    public static final String 	PROPERTY_KEY_SUFFIX_APP_SERVER	= ".applicationServer";
    public static final String 	PROPERTY_KEY_SUFFIX_MSG_SERVER	= ".messageServer";
    public static final String 	PROPERTY_KEY_SUFFIX_SYSTEM_ID	= ".systemId";
    public static final String 	PROPERTY_KEY_SUFFIX_CLIENT		= ".client";
    
    
	// Added as per Inventory
	private ArrayList<String> path;
	private String name;
	
	// Added as per extraction
	private String extractTime;
	private String extractAlias;
	private LinkedList<Property> properties;
	private LinkedList<Component> components;

	public RepoInstallation() {
	}
	
	public RepoInstallation(ArrayList<String> path, String name) {
		this.properties = new LinkedList<Property>();
		this.components = new LinkedList<Component>();
		setPath(path);
		setName(name);
	}

	public ArrayList<String> getPath() {
		return this.path;
	}
	public void setPath(ArrayList<String> path) {
		this.path = path; 
	}
	
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name; 
	}

	public String getExtractTime() {
		return this.extractTime;
	}
	public void setExtractTime(String time) {
		this.extractTime = time; 
	}

	public String getExtractAlias() {
		return this.extractAlias;
	}
	public void setExtractAlias(String alias) {
		this.extractAlias = alias; 
	}

	public LinkedList<Property> getProperties() {
		return this.properties;
	}
	public void setProperties(LinkedList<Property> props) {
		this.properties = props; 
	}
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
	
	public LinkedList<Component> getComponents() {
		return this.components;
	}
    @JsonIgnore
	public void addComponent(Component comp) {
		this.components.add(comp); 
	}
    @JsonIgnore
	public Component getComponent(String name) {
		for (Component comp: getComponents()) {
			if (comp.getName().equals(name)) {
				return comp;
			}
		}
		return null;
	}
    @JsonIgnore
	public boolean hasComponents() {
		if (this.components.isEmpty())  {
			return false;
		}
		return true; 
	}
    // ------------------------------------------------------
    // Helper methods for populating/decorating UI inventory information
    // ------------------------------------------------------
    @JsonIgnore
	public String getInstallationDirectory() {
    	Component component = getComponent(Component.COMPONENT_NAME_OSGI_SPM);
    	if (component != null) {
    		Instance instance = component.getInstance(Instance.INSTANCE_NAME_INSTALL_DIR);
    		if (instance != null) {
    			Property property = instance.getProperty(Property.PROPERTY_NAME_PLAIN_TEXT_PROP);
    			if (property != null) {
    				return property.getValue();
    			}
    		}
    	}
		return null;
	}
    
    @JsonIgnore
	public String getVersion() {
		Property property = getProperty(Property.PROPERTY_NAME_PRODUCT_VERSION);
		if (property != null) {
			return property.getValue();
		}
		return null;
	}

    @JsonIgnore
	public String getInstallTime() {
		Property property = getProperty(Property.PROPERTY_NAME_INSTALL_TIME);
		if (property != null) {
			return property.getValue();
		}
		return null;
	}

    @JsonIgnore
	public String getOperatingSystem() {
		Property property = getProperty(Property.PROPERTY_NAME_DISPLAY_NAME);
		if (property != null) {
			return property.getValue();
		}
		return null;
	}
    
    @JsonIgnore
	public String getCpuPhysical() {
		Property property = getProperty(Property.PROPERTY_NAME_CPU_PHYSICAL);
		if (property != null) {
			return property.getValue();
		}
		return null;
	}
    
    @JsonIgnore
	public String getCpuLogical() {
		Property property = getProperty(Property.PROPERTY_NAME_CPU_LOGICAL);
		if (property != null) {
			return property.getValue();
		}
		return null;
	}
    
    @JsonIgnore
	public LinkedList<Port> getPorts() {
		LinkedList<Port> ports = new LinkedList<Port>();
		for (Component component : getComponents()) {
			// SPM ports
	    	if ((component != null) && component.getName().equals(Component.COMPONENT_NAME_OSGI_SPM)) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().startsWith(Instance.INSTANCE_PREFIX_PORTS)) {
	        			// Get id (for port "identifier")
	        			Property portId = instance.getProperty(Property.PROPERTY_NAME_ID);
	        			String identifier = Property.PROPERTY_NAME_ID;
	        			if ((portId != null) && (portId.getValue() != null)) {
	        				identifier = portId.getValue().substring(Instance.INSTANCE_PREFIX_PORTS.length());
	        			}
	        			Property portProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_NUMBER);
	        			Property protProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_PROTOCOL);
	        			Property portEnabled = instance.getProperty(Property.PROPERTY_NAME_PORT_ENABLED);
	        			Property portBindAddress = instance.getProperty(Property.PROPERTY_NAME_PORT_BINDADDRESS);
	        			String propBindAddress = "";
	        			if (portBindAddress != null) {
	        				propBindAddress = portBindAddress.getValue(); 
	        			}
        				Port port = new Port(INSTANCE_NAME_PORT + "-SPM-" + idx++, component.getName(), identifier, portProperty.getValue(), protProperty.getValue(), portEnabled.getValue(), propBindAddress);
        				ports.add(port);
	        		}
	    		}
	    	}
			// Integration Server ports
	    	if ((component != null) && component.getName().startsWith(Component.COMPONENT_PREFIX_INTEGRATION_SERVER)) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().startsWith(Instance.INSTANCE_PREFIX_PORTS)) {
	        			Property portId = instance.getProperty(Property.PROPERTY_NAME_ID);
	        			String identifier = Property.PROPERTY_NAME_ID;
	        			if ((portId != null) && (portId.getValue() != null)) {
	        				identifier = portId.getValue().substring(Instance.INSTANCE_PREFIX_PORTS.length());
	        			} else {
	        				// Should exist
	        				continue;
	        			}
	        			Property portProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_NUMBER);
	        			if (portProperty == null) {
	        				// E.g. file polling port
	        				continue;
	        			}
	        			Property protProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_PROTOCOL);
	        			Property portEnabled = instance.getProperty(Property.PROPERTY_NAME_PORT_ENABLED);
	        			Property portBindAddress = instance.getProperty(Property.PROPERTY_NAME_PORT_BINDADDRESS);
	        			String propBindAddress = "";
	        			if (portBindAddress != null) {
	        				propBindAddress = portBindAddress.getValue(); 
	        			}
        				Port port = new Port(INSTANCE_NAME_PORT + "-IS-" + component.getName() + "-" + idx++, component.getName(), identifier, portProperty.getValue(), protProperty.getValue(), portEnabled.getValue(), propBindAddress);
        				ports.add(port);
	        		}
	    		}
	    		
	    	}
			// Universal Messaging ports
	    	if ((component != null) && component.getName().startsWith(Component.COMPONENT_PREFIX_UNIVERSAL_MESSAGING)) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().startsWith(Instance.INSTANCE_PREFIX_PORTS)) {
	        			Property portId = instance.getProperty(Property.PROPERTY_NAME_ID);
	        			String identifier = Property.PROPERTY_NAME_ID;
	        			if ((portId != null) && (portId.getValue() != null)) {
	        				identifier = portId.getValue().substring(Instance.INSTANCE_PREFIX_PORTS.length());
	        			}
	        			Property portProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_NUMBER);
	        			Property protProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_PROTOCOL);
	        			Property portEnabled = instance.getProperty(Property.PROPERTY_NAME_PORT_ENABLED);
	        			Property portBindAddress = instance.getProperty(Property.PROPERTY_NAME_PORT_BINDADDRESS);
	        			String propBindAddress = "";
	        			if (portBindAddress != null) {
	        				propBindAddress = portBindAddress.getValue(); 
	        			}
        				Port port = new Port(INSTANCE_NAME_PORT + "-UM-" + component.getName() + "-" + idx++, component.getName(), identifier, portProperty.getValue(), protProperty.getValue(), portEnabled.getValue(), propBindAddress);
        				ports.add(port);
	        		}
	    		}
	    		
	    	}
			// My webmethods ports
	    	if ((component != null) && component.getName().startsWith(Component.COMPONENT_PREFIX_MY_WEBMETHODS_SERVER)) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().startsWith(Instance.INSTANCE_PREFIX_PORTS)) {
	        			Property portId = instance.getProperty(Property.PROPERTY_NAME_ID);
	        			String identifier = Property.PROPERTY_NAME_ID;
	        			if ((portId != null) && (portId.getValue() != null)) {
	        				identifier = portId.getValue().substring(Instance.INSTANCE_PREFIX_PORTS.length());
	        			}
	        			Property portProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_NUMBER);
	        			Property protProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_PROTOCOL);
	        			Property portEnabled = instance.getProperty(Property.PROPERTY_NAME_PORT_ENABLED);
	        			Property portBindAddress = instance.getProperty(Property.PROPERTY_NAME_PORT_BINDADDRESS);
	        			String propBindAddress = "";
	        			if (portBindAddress != null) {
	        				propBindAddress = portBindAddress.getValue(); 
	        			}
        				Port port = new Port(INSTANCE_NAME_PORT + "-MWS-" + component.getName() + "-" + idx++, component.getName(), identifier, portProperty.getValue(), protProperty.getValue(), portEnabled.getValue(), propBindAddress);
        				ports.add(port);
	        		}
	    		}
	    	}
			// Terracotta Server ports
	    	if ((component != null) && component.getName().startsWith(Component.COMPONENT_PREFIX_TERRACOTTA_SERVER)) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().startsWith(Instance.INSTANCE_PREFIX_PORTS)) {
	        			Property portId = instance.getProperty(Property.PROPERTY_NAME_ID);
	        			String identifier = Property.PROPERTY_NAME_ID;
	        			if ((portId != null) && (portId.getValue() != null)) {
	        				identifier = portId.getValue().substring(Instance.INSTANCE_PREFIX_PORTS.length());
	        			}
	        			Property portProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_NUMBER);
	        			Property protProperty = instance.getProperty(Property.PROPERTY_NAME_PORT_PROTOCOL);
	        			Property portEnabled = instance.getProperty(Property.PROPERTY_NAME_PORT_ENABLED);
	        			Property portBindAddress = instance.getProperty(Property.PROPERTY_NAME_PORT_BINDADDRESS);
	        			String propBindAddress = "";
	        			if (portBindAddress != null) {
	        				propBindAddress = portBindAddress.getValue(); 
	        			}
        				Port port = new Port(INSTANCE_NAME_PORT + "-TSA-" + component.getName() + "-" + idx++, component.getName(), identifier, portProperty.getValue(), protProperty.getValue(), portEnabled.getValue(), propBindAddress);
        				ports.add(port);
	        		}
	    		}
	    	}
		}
		
		return ports;
	}

    @JsonIgnore
	public LinkedList<WmDb> getWmDbs() {
		LinkedList<WmDb> wmdbs = new LinkedList<WmDb>();
		for (Component component : getComponents()) {
			// Integration Server 
	    	if ((component != null) && (component.getName().startsWith(Component.COMPONENT_PREFIX_INTEGRATION_SERVER) || component.getName().startsWith(Component.COMPONENT_PREFIX_MY_WEBMETHODS_SERVER))) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().startsWith(Instance.INSTANCE_PREFIX_WMDB)) {
	        			Property jdbcId = instance.getProperty(Property.PROPERTY_NAME_ID);
	        			String identifier = Property.PROPERTY_NAME_ID;
	        			if ((jdbcId != null) && (jdbcId.getValue() != null)) {
	        				identifier = jdbcId.getValue().substring(Instance.INSTANCE_PREFIX_WMDB.length());
	        			}
	        			Property jdbcUrl = instance.getProperty(Property.PROPERTY_NAME_WMDB_URL);
	        			Property jdbcUsername = instance.getProperty(Property.PROPERTY_NAME_WMDB_USERNAME);
        				WmDb wmdb = new WmDb(INSTANCE_NAME_WMDB + "-" + component.getName() + "-" + idx++, identifier, jdbcUrl.getValue(), jdbcUsername.getValue());
        				wmdbs.add(wmdb);
	        		}
	    		}
	    	}
		}
		return wmdbs;
	}
    
    @JsonIgnore
	public LinkedList<JdbcConnection> getJdbcConnections() {
		LinkedList<JdbcConnection> jdbcConns = new LinkedList<JdbcConnection>();
		for (Component component : getComponents()) {
			// Integration Server 
	    	if ((component != null) && component.getName().startsWith(Component.COMPONENT_PREFIX_INTEGRATION_SERVER)) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().equals(IntegrationServerExtractor.INSTANCE_JDBC)) {
	        			// Each property = conn_name.Username/Enabled/etc.
	        			// Hashmap of conn name + Jdbc object
	        			HashMap<String,JdbcConnection> hmConns = new HashMap<String,JdbcConnection>();
	        			LogUtils.logDebug("getJdbcConnections :: searching for hostname ... ");
	    	    		for (Property prop : instance.getProperties()) {
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_HOSTNAME)) {
	    	    				String connId = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_HOSTNAME));
	    	    				JdbcConnection jdbcConn = new JdbcConnection(INSTANCE_NAME_JDBC + "-" + component.getName() + "-" + idx++, connId, prop.getValue());
	    	    				hmConns.put(connId, jdbcConn);
	    	    			}
	    	    		}
	    	    		// Retrieve the rest
	        			LogUtils.logDebug("getJdbcConnections :: retrieving the rest of the properties ... ");
	    	    		for (Property prop : instance.getProperties()) {
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_ENABLED)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_ENABLED));
	    	    				if (hmConns.containsKey(key)) {
		    	    				hmConns.get(key).setEnabled(prop.getValue());
	    	    				}
	    	    				continue;
	    	    			}
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_PORT)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_PORT));
	    	    				if (hmConns.containsKey(key)) {
		    	    				hmConns.get(key).setPort(prop.getValue());
	    	    				}
	    	    				continue;
	    	    			}
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_DATABASE)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_DATABASE));
	    	    				if (hmConns.containsKey(key)) {
	    	    					hmConns.get(key).setDatabase(prop.getValue());
	    	    				}
	    	    				continue;
	    	    			}
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_USERNAME)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_USERNAME));
	    	    				if (hmConns.containsKey(key)) {
		    	    				hmConns.get(key).setUsername(prop.getValue());
	    	    				}
	    	    			}
	    	    		}
	    	    		
	        			LogUtils.logDebug("getJdbcConnections :: adding JDBC Connection objects ... ");
	    	    		Iterator<String> it = hmConns.keySet().iterator();
	    	    		while (it.hasNext()) {
	        				jdbcConns.add(hmConns.get(it.next()));
	    	    		}
	        		}
	    		}
	    	}
		}
		LogUtils.logDebug("getJdbcConnections :: Returning JDBC connection properties ... ");
		return jdbcConns;
	}
    @JsonIgnore
	public LinkedList<SapConnection> getSapConnections() {
		LinkedList<SapConnection> sapConns = new LinkedList<SapConnection>();
		for (Component component : getComponents()) {
			// Integration Server 
	    	if ((component != null) && component.getName().startsWith(Component.COMPONENT_PREFIX_INTEGRATION_SERVER)) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().equals(IntegrationServerExtractor.INSTANCE_SAP)) {
	        			// Each property = conn_name.Username/Enabled/etc.
	        			// Hashmap of conn name + Sap object
	        			HashMap<String,SapConnection> hmConns = new HashMap<String,SapConnection>();
	        			LogUtils.logDebug("getSapConnections :: searching for username ... ");
	    	    		for (Property prop : instance.getProperties()) {
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_USERNAME)) {
	    	    				String connId = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_USERNAME));
	    	    				SapConnection sapConn = new SapConnection(INSTANCE_NAME_SAP + "-" + component.getName() + "-" + idx++, connId, prop.getValue());
	    	    				hmConns.put(connId, sapConn);
	    	    			}
	    	    		}
	    	    		// Retrieve the rest
	        			LogUtils.logDebug("getSapConnections :: retrieving the rest of the properties ... ");
	    	    		for (Property prop : instance.getProperties()) {
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_ENABLED)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_ENABLED));
	    	    				if (hmConns.containsKey(key)) {
		    	    				hmConns.get(key).setEnabled(prop.getValue());
	    	    				}
	    	    				continue;
	    	    			}
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_APP_SERVER)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_APP_SERVER));
	    	    				if (hmConns.containsKey(key)) {
		    	    				hmConns.get(key).setApplicationServer(prop.getValue());
	    	    				}
	    	    				continue;
	    	    			}
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_MSG_SERVER)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_MSG_SERVER));
	    	    				if (hmConns.containsKey(key)) {
		    	    				hmConns.get(key).setMessageServer(prop.getValue());
	    	    				}
	    	    				continue;
	    	    			}
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_SYSTEM_ID)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_SYSTEM_ID));
	    	    				if (hmConns.containsKey(key)) {
		    	    				hmConns.get(key).setSystemId(prop.getValue());
	    	    				}
	    	    				continue;
	    	    			}
	    	    			if (prop.getKey().endsWith(PROPERTY_KEY_SUFFIX_CLIENT)) {
	    	    				String key = prop.getKey().substring(0, prop.getKey().lastIndexOf(PROPERTY_KEY_SUFFIX_CLIENT));
	    	    				if (hmConns.containsKey(key)) {
	    	    					hmConns.get(key).setClient(prop.getValue());
	    	    				}
	    	    				continue;
	    	    			}
	    	    		}
	    	    		
	        			LogUtils.logDebug("getSapConnections :: adding SAP Connection objects ... ");
	    	    		Iterator<String> it = hmConns.keySet().iterator();
	    	    		while (it.hasNext()) {
	        				sapConns.add(hmConns.get(it.next()));
	    	    		}
	        		}
	    		}
	    	}
		}
		LogUtils.logDebug("getSapConnections :: Returning SAP connection properties ... ");
		return sapConns;
	}

    @JsonIgnore
	public LinkedList<Messaging> getMessaing() {
		LinkedList<Messaging> msgs = new LinkedList<Messaging>();
		for (Component component : getComponents()) {
			// Integration Server 
	    	if ((component != null) && component.getName().startsWith(Component.COMPONENT_PREFIX_INTEGRATION_SERVER)) {
	    		int idx = 0;
	    		for (Instance instance : component.getInstances()) {
	        		if ((instance != null) && instance.getName().startsWith(Instance.INSTANCE_PREFIX_MSG_WM)) {
	        			Property msgId = instance.getProperty(Property.PROPERTY_NAME_ID);
	        			String identifier = Property.PROPERTY_NAME_ID;
	        			if ((msgId != null) && (msgId.getValue() != null)) {
	        				identifier = msgId.getValue().substring(Instance.INSTANCE_PREFIX_MSG_WM.length());
	        			}
	        			Property msgUrl = instance.getProperty(Property.PROPERTY_KEY_MSG_WM_URL);
	        			if ((msgUrl != null) && (msgUrl.getValue() != null) && (msgUrl.getValue() != "")) {
	        				String user = "";
		        			Property msgUsername = instance.getProperty(Property.PROPERTY_KEY_MSG_WM_USERNAME);
		        			if (msgUsername != null) {
		        				user = msgUsername.getValue();
		        			}
	        				Messaging msg = new Messaging(INSTANCE_NAME_MESSAGING + "-" + component.getName() + "-" + idx++, identifier, "WM", msgUrl.getValue(), user);
	        				msgs.add(msg);
	        			}
	        		}
	        		if ((instance != null) && instance.getName().startsWith(Instance.INSTANCE_PREFIX_MSG_JNDI)) {
	        			Property msgId = instance.getProperty(Property.PROPERTY_NAME_ID);
	        			String identifier = Property.PROPERTY_NAME_ID;
	        			if ((msgId != null) && (msgId.getValue() != null)) {
	        				identifier = msgId.getValue().substring(Instance.INSTANCE_PREFIX_MSG_JNDI.length());
	        			}
	        			Property msgUrl = instance.getProperty(Property.PROPERTY_KEY_MSG_JNDI_URL);
	        			if ((msgUrl != null) && (msgUrl.getValue() != null) && (msgUrl.getValue() != "")) {
	        				String user = "";
		        			Property msgUsername = instance.getProperty(Property.PROPERTY_KEY_MSG_JNDI_USERNAME);
		        			if (msgUsername != null) {
		        				user = msgUsername.getValue();
		        			}
	        				Messaging msg = new Messaging(INSTANCE_NAME_MESSAGING + "-" + component.getName() + "-" + idx++, identifier, "JNDI", msgUrl.getValue(), user);
	        				msgs.add(msg);
	        			}
	        		}
	    		}
	    	}
		}
		return msgs;
	}
    
}
