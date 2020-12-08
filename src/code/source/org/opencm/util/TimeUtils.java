package org.opencm.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

	public static String formatDate01(String inString) {
		// Informat: "2020-03-13T13:54:17.877+01:00"
		try {
			DateTimeFormatter inFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
			LocalDateTime date = LocalDateTime.parse(inString,inFormat);
			DateTimeFormatter outFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			return date.format(outFormat);
		} catch (Exception ex) {
			LogUtils.logDebug("TimeUtils :: Exception in formatDate: " + ex.getMessage());
		}
		
		// Try alternative pattern...
		try {
			DateTimeFormatter inFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
			LocalDateTime date = LocalDateTime.parse(inString,inFormat);
			DateTimeFormatter outFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			return date.format(outFormat);
		} catch (Exception ex) {
			LogUtils.logDebug("TimeUtils :: Exception in formatDate: " + ex.getMessage());
		}
		return inString;
	}
	
	public static String getDateTimeFileFormat() {
		String stDateTime = "";
		try {
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter outFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'_'HH-mm-ss");
			stDateTime = date.format(outFormat);
		} catch (Exception ex) {
			LogUtils.logError("TimeUtils :: Exception in getCurrentDateTime: " + ex.getMessage());
		}
		return stDateTime;
	}
		
}
