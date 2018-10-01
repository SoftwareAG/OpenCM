package org.opencm.audit.assertion;

import java.util.HashMap;
import java.util.LinkedList;

import org.opencm.configuration.Configuration;
import org.opencm.audit.env.AssertionValuePair;


public class AssertionStore {
	
	public final static String ASSERTION_PARAMETER_TESTKEY 		= "ASSERTION_PARAMETER_TESTKEY";
	
	public final static String ANY_ASSERTION_KEYWORD	= "ANY";
	public final static String ASSERTION_NOT_PRESENT 	= "ABSENT";
	
	public static HashMap<String,TestWrapper> assertionWrappers;

	public static Configuration opencmConfig;

	public AssertionStore(Configuration opencmConfig) {
		this.opencmConfig = opencmConfig;
		this.assertionWrappers = new HashMap<String,TestWrapper>();
	}
	
	public TestWrapper getTestWrapper(String testName) {
	  	return this.assertionWrappers.get(testName);
	 }
	public void setTestWrapper(TestWrapper testWrapper) {
		this.assertionWrappers.put(testWrapper.getTestName(), testWrapper);
	}
	
	public Configuration getConfiguration() {
		return this.opencmConfig;
	}
	public void setConfiguration(Configuration opencmConfig) {
		this.opencmConfig = opencmConfig;
	}

}
