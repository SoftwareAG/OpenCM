<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset //EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="" lang="">
<head>
  <title>OpenCM</title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
  <meta name="description" content="webMethods Configuration Management Repository" />
  <script type="text/javascript">
 	var audit_prop_templates = [
	   %invoke OpenCM.pub.dsp.audit:getAuditProps%
		%loop propertyFiles%
		 "%value propertyFile encode(html)%"
		 %loopsep ','%
		%endloop%
	   %endinvoke%
	  ];
  </script>

</head>
<frameset cols="20%,*" border="0">
  <frame src="menu.html" name="menu" />
  <frame src="result.html" name="result" />
</frameset>
</html>
        