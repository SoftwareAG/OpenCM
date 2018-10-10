<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="" lang="">
<head>
  <title>OpenCM Configuration Management Respoitory - Main</title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
  <meta name="description" content="Assertion Result" />
  <link rel="stylesheet" type="text/css" href="../css/opencm.css" />
  <link rel="stylesheet" type="text/css" href="../css/datatables.min.css"/>
  <script src="../js/jquery-1.10.2.min.js"></script>
  <script type="text/javascript" src="../js/datatables.min.js"></script>
   <script>
	$(document).ready(function() {
		$('#all_nodes').DataTable( {
			data: parent.window.opencmTableData,
			ordering: true,
			columns: [
				{ title: "Environment" },
				{ title: "Layer" },
				{ title: "Hostname" },
				{ title: "Node Name",
				  render: function(data, type, row, meta){
					return '<a target="_top" href="/OpenCM/?node='+data+'">' + data + '</a>';
				  }
				}
			],
			columnDefs: [
				{ 
				targets: '_all',
				className: 'dt-head-left' }
			]
		} );
		
	}); 
   </script>
  </head>
<body>

<section id="popupArea">
  <h2>Infrastructure Inventory</h2>
  <div>
	<table id="all_nodes" class="display" width="90%"></table>	
  </div>
</section>
</body>
</html>
