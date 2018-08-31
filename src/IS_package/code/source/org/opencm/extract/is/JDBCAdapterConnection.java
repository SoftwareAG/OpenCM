package org.opencm.extract.is;

public class JDBCAdapterConnection {


	private String connectionAlias;
	private String packageName;
	private String connectionState;
	
	private String connectionType;
	private String transactionType;
	private String dataSourceClass;
	private String serverName;
	private String user;
	private String databaseName;
	private String portNumber;
	private String networkProtocol;
	private String otherProperties;
	
	private String enableConnectionPooling;
	private String minimumPoolSize;
	private String maximumPoolSize;
	private String poolIncrementSize;
	private String blockTimeout;
	private String expireTimeout;
	private String startupRetryCount;
	private String startupBackoffTimeout;
	
	public JDBCAdapterConnection() {
	}
	
	public String getConnectionAlias() {
		return this.connectionAlias;
	}
	public void setConnectionAlias(String name) {
		this.connectionAlias = name;
	}
	
	public String getPackageName() {
		return this.packageName;
	}
	public void setPackageName(String name) {
		this.packageName = name;
	}
	
	public String getConnectionState() {
		return this.connectionState;
	}
	public void setConnectionState(String state) {
		this.connectionState = state;
	}

	public String getConnectionType() {
		return this.connectionType;
	}
	public void setConnectionType(String type) {
		this.connectionType = type;
	}
	
	public String getTransactionType() {
		return this.transactionType;
	}
	public void setTransactionType(String name) {
		this.transactionType = name;
	}
	
	public String getDataSourceClass() {
		return this.dataSourceClass;
	}
	public void setDataSourceClass(String name) {
		this.dataSourceClass = name;
	}

	public String getServerName() {
		return this.serverName;
	}
	public void setServerName(String name) {
		this.serverName = name;
	}

	public String getUser() {
		return this.user;
	}
	public void setUser(String name) {
		this.user = name;
	}

	public String getDatabaseName() {
		return this.databaseName;
	}
	public void setDatabaseName(String name) {
		this.databaseName = name;
	}

	public String getPortNumber() {
		return this.portNumber;
	}
	public void setPortNumber(String name) {
		this.portNumber = name;
	}

	public String getNetworkProtocol() {
		return this.networkProtocol;
	}
	public void setNetworkProtocol(String name) {
		this.networkProtocol = name;
	}

	public String getOtherProperties() {
		return this.otherProperties;
	}
	public void setOtherProperties(String name) {
		this.otherProperties = name;
	}

	public String getEnableConnectionPooling() {
		return this.enableConnectionPooling;
	}
	public void setEnableConnectionPooling(String name) {
		this.enableConnectionPooling = name;
	}

	public String getMinimumPoolSize() {
		return this.minimumPoolSize;
	}
	public void setMinimumPoolSize(String name) {
		this.minimumPoolSize = name;
	}

	public String getMaximumPoolSize() {
		return this.maximumPoolSize;
	}
	public void setMaximumPoolSize(String name) {
		this.maximumPoolSize = name;
	}

	public String getPoolIncrementSize() {
		return this.poolIncrementSize;
	}
	public void setPoolIncrementSize(String name) {
		this.poolIncrementSize = name;
	}

	public String getBlockTimeout() {
		return this.blockTimeout;
	}
	public void setBlockTimeout(String name) {
		this.blockTimeout = name;
	}

	public String getExpireTimeout() {
		return this.expireTimeout;
	}
	public void setExpireTimeout(String name) {
		this.expireTimeout = name;
	}

	public String getStartupRetryCount() {
		return this.startupRetryCount;
	}
	public void setStartupRetryCount(String name) {
		this.startupRetryCount = name;
	}

	public String getStartupBackoffTimeout() {
		return this.startupBackoffTimeout;
	}
	public void setStartupBackoffTimeout(String name) {
		this.startupBackoffTimeout = name;
	}

}
