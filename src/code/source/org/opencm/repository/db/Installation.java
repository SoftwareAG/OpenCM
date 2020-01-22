package org.opencm.repository.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.ResultSet;

import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;

public class Installation {
	
	public static String CREATE_TABLE		= 	"CREATE TABLE INSTALLATION (" + 
												"  SERVER_NAME	VARCHAR(128)	NOT NULL" + 
												")";
	public static String CREATE_PRIMARY_KEY	= 	"ALTER TABLE INSTALLATION " + 
												"  ADD CONSTRAINT INSTALLATION_PK PRIMARY KEY (INSTALLATION_NAME)";
	
	public static String SELECT_ALL			= 	"SELECT INSTALLATION_NAME FROM INSTALLATION";
	

    private String name;
    private LinkedList<Component> components;

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Component> getComponents() {
        return this.components;
    }
    public void setComponents(LinkedList<Component> comps) {
        this.components = comps;
    }

    public static boolean create(Configuration opencmConfig, Connection conn) {
		try {
			Statement s = conn.createStatement();
			s.executeUpdate(CREATE_TABLE);
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_INFO,"org.opencm.repository.db.Installation: create - Schemas created successfully.");
		} catch (SQLException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Installation: create Exception: " + ex.getMessage());
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
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Installation: selectAll Exception: " + ex.getMessage());
			return false;
		} finally {
			
	        // Close objects
	        try {
	            if (rs != null) {
	                rs.close();
	                rs = null;
	            }
	        } catch (SQLException sqle) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Installation: resultSet: " + sqle.getMessage());
	        }

            try {
                if (statement != null) {
                	statement.close();
                	statement = null;
                }
            } catch (SQLException sqle) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Installation:  statement: " + sqle.getMessage());
            }

		}
		return exists;
    }
}