package org.opencm.inventory;

import java.util.LinkedList;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Server {

	public static final String		SERVER_METADATA_NAME			= "META_DATA";
	public static final String 		KEEPASS_PROPERTY_DESCRIPTION	= "description";
	public static final String 		KEEPASS_PROPERTY_OS				= "os";
	public static final String 		KEEPASS_PROPERTY_TYPE			= "type";

    private String name;
    private String description;
    private String os;
    private String type;
    private LinkedList<Installation> installations;

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }
    public void setDescription(String desc) {
        this.description = desc;
    }
    
    public String getOs() {
        return this.os;
    }
    public void setOs(String os) {
        this.os = os;
    }
    
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    public LinkedList<Installation> getInstallations() {
        return this.installations;
    }
    public void setInstallations(LinkedList<Installation> installs) {
        this.installations = installs;
    }
    
    @JsonIgnore
    public LinkedList<Installation> getInstallations(String env, String layer, String sublayer) {
    	LinkedList<Installation> insts = new LinkedList<Installation>();
 		for (int n = 0; n < getInstallations().size(); n++) {
 			Installation inst = getInstallations().get(n);
			if ((env == null) || ((inst.getEnvironment() != null) && inst.getEnvironment().equals(env))) {
				if ((layer == null) || ((inst.getLayer() != null) && inst.getLayer().equals(layer))) {
					if ((sublayer == null) || ((inst.getSublayer() != null) && inst.getSublayer().equals(sublayer))) {
						if (!insts.contains(inst)) {
							insts.add(inst);
						}
					}
				}
			}
 		}
        return insts;
    }
    
    @JsonIgnore
    public String getUnqualifiedHostname() {
		if (getName().indexOf(".") > 0) {
			return getName().substring(0,getName().indexOf("."));
		}
        return getName();
    }
    
    @JsonIgnore
    public Server getCopy() {
    	Server srv = new Server();
		srv.setName(getName());
		srv.setDescription(getDescription());
		srv.setOs(getOs());
		srv.setType(getType());
		return srv;
    }

}
