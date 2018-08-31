package org.opencm.audit.assertion;

import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import org.testng.ITest;
import org.testng.ITestContext;

import org.opencm.audit.assertion.AssertionStore;
import org.opencm.audit.assertion.TestWrapper;
import org.opencm.audit.env.AssertionValuePair;
import com.wm.app.b2b.server.ServiceException;

public class TwoNodeTest implements ITest {
	/**
	 * 
	 */	
	private String methodName;
	private TestWrapper testWrapper;
    
	/** 
     * Sets up the test fixture. 
     * (Called before every test case method.)
     */
    @BeforeTest
    public void setUp(ITestContext testCase) throws ServiceException  {
    	String wrapperKey = testCase.getCurrentXmlTest().getLocalParameters().get(AssertionStore.ASSERTION_PARAMETER_TESTKEY);
		this.testWrapper = AssertionStore.assertionWrappers.get(wrapperKey);
		if (this.testWrapper == null) {
			throw new ServiceException("Test Exception: @BeforeTest - testWrapper NULL for " + testCase.getName());
		}
        
		this.methodName = this.testWrapper.getTestName();
	}

    /**
     * Tears down the test fixture. 
     * (Called after every test case method.)
     */
    @AfterTest
    public void tearDown() {
    	this.methodName = null;
    }

    @Override
    public String getTestName() {
        return this.methodName;
    }

    /*
     *  Pass all the Instances to the Test method 
     */
    @DataProvider(name = "Properties")
    public Iterator<AssertionValuePair> instances() {
    	return this.testWrapper.getAssertionValuePairs().iterator(); 
    }

    @BeforeMethod (alwaysRun = true)
    public void setTestName(Object[] pairs) {
        if (pairs != null && pairs.length > 0) {
        	AssertionValuePair pair = null;
            for (Object pairObject : pairs) {
                if (pairObject instanceof AssertionValuePair) {
                    pair = (AssertionValuePair) pairObject;
                    break;
                }
            }
            if (pair.getAssertionValue_01() != null) {
                this.methodName = String.format("%s", pair.getAssertionValue_01().getPropertyName());
            } else {
                this.methodName = String.format("%s", pair.getAssertionValue_02().getPropertyName());
            }
        }
    }

    @Test(dataProvider = "Properties")
    public void assertProperty(AssertionValuePair pair) {
    	Assert.assertEquals(pair.getAssertionValue_01().getValue(),pair.getAssertionValue_02().getValue());
    }
    
}
