package org.opencm.util;

import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;

public class LogUtils {

	public static void log(String configLevel, String msgSeverity, String msg) {
		boolean printMsg = false;
		
		if (configLevel.equals(Configuration.OPENCM_LOG_CRITICAL)) {
			if (msgSeverity.equals(Configuration.OPENCM_LOG_CRITICAL)) {
				printMsg = true;
			}
		} else if (configLevel.equals(Configuration.OPENCM_LOG_ERROR)) {
			if (msgSeverity.equals(Configuration.OPENCM_LOG_CRITICAL) || msgSeverity.equals(Configuration.OPENCM_LOG_ERROR)) {
				printMsg = true;
			}
		} else if (configLevel.equals(Configuration.OPENCM_LOG_WARNING)) {
			if (msgSeverity.equals(Configuration.OPENCM_LOG_CRITICAL) || msgSeverity.equals(Configuration.OPENCM_LOG_ERROR) || msgSeverity.equals(Configuration.OPENCM_LOG_WARNING)) {
				printMsg = true;
			}
		} else if (configLevel.equals(Configuration.OPENCM_LOG_INFO)) {
			if (msgSeverity.equals(Configuration.OPENCM_LOG_CRITICAL) || msgSeverity.equals(Configuration.OPENCM_LOG_ERROR) || msgSeverity.equals(Configuration.OPENCM_LOG_WARNING) || msgSeverity.equals(Configuration.OPENCM_LOG_INFO)) {
				printMsg = true;
			}
		} else if (configLevel.equals(Configuration.OPENCM_LOG_DEBUG)) {
			if (msgSeverity.equals(Configuration.OPENCM_LOG_CRITICAL) || msgSeverity.equals(Configuration.OPENCM_LOG_ERROR) || msgSeverity.equals(Configuration.OPENCM_LOG_WARNING) || msgSeverity.equals(Configuration.OPENCM_LOG_INFO) || msgSeverity.equals(Configuration.OPENCM_LOG_DEBUG)) {
				printMsg = true;
			}
		} else if (configLevel.equals(Configuration.OPENCM_LOG_TRACE)) {
			printMsg = true;
		}
		if (printMsg) {
			System.out.println(PkgConfiguration.OPENCM_PACKAGE_NAME + " [" + msgSeverity + "] :: " + msg);
		}
	}
}