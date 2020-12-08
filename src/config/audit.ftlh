<html>
<head>
  <title>OpenCM Audit Result</title>
  <style type="text/css">
	body {
			font-family: Arial, Verdana, sans-serif;
			font-size: 90%;
			color: #666;
			background-color: #ffffff;
	}
	h1 {
			color: #3498db;
			font-size: 1.0em;
	}
    table.audit_mail {
	 border-spacing: 0px;
    }
	.audit_mail th, .audit_mail td {
		padding: 5px 30px 5px 10px;
		border-spacing: 0px;
		font-size: 90%;
		margin: 0px;
	}
	.audit_mail th, .audit_mail td {
		text-align: left;
		background-color: #e0e9f0;
		border-top: 1px solid #f1f8fe;
		border-bottom: 1px solid #cbd2d8;
		border-right: 1px solid #cbd2d8;
	}
	.audit_mail tr.head th {
		color: #fff;
		background-color: #90b4d6;
		border-bottom: 2px solid #547ca0;
		border-right: 1px solid #749abe;
		border-top: 1px solid #90b4d6;
		text-align: center;
		text-shadow: -1px -1px 1px #666666;
		letter-spacing: 0.15em;
	}
	.audit_mail td {
		text-shadow: 1px 1px 1px #ffffff;
	}
	.audit_mail tr.even td, .audit_mail tr.even th {
		background-color: #e8eff5;
	}
	.audit_mail tr.head th:first-child {
		-webkit-border-top-left-radius: 5px;
		-moz-border-radius-topleft: 5px;
		border-top-left-radius: 5px;
	}
	.audit_mail tr.head th:last-child {
		-webkit-border-top-right-radius: 5px;
		-moz-border-radius-topright: 5px;
		border-top-right-radius: 5px;
	 }
  </style>
</head>
<body>
  <h1>${audit_name}</h1>
  <p>Total number of properties audited: <b>${result.numPropertiesAudited}</b> - Total Differences: <b>${result.numPropertiesDifferent}</b></p>
	<table class="audit_mail">
		<tr class="head">
			<th>Property</th>
			<th>Inventory Location</th>
			<th>Value</th>
		</tr>
		<#list result.auditProperties as auditProperty>
		  <tr class="even">
			<th>${auditProperty.propertyName}</th>
			<td>
				<#list auditProperty.auditValues as auditValue>
					${auditValue.path?last} -> ${auditValue.component} -> ${auditValue.instance} </br>
				</#list>
			</td>
			<td>
				<#list auditProperty.auditValues as auditValue>
					<#if auditValue.governanceValue ??>
						<#if auditValue.value == auditValue.governanceValue>
							<span style="color: green;">${auditValue.value}</br>
						<#else>
							<span style="color: red;">${auditValue.value}</br>
						</#if>
					<#else>
						${auditValue.value}</br>
					</#if>
				</#list>
			</td>
		  </tr>
		</#list>
	</table>    
</body>
</html>