package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.inventory.*;
import org.opencm.d3.Tree;
import org.opencm.d3.Child;
import org.opencm.util.Cache;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
import org.opencm.util.PackageUtils;
import org.opencm.security.KeyUtils;
// --- <<IS-END-IMPORTS>> ---

public final class visualization

{
	// ---( internal utility methods )---

	final static visualization _instance = new visualization();

	static visualization _newInstance() { return new visualization(); }

	static visualization _cast(Object o) { return (visualization)o; }

	// ---( server methods )---




	public static final void generate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(generate)>> ---
		// @sigtype java 3.5
		// --------------------------------------------------------------------
		// Read in Default Package Properties 
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate(); 
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		
		// --------------------------------------------------------------------
		// Ensure that master password is stored in cache
		// --------------------------------------------------------------------
		if (KeyUtils.getMasterPassword() == null) {
			try {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO," vizualization:generate : Master Pwd NULL - running startup service ... ");
				Service.doInvoke(com.wm.lang.ns.NSName.create("OpenCM.pub.startup", "startup"), IDataFactory.create());
			} catch (Exception ex) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"OpenCM vizualization:generate :: " + ex.getMessage());
			}
		}
		
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Cache.getInstance().set(org.opencm.inventory.Inventory.INVENTORY_CACHE_KEY, null);
		Inventory inv = Inventory.instantiate(opencmConfig);
		 
		// --------------------------------------------------------------------
		// Where to store the dnd tree output
		// --------------------------------------------------------------------
		String outputDirectory = new File(PackageUtils.getPackageConfigPath()).getParent() + File.separator + "pub" + File.separator + Configuration.OPENCM_RESULTS_DIR_DNDTREE;
		File fDir = new File(outputDirectory);
		if (!fDir.exists()) {
			fDir.mkdir();
		}
		
		// --------------------------------------------------------------------
		// Generate Tree structure based on defined nodes and presence of baseline/runtime directories
		// --------------------------------------------------------------------
		Tree overviewTree = new Tree();
		overviewTree.setName("Integration Platform");
		overviewTree.setLevel("ROOT");
		
		// --------------------------------------------------------------------
		// Create a subset of the inventory to include based on the tree config
		// --------------------------------------------------------------------
		Inventory filteredInv = inv.createInventory(opencmConfig, opencmConfig.getInventory_tree());
		
		// --------------------------------------------------------------------
		// Construct the tree data
		// --------------------------------------------------------------------
		LinkedList<Organisation> orgs = filteredInv.getInventory();
		LinkedList<Child> orgChildren = new LinkedList<Child>();
		for (int o = 0; o < orgs.size(); o++) {
			// Organisation
			Organisation org = orgs.get(o);
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Visualization: Processing org : " + org.getName());
			Child orgChild = new Child();
			orgChild.setName(org.getName());
			orgChild.setLevel("ORG");
			LinkedList<Department> deps = org.getDepartments();
			LinkedList<Child> depChildren = new LinkedList<Child>();
			for (int d = 0; d < deps.size(); d++) {
				// Department
				Department dep = deps.get(d);
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Visualization: Processing department : " + dep.getName());
				Child depChild = new Child();
				depChild.setName(dep.getName());
				depChild.setLevel("DEP");
				
				LinkedList<String> envs = dep.getEnvironments();
				LinkedList<Child> envChildren = new LinkedList<Child>();
				for (int e = 0; e < envs.size(); e++) {
					// Environment
					String env = envs.get(e);
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Visualization: Processing environment : " + env);
					Child envChild = new Child();
					envChild.setName(env);
					envChild.setLevel("ENV");
					LinkedList<String> layers = dep.getLayersByEnv(env);
					LinkedList<Child> layerChildren = new LinkedList<Child>();
					for (int l = 0; l < layers.size(); l++) {
						// Layer
						String layer = layers.get(l);
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Visualization: Processing layer : " + layer);
						Child layerChild = new Child();
						layerChild.setName(layer);
						layerChild.setLevel("LAYER");
						LinkedList<Server> servers = dep.getServers(env, layer);
						LinkedList<Child> serverChildren = new LinkedList<Child>();
						for (int s = 0; s < servers.size(); s++) {
							// Server
							Server server = servers.get(s);
							LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Visualization: Processing server : " + server.getName());
							Child serverChild = new Child();
							serverChild.setName(server.getUnqualifiedHostname());
							serverChild.setLevel("SERVER");
							LinkedList<Installation> installations = server.getInstallations(env, layer, null);
							LinkedList<Child> nodeChildren = new LinkedList<Child>();
							for (int i = 0; i < installations.size(); i++) {
								// Installation
								Installation installation = installations.get(i);
								LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Visualization: Processing Installation: " + installation.getName());
								Child nodeChild = new Child();
								nodeChild.setName(installation.getName());
								nodeChild.setLevel("NODE");
								nodeChild.setUrl("node=" + installation.getName());
								nodeChild.setHasBaseline(false); 
								nodeChild.setHasRuntime(false); 
								
								RuntimeComponent spmRC = installation.getRuntimeComponent(RuntimeComponent.RUNTIME_COMPONENT_NAME_SPM);
					
								// -------------------------------------
								// ------------ BASELINE ---------------
								// -------------------------------------
								File baselineNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_BASELINE_DIR + File.separator + nodeChild.getName());
								LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Inspecting Baseline: " + baselineNodeDir.getPath());
								Tree nodeTree = new Tree();
								nodeTree.setName(installation.getName());
								nodeTree.setLevel("NODE");
								nodeTree.setLayer(installation.getLayer());
								nodeTree.setEnvironment(installation.getEnvironment());
								if (spmRC != null) {
									nodeTree.setSpmURL(spmRC.getProtocol() + "://" + server.getName() + ":" + spmRC.getPort() + "/spm");
								}
								if (baselineNodeDir.exists()) {
									nodeTree.setChildren(getComponents(baselineNodeDir));
									nodeChild.setHasBaseline(true);
								}
								// Convert Overview Tree to Json
								String json = JsonUtils.convertJavaObjectToJson(nodeTree);
								
								// Write to File (node directory)
								FileUtils.writeToFile(outputDirectory + File.separator + installation.getName() + "_BASELINE.json", json);
								
								// -------------------------------------
								// ------------  RUNTIME ---------------
								// -------------------------------------
								File runtimeNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR + File.separator + nodeChild.getName());
								LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Inspecting Runtime: " + runtimeNodeDir.getPath());
								nodeTree = new Tree();
								nodeTree.setName(installation.getName());
								nodeTree.setLevel("NODE");
								nodeTree.setLayer(installation.getLayer());
								nodeTree.setEnvironment(installation.getEnvironment());
								if (spmRC != null) {
									nodeTree.setSpmURL(spmRC.getProtocol() + "://" + server.getName() + ":" + spmRC.getPort() + "/spm");
								}
								if (runtimeNodeDir.exists()) {
									nodeTree.setChildren(getComponents(runtimeNodeDir));
									nodeChild.setHasRuntime(true);
								} else {
									LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," No Runtime for: " + runtimeNodeDir.getPath());
								}
								
								// Convert Overview Tree to Json
								json = JsonUtils.convertJavaObjectToJson(nodeTree);
								
								// Write to File (node directory)
								FileUtils.writeToFile(outputDirectory + File.separator + installation.getName() + "_RUNTIME.json", json);
								
								nodeChildren.add(nodeChild);
							}
							serverChild.setChildren(nodeChildren);
							serverChildren.add(serverChild);
						}
						layerChild.setChildren(serverChildren);
						layerChildren.add(layerChild);
					}
					envChild.setChildren(layerChildren);
					envChildren.add(envChild);
				}
				depChild.setChildren(envChildren);
				depChildren.add(depChild);
			}				
			orgChild.setChildren(depChildren);
			orgChildren.add(orgChild);
		}
		overviewTree.setChildren(orgChildren);
		
		// Convert Overview Tree to Json
		String json = JsonUtils.convertJavaObjectToJson(overviewTree);
		
		// Write to File 
		FileUtils.writeToFile(outputDirectory + File.separator + "overview.json", json);
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   D3 visualizations Completed ... ========== ");
			
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static LinkedList<Child> getComponents(File cmDirectory) {
		// -------- Components ----------
		File[] components = cmDirectory.listFiles();
		LinkedList<Child> componentChildren = new LinkedList<Child>();
		for (int c = 0; c < components.length; c++) {
			File componentDir = components[c];
			if (componentDir.isDirectory()) {
				String componentName = componentDir.getName(); 
				Child componentChild = new Child();
				componentChild.setName(componentName);
				componentChild.setLevel("COMPONENT");
				// -------- Instances ----------
				File[] instances = componentDir.listFiles();
				LinkedList<Child> instanceChildren = new LinkedList<Child>();
				for (int i = 0; i < instances.length; i++) {
					File instanceDir = instances[i];
					if (instanceDir.isDirectory()) {
						String instanceName = instanceDir.getName(); 
						Child instanceChild = new Child();
						instanceChild.setName(instanceName);
						instanceChild.setLevel("INSTANCE");
						instanceChildren.add(instanceChild);
					}
				}
				componentChild.setChildren(instanceChildren);
				componentChildren.add(componentChild);
			}
		}
		return componentChildren;
	
	}
	// --- <<IS-END-SHARED>> ---
}

