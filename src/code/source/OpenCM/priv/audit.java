package OpenCM.priv;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.LinkedList;
import org.opencm.audit.Collection;
import org.opencm.audit.configuration.AuditConfiguration;
import org.opencm.audit.configuration.PropertyFilter;
import org.opencm.audit.configuration.TreeNode;
import org.opencm.audit.result.AuditResult;
import org.opencm.configuration.Configuration;
import org.opencm.configuration.PkgConfiguration;
import org.opencm.inventory.Inventory;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
// --- <<IS-END-IMPORTS>> ---

public final class audit

{
	// ---( internal utility methods )---

	final static audit _instance = new audit();

	static audit _newInstance() { return new audit(); }

	static audit _cast(Object o) { return (audit)o; }

	// ---( server methods )---




	public static final void runAudit (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(runAudit)>> ---
		// @sigtype java 3.5
		// [i] recref:0:required auditConfiguration OpenCM.doc:auditConfiguration
		// [o] field:0:required json_response
		// --------------------------------------------------------------------
		// Read in Default Package Properties 
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate(); 
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== Run Audit - Starting Process ..... ============ ");
		
		AuditConfiguration auditConf = new AuditConfiguration(); 
		String templateName = null;
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		// auditConfiguration
		IData	auditConfiguration = IDataUtil.getIData( pipelineCursor, "auditConfiguration" );
		if ( auditConfiguration != null) {
			IDataCursor auditConfigurationCursor = auditConfiguration.getCursor();
			templateName = IDataUtil.getString( auditConfigurationCursor, "templateName" );
		
			// i.submitData
			IData	submitData = IDataUtil.getIData( auditConfigurationCursor, "submitData" );
			if ( submitData != null) {
				IDataCursor submitDataCursor = submitData.getCursor();
				auditConf.setAudit_description(IDataUtil.getString( submitDataCursor, "audit_description" ));
				auditConf.setAudit_type(IDataUtil.getString( submitDataCursor, "audit_type" ));
				auditConf.setRepo_name(IDataUtil.getString( submitDataCursor, "repo_name" ));
				auditConf.setProp_component(IDataUtil.getString( submitDataCursor, "prop_component" ));
				auditConf.setProp_instance(IDataUtil.getString( submitDataCursor, "prop_instance" ));
				auditConf.setProp_key(IDataUtil.getString( submitDataCursor, "prop_key" ));
		
				// i.prop_filters
				IData[]	prop_filters = IDataUtil.getIDataArray( submitDataCursor, "prop_filters" );
				if ( prop_filters != null) {
					LinkedList<PropertyFilter> filters = new LinkedList<PropertyFilter>();
					for ( int i = 0; i < prop_filters.length; i++ ) {
						PropertyFilter filter = new PropertyFilter();
						IDataCursor prop_filtersCursor = prop_filters[i].getCursor();
						filter.setComponent(IDataUtil.getString( prop_filtersCursor, "component" ));
						filter.setInstance(IDataUtil.getString( prop_filtersCursor, "instance" ));
						filter.setKey(IDataUtil.getString( prop_filtersCursor, "key" ));
						prop_filtersCursor.destroy();
						filters.add(filter);
					}
					auditConf.setProp_filters(filters);
				}
				
				// i.prop_filters
				IData[]	tree_nodes = IDataUtil.getIDataArray( submitDataCursor, "tree_nodes" );
				if ( tree_nodes != null) {
					LinkedList<TreeNode> treeNodes = new LinkedList<TreeNode>();
					for ( int i = 0; i < tree_nodes.length; i++ ) {
						TreeNode treeNode = new TreeNode();
						IDataCursor tree_nodesCursor = tree_nodes[i].getCursor();
						treeNode.setOrganisation(IDataUtil.getString( tree_nodesCursor, "organisation" ));
						treeNode.setDepartment(IDataUtil.getString( tree_nodesCursor, "department" ));
						treeNode.setEnvironment(IDataUtil.getString( tree_nodesCursor, "environment" ));
						treeNode.setLayer(IDataUtil.getString( tree_nodesCursor, "layer" ));
						treeNode.setInstallation(IDataUtil.getString( tree_nodesCursor, "installation" ));
						tree_nodesCursor.destroy();
						treeNodes.add(treeNode);
					}
					auditConf.setTree_nodes(treeNodes);
				}
				
				submitDataCursor.destroy();
			}
			auditConfigurationCursor.destroy();
		}
		pipelineCursor.destroy();
		
		// -----------------------------------------------------
		// Validate
		// -----------------------------------------------------
		if ((auditConf.getAudit_type() == null) || (auditConf.getAudit_type().equals(""))) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR, " - Audit :: Invalid audit_type ..... " + auditConf.getAudit_type());
			return;
		}
		if ((auditConf.getRepo_name() == null) || (auditConf.getRepo_name().equals(""))) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR," - Audit :: Invalid repo_name ..... " + auditConf.getRepo_name());
			return;
		}
		if ((auditConf.getProp_component() == null) || (auditConf.getProp_component().equals(""))) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR," - Audit :: Invalid prop_component ..... " + auditConf.getProp_component());
			return;
		}
		if ((auditConf.getProp_instance() == null) || (auditConf.getProp_instance().equals(""))) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR," - Audit :: Invalid prop_instance ..... " + auditConf.getProp_instance());
			return;
		}
		if ((auditConf.getProp_key() == null) || (auditConf.getProp_key().equals(""))) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR," - Audit :: Invalid prop_key ..... " + auditConf.getProp_key());
			return;
		}
		if ((auditConf.getTree_nodes() == null) || (auditConf.getTree_nodes().size() == 0)) {
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR," - Audit :: Invalid TreeNodes ..... Null or Empty ");
			return;
		}
		
		String jsonResponse = "";
		// --------------------------------------------------------------------
		// Read in Inventory
		// --------------------------------------------------------------------
		Inventory inv = Inventory.instantiate(opencmConfig);
		if (inv == null){
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL,"runAudit - Inventory is NULL ..... ");
			jsonResponse = JsonUtils.createJsonField("msg","runAudit - Inventory is NULL ..... refresh UI.");
		} else {
			// -----------------------------------------------------
			// Call Audit Collection  
			// -----------------------------------------------------
			AuditResult ar = Collection.collect(opencmConfig, inv, auditConf);
			if (ar != null) {
				jsonResponse = JsonUtils.convertJavaObjectToJson(ar);
			} else {
				jsonResponse = JsonUtils.createJsonField("msg","runAudit - No Audit Result ..... refer to log.");
			}
		}
		
		// -----------------------------------------------------
		// Pass back result
		// -----------------------------------------------------
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		// LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO, jsonResponse);
		IDataUtil.put( pipelineCursor2, "json_response", jsonResponse);
		pipelineCursor.destroy(); 
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== Audit - Process Completed ..... ============ ");
			
		// --- <<IS-END>> ---

                
	}



	public static final void saveTemplate (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(saveTemplate)>> ---
		// @sigtype java 3.5
		// [i] recref:0:required auditConfiguration OpenCM.doc:auditConfiguration
		// [o] field:0:required json_response
		// --------------------------------------------------------------------
		// Read in Default Package Properties
		// --------------------------------------------------------------------
		PkgConfiguration pkgConfig = PkgConfiguration.instantiate();  
		
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		Configuration opencmConfig = Configuration.instantiate(pkgConfig.getConfig_directory());
		opencmConfig.setConfigDirectory(pkgConfig.getConfig_directory());
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== Save Template - Starting Process ..... ============ ");
		
		AuditConfiguration auditConf = new AuditConfiguration();
		String templateName = null;
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		
		// auditConfiguration
		IData	auditConfiguration = IDataUtil.getIData( pipelineCursor, "auditConfiguration" );
		if ( auditConfiguration != null) {
			IDataCursor auditConfigurationCursor = auditConfiguration.getCursor();
			templateName = IDataUtil.getString( auditConfigurationCursor, "templateName" );
		
			// i.submitData
			IData	submitData = IDataUtil.getIData( auditConfigurationCursor, "submitData" );
			if ( submitData != null) {
				IDataCursor submitDataCursor = submitData.getCursor();
				auditConf.setAudit_description(IDataUtil.getString( submitDataCursor, "audit_description" ));
				auditConf.setAudit_type(IDataUtil.getString( submitDataCursor, "audit_type" ));
				auditConf.setRepo_name(IDataUtil.getString( submitDataCursor, "repo_name" ));
				auditConf.setProp_component(IDataUtil.getString( submitDataCursor, "prop_component" ));
				auditConf.setProp_instance(IDataUtil.getString( submitDataCursor, "prop_instance" ));
				auditConf.setProp_key(IDataUtil.getString( submitDataCursor, "prop_key" ));
		
				// i.prop_filters
				IData[]	prop_filters = IDataUtil.getIDataArray( submitDataCursor, "prop_filters" );
				if ( prop_filters != null) {
					LinkedList<PropertyFilter> filters = new LinkedList<PropertyFilter>();
					for ( int i = 0; i < prop_filters.length; i++ ) {
						PropertyFilter filter = new PropertyFilter();
						IDataCursor prop_filtersCursor = prop_filters[i].getCursor();
						filter.setComponent(IDataUtil.getString( prop_filtersCursor, "component" ));
						filter.setInstance(IDataUtil.getString( prop_filtersCursor, "instance" ));
						filter.setKey(IDataUtil.getString( prop_filtersCursor, "key" ));
						prop_filtersCursor.destroy();
						filters.add(filter);
					}
					auditConf.setProp_filters(filters);
				}
				
				// i.tree_nodes
				IData[]	tree_nodes = IDataUtil.getIDataArray( submitDataCursor, "tree_nodes" );
				if ( tree_nodes != null) {
					LinkedList<TreeNode> treeNodes = new LinkedList<TreeNode>();
					for ( int i = 0; i < tree_nodes.length; i++ ) {
						TreeNode treeNode = new TreeNode();
						IDataCursor tree_nodesCursor = tree_nodes[i].getCursor();
						treeNode.setOrganisation(IDataUtil.getString( tree_nodesCursor, "organisation" ));
						treeNode.setDepartment(IDataUtil.getString( tree_nodesCursor, "department" ));
						treeNode.setEnvironment(IDataUtil.getString( tree_nodesCursor, "environment" ));
						treeNode.setLayer(IDataUtil.getString( tree_nodesCursor, "layer" ));
						treeNode.setInstallation(IDataUtil.getString( tree_nodesCursor, "installation" ));
						tree_nodesCursor.destroy();
						treeNodes.add(treeNode);
					}
					auditConf.setTree_nodes(treeNodes);
				}
				
				submitDataCursor.destroy();
			}
			auditConfigurationCursor.destroy();
		}
		pipelineCursor.destroy();
		
		// -----------------------------------------------------
		// Validate
		// -----------------------------------------------------
		String msg = "Success";
		int rc = 0;
		
		if ((templateName == null) || (templateName.equals(""))) {
			msg = " - Save Template :: Invalid templateName ..... " + templateName;
			rc = -1;
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,msg);
		}
		if ((auditConf.getAudit_description() == null) || (auditConf.getAudit_description().equals(""))) {
			msg = " - Save Template :: Invalid audit_description ..... " + auditConf.getAudit_description();
			rc = -1;
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,msg);
		}
		if ((auditConf.getAudit_type() == null) || (auditConf.getAudit_type().equals(""))) {
			msg = " - Save Template :: Invalid audit_type ..... " + auditConf.getAudit_type();
			rc = -1;
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,msg);
		}
		if ((auditConf.getRepo_name() == null) || (auditConf.getRepo_name().equals(""))) {
			msg = " - Save Template :: Invalid repo_name ..... " + auditConf.getRepo_name();
			rc = -1;
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,msg);
		}
		if ((auditConf.getProp_component() == null) || (auditConf.getProp_component().equals(""))) {
			msg = " - Save Template :: Invalid prop_component ..... " + auditConf.getProp_component();
			rc = -1;
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,msg);
		}
		if ((auditConf.getProp_instance() == null) || (auditConf.getProp_instance().equals(""))) {
			msg = " - Save Template :: Invalid prop_instance ..... " + auditConf.getProp_instance();
			rc = -1;
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,msg);
		}
		if ((auditConf.getProp_key() == null) || (auditConf.getProp_key().equals(""))) {
			msg = " - Save Template :: Invalid prop_key ..... " + auditConf.getProp_key();
			rc = -1;
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,msg);
		}
		if ((auditConf.getTree_nodes() == null) || (auditConf.getTree_nodes().size() == 0)) {
			msg = " - Save Template :: Invalid Tree Nodes ..... Null or Empty ";
			rc = -1;
			LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_ERROR,msg);
		}
		
		// -----------------------------------------------------
		// Write
		// -----------------------------------------------------
		if (rc == 0) {
			auditConf.write(opencmConfig, templateName);
		}
		
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_INFO,"=========== Save Template - Process Completed ..... ============ ");
		
		// pipeline
		IDataCursor pipelineCursor2 = pipeline.getCursor();
		
		String jResp = JsonUtils.createJsonField("msg",msg);
		jResp = JsonUtils.addField(jResp,"rc", new Integer(rc).toString());
		
		IDataUtil.put( pipelineCursor2, "json_response", jResp);
		pipelineCursor.destroy(); 
			
		// --- <<IS-END>> ---

                
	}
}

