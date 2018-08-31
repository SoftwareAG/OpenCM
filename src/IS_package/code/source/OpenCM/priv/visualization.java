package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.util.LinkedList;
import java.util.Collections;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.Node;
import org.opencm.configuration.Instance;
import org.opencm.d3.Tree;
import org.opencm.d3.Child;
import org.opencm.util.FileUtils;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
import org.opencm.util.PackageUtils;
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
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========   Generating D3 visualizations... ========== ");
		
		// --------------------------------------------------------------------
		// Read in Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		
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
		overviewTree.setName("DBP");
		overviewTree.setLevel("DBP");
		LinkedList<String> opencmEnvs = nodes.getAllOpencmEnvironments();
		LinkedList<Child> envChildren = new LinkedList<Child>();
		for (int e = 0; e < opencmEnvs.size(); e++) {
			String env = opencmEnvs.get(e);
			Child envChild = new Child();
			envChild.setName(env);
			envChild.setLevel("ENVIRONMENT");
			
			LinkedList<String> assGroups = nodes.getAllAssertionGroupsForOpenCMEnvironment(env);
			// Sort the assertion groups alphabetically ascending
			Collections.sort(assGroups);
			LinkedList<Child> groupChildren = new LinkedList<Child>();
			for (int a = 0; a < assGroups.size(); a++) {
				String assGroup = assGroups.get(a);
				Child groupChild = new Child();
				groupChild.setName(assGroup);
				groupChild.setLevel("ASS_GROUP");
			
				LinkedList<String> servers = nodes.getAllServerNamesUnqualifiedByEnvAndGroup(env,assGroup);
				LinkedList<Child> serverChildren = new LinkedList<Child>();
				for (int s = 0; s < servers.size(); s++) {
					String server = servers.get(s);
					Child serverChild = new Child();
					serverChild.setName(server);
					serverChild.setLevel("SERVER");
					LinkedList<Node> opencmNodes = nodes.getNodesByOpencmEnvGroupAndServer(env,assGroup,server);
					LinkedList<Child> nodeChildren = new LinkedList<Child>();
					for (int n = 0; n < opencmNodes.size(); n++) {
						Node opencmNode = opencmNodes.get(n);
						// Ignore nodes without defined environments
						if (opencmNode.getOpencm_environment().equals("UNDEFINED")) {
							continue;
						}
						String nodeName = opencmNode.getNode_name();
						Child nodeChild = new Child();
						nodeChild.setName(nodeName);
						nodeChild.setLevel("NODE");
						nodeChild.setUrl("server=" + server + "&node=" + nodeName);
						nodeChild.setHasBaseline(false); 
						nodeChild.setHasRuntime(false); 
						
						Instance spmInstance = opencmNode.getInstance("SPM");
			
						// -------------------------------------
						// ------------ BASELINE ---------------
						// -------------------------------------
						File baselineNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_BASELINE_DIR + File.separator + serverChild.getName() + File.separator + nodeChild.getName());
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Inspecting Baseline: " + baselineNodeDir.getPath());
						Tree nodeTree = new Tree();
						nodeTree.setName(nodeName);
						nodeTree.setLevel("NODE");
						nodeTree.setAssertionGroup(opencmNode.getAssertion_group());
						nodeTree.setCceEnvironment(opencmNode.getCce_environment());
						nodeTree.setOpencmEnvironment(opencmNode.getOpencm_environment());
						nodeTree.setSpmURL(spmInstance.getProtocol() + "://" + opencmNode.getHostname() + ":" + spmInstance.getPort() + "/spm");
						if (baselineNodeDir.exists()) {
							nodeTree.setChildren(getComponents(baselineNodeDir));
							nodeChild.setHasBaseline(true);
						}
						// Convert Overview Tree to Json
						String json = JsonUtils.convertJavaObjectToJson(nodeTree);
						
						// Write to File (node directory)
						FileUtils.writeToFile(outputDirectory + File.separator + nodeName + "_BASELINE.json", json);
						
						// -------------------------------------
						// ------------  RUNTIME ---------------
						// -------------------------------------
						File runtimeNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR + File.separator + serverChild.getName() + File.separator + nodeChild.getName());
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_TRACE," Inspecting Runtime: " + runtimeNodeDir.getPath());
						nodeTree = new Tree();
						nodeTree.setName(nodeName);
						nodeTree.setLevel("NODE");
						nodeTree.setAssertionGroup(opencmNode.getAssertion_group());
						nodeTree.setCceEnvironment(opencmNode.getCce_environment());
						nodeTree.setOpencmEnvironment(opencmNode.getOpencm_environment());
						nodeTree.setSpmURL(spmInstance.getProtocol() + "://" + opencmNode.getHostname() + ":" + spmInstance.getPort() + "/spm");
						if (runtimeNodeDir.exists()) {
							nodeTree.setChildren(getComponents(runtimeNodeDir));
							nodeChild.setHasRuntime(true);
						} else {
							LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," No Runtime for: " + runtimeNodeDir.getPath());
						}
						
						// Convert Overview Tree to Json
						json = JsonUtils.convertJavaObjectToJson(nodeTree);
						
						// Write to File (node directory)
						FileUtils.writeToFile(outputDirectory + File.separator + nodeName + "_RUNTIME.json", json);
						
						nodeChildren.add(nodeChild);
					}
					serverChild.setChildren(nodeChildren);
					serverChildren.add(serverChild);
				}
				
				groupChild.setChildren(serverChildren);
				groupChildren.add(groupChild);
				
			}
			envChild.setChildren(groupChildren);
			envChildren.add(envChild);
		}
		overviewTree.setChildren(envChildren);
		
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

