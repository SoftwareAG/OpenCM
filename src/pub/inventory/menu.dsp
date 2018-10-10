<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="" lang="">
<head>
  <title>OpenCM Configuration Management Respoitory - Menu</title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
  <meta name="description" content="About OpenCM." />
  <link rel="stylesheet" type="text/css" href="../css/opencm.css" />
  <script src="../js/jquery-1.10.2.min.js"></script>
   <script>
	$(document).ready(function() {
		$('#inventory_total_environments').text(parent.window.arrEnvs.length);
		$('#inventory_total_layers').text(parent.window.arrLayers.length);
		$('#inventory_total_servers').text(parent.window.arrServers.length);
		$('#inventory_total_installations').text(parent.window.totalNodes);
	}); 
   </script>
</head>


<body style="margin-top: 0;">

<section id="popupArea">
  <h2></h2>
  <div>
    </br></br>
    <h3>Summary</h3>
	<table class="bordered">
		<thead>
			<tr><th width="50%">Total</th><th>#</th></tr>
		</thead>
		<tr><td>Environments</td><td><span id="inventory_total_environments"></td></tr>        
		<tr><td>Layers</td><td><span id="inventory_total_layers"></td></tr>        
		<tr><td>Servers</td><td><span id="inventory_total_servers"></td></tr>        
		<tr><td>Installations</td><td><span id="inventory_total_installations"></td></tr>        
	</table>
  </div>
</section>
</body>
</html>
