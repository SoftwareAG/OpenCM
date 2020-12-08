// ------------------------------------------------------------
// OpenCM main javascript
// ------------------------------------------------------------
var authenticated = false;

// ---------------------------------------
// General purpose functions
// ---------------------------------------
function getSession() {
	var resp = callJsonService( "/invoke/org.opencm.pub.ui/login", "{}");
	return resp.auth;
}
function opencmLogin() {
	if (getSession()) {
		authenticated = true;
		$('#div_menu').show();
		$('#div_login').hide();
		$('#div_logout').show();
	} else {
		authenticated = false;
		$('#div_menu').hide();
		$('#div_login').show();
		$('#div_logout').hide();
	}
}
function opencmLogout() {
	callJsonService( "/invoke/org.opencm.pub.ui/logout", "{}");
	authenticated = false;
	$('#div_menu').hide();
	$('#div_login').show();
	$('#div_logout').hide();
}
function notify(info) {
    $('#notification').html(info);
    $('#notification').fadeIn(500).delay(2500).fadeOut(500);
}
// ---------------------------
// Server Json functions
// ---------------------------
function callJsonService(uri, jsonData){
	var jsonResponse;
	$.ajax({
		type: "POST",
		url: uri,
		async: false,
		data: jsonData,
		contentType: "application/json; charset=utf-8",
		dataType: 'json',
		success: function (data) {
			jsonResponse = data;
		},
		error: function(xhr, textStatus, errorThrown) {
			jsonResponse = "callJsonService exception:: " + textStatus + ": " + errorThrown;
		}
	});
	return jsonResponse;
}
function callService(uri, input) {
	var jsonResponse;
	$.ajax({
		type: "POST",
		url: uri,
		async: false,
		data: input,
		success: function (data) {
			jsonResponse = data;
		},
		error: function(xhr, textStatus, errorThrown) {
			jsonResponse = "callJsonService exception:: " + textStatus + ": " + errorThrown;
		}
	});
	return jsonResponse;
}

// ---------------------------
// Time functions
// ---------------------------
function getFormattedDateTime() {
	var d = new Date();
	d = d.getFullYear() + ('0' + (d.getMonth() + 1)).slice(-2) + ('0' + d.getDate()).slice(-2) + ('0' + d.getHours()).slice(-2) + ('0' + d.getMinutes()).slice(-2);
	return d;
}

// -------------------------------------
// Inventory Functions - For Tree Data
// -------------------------------------
function getInventory() {
	return opencm_inventory;
}

// -------------------------------------
// Configuration UI
// -------------------------------------
function showSecretsConfiguration() {
	$('#div_inv_mgmt').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	$('#div_inventory').hide();
	var secretsHTML = $('#div_secrets').load('secrets.html');
	$('#div_secrets').show();
}
function showSmtpConfiguration() {
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	$('#div_inventory').hide();
	var smtpHTML = $('#div_smtp').load('smtp.html');
	$('#div_smtp').show();
}

// -------------------------------------
// Inventory UI
// -------------------------------------
function showInventory() {
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	$('#div_inventory').show();
}
function refreshInventory() {
	callJsonService( "/invoke/org.opencm.pub.inventory/refreshInventory", "{}");
	location.reload();
}
// -------------------------------------
// Inventory Mgmt UI
// -------------------------------------
function showInvMgmt() {
	$('#div_inventory').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	var invMgmtHTML = $('#div_inv_mgmt').load('inventoryMgmt.html');
	$('#div_inv_mgmt').show();
}


// -------------------------------------
// Respository UI
// -------------------------------------
var repoInstallationPath;
function openRepository(path) {
	repoInstallationPath = decodeURI(path);
	$('#div_inventory').hide();
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	var repositoryHTML = $('#div_repository').load('repository.html');
	$('#div_repository').show();
}
function getRepoInstallationPath() {
	return repoInstallationPath;
}
function refreshRepository() {
	callJsonService( "/invoke/org.opencm.pub.repository/refreshRepository", "{}");
	location.reload();
}

// -------------------------------------
// Extractions UI
// -------------------------------------
function showExtractions() {
	$('#div_inventory').hide();
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	var extractionsHTML = $('#div_extractions').load('extractions.html');
	$('#div_extractions').show();
}
function showSynchronization() {
	$('#div_inventory').hide();
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	var synchHTML = $('#div_synch').load('synchronization.html');
	$('#div_synch').show();
}

// -------------------------------------
// Governance UI
// -------------------------------------
function showGovernance() {
	$('#div_inventory').hide();
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	var governanceHTML = $('#div_governance').load('governance.html');
	$('#div_governance').show();
}

// -------------------------------------
// Auditing UI
// -------------------------------------
function showAuditing() {
	$('#div_inventory').hide();
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_cce').hide();
	$('#div_about').hide();
	var auditingHTML = $('#div_auditing').load('auditing.html');
	$('#div_auditing').show();
}

// -------------------------------------
// Command Central UI
// -------------------------------------
function showCommandCentral() {
	$('#div_inventory').hide();
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_about').hide();
	var cceHTML = $('#div_cce').load('command_central.html');
	$('#div_cce').show();
}

// -------------------------------------
// About OpenCM
// -------------------------------------
function showAbout() {
	$('#div_inventory').hide();
	$('#div_inv_mgmt').hide();
	$('#div_secrets').hide();
	$('#div_smtp').hide();
	$('#div_repository').hide();
	$('#div_extractions').hide();
	$('#div_synch').hide();
	$('#div_governance').hide();
	$('#div_auditing').hide();
	$('#div_cce').hide();
	$('#div_about').load('about.html');
	$('#div_about').show();
}

// -------------------------------------
// Common Tree Functions 
// -------------------------------------
// Return jsTree data for the whole inventory (in JsTree format) - excluding installation nodes
function getTreeDataWithoutInstallations(group, groupData, level, node_id) {
	
	if (node_id == "") {
		node_id = group.name.replace(/\s/g,'');
	} else {
		node_id = node_id + "_" + group.name.replace(/\s/g,'');
	}

	var leafNode = false;
	
	var subTreeArray = Array();
	if (group.groups.length == 0) {
		leafNode = true;
	} else {
		$.each(group.groups, function( index, groupChild ) {
			getTreeDataWithoutInstallations(groupChild, subTreeArray, level + 1, node_id);
		});
	}
	
	if (leafNode) {
		subTreeArray.push({ id: node_id, text: group.name, icon: false});
		groupData.push({ id: node_id, text: group.name, icon: false, li_attr : { "style" : "font-weight: normal" }});
	} else if (subTreeArray.length > 0) {
		if (level > 1) {
			groupData.push({ id: node_id, text: group.name, icon: false, state : { opened : false }, li_attr : { "style" : "font-weight: normal" }, children: subTreeArray });
		} else {
			groupData.push({ id: node_id, text: group.name, icon: false, state : { opened : true }, li_attr : { "style" : "font-weight: bold" }, children: subTreeArray });
		}
	}

	return groupData;
}
// Return jsTree data for the whole inventory (in JsTree format) - including installation nodes
function getTreeDataWithInstallations(group, groupData, level, node_id) {
	if (node_id == "") {
		node_id = group.name.replace(/\s/g,'');
	} else {
		node_id = node_id + "_" + group.name.replace(/\s/g,'');
	}

	var subTreeArray = Array();
	$.each(group.groups, function( index, groupChild ) {
		getTreeDataWithInstallations(groupChild, subTreeArray, level + 1, node_id);
	});
	
	if (group.installations.length > 0) {
		var instArray = Array();
		$.each(group.installations, function( index, installation ) {
			var leaf_id = node_id + "_" + installation.name.replace(/\s/g,'');
			instArray.push({ id: leaf_id, text: installation.name, description: installation.description, hostname: installation.hostname, runtimes: installation.runtimes, load_balancers: installation.load_balancers, type: "leaf", icon: "./img/tree_inst.svg" });
		});
		subTreeArray.push({ id: node_id, text: group.name, icon: false, li_attr : { "style" : "font-weight: normal" } });
		groupData.push({ id: node_id, text: group.name, icon: false, li_attr : { "style" : "font-weight: normal" }, children: instArray });
	} else {
		if (subTreeArray.length > 0) {
			if (level > 1) {
				groupData.push({ id: node_id, text: group.name, icon: false, state : { opened : false }, li_attr : { "style" : "font-weight: normal" }, children: subTreeArray });
			} else {
				groupData.push({ id: node_id, text: group.name, icon: false, state : { opened : true }, li_attr : { "style" : "font-weight: bold" }, children: subTreeArray });
			}
		} else {
			// Empty Tree
			groupData.push({ id: node_id, text: group.name, icon: false, li_attr : { "style" : "font-weight: normal" } });
		}
	}
	return groupData;
}

// Return installation table data for a subset of the inventory based on paths
function getFilteredInstallations(invGroup, paths, installations, includeAllInstallations) {
	
	if ((invGroup.groups.length == 0) && (invGroup.installations.length == 0)) {
		return;
	}
	if (invGroup.installations.length > 0) {
		$.each(invGroup.installations, function( index, installation ) {
			var eligible = false;
			$.each(paths, function( index, path ) {
				for (i = 0; i < path.length; i++) {
					if ((invGroup.path[i] != null) && (path[i] != invGroup.path[i])) {
						break;
					}
                    if ((i+1) == path.length) { 
                        if (includeAllInstallations) {
                            eligible = true;
                        } else if (path[i] == installation.name) {
                            eligible = true;
                        }
                    }
				}
				if (eligible) {
					return false;
				}
			});
			if (eligible) {
				installations.push(installation);
			}
		});

		return;
	}

	$.each(invGroup.groups, function( index, group ) {
		getFilteredInstallations(group, paths, installations, includeAllInstallations);
	});
	
	return installations;
}
// Return installation info as array (for displaying table data)
function getSimpleTableData(selectedTreeItems) {

	var installations = Array();
    var includeAllInstallations = false;
    invInstallations = getFilteredInstallations(getInventory().root_group, selectedTreeItems, installations, includeAllInstallations);

	var invInstallationData = Array();
	$.each(invInstallations, function( index, installation ) {
		invInstallationData.push( { 'location': getPathString(installation.path), 
									'hostname': installation.hostname,
									'installation': installation.name
								});
	});
	return invInstallationData;
}

// Return path as a string delimied with "->"
function getPathString(path) {
	var locationString = "";
	$.each(path, function( index, level ) {
		if (index == 0) {
			return true;
		} else if ((index + 1) == path.length) {
			return false;
		} else if (index > 1) {
			locationString += "->";
		}
		locationString += level;
	});
	return locationString;
}
// Convert a string array (path) to a string delimited with "_"
function getTreeNodeId(path) {
	var node_id = "";
	$.each(path, function( idx, level ) {
		node_id += level.replace(/\s/g,'');
		if ((idx +1) < path.length) {
			node_id += "_";
		}
	});
	return node_id;
}
// -------------------------------------
// Auto-completion
// -------------------------------------
var aut_components_inventory = [
	{ value: 'OSGI-SPM', data: 'OSGI-SPM' },
	{ value: 'OPENCM-SYNCH', data: 'OPENCM-SYNCH' },
	{ value: 'integrationServer-default', data: 'integrationServer-default' },
	{ value: 'Universal-Messaging-default', data: 'Universal-Messaging-default' }
];
var aut_components = [
	{ value: 'NODE-FIXES', data: 'NODE-FIXES' },
	{ value: 'NODE-PRODUCTS', data: 'NODE-PRODUCTS' },
	{ value: 'OSGI-SPM', data: 'OSGI-SPM' },
	{ value: 'OPENCM-SYNCH', data: 'OPENCM-SYNCH' },
	{ value: 'integrationServer-default', data: 'integrationServer-default' },
	{ value: 'Universal-Messaging-umserver', data: 'Universal-Messaging-umserver' }
];
var aut_instances = [
	{ value: 'COMMON-MEMORY', data: 'COMMON-MEMORY' },
	{ value: 'COMMON-SYSPROPS', data: 'COMMON-SYSPROPS' },
	{ value: 'IS-RESOURCES', data: 'IS-RESOURCES' },
	{ value: 'IS-EXT-PACKAGES', data: 'IS-EXT-PACKAGES' },
	{ value: 'IS-EXT-PACKAGES', data: 'IS-EXT-PACKAGES' },
	{ value: 'IS-EXT-JCE-POLICYINFO', data: 'IS-EXT-JCE-POLICYINFO' },
	{ value: 'IS-EXT-WXCONFIG', data: 'IS-EXT-WXCONFIG' }
];
var aut_usernames = [
	{ value: 'Administrator', data: 'Administrator' },
	{ value: 'sys_usr_opencm', data: 'sys_usr_opencm' }
];


		
