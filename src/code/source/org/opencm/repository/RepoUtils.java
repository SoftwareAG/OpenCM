package org.opencm.repository;

import java.io.File;
// import java.util.HashMap;
// import java.util.Iterator;
import java.util.LinkedList;
// import java.util.StringTokenizer;

/*
import org.opencm.audit.assertion.AssertionStore;
import org.opencm.audit.configuration.Property;
import org.opencm.audit.configuration.PropertyFilter;
import org.opencm.audit.env.AssertionConfig;
import org.opencm.audit.env.AssertionEnvironment;
import org.opencm.audit.env.AssertionGroup;
import org.opencm.audit.env.AssertionProperty;
import org.opencm.audit.env.AssertionValue;
import org.opencm.audit.env.AssertionValuePair;
*/

// import org.opencm.audit.util.JsonParser;
import org.opencm.configuration.Configuration;
import org.opencm.inventory.*;
import org.opencm.extract.spm.SpmOps;
// import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;

public class RepoUtils {
	
	
	public static LinkedList<String> getFixList(Configuration opencmConfig, Installation opencmNode) {
		LinkedList<String> fixList = new LinkedList<String>();
		File fixesDirectory = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR + File.separator + opencmNode.getName() + File.separator + SpmOps.SPM_COMP_FIXES);
		if (!fixesDirectory.exists()) {
			return fixList;
		}
		
		try {
			File[] fixDirs = fixesDirectory.listFiles();
			for (int i = 0; i < fixDirs.length; i++) {
				File fixDir = fixDirs[i];
				if (fixDir.isDirectory()) {
					File jsonFixInfo = new File(fixDir.getPath() + File.separator + SpmOps.SPM_CONF_FILENAME);
					if (jsonFixInfo.exists()) {
						String fixName = JsonUtils.getJsonValue(jsonFixInfo,"/id");
						String fixVersion = JsonUtils.getJsonValue(jsonFixInfo,"/version");
						fixList.add(fixName + "_" + fixVersion);
					}
				}
				
			}
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"RepoUtils :: getFixList :: " + ex.getMessage());
		}

		return fixList;
	}
	
	public static String getInstallDir(Configuration opencmConfig, Installation opencmNode) {
		File spmDirectory = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR + File.separator + opencmNode.getName() + File.separator + SpmOps.SPM_COMP_NAME);
		if (!spmDirectory.exists()) {
			return null;
		}
		
		File jsonSpmInstallInfo = new File(spmDirectory.getPath() + File.separator + SpmOps.SPM_INST_INSTALL_DIR + File.separator + SpmOps.SPM_CONF_FILENAME);
		if (!jsonSpmInstallInfo.exists()) {
			return null;
		}
		
		try {
			return JsonUtils.getJsonValue(jsonSpmInstallInfo,"/plain-text-property");
		} catch (Exception ex) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"RepoUtils :: getInstallDir :: " + ex.getMessage());
		}

		return null;
	}
	
}
