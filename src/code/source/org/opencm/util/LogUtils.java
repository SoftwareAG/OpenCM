package org.opencm.util;

import java.io.File;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.opencm.configuration.Configuration;

public class LogUtils {

	public static final File LOGGING_CONFIG_DIRECTORY 	= new File(Configuration.getRootDirectory() + File.separator + Configuration.OPENCM_DIR_CONFIG + File.separator + Configuration.OPENCM_DIR_LOG_CONFIG);
	public static final File LOGGING_CONFIGURATION_FILE	= new File(LOGGING_CONFIG_DIRECTORY.getPath() + File.separator + "log4j2.json");
	
	private static final Logger logger 		= LogManager.getLogger(LogUtils.class);
	
    public static String LOG_CRITICAL 		= "CRITICAL";
    public static String LOG_ERROR 			= "ERROR";
    public static String LOG_WARNING 		= "WARNING";
    public static String LOG_INFO 			= "INFO";
    public static String LOG_DEBUG 			= "DEBUG";
    public static String LOG_TRACE 			= "TRACE";

	public static void logFatal(String msg) {
		logger.fatal(msg);
	}
	public static void logError(String msg) {
		logger.error(msg);
	}
	public static void logWarning(String msg) {
		logger.warn(msg);
	}
	public static void logInfo(String msg) {
		logger.info(msg);
	}
	public static void logDebug(String msg) {
		logger.debug(msg);
	}
	public static void logTrace(String msg) {
		logger.trace(msg);
	}
	
	public static void loadConfiguration() {
		if (!LOGGING_CONFIGURATION_FILE.exists()) {
	    	System.out.println("Logging Configuration not found: " + LOGGING_CONFIGURATION_FILE.getPath());
	    	return;
		}

		LogManager.getContext(LogUtils.class.getClassLoader(),false,LOGGING_CONFIGURATION_FILE.toURI());
		
	}

}