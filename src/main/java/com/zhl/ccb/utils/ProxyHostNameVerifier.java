package com.zhl.ccb.utils;	

import javax.net.ssl.SSLSession;

public class ProxyHostNameVerifier implements javax.net.ssl.HostnameVerifier,
		com.sun.net.ssl.HostnameVerifier {
	public boolean verify(String arg0, SSLSession arg1) {
		return true;
	}

	public boolean verify(String arg0, String arg1) {
		return true;
	}
}
