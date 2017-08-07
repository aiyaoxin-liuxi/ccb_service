package com.zhl.ccb.utils;

import java.util.Map;

public class HttpRequestParam {

	private String url;
	
	private Object context;
	
	private boolean isProxy;
	
	private int connectTimeout = 3000;
	
	private int readTimeout = 3000;
	
	private Map<String,String> heads;
	
	private Map<String,String> params;
	
    private String encoding="UTF-8";
    
    private String respEncoding = "UTF-8";
    
 

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
	}

	public boolean isProxy() {
		return isProxy;
	}

	public void setProxy(boolean isProxy) {
		this.isProxy = isProxy;
	}

	public Map<String, String> getHeads() {
		return heads;
	}

	public void setHeads(Map<String, String> heads) {
		this.heads = heads;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Map<String,String> getParams() {
		return params;
	}

	public void setParams(Map<String,String> params) {
		this.params = params;
	}

	public String getRespEncoding() {
		return respEncoding;
	}

	public void setRespEncoding(String respEncoding) {
		this.respEncoding = respEncoding;
	}




	
	
}
