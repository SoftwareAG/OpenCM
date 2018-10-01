package org.opencm.extract.is;

import java.util.LinkedList;
import org.opencm.extract.configuration.ExtractKeyValue;

public class WxConfigInfo {

	private LinkedList<ExtractKeyValue> keyValues;

	public WxConfigInfo() {
	}
	
	public LinkedList<ExtractKeyValue> getKeyValues() {
		return this.keyValues;
	}
	public void setKeyValues(LinkedList<ExtractKeyValue> keyValues) {
		this.keyValues = keyValues;
	}
	

}
