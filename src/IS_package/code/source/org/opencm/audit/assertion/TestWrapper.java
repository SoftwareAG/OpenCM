package org.opencm.audit.assertion;

import java.util.LinkedList;
import org.opencm.audit.env.AssertionValuePair;

public class TestWrapper {

	
	private String testName;
	private LinkedList<AssertionValuePair> assertionValuePairs;

	public TestWrapper(String testName, LinkedList<AssertionValuePair> pairs) {
		setTestName(testName);
		setAssertionValuePairs(pairs);
	}
	
    public String getTestName() {
        return this.testName;
    }
    public void setTestName(String name) {
        this.testName = name;
    }

    public LinkedList<AssertionValuePair> getAssertionValuePairs() {
        return this.assertionValuePairs;
    }
    public void setAssertionValuePairs(LinkedList<AssertionValuePair> pairs) {
        this.assertionValuePairs = pairs;
    }

}
