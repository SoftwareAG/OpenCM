package org.opencm.util;

import java.io.File;
import org.opencm.configuration.PkgConfiguration;

public class PackageUtils {

	private static String PACKAGE_RESOURCES_DIR 		= "resources";
	private static String PACKAGE_TEMPLATE_DIR 			= "templates";
	
	public static String getPackageResourcePath() {
		return getPackagePath() + File.separator + PACKAGE_RESOURCES_DIR;
	}
	
	public static String getPackageTemplatePath() {
		return getPackagePath() + File.separator + PACKAGE_TEMPLATE_DIR;
	}
	
	public static String getPackagePath() {
		return getPackageConfigPath() + File.separator + "..";
	}
	
	public static String getPackageConfigPath() {
		return com.wm.app.b2b.server.ServerAPI.getPackageConfigDir(PkgConfiguration.OPENCM_PACKAGE_NAME).getPath();
	}
}

