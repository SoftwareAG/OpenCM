package org.opencm.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.util.LinkedList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
// --- <<IS-END-IMPORTS>> ---

public final class manual

{
	// ---( internal utility methods )---

	final static manual _instance = new manual();

	static manual _newInstance() { return new manual(); }

	static manual _cast(Object o) { return (manual)o; }

	// ---( server methods )---




	public static final void arrange (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(arrange)>> ---
		// @sigtype java 3.5
		String IDENTIFIER_PRODUCTS 	= "NODE-PRODUCTS";
		String IDENTIFIER_FIXES 	= "NODE-FIXES";
		String IDENTIFIER_SYSPROPS 	= "COMMON-SYSPROPS";
		String IDENTIFIER_RESOURCES = "IS-RESOURCES";
		String props_dir = "C:\\Users\\hhansson\\Documents\\SoftwareAG\\Working\\projects\\Vodafone\\instReview\\properties";
		
		// --------------------------------------------------------------------
		// Read in Default Package Properties 
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		
		String runtimeDir = opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR;
		
		System.out.println("OpenCM [INFO] : Arrange Json manually: starting process... ");
		
		LinkedList<String> nodeDirs = FileUtils.getSubDirectories(props_dir, "*");
		for (int i = 0; i < nodeDirs.size(); i++) {
			try {
				String node = nodeDirs.get(i);
				System.out.println("OpenCM [INFO] : Processing Node: " + node);
				
				// ------------------------ 
				// Create Node Dir
				// ------------------------ 
				FileUtils.createDirectory(runtimeDir + File.separator + node);
				// ------------------------ 
				// Products
				// ------------------------ 
				File file_products = new File(props_dir + File.separator + node + File.separator + IDENTIFIER_PRODUCTS + ".json");
				if (file_products.exists()) {
					System.out.println("OpenCM [INFO] :   Processing Products ... ");
					FileUtils.createDirectory(runtimeDir + File.separator + node + File.separator + IDENTIFIER_PRODUCTS);
					// Scan the products
					ObjectMapper mapper = new ObjectMapper();
					JsonNode jsonProducts = mapper.readTree(file_products);
			    	ArrayNode arrayNode = (ArrayNode) jsonProducts.at("/products/product");
					for (int a = 0; a < arrayNode.size(); a++) {
						JsonNode jsonProduct = arrayNode.get(a);
						String productName = jsonProduct.at("/id").asText();
						String productDir = runtimeDir + File.separator + node + File.separator + IDENTIFIER_PRODUCTS + File.separator + productName;
						FileUtils.createDirectory(productDir);
						FileUtils.writeToFile(productDir + File.separator + "ci-properties.json",jsonProduct.toString());
					}
				}
				// ------------------------ 
				// Fixes
				// ------------------------ 
				File file_fixes = new File(props_dir + File.separator + node + File.separator + IDENTIFIER_FIXES + ".json");
				if (file_fixes.exists()) {
					System.out.println("OpenCM [INFO] :   Processing Fixes ... ");
					FileUtils.createDirectory(runtimeDir + File.separator + node + File.separator + IDENTIFIER_FIXES);
					// Scan the fixes
					ObjectMapper mapper = new ObjectMapper();
					JsonNode jsonFixes = mapper.readTree(file_fixes);
			    	ArrayNode arrayNode = (ArrayNode) jsonFixes.at("/fixes/fix");
					for (int a = 0; a < arrayNode.size(); a++) {
						JsonNode jsonFix = arrayNode.get(a);
						String fixName = jsonFix.at("/id").asText();
						String fixDir = runtimeDir + File.separator + node + File.separator + IDENTIFIER_FIXES + File.separator + fixName;
						FileUtils.createDirectory(fixDir);
						FileUtils.writeToFile(fixDir + File.separator + "ci-properties.json",jsonFix.toString());
					}
				}
				
				// ------------------------------
				// Integration Server instances
				// ------------------------------
				LinkedList<String> instDirs = FileUtils.getSubDirectories(props_dir + File.separator + node, "integrationServer-*");
				for (int x = 0; x < instDirs.size(); x++) {
					String isInstance = instDirs.get(x);
					System.out.println("OpenCM [INFO] :   Processing instance: " + isInstance);
					String isInstDir = props_dir + File.separator + node + File.separator + isInstance;
					
					// ------------------------ 
					// Resources
					// ------------------------ 
					String isDir = runtimeDir + File.separator + node + File.separator + isInstance;
					File file_resources = new File(isInstDir + File.separator + IDENTIFIER_RESOURCES + ".json");
					if (file_resources.exists()) {
						System.out.println("OpenCM [INFO] :   Processing Resources ... ");
						FileUtils.createDirectory(isDir);
						String resDir = isDir + File.separator + IDENTIFIER_RESOURCES;
						FileUtils.createDirectory(resDir);
						org.apache.commons.io.FileUtils.copyFile(file_resources,new File(resDir + File.separator + "ci-properties.json"));
					}
					// ------------------------ 
					// Ext Settings
					// ------------------------ 
					File file_sysprops = new File(isInstDir + File.separator + IDENTIFIER_SYSPROPS + ".json");
					if (file_sysprops.exists()) {
						System.out.println("OpenCM [INFO] :   Processing Extended Settings ... ");
						File fISDir = new File(isDir);
						if (!fISDir.exists()) {
							FileUtils.createDirectory(isDir);
						}
						String syspropsDir = isDir + File.separator + IDENTIFIER_SYSPROPS;
						FileUtils.createDirectory(syspropsDir);
						org.apache.commons.io.FileUtils.copyFile(file_sysprops, new File(syspropsDir + File.separator + "ci-properties.json"));
					}
				}
			} catch (Exception e) {
				throw new ServiceException("OpenCM [CRITICAL] : Arrange Json manually: " + e.toString());
			}
		}
			
		// --- <<IS-END>> ---

                
	}
}

