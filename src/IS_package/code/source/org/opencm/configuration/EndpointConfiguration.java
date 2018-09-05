package org.opencm.configuration;

public class EndpointConfiguration {
	
	public static final String 		ENDPOINT_CONFIG_OPENCM 	= 	"opencm";
	public static final String 		ENDPOINT_CONFIG_KEEPASS	= 	"keepass";

    private String type;
    private String db;
    private String top_group;
    
    public EndpointConfiguration() {
    }

    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDb() {
        return this.db;
    }
    public void setDb(String db) {
        this.db = db;
    }
    public String getTop_group() {
        return this.top_group;
    }
    public void setTop_group(String topgroup) {
        this.top_group = topgroup;
    }
}
