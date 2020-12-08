package org.opencm.util;

import java.util.ArrayList;

import org.apache.http.HttpEntity; 
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;


import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import com.wm.app.b2b.server.ServiceException;

public class HttpClient {

	private int HTTP_CONNECTION_TIMEOUT = 30000;
	private int HTTP_REQUEST_TIMEOUT = 30000;
	private int HTTP_SOCKET_TIMEOUT = 30000;
	
	private String url;
	private String username;
	private String password;
	private int statusCode;
	private String statusLine;
	private String response;
	private boolean isSSL = false;
	private boolean isJson = false;
	private boolean isXmlOrText = false;

	private ArrayList<BasicNameValuePair> params;
	private CloseableHttpClient httpClient;
	private CloseableHttpClient httpsClient;

	public HttpClient() {
	}

	public void setURL(String url) {
		this.url = url;
		if (url.toLowerCase().startsWith("https")) {
			this.isSSL = true;
		}
	}
	
	public String getURL() {
		return this.url;
	}

	public void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void setJsonContent() {
		this.isJson = true;
		this.isXmlOrText = false;
	}
	public void setXmlorTextContent() {
		this.isJson = false;
		this.isXmlOrText = true;
	}

	private CloseableHttpClient getHttpClient() {
		if (this.httpClient == null) {
			this.httpClient = HttpClients.createDefault();
		}
		return this.httpClient;
	}
	
	private CloseableHttpClient getHttpsClient() throws ServiceException {
		if (this.httpsClient == null) {
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
			    sslContext.init(null, new TrustManager[]{new X509TrustManager() {
			        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
			        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
			        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
			    }}, new java.security.SecureRandom());
		        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
		        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
				this.httpsClient = HttpClients.custom().setSSLSocketFactory(connectionFactory).build();
			} catch (KeyManagementException | NoSuchAlgorithmException ex) {
				throw new ServiceException("SSL Exception: " + ex.toString());
			}
		}
		return this.httpsClient;
	}

	public void get() throws ServiceException {
		
		try {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.username,this.password));
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setCredentialsProvider(credentialsProvider);
			
			RequestConfig reqConfig = RequestConfig.custom()
					.setConnectTimeout(HTTP_CONNECTION_TIMEOUT)
					.setConnectionRequestTimeout(HTTP_REQUEST_TIMEOUT)
					.setSocketTimeout(HTTP_SOCKET_TIMEOUT)
					.build();
			
			CloseableHttpClient client;
			if (this.isSSL) {
				client = getHttpsClient();
			} else {
				client = getHttpClient();
			}

			HttpGet httpGet = new HttpGet(this.url);
			httpGet.setConfig(reqConfig);
			httpGet.addHeader("DoNotCreateSession", "true");
			// httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");

			if (isJson) {
				httpGet.addHeader("Accept", "application/json");
			}
			if (isXmlOrText) {
				httpGet.addHeader("Accept", "text/*, application/xml");
			}
			CloseableHttpResponse httpResp = client.execute(httpGet,localContext);
			
			try {
			    this.statusLine = httpResp.getStatusLine().toString();
			    this.statusCode = httpResp.getStatusLine().getStatusCode();
			    HttpEntity respEntity = httpResp.getEntity();
			    this.response = EntityUtils.toString(respEntity, Charset.defaultCharset());
			    EntityUtils.consume(respEntity);
			} finally {
				httpResp.close();
			}
		} catch (Exception ex) {
			throw new ServiceException("HttpClient :: " + ex.toString());
		}
	}

	public void addParameter(String name, String value) {
		if (this.params == null) {
			this.params = new ArrayList<BasicNameValuePair>();
		}
		this.params.add(new BasicNameValuePair(name, value));
	}
	
	public void flushParameters() {
		this.params = null;
	}
	
	public void post() throws ServiceException {
		
		try {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.username,this.password));
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setCredentialsProvider(credentialsProvider);
			
			RequestConfig reqConfig = RequestConfig.custom()
					.setConnectTimeout(HTTP_CONNECTION_TIMEOUT)
					.setConnectionRequestTimeout(HTTP_REQUEST_TIMEOUT)
					.setSocketTimeout(HTTP_SOCKET_TIMEOUT)
					.build();
			
			CloseableHttpClient client;
			if (this.isSSL) {
				client = getHttpsClient();
			} else {
				client = getHttpClient();
			}

			HttpPost httpPost = new HttpPost(this.url);
			httpPost.setConfig(reqConfig);
			httpPost.addHeader("DoNotCreateSession", "true");
			
			if (this.params != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			}

			CloseableHttpResponse httpResp = client.execute(httpPost,localContext);
			
			try {
			    this.statusLine = httpResp.getStatusLine().toString();
			    this.statusCode = httpResp.getStatusLine().getStatusCode();
			    HttpEntity respEntity = httpResp.getEntity();
			    this.response = EntityUtils.toString(respEntity);
			    EntityUtils.consume(respEntity);
			} finally {
				httpResp.close();
			}
		} catch (Exception ex) {
			throw new ServiceException("HttpClient :: " + ex.toString());
		}
	}

	public void put() throws ServiceException {
		
		try {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.username,this.password));
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setCredentialsProvider(credentialsProvider);
			
			RequestConfig reqConfig = RequestConfig.custom()
					.setConnectTimeout(HTTP_CONNECTION_TIMEOUT)
					.setConnectionRequestTimeout(HTTP_REQUEST_TIMEOUT)
					.setSocketTimeout(HTTP_SOCKET_TIMEOUT)
					.build();
			
			CloseableHttpClient client;
			if (this.isSSL) {
				client = getHttpsClient();
			} else {
				client = getHttpClient();
			}

			HttpPut httpPut = new HttpPut(this.url);
			httpPut.setConfig(reqConfig);
			httpPut.addHeader("DoNotCreateSession", "true");
			
			if (this.params != null) {
				httpPut.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			}

			CloseableHttpResponse httpResp = client.execute(httpPut,localContext);
			
			try {
			    this.statusLine = httpResp.getStatusLine().toString();
			    this.statusCode = httpResp.getStatusLine().getStatusCode();
			    HttpEntity respEntity = httpResp.getEntity();
			    this.response = EntityUtils.toString(respEntity);
			    EntityUtils.consume(respEntity);
			} finally {
				httpResp.close();
			}
		} catch (Exception ex) {
			throw new ServiceException("HttpClient :: " + ex.toString());
		}
	}
	
	public void delete() throws ServiceException {
		
		try {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.username,this.password));
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setCredentialsProvider(credentialsProvider);
			
			RequestConfig reqConfig = RequestConfig.custom()
					.setConnectTimeout(HTTP_CONNECTION_TIMEOUT)
					.setConnectionRequestTimeout(HTTP_REQUEST_TIMEOUT)
					.setSocketTimeout(HTTP_SOCKET_TIMEOUT)
					.build();
			
			CloseableHttpClient client;
			if (this.isSSL) {
				client = getHttpsClient();
			} else {
				client = getHttpClient();
			}

			HttpDelete httpDelete = new HttpDelete(this.url);
			httpDelete.setConfig(reqConfig);
			httpDelete.addHeader("DoNotCreateSession", "true");
			
			CloseableHttpResponse httpResp = client.execute(httpDelete,localContext);
			
			try {
			    this.statusLine = httpResp.getStatusLine().toString();
			    this.statusCode = httpResp.getStatusLine().getStatusCode();
			    HttpEntity respEntity = httpResp.getEntity();
			    this.response = EntityUtils.toString(respEntity);
			    EntityUtils.consume(respEntity);
			} finally {
				httpResp.close();
			}
		} catch (Exception ex) {
			throw new ServiceException("HttpClient :: " + ex.toString());
		}
	}
	
	public String getStatusLine() {
		return this.statusLine;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public String getResponse() {
		return this.response;
	}

	public void setResponse(String resp) {
		this.response = resp;
	}
	

}
