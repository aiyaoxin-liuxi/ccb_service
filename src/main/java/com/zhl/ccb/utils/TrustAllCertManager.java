package com.zhl.ccb.utils;

import javax.net.ssl.*;
import java.security.cert.*;

public class TrustAllCertManager implements X509TrustManager{
	public void checkClientTrusted(X509Certificate[] chain,String authType) throws CertificateException{
	}
	public void checkServerTrusted(X509Certificate[] chain,  String authType)   throws CertificateException{
	
	}
	public X509Certificate[] getAcceptedIssuers(){
		return new java.security.cert.X509Certificate[0];  
	}

}