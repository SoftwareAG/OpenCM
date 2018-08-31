package org.opencm.extract.is;

public class ISPackage {

	private String packageName;
	private String enabled;
	private String version;

	public ISPackage(String packageName) {
		setPackageName(packageName);
	}
	
	public String getPackageName() {
		return this.packageName;
	}
	public void setPackageName(String pkgName) {
		this.packageName = pkgName;
	}
	
	public String getEnabled() {
		return this.enabled;
	}
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getVersion() {
		return this.version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
