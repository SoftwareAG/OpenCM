package org.opencm.repository.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.ResultSet;

import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;

public class Server {
	
	public static String CREATE_TABLE		= 	"CREATE TABLE SERVER (" + 
												"  SERVER_NAME	VARCHAR(128)	NOT NULL" + 
												")";
	public static String CREATE_PRIMARY_KEY	= 	"ALTER TABLE SERVER " + 
												"  ADD CONSTRAINT SERVER_PK PRIMARY KEY (SERVER_NAME)";
	
	public static String SELECT_ALL			= 	"SELECT SERVER_NAME FROM SERVER";
	
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

    public static boolean create(Configuration opencmConfig, Connection conn) {
		try {
			Statement s = conn.createStatement();
			s.executeUpdate(CREATE_TABLE);
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_INFO,"org.opencm.repository.db.Server: create - Schemas created successfully.");
		} catch (SQLException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Server: create Exception: " + ex.getMessage());
			return false;
    	}

		return false;
    }

    public static boolean exists(Configuration opencmConfig, Connection conn) {
    	boolean exists = false;
    	
    	Statement statement = null;
        ResultSet rs = null;
		try {
			statement = conn.createStatement();
	        rs = statement.executeQuery(SELECT_ALL);
    		if (rs.next()) {
                exists = true;
            }
		} catch (SQLException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Server: selectAll Exception: " + ex.getMessage());
			return false;
		} finally {
			
	        // Close objects
	        try {
	            if (rs != null) {
	                rs.close();
	                rs = null;
	            }
	        } catch (SQLException sqle) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Server: resultSet: " + sqle.getMessage());
	        }

            try {
                if (statement != null) {
                	statement.close();
                	statement = null;
                }
            } catch (SQLException sqle) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Server:  statement: " + sqle.getMessage());
            }

		}
		return exists;
    }
}