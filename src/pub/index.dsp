<!DOCTYPE html>
<head>
    <meta charset="utf-8" />
    <meta name="author" content="Håkan Hansson" />
    <meta name="keywords" content="Configuration Management" />
    <meta name="description" content="webmethods Configuration Management" />
    <meta name="robots" content="noindex,nofollow" />
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=1">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <link rel="icon" href="img/tab.png" type="image/x-icon" />
    <link rel="stylesheet" type="text/css" href="css/opencm.css" />
    <link rel="stylesheet" type="text/css" href="css/jquery.json-view.css" />
    <link rel="stylesheet" type="text/css" href="css/datatables.min.css"/>
    <title>OpenCM</title>
</head>
<script src="js/jquery-1.10.2.min.js"></script>
<script src="js/d3.v3.min.js"></script>
<script src="js/jquery.json-view.js"></script>
<script type="text/javascript" src="js/datatables.min.js"></script>
<script>
 	var opencm_nodes = [
		%invoke OpenCM.pub.dsp.configuration:getNodes%
			%loop Environment%
			 {"name": "%value name%",
			  "layers": [
				%loop Layer%
			     {"name": "%value name%",
				  "servers": [
					%loop Server%
					 {"name": "%value name%",
					  "nodes": [
						%loop Node%
						 {"name": "%value name%",
						  "rcs": [
							%loop RuntimeComponent%
							  {"name": "%value name%",
							   "protocol": "%value protocol%",
							   "port": "%value port%",
							   "username": "%value username%"
							  }
							  %loopsep ','%
						    %endloop%
						 ]}
						 %loopsep ','%
						%endloop%
				     ]}
					 %loopsep ','%
				   %endloop%
				 ]}
				 %loopsep ','%
			   %endloop%
			  ]}
			 %loopsep ','%
		   %endloop%
		%endinvoke%
	  ];
</script>
<body>
    <div id="auditReport">
        <span class="auditButton b-close"><span>X</span></span>
        <div class="auditReportFrame"></div>
    </div>
    <div id="aboutPopup">
        <span class="auditButton b-close"><span>X</span></span>
        <div class="aboutFrame"></div>
    </div>
    <div id="assertPopup">
        <span class="auditButton b-close"><span>X</span></span>
        <div class="assertFrame"></div>
    </div>
    <div id="inventoryPopup">
        <span class="auditButton b-close"><span>X</span></span>
        <div class="inventoryFrame"></div>
    </div>
	<section id="canvasArea">
		<div id="canvasLogo">
			<h2><a title="DBP Overview" href="/OpenCM">OpenCM Configuration Management Repository</a><br/><span>v1.8.9</span></h2>
		</div>
		<div id="canvasTree"></div>
	</section>
    <div id="notification" class="notice info"></div>
	<nav id="optionsArea">
	   <ul id="optionsMenu">
			<!-- Repository Menu -->
			<li id="repoMenu"><a href="/OpenCM">Repository</a>
               <ul class="subMenu repoMenu">
				   <!-- Initiate Full Snapshot generation -->
				   <li id="extractFromPropsMenu"><a href="javascript:;" onclick="extractWithProps();">Extract using Properties</a></li>
				   <!-- Initiate Snapshot for a single node -->
				   <li id="extractNodeMenu"><a href="javascript:;" onclick="extractNode();">Extract this Node</a></li>
				   <!-- Promote Snapshot to Baseline -->
				   <li id="promoteRuntimeMenu"><a href="javascript:;" onclick="promoteNode();">Promote Node to Baseline</a></li>
               </ul>
			</li>
			<!-- Inventory Menu -->
	        <li id="inventoryMenu"><a class="inventoryMenuLink" data-bpopup='{"content":"iframe","contentContainer":".inventoryFrame","loadUrl":"/OpenCM/inventory"}' href="#">Inventory</a></li>
			<!-- Assert Menu -->
			<!-- <li id="assertMenu"><a class="assertMenuLink" data-bpopup='{"content":"iframe","contentContainer":".assertFrame","loadUrl":"/OpenCM/assert"}' href="#">Assertions</a></li> -->
			<!-- Audit Menu -->
			<li id="auditTwoNodeMenu"><a href="#">2-Node Assertions</a>
                  <ul class="subMenu auditTwoNodeMenu">
				   <!-- Initiate Snapshot generation via Browser -->
				   <li id="audit_PerformBaselineFullAudit"><a href="javascript:;" onclick="performBaselineFullAudit();">Baseline vs. Runtime Full Audit</a></li>
				   <li id="audit_PerformBaselineSingleAudit"><a href="javascript:;" onclick="performBaselineSingleAudit();">Baseline vs. Runtime Node Audit</a></li>
				   <li id="audit_PerformDefaultBaselineAudit"><a href="javascript:;" onclick="performDefaultBaselineAudit();">Default vs. Baseline Node Audit</a></li>
				   <li id="audit_PerformDefaultRuntimeAudit"><a href="javascript:;" onclick="performDefaultRuntimeAudit();">Default vs. Runtime Node Audit</a></li>
  			       %invoke OpenCM.pub.dsp.audit:getTwoNodeAuditProps%
  	                  %ifvar propertyFiles%
                        %loop propertyFiles%
							%ifvar propertyFile equals('audit_nodes')%
							 %else%
                              <li id="audit_PerformTwoNodeAudit"><a href="javascript:;" onclick="performTwoNodeAudit('%value propertyFile encode(html)%');">Custom Audit: %value propertyFile encode(html)%</a></li>
				            %endif%
				        %endloop%
				      %endif%
			       %endinvoke%
					<li id="auditTwoNode_ViewReport"><a class="auditTwoNodeMenuLink" data-bpopup='{"content":"iframe","contentContainer":".auditReportFrame","loadUrl":"/OpenCM/output/html/index.html"}' href="#">Open 2-Node Audit Report</a></li>
                  </ul>
			</li>
			<!-- Env Audit Menu -->
			<li id="auditLayeredMenu"><a href="#">Layered Audits</a>
                  <ul class="subMenu auditLayeredMenu">
  			       %invoke OpenCM.pub.dsp.audit:getLayeredAuditProps%  
  	                  %ifvar propertyFiles%
                        %loop propertyFiles%
                        <li id="audit_PerformLayeredAudit"><a href="javascript:;" onclick="performLayeredAudit('%value propertyFile encode(html)%');">Audit: %value propertyFile encode(html)%</a></li>
				        %endloop%
				      %endif%
			       %endinvoke%
  			       %invoke OpenCM.pub.dsp.audit:getLayeredAuditResults%
  	                  %ifvar resultFiles%
                        %loop resultFiles%
                        <li id="auditLayered_ViewReport"><a href="output/excel/%value resultFile encode(html)%.xlsx" download="%value resultFile encode(html)%.xlsx"><img src="img/xlsx.ico" height="20" width="18"> %value resultFile encode(html)%</a></li>
				        %endloop%
				      %endif%
			       %endinvoke%
                  </ul>
			</li>
			<!-- OpenCM Configuration Menu -->
			<li id="confMenu"><a href="#">OpenCM Administration</a>
                  <ul class="subMenu confMenu">
                    <li id="conf_UpdateTree"><a href="javascript:;" onclick="performTreeUpdate();">Refresh Tree</a></li>
  			        %invoke OpenCM.pub.dsp.configuration:getConfig%
 					 %ifvar endpoint_config_type equals('opencm')%
						<li id="conf_Encrypt"><a href="javascript:;" onclick="performEncrypt();">Encrypt Endpoints</a></li>
						<li id="conf_Decrypt"><a href="javascript:;" onclick="performDecrypt();">Decrypt Endpoints</a></li>
					 %endif%
			        %endinvoke%
                    <li id="conf_SynchSend"><a href="javascript:;" onclick="performSynchSend();">Synchronize :: Send</a></li>
                  </ul>
			</li>
			<!-- Command Central Configuration Menu -->
			<li id="confMenu"><a href="#">Command Central</a>
                  <ul class="subMenu cceMenu">
                    <li id="conf_CceRefresh"><a href="javascript:;" onclick="performCceRefresh();">Create All Nodes</a></li>
                    <li id="conf_CceAddNode"><a href="javascript:;" onclick="performCceAddNode();">Add Node</a></li>
                    <li id="conf_CceFixScript"><a href="javascript:;" onclick="performGenerateCceFixScript();">Create Fix Script</a></li>
                  </ul>
			</li>
			<!-- About Menu -->
			<li id="aboutMenu"><a class="aboutMenuLink" data-bpopup='{"content":"iframe","contentContainer":".aboutFrame","loadUrl":"/OpenCM/about/index.html"}' href="#">About</a></li>
		</ul>
	</nav>
    <aside id="detailsArea">
        <section id="generalDetails">
            <h2 id="opencm_page"></h2>
            <div class="accordion-container" id="opencm-node-details">
                <p class="propDetails" align="left">Assertion Group: <span id="ass_group"></span></p>
                <p class="propDetails" align="left">Environment: <span id="env"></span></p>
                <p class="propDetails" align="left">SPM URL: <span id="spm_url"></span></p>
            </div>
			<h3 class="accordion-trigger" id="selection-server-trigger">Server: <span id="cm_server_hostname"></span></h3>
            <div class="accordion-container" id="server-details">
                <p class="propDetails" align="left">Operating System: <span id="cm_os_name"></span> (<span id="cm_os_code"></span>)</p>
                <p class="propDetails" align="left">OS Version: <span id="cm_os_version"></span></p>
                <p class="propDetails" align="left">CPU Cores: <span id="cm_server_cpuCores"></span></p>
                <p class="propDetails" align="left">Extracted by: <span id="cm_server_extractAlias"></span></p>
            </div>
            <div class="accordion-container" id="no-cmdata-details">
			  <span id="cm_no_cmdata">
				<hr>
                <h3 align="center"><br/><br/><br/>"No Baseline nor Runtime Snapshot data available for this node"</h3>
    	      </span>
            </div>
			<h3 class="accordion-trigger" id="selection-node-trigger">Node: <span id="cm_node_name"></h3>
            <div class="accordion-container" id="node-details">
                <p class="propDetails">Version: <span id="cm_node_version"></span></p>
                <p class="propDetails">Install Time: <span id="cm_node_installtime"></span></p>
                <p class="propDetails">Latest Extraction: <span id="cm_node_ext_date"></span></p>
            </div>
			<h3 class="accordion-trigger" id="selection-comp-trigger">Component: <span id="cm_comp_id"></h3>
            <div class="accordion-container" id="comp-details">
                <p class="propDetails">Component Name: <span id="cm_comp_name"></span></p>
                <p class="propDetails">Product ID: <span id="cm_comp_pid"></span></p>
            </div>
			<h3 class="accordion-trigger" id="selection-inst-trigger">Instance: <span id="cm_inst_id"></h3>
            <div class="accordion-container" id="inst-details">
                <p class="propDetails">Instance Name: <span id="cm_inst_name"></span></p>
                <p class="propDetails">Type ID: <span id="cm_inst_type_id"></span></p>
                <p class="propDetails">Runtime ID: <span id="cm_inst_runtime_id"></span></p>
            </div>
   		    <h3 class="accordion-trigger" id="selection-cmdata-baseline-properties">Baseline Properties</h3>
			<div class="accordion-container" id="selection-config">
				<div id="cmdata-baseline-content" class="hidden"/>
			</div>
			<h3 class="accordion-trigger" id="selection-cmdata-runtime-properties">Runtime Properties</h3>
			<div class="accordion-container" id="selection-config">
				<div id="cmdata-runtime-content" class="hidden"/>
			</div>
		    <div id="nodesTableDiv">
		       <table id="all_nodes" class="display" width="90%"></table>	
		    </div>
        </section>
    </aside>
	
  <script src="js/opencm.js"></script>
  %invoke OpenCM.pub.dsp.util:hasCmdataLink%
  	%ifvar exists equals('false')%
  	  <script>notifyMissingLink("cmdata");</script>
    %endif%
  %endinvoke%
  %invoke OpenCM.pub.dsp.util:hasOutputLink%
  	%ifvar exists equals('false')%
  	  <script>notifyMissingLink("output");</script>
    %endif%
  %endinvoke%

</body>
</html>
