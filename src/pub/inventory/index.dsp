<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset //EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="" lang="">
<head>
  <title>OpenCM</title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
  <meta name="description" content="webMethods Configuration Management Repository" />
  <link rel="stylesheet" type="text/css" href="../css/opencm.css" />
  <script src="../js/jquery-1.10.2.min.js"></script>
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
						 {"name": "%value name%"}
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
	  
	var opencmTableData = Array();
	var arrEnvs = Array();
	var arrLayers = Array();
	var arrServers = Array();
	var totalNodes = 0;
	$.each(opencm_nodes, function( index, env ) {
		var envServers = 0;
		var envNodes = 0;
		if ($.inArray(env.name, arrEnvs) == -1) {
			arrEnvs.push(env.name);
		}
		$.each(env.layers, function( index2, layer ) {
			var layServers = 0;
			var layNodes = 0;
			if ($.inArray(layer.name, arrLayers) == -1) {
				arrLayers.push(layer.name);
			}
			$.each(layer.servers, function( index, server ) {
				envServers++;
				layServers++;
				if ($.inArray(server.name, arrServers) == -1) {
					arrServers.push(server.name);
				}
				$.each(server.nodes, function( index, node ) {
					opencmTableData.push([ env.name, layer.name, server.name, node.name ]);
					envNodes++;
					layNodes++;
					totalNodes++;
				})
			})
		})
	});
   </script>
</head>
<section id="generalDetails">
	<frameset cols="20%,*" border="0">
	  <frame src="menu.dsp" name="menu" />
	  <frame src="result.dsp" name="result" />
	</frameset>
</section>
	
</html>
        