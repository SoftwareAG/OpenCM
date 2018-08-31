package org.opencm.extract.spm;

import java.io.File;

import org.opencm.configuration.Configuration;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.Node;
import org.opencm.util.LogUtils;
import org.opencm.extract.configuration.ExtractProperties;
import org.opencm.extract.spm.SpmOps;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class StandAlone {

	public static void main(String[] args) {
		
		/*
		 * Extract Service 
		 */ 
		
		if ((args == null) || (args.length != 2)) {
			System.out.println("No OpenCM property file passed as argument... ");
			System.out.println("Usage:: org.opencm.extract.spm.main PROP_FILE MASTER_PASSWORD");
			return;
		}
		String opencmPropertyFile = args[0];
		String stMaster = args[1];
		
		// --------------------------------------------------------------------
		// Read in WxCM Properties
		// --------------------------------------------------------------------
		File opencmConfigFile = new File(opencmPropertyFile);
		if (!opencmConfigFile.exists()) {
			System.out.println("Unable to access OpenCM configuration file: " + opencmConfigFile.getPath());
		}
		Configuration opencmConfig = null;
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			opencmConfig = mapper.readValue(opencmConfigFile, Configuration.class);
			
		} catch (Exception e) {
			System.out.println("WxCM Configuration Exception: " + e.toString());
		}
		
		// --------------------------------------------------------------------
		// Read in Extract Properties
		// --------------------------------------------------------------------
		ExtractProperties extractProps = ExtractProperties.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Read in Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Ensure Encrypted passwords ...
		// --------------------------------------------------------------------
		nodes.ensureDecryptedPasswords(opencmConfig, stMaster);
		
		if ((extractProps.getNodes() == null) || extractProps.getNodes().isEmpty()) {
			System.out.println("Stand-alone extraction supports only specifically defined nodes to extract.");
			System.out.println("Add desired nodes in the nodes section of extract.properties");
			return;
		}
		
		// --------------------------------------------------------------------
		// Perform Snapshot extraction 
		// --------------------------------------------------------------------
		for (int i = 0; i < extractProps.getNodes().size(); i++) {
			String nodeName = extractProps.getNodes().get(i);
			Node wxcmNode = nodes.getNode(nodeName);
			if (wxcmNode == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"No WxCM Node defined for .. " + nodeName);
			} else {
				SpmOps spm = new SpmOps(opencmConfig, wxcmNode, stMaster);
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Extracting Node .. " + nodeName);
				spm.extractAll();
				spm.persist();
			}
		}
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Extraction Completed.");
		
	}
}
