package org.opencm.repository.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.ResultSet;

import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;

public class Organisation {
	
	public static String CREATE_TABLE		= 	"CREATE TABLE ORGANISATION (" + 
												"  ORGANISATION_NAME	VARCHAR(128)	NOT NULL" + 
												")";
	public static String CREATE_PRIMARY_KEY	= 	"ALTER TABLE ORGANISATION " + 
												"  ADD CONSTRAINT ORGANISATION_PK PRIMARY KEY (ORGANISATION_NAME)";
	
	public static String SELECT_ALL			= 	"SELECT ORGANISATION_NAME FROM ORGANSIATION";

	
    private String name;
    private LinkedList<Department> departments;

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Department> getDepartments() {
        return this.departments;
    }
    public void setDepartments(LinkedList<Department> deps) {
        this.departments = deps;
    }
	
	
    public static boolean create(Configuration opencmConfig, Connection conn) {
		try {
			Statement s = conn.createStatement();
			s.executeUpdate(CREATE_TABLE);
			LogUtils.log(Configuration.OPENCM_LOG_INFO,Configuration.OPENCM_LOG_INFO,"org.opencm.repository.db.Organisation: create - Schemas created successfully.");
		} catch (SQLException ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Organisation: create Exception: " + ex.getMessage());
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
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Organisation: selectAll Exception: " + ex.getMessage());
			return false;
		} finally {
			
	        // Close objects
	        try {
	            if (rs != null) {
	                rs.close();
	                rs = null;
	            }
	        } catch (SQLException sqle) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Organisation: resultSet: " + sqle.getMessage());
	        }

            try {
                if (statement != null) {
                	statement.close();
                	statement = null;
                }
            } catch (SQLException sqle) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"org.opencm.repository.db.Organisation:  statement: " + sqle.getMessage());
            }

		}
		return exists;
    }
}