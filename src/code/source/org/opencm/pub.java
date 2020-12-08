package org.opencm;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import org.opencm.util.JsonParser;
import org.opencm.util.JsonUtils;
import org.opencm.util.LogUtils;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import org.opencm.configuration.Configuration;
import org.opencm.repository.RepoInstallation;
import org.opencm.secrets.SecretsConfiguration;
import org.opencm.secrets.SecretsUtils;
// --- <<IS-END-IMPORTS>> ---

public final class pub

{
	// ---( internal utility methods )---

	final static pub _instance = new pub();

	static pub _newInstance() { return new pub(); }

	static pub _cast(Object o) { return (pub)o; }

	// ---( server methods )---




	public static final void test (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(test)>> ---
		// @sigtype java 3.5
		// [o] field:0:required out
		// --------------------------------------------------------------------
		// Read in OpenCM Properties
		// --------------------------------------------------------------------
		// Configuration opencmConfig = Configuration.instantiate();
		
		// String json = "{\"@alias\":\"local\",\"Transport\":{\"Host\":\"MCHHANSSON04.eur.ad.sag\",\"Port\":\"5555\"},\"Auth\":{\"@type\":\"BASIC\",\"User\":\"Administrator\"},\"ExtendedProperties\":{\"Property\":[{\"@name\":\"acl\",\"$\":\"Administrators\"},{\"@name\":\"retryServer\",\"$\":\"\"},{\"@name\":\"keepalive\",\"$\":\"1\"},{\"@name\":\"timeout\",\"$\":\"5.0\"}]}}";
		
		// JsonParser.getProperties(opencmConfig, json);
		
		// String path = "[OpenCM, Acme, Dev Zone, User Acceptance Test, API, srv249.acme.local, ACME_UAT_API_V105_APIGW_01]";
		// path = path.replaceAll("\\s","").replaceAll(",","_").replaceAll("\\[","").replaceAll("\\]","");
		
		// String json = JsonUtils.convertJavaObjectToJson(pathArray);
		// System.out.println(json);
		/**
		String installTime = "2020-03-13T13:54:17.877+01:00";
		DateTimeFormatter inFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
		LocalDate date = LocalDate.parse(installTime,inFormat);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String newDate = date.format(formatter);
		System.out.println(newDate);
		
		SecretsConfiguration secConfig = SecretsConfiguration.instantiate();
		secConfig.setMasterPassword("manage");
		org.linguafranca.pwdb.kdbx.simple.SimpleDatabase db = SecretsUtils.getDatabase(secConfig);
		SecretsUtils.test(secConfig, db); 
		
		ArrayList<String> path01 = new ArrayList<String>();
		path01.add("path_01");
		path01.add("path_02");
		path01.add("path_03");
		path01.add("path_04");
		
		ArrayList<String> path02 = new ArrayList<String>();
		path02.add("path_01");
		path02.add("path_02");
		path02.add("path_03");
		path02.add("path_04");
		String strPath = path01.toString();
		strPath = strPath.replaceAll("\\s","").replaceAll(",","_").replaceAll("\\[","").replaceAll("\\]","");
		System.out.println(strPath);
		if (path01.equals(path02)) {
			System.out.println("Both paths equal");
		} else {
			System.out.println("Paths are not equal");
		}
		**/
		
		// PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("conf/log4j.properties"));
		
		// LocalDate dateTime = LocalDate.parse(dateInString, formatter);
		
		//DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		
			
		// --- <<IS-END>> ---

                
	}
}

