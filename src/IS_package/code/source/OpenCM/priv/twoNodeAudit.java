package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.ws.codegen.PackageUtil;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.extract.spm.SpmOps;
import org.opencm.configuration.Configuration;
import org.opencm.util.LogUtils;
import org.opencm.util.PackageUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencm.audit.assertion.AssertionStore;
import org.opencm.audit.assertion.TestWrapper;
import org.opencm.audit.assertion.TwoNodeTest;
import org.opencm.audit.configuration.AuditConfiguration;
import org.opencm.audit.configuration.AuditNodesConfiguration;
import org.opencm.audit.configuration.Property;
import org.opencm.audit.env.AssertionValue;
import org.opencm.audit.env.AssertionValuePair;
import org.opencm.audit.util.RepoParser;
import org.opencm.audit.configuration.AuditNodePair;
import org.opencm.audit.configuration.AuditNode;
import org.opencm.configuration.Nodes;
import org.opencm.configuration.Node;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.XmlClass;
// --- <<IS-END-IMPORTS>> ---

public final class twoNodeAudit

{
	// ---( internal utility methods )---

	final static twoNodeAudit _instance = new twoNodeAudit();

	static twoNodeAudit _newInstance() { return new twoNodeAudit(); }

	static twoNodeAudit _cast(Object o) { return (twoNodeAudit)o; }

	// ---( server methods )---




	public static final void auditBaseline (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(auditBaseline)>> ---
		// @sigtype java 3.5
		// [i] field:0:required node
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	node = IDataUtil.getString( pipelineCursor, "node" );
		pipelineCursor.destroy();
		
		if (node == null) { 
			throw new ServiceException("Missing Node Parameter");
		}
		
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		 
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== 2-Node Baseline vs Runtime Audit - Starting Process ..... ============ ");
		
		// --------------------------------------------------------------------
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Either a full 2-node audit or a single node audit
		// --------------------------------------------------------------------
		LinkedList<Node> validNodes = new LinkedList<Node>();
		if (node.equals("ALL")) {
			LinkedList<Node> openCMNodes = nodes.getNodes();
			for (int i = 0; i < openCMNodes.size(); i++) { 
				Node openCMNode = openCMNodes.get(i);
				// Ensure we have both baseline and runtime available...
				File baselineNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_BASELINE_DIR + File.separator + openCMNode.getUnqualifiedHostname() + File.separator + openCMNode.getNode_name());
				File runtimeNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR + File.separator + openCMNode.getUnqualifiedHostname() + File.separator + openCMNode.getNode_name());
				if (baselineNodeDir.exists() && runtimeNodeDir.exists()) {
					validNodes.add(openCMNode);
				}
			}
		} else {
			// Single node 
			Node openCMNode = nodes.getNode(node);
			// Ensure we have both baseline and runtime available...
			File baselineNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_BASELINE_DIR + File.separator + openCMNode.getUnqualifiedHostname() + File.separator + openCMNode.getNode_name());
			File runtimeNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_RUNTIME_DIR + File.separator + openCMNode.getUnqualifiedHostname() + File.separator + openCMNode.getNode_name());
			if (baselineNodeDir.exists() && runtimeNodeDir.exists()) {
				validNodes.add(openCMNode);
			}
		}
		
		LinkedList<AuditNodePair> llPairs = new LinkedList<AuditNodePair>();
		for (int i = 0; i < validNodes.size(); i++) { 
			Node openCMNode = validNodes.get(i);
			// --------------------------------------------------------------------
			// Set 2-Node Assertion Pairs
			// --------------------------------------------------------------------
			AuditNode node01 = new AuditNode();
			node01.setEnvironment(openCMNode.getOpencm_environment());
			node01.setHostname(openCMNode.getHostname());
			node01.setNodeAlias(openCMNode.getNode_name());
			node01.setRepoType(Configuration.OPENCM_BASELINE_DIR);
			
			AuditNode node02 = new AuditNode();
			node02.setEnvironment(openCMNode.getOpencm_environment());
			node02.setHostname(openCMNode.getHostname());
			node02.setNodeAlias(openCMNode.getNode_name());
			node02.setRepoType(Configuration.OPENCM_RUNTIME_DIR);
			
			AuditNodePair nodepair = new AuditNodePair();
			nodepair.setNode_01(node01);
			nodepair.setNode_02(node02);
			
			llPairs.add(nodepair);
			
		}
		
		AuditNodesConfiguration nodesProps = new AuditNodesConfiguration();
		nodesProps.setNodes(llPairs);
		
		// --------------------------------------------------------------------
		// Read in Audit Properties
		// --------------------------------------------------------------------
		// AuditConfiguration auditProps = AuditConfiguration.instantiate(opencmConfig, Configuration.OPENCM_AUDIT_BASELINE_RUNTIME_NODE_PROPS);
		AuditConfiguration auditProps = new AuditConfiguration();
		
		auditProps.setTestName("OpenCM 2-Node Audit - Baseline vs Runtime");
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Performing Audit Assertions based on " + Configuration.OPENCM_AUDIT_BASELINE_RUNTIME_NODE_PROPS);
		
		// --------------------------------------------------------------------
		// Initiate Test Objects
		// --------------------------------------------------------------------
		TestNG testng = new TestNG();
		testng.setUseDefaultListeners(false);
		List<Class<? extends ITestNGListener>> listenerClasses = new java.util.ArrayList<>();
		listenerClasses.add(org.uncommons.reportng.HTMLReporter.class);
		listenerClasses.add(org.uncommons.reportng.JUnitXMLReporter.class);
		testng.setListenerClasses(listenerClasses);
		testng.setOutputDirectory(opencmConfig.getOutput_dir());
		System.setProperty("org.uncommons.reportng.stylesheet",PackageUtils.getPackageTemplatePath() + File.separator + "opencm_report.css");
		System.setProperty("org.uncommons.reportng.failures-only","true");
		System.setProperty("org.uncommons.reportng.title",auditProps.getTestName());
		
		// --------------------------------------------------------------------
		// Create Suites and Central Store 
		// --------------------------------------------------------------------
		AssertionStore store = new AssertionStore(opencmConfig);
		ArrayList<XmlSuite> opencmTestSuites = new ArrayList<XmlSuite>();
		XmlSuite propertySuite = new XmlSuite(); 
		propertySuite.setName(auditProps.getTestName());
		
		// ------------------------------------------------------
		// Construct the property definitions 
		// ------------------------------------------------------
		Property refProps = new Property();
		refProps.setKey("/");
		
		// Process the list of Nodes to Audit
		LinkedList<AuditNodePair> auditNodesList = nodesProps.getNodes(); 
		
		for (int i = 0; i < auditNodesList.size(); i++) { 
			AuditNodePair nodePair = auditNodesList.get(i);
			// ----------------------------------------------
			// Get OpenCM definitions for these nodes
			// ----------------------------------------------
			Node opencmBaselineNode = nodes.getNode(nodePair.getNode_01().getNodeAlias());
			if (opencmBaselineNode == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING,"Assertion skipped: node is not defined: " + nodePair.getNode_01().getNodeAlias());
				continue;
			}
			Node opencmRuntimeNode = opencmBaselineNode.getCopy();
		
			// ----------------------------------------------
			// Set Repository types
			// ----------------------------------------------
			opencmRuntimeNode.setRepositoryType(nodePair.getNode_02().getRepoType());
			opencmBaselineNode.setRepositoryType(nodePair.getNode_01().getRepoType());
			
			// ------------------------------------------------------
			// For each nodePair - Main Processing...
			// ------------------------------------------------------
			File baselineNodeDir = new File(opencmConfig.getCmdata_root() + File.separator + Configuration.OPENCM_BASELINE_DIR + File.separator + opencmBaselineNode.getUnqualifiedHostname() + File.separator + opencmBaselineNode.getNode_name());
			File [] fBaselineCompDirectories = baselineNodeDir.listFiles();
			for (File baselineCompPath:fBaselineCompDirectories) {
				if (baselineCompPath.isDirectory()) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, "  -- Processing Component " + baselineCompPath.getName());
					XmlSuite componentSuite = new XmlSuite(); 
					componentSuite.setName(opencmBaselineNode.getNode_name() + " -> " + baselineCompPath.getName());
					File [] fBaselineInstDirectories = baselineCompPath.listFiles();
					for (File baselineInstPath:fBaselineInstDirectories) {
						if (baselineInstPath.isDirectory()) {
							refProps.setInstance(baselineInstPath.getName());
							LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, "    -- Processing Instance " + baselineInstPath.getName());
							// --------------------------------------------------------------------
						    // Get Assertion Values for Baseline
							// --------------------------------------------------------------------
							refProps.setComponent(baselineCompPath.getName()); // Always the actual component name (not normalized)
						    LinkedList<AssertionValue> avsBaseline = RepoParser.getAssertionValues(opencmConfig, opencmBaselineNode, refProps, auditProps.getPropertyFilters());
							if (avsBaseline.size() == 0) {
								continue;
							}
							
							// --------------------------------------------------------------------
						    // Get Assertion Values for Runtime
							// --------------------------------------------------------------------
						    LinkedList<AssertionValue> avsRuntime = RepoParser.getAssertionValues(opencmConfig, opencmRuntimeNode, refProps, auditProps.getPropertyFilters());
							if (avsRuntime.size() == 0) {
								continue;
							}
							LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, "      -- Runtime Instance Size: " + avsRuntime.size());
							
							// --------------------------------------------------------------------
						    // Generate value pairs and put into store
							// --------------------------------------------------------------------
							LinkedList<AssertionValuePair> avsPairs = RepoParser.createAssertionValuePairs(avsBaseline,avsRuntime,false,false); // Do not ignore anything
							if (avsPairs.size() == 0) {
								continue;
							}
							LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, "      -- AVS Pairs Size: " + avsPairs.size());
							
							String twHashMapKey = refProps.getComponent() + "_" + refProps.getInstance();
							TestWrapper tw = new TestWrapper(twHashMapKey,avsPairs);
							store.setTestWrapper(tw);
							 
							// --------------------------------------------------------------------
							// Create Property Test (one per instance)
							// --------------------------------------------------------------------
						    XmlTest propertyTest = new XmlTest();
						    propertyTest.setName(refProps.getInstance());
						    propertyTest.setSuite(componentSuite);
						    propertyTest.addParameter(AssertionStore.ASSERTION_PARAMETER_TESTKEY, twHashMapKey); // For the test class to pick up
						    
						    ArrayList<XmlClass> propTest_classes = new ArrayList<XmlClass>();
						    propTest_classes.add(new XmlClass(TwoNodeTest.class));
						     
						    propertyTest.getClasses().addAll(propTest_classes);
						    componentSuite.addTest(propertyTest);
						}
					}
					if (componentSuite.getTests().size() > 0) {
						opencmTestSuites.add(componentSuite);
					}
				}
		
			}
		
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, " -- Tests Defined: " + propertySuite.getTests().size());
		if (propertySuite.getTests().size() > 0) {
			opencmTestSuites.add(propertySuite);
		}
		// --------------------------------------------------------------------
		// Combine the test suites
		// --------------------------------------------------------------------
		testng.setXmlSuites(opencmTestSuites); 
		
		// --------------------------------------------------------------------
		// Execute Tests
		// --------------------------------------------------------------------
		testng.run();
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== 2-Node Baseline vs Runtime Audit - Process Completed ..... ============ ");
			
		// --- <<IS-END>> ---

                
	}



	public static final void auditCustom (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(auditCustom)>> ---
		// @sigtype java 3.5
		// [i] field:0:required properties
		 
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	properties = IDataUtil.getString( pipelineCursor, "properties" );
		pipelineCursor.destroy();  
		
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== 2-Node Custom Audit - Starting Process ..... ============ ");
		
		// --------------------------------------------------------------------
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		if (nodes == null) {
			return;
		}
		
		// --------------------------------------------------------------------
		// Read in 2-Node Assertion Pairs from config Properties
		// --------------------------------------------------------------------
		AuditNodesConfiguration auditNodes = AuditNodesConfiguration.instantiate(opencmConfig);
		if (auditNodes == null) {
			return;
		}
		
		// --------------------------------------------------------------------
		// Read in audit config Properties (specific for this audit)
		// --------------------------------------------------------------------
		AuditConfiguration auditProps = AuditConfiguration.instantiate(opencmConfig, properties);
		if (auditProps == null) {
			return;
		}
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Performing Audit Assertions based on " + properties);
		
		// --------------------------------------------------------------------
		// Initiate Test Objects
		// --------------------------------------------------------------------
		TestNG testng = new TestNG();
		testng.setUseDefaultListeners(false);
		List<Class<? extends ITestNGListener>> listenerClasses = new java.util.ArrayList<>();
		listenerClasses.add(org.uncommons.reportng.HTMLReporter.class);
		listenerClasses.add(org.uncommons.reportng.JUnitXMLReporter.class);
		testng.setListenerClasses(listenerClasses);
		testng.setOutputDirectory(opencmConfig.getOutput_dir());
		System.setProperty("org.uncommons.reportng.failures-only","false");
		System.setProperty("org.uncommons.reportng.stylesheet",PackageUtils.getPackageTemplatePath() + File.separator + "opencm_report.css");
		System.setProperty("org.uncommons.reportng.title",auditProps.getTestName());
		
		// --------------------------------------------------------------------
		// Create Suites and Central Store 
		// --------------------------------------------------------------------
		AssertionStore store = new AssertionStore(opencmConfig);
		ArrayList<XmlSuite> opencmTestSuites = new ArrayList<XmlSuite>();
		XmlSuite propertySuite = new XmlSuite(); 
		propertySuite.setName(auditProps.getTestName());
		
		// Process the list of Nodes to Audit
		LinkedList<AuditNodePair> auditNodesList = auditNodes.getNodes(); 
		for (int i = 0; i < auditNodesList.size(); i++) { 
			AuditNodePair nodePair = auditNodesList.get(i);
			// ----------------------------------------------
			// Get OpenCM definitions for these nodes
			// ----------------------------------------------
			Node opencmNode01 = nodes.getNode(nodePair.getNode_01().getNodeAlias());
			Node opencmNode02 = nodes.getNode(nodePair.getNode_02().getNodeAlias());
		
			if (opencmNode01 == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING,"Assertion skipped: node is not defined: " + nodePair.getNode_01().getNodeAlias());
				continue;
			}
			if (opencmNode02 == null) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING,"Assertion skipped: node is not defined: " + nodePair.getNode_02().getNodeAlias());
				continue;
			}
			// ----------------------------------------------
			// Set Repository types
			// ----------------------------------------------
			opencmNode01.setRepositoryType(nodePair.getNode_01().getRepoType());
			opencmNode02.setRepositoryType(nodePair.getNode_02().getRepoType());
			
			// --------------------------------------------------------------------
		    // Loop through the Defined properties to audit
			// --------------------------------------------------------------------
			int testNumber = 1;
			for (int p = 0; p < auditProps.getProperties().size(); p++) {
				Property refProps = auditProps.getProperties().get(p);
		
				// --------------------------------------------------------------------
			    // Get Assertion Values for Node 01
				// --------------------------------------------------------------------
			    LinkedList<AssertionValue> avsNode01 = RepoParser.getAssertionValues(opencmConfig, opencmNode01, refProps, auditProps.getPropertyFilters());
				if (avsNode01.size() == 0) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING," - Assertion skipped: no data found for " + opencmNode01.getNode_name() + " in repo " + opencmNode01.getRepositoryType());
					continue;
				}
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, " -- AvsNode01 List Size: " + avsNode01.size());
		
				// --------------------------------------------------------------------
			    // Get Assertion Values for Node 02
				// --------------------------------------------------------------------
			    LinkedList<AssertionValue> avsNode02 = RepoParser.getAssertionValues(opencmConfig, opencmNode02, refProps, auditProps.getPropertyFilters());
				if (avsNode02.size() == 0) {
					LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING," - Assertion skipped: no data found for " + opencmNode02.getNode_name() + " in repo " + opencmNode02.getRepositoryType());
					continue;
				}
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, " -- AvsNode02 List Size: " + avsNode02.size());
		
				// --------------------------------------------------------------------
			    // Generate value pairs and put into store
				// --------------------------------------------------------------------
				LinkedList<AssertionValuePair> avsPairs = RepoParser.createAssertionValuePairs(avsNode01,avsNode02,false,false); // Always pick up on missing values
				if (avsPairs.size() == 0) {
					continue;
				}
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, " -- AVS Pairs Size: " + avsPairs.size());
				
				// --------------------------------------------------------------------
			    // Create a test identification number
				// --------------------------------------------------------------------
				String testIdent;
				if (auditProps.getProperties().size() == 1) {
					testIdent = "[" + (i+1) + "] ";
				} else {
					testIdent = "[" + (i+1) + " / " + (testNumber++) + "] ";
				}
		
				String twHashMapKey = testIdent + opencmNode01.getNode_name() + " <=> " + opencmNode02.getNode_name();
				TestWrapper tw = new TestWrapper(twHashMapKey,avsPairs);
				store.setTestWrapper(tw);
		
				// --------------------------------------------------------------------
				// Create Property Test (one per defined property assertion)
				// --------------------------------------------------------------------
			    XmlTest propertyTest = new XmlTest();
				propertyTest.setName(twHashMapKey);
			    propertyTest.addParameter(AssertionStore.ASSERTION_PARAMETER_TESTKEY, twHashMapKey); // For the test class to pick up
			    propertyTest.setSuite(propertySuite);
			    
			    ArrayList<XmlClass> propTest_classes = new ArrayList<XmlClass>();
			    propTest_classes.add(new XmlClass(TwoNodeTest.class));
			     
			    propertyTest.getClasses().addAll(propTest_classes);
			    propertySuite.addTest(propertyTest);
			    
			}
		
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, " -- Tests Defined: " + propertySuite.getTests().size());
		if (propertySuite.getTests().size() > 0) {
			opencmTestSuites.add(propertySuite);
			// --------------------------------------------------------------------
			// Combine the test suites
			// --------------------------------------------------------------------
			testng.setXmlSuites(opencmTestSuites); 
			// --------------------------------------------------------------------
			// Execute Tests
			// --------------------------------------------------------------------
			testng.run();
		} else {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_WARNING,"No Tests defined, nothing to assert....");
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== 2-Node Custom Audit - Process Completed ..... ============ ");
			
		// --- <<IS-END>> ---

                
	}



	public static final void auditDefault (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(auditDefault)>> ---
		// @sigtype java 3.5
		// [i] field:0:required node
		// [i] field:0:required repo
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	node = IDataUtil.getString( pipelineCursor, "node" );
		String	repo = IDataUtil.getString( pipelineCursor, "repo" );
		pipelineCursor.destroy();
		
		if (node == null) {
			throw new ServiceException("auditDefault :: Missing Node Parameter");
		} 
		
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== 2-Node Default Audit - Starting Process using " + repo + " data ..... ============ ");
		
		// --------------------------------------------------------------------
		// Read in OpenCM Nodes Properties
		// --------------------------------------------------------------------
		Nodes nodes = Nodes.instantiate(opencmConfig);
		
		// --------------------------------------------------------------------
		// Get runtime node
		// --------------------------------------------------------------------
		Node repoNode = nodes.getNode(node);
		if (repoNode == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL, "auditDefault :: Unable to identify repo node: " + node);
			return;
		}
		if ((repo != null) && (repo.equals(Configuration.OPENCM_BASELINE_DIR))) {
			repoNode.setRepositoryType(Configuration.OPENCM_BASELINE_DIR);
		} else {
			repoNode.setRepositoryType(Configuration.OPENCM_RUNTIME_DIR);
		}
		
		// --------------------------------------------------------------------
		// Identify wM version from runtime node
		// --------------------------------------------------------------------
		String stVersion = repoNode.getVersion();
		if (stVersion == null) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL, "auditDefault :: Unable to parse version from node " + node);
			return;
		}
		
		// --------------------------------------------------------------------
		// Set Default Node
		// --------------------------------------------------------------------
		Node defaultRefNode = new Node();
		defaultRefNode.setNode_name(stVersion);
		defaultRefNode.setRepositoryType(Configuration.OPENCM_DEFAULT_DIR);
		
		// --------------------------------------------------------------------
		// Read in Audit Properties
		// --------------------------------------------------------------------
		AuditConfiguration auditProps = AuditConfiguration.instantiate(opencmConfig, Configuration.OPENCM_AUDIT_DEFAULT_PROPS);
		auditProps.setTestName(auditProps.getTestName() + repo + " data for " + node);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"Performing Default Audit Assertions based on " + Configuration.OPENCM_AUDIT_DEFAULT_PROPS);
		
		// --------------------------------------------------------------------
		// Initiate Test Objects
		// --------------------------------------------------------------------
		TestNG testng = new TestNG();
		testng.setUseDefaultListeners(false);
		List<Class<? extends ITestNGListener>> listenerClasses = new java.util.ArrayList<>();
		listenerClasses.add(org.uncommons.reportng.HTMLReporter.class);
		listenerClasses.add(org.uncommons.reportng.JUnitXMLReporter.class);
		testng.setListenerClasses(listenerClasses);
		testng.setOutputDirectory(opencmConfig.getOutput_dir());
		System.setProperty("org.uncommons.reportng.stylesheet",PackageUtils.getPackageTemplatePath() + File.separator + "template/opencm_report.css");
		System.setProperty("org.uncommons.reportng.failures-only","true");
		System.setProperty("org.uncommons.reportng.title",auditProps.getTestName());
		
		// --------------------------------------------------------------------
		// Define reference directory
		// --------------------------------------------------------------------
		String defaultDir = PackageUtils.getPackageResourcePath() + File.separator + Configuration.OPENCM_DEFAULT_DIR + File.separator + Configuration.OPENCM_REFERENCE_DIR_PREFIX + stVersion;
		File defaultReferenceDir = new File(defaultDir);
		if (!defaultReferenceDir.exists() || !defaultReferenceDir.isDirectory()) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL, "auditDefault :: Reference Directory (for default values) does not exist: " + defaultReferenceDir.getPath());
			return;
		}
		
		// --------------------------------------------------------------------
		// Create Suites and Central Store 
		// --------------------------------------------------------------------
		AssertionStore store = new AssertionStore(opencmConfig);
		ArrayList<XmlSuite> opencmTestSuites = new ArrayList<XmlSuite>();
		
		// ------------------------------------------------------
		// Construct the property definitions based on default directories
		// ------------------------------------------------------
		Property refProps = new Property();
		refProps.setKey("/");
		
		// ------------------------------------------------------
		// Main Processing...
		// ------------------------------------------------------
		File [] fRefCompDirectories = defaultReferenceDir.listFiles();
		for (File refCompPath:fRefCompDirectories) {
			if (refCompPath.isDirectory()) {
				LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, "  -- Processing Component " + refCompPath.getName());
				XmlSuite componentSuite = new XmlSuite(); 
				componentSuite.setName(refCompPath.getName());
				File [] fRefInstDirectories = refCompPath.listFiles();
				for (File refInstPath:fRefInstDirectories) {
					if (refInstPath.isDirectory()) {
						refProps.setInstance(refInstPath.getName());
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, "    -- Processing Instance " + refInstPath.getName());
						// --------------------------------------------------------------------
					    // Get Assertion Values for Default
						// --------------------------------------------------------------------
						refProps.setComponent(refCompPath.getName()); // Always the actual component name (not normalized)
					    LinkedList<AssertionValue> avsDefault = RepoParser.getAssertionValues(opencmConfig, defaultRefNode, refProps, auditProps.getPropertyFilters());
						if (avsDefault.size() == 0) {
							continue;
						}
						
						// --------------------------------------------------------------------
					    // Get Assertion Values for Runtime
						// --------------------------------------------------------------------
						// For runtime, it is possible/likely that the component name is different from default:
						refProps.setComponent(RepoParser.normalizeComponentName(refCompPath.getName()));
					    LinkedList<AssertionValue> avsRuntime = RepoParser.getAssertionValues(opencmConfig, repoNode, refProps, auditProps.getPropertyFilters());
						if (avsRuntime.size() == 0) {
							continue;
						}
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, "      -- Runtime Instance Size: " + avsRuntime.size());
						
						// --------------------------------------------------------------------
					    // Generate value pairs and put into store
						// --------------------------------------------------------------------
						LinkedList<AssertionValuePair> avsPairs = RepoParser.createAssertionValuePairs(avsDefault,avsRuntime,true,false); // Ignore additional default values but do not ignore additional runtime values
						if (avsPairs.size() == 0) {
							continue;
						}
						LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG, "      -- AVS Pairs Size: " + avsPairs.size());
						
						String twHashMapKey = refProps.getComponent() + "_" + refProps.getInstance();
						TestWrapper tw = new TestWrapper(twHashMapKey,avsPairs);
						store.setTestWrapper(tw);
						 
						// --------------------------------------------------------------------
						// Create Property Test (one per instance)
						// --------------------------------------------------------------------
					    XmlTest propertyTest = new XmlTest();
					    propertyTest.setName(refProps.getInstance());
					    propertyTest.setSuite(componentSuite);
					    propertyTest.addParameter(AssertionStore.ASSERTION_PARAMETER_TESTKEY, twHashMapKey); // For the test class to pick up
					    
					    ArrayList<XmlClass> propTest_classes = new ArrayList<XmlClass>();
					    propTest_classes.add(new XmlClass(TwoNodeTest.class));
					     
					    propertyTest.getClasses().addAll(propTest_classes);
					    componentSuite.addTest(propertyTest);
					}
				}
				if (componentSuite.getTests().size() > 0) {
					opencmTestSuites.add(componentSuite);
				}
			}
		}
		
		
			
		// --------------------------------------------------------------------
		// Combine the test suites
		// --------------------------------------------------------------------
		testng.setXmlSuites(opencmTestSuites); 
		
		// --------------------------------------------------------------------
		// Execute Tests
		// --------------------------------------------------------------------
		testng.run();
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== 2-Node Default Audit - Process Completed. ============ ");
			
		// --- <<IS-END>> ---

                
	}
}

