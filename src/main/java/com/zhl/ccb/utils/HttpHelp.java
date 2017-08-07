package com.zhl.ccb.utils;	

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import com.google.common.collect.Lists;

public class HttpHelp {
	private static Logger logger = Logger.getLogger(HttpHelp.class);
	private static HttpParams httpParams;  
    private static ClientConnectionManager connectionManager; 
    private static final RequestConfig config;
    static {
        config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
    }
 
   public static HttpResponser get(String urlStr,boolean isProxy){
		HttpURLConnection conn=null;
     	URL url=null;
     	logger.info("url:"+urlStr);
   	HttpResponser reponse = new HttpResponser();
   	try {
   		url = new java.net.URL(urlStr);
   		if(isProxy){/*
   			SocketAddress sa = new InetSocketAddress(PropFileUtil.get("PROXY_HOST"), Integer.valueOf(PropFileUtil.get("PROXY_PORT")));
   			Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
   			String userName=PropFileUtil.get("PROXY_USERNAME");
   			String password =PropFileUtil.get("PROXY_PASSWORD");
   			if((!Strings.isNullOrEmpty(userName))&&(!Strings.isNullOrEmpty(password))){
   				Authenticator.setDefault(new BasicAuthenticator(userName, password));
   			}
   			conn = (HttpURLConnection) url.openConnection(proxy);
   		*/}else{
   			conn = (HttpURLConnection) url.openConnection();
   		}
   		conn.setRequestMethod("GET");		
   		conn.setDoInput(true);
   		conn.setDoOutput(true);
   		conn.setUseCaches(false);
   		conn.setConnectTimeout(3000);  
   		conn.setReadTimeout(3000); 
   		conn.connect();		
   		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
   		String line = null ;
   		StringBuffer result = new StringBuffer();
   		while((line=br.readLine())!=null){
   			result.append(line);
   		}
   		br.close();
   	    int code =conn.getResponseCode();	
   	    reponse.setCode(code);
   	    reponse.setContent(result.toString());
   	} catch (Exception e) {
   		logger.error("HttpUtil:httpGet",e);
   	}finally{
   		if(conn!=null){
   			conn.disconnect();
   		}
   	}	
		return reponse;
	}
  
   public static HttpResponser post(HttpRequestParam requestParam){
		HttpURLConnection conn=null;
    	URL url=null;
    	logger.info("url:"+requestParam.getUrl());
  	    HttpResponser reponse = new HttpResponser();
  	try {
  		url = new java.net.URL(requestParam.getUrl());
  		if(requestParam.isProxy()){/*
  			SocketAddress sa = new InetSocketAddress(PropFileUtil.get("PROXY_HOST"), Integer.valueOf(PropFileUtil.get("PROXY_PORT")));
  			Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
  			String userName=PropFileUtil.get("PROXY_USERNAME");
  			String password =PropFileUtil.get("PROXY_PASSWORD");
  			if((!Strings.isNullOrEmpty(userName))&&(!Strings.isNullOrEmpty(password))){
  				Authenticator.setDefault(new BasicAuthenticator(userName, password));
  			}
  			conn = (HttpURLConnection) url.openConnection(proxy);
  		*/}else{
  			conn = (HttpURLConnection) url.openConnection();
  		}
  		conn.setRequestMethod("POST");		
  		conn.setDoInput(true);
  		conn.setDoOutput(true);
  		conn.setUseCaches(false);
  		conn.setConnectTimeout(requestParam.getConnectTimeout());  
  		conn.setReadTimeout(requestParam.getReadTimeout());
  		Map<String,String> heads =requestParam.getHeads();
  		if(heads!=null){
  			Iterator<String> keys =heads.keySet().iterator();
  			while(keys.hasNext()){
  				String key = keys.next();
  				conn.setRequestProperty(key,heads.get(key));
  			}
  		}
  		conn.connect();
  		Object content =requestParam.getContext();
  		if(content!=null){	
  			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
  			out.writeBytes(content.toString());
  			out.flush();
  			out.close();
  		}
  		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
  		String line = null ;
  		StringBuffer result = new StringBuffer();
  		while((line=br.readLine())!=null){
  			result.append(line);
  		}
  		br.close();
  	    int code =conn.getResponseCode();	
  	    reponse.setCode(code);
  	    reponse.setContent(result.toString());
  	} catch (Exception e) {
  		logger.error("HttpUtil:httpGet",e);
  	}finally{
  		if(conn!=null){
  			conn.disconnect();
  		}
  	}	
		return reponse;
	}
   private static SSLConnectionSocketFactory setHttps(){
	   try {
		    TrustAllCertManager[] tm = {new TrustAllCertManager()};
		    SSLContext sslContext = SSLContext.getInstance("SSL","SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			 SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					 sslContext,new ProxyHostNameVerifier());
		    return sslsf;
	   }catch (Exception e) {
				
			}
		return null;
   }
   private static CloseableHttpClient getDualSSLConnection(String keyStorePath,String keyStorePass) throws AppException{
	 	CloseableHttpClient httpClient = null;
	 	try {
				File file = new File(keyStorePath);
				URL sslJksUrl = file.toURI().toURL();
				KeyStore keyStore  = KeyStore.getInstance("jks");
				InputStream is = null;
				try {
					is = sslJksUrl.openStream(); 
					keyStore.load(is, keyStorePass != null ? keyStorePass.toCharArray(): null);
				} finally {
					if (is != null) is.close();
				}
				SSLContext sslContext = new SSLContextBuilder().loadKeyMaterial(keyStore, keyStorePass != null ? keyStorePass.toCharArray(): null)
				.loadTrustMaterial(null,new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] paramArrayOfX509Certificate,
							String paramString) throws CertificateException {
						return true;
					}
				})
				.build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				httpClient =  HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(config).build();
				return httpClient;
			} catch (Exception e) {
				throw new AppException(e);
			}
	 	
	 }
	 /**
	  * 创建单向ssl的连接
	  * @return
	  * @throws AppException
	  */
	private static CloseableHttpClient getSingleSSLConnection() throws AppException{
	 	CloseableHttpClient httpClient = null;
	 	try {
				SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] paramArrayOfX509Certificate,
							String paramString) throws CertificateException {
						return true;
					}
				}).build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
				httpClient =  HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(config).build();
				return httpClient;
			} catch (Exception e) {
				throw new AppException(e);
			}
	 	
	}
   public static HttpResponser postByHttpClient(HttpRequestParam requestParam){
	   HttpResponser reponse = new HttpResponser(); 
      HttpClientBuilder clientBuilder = HttpClients.custom();
      CloseableHttpClient httpclient = null;
      CloseableHttpResponse response = null;
      try {
    	  HttpPost post = new HttpPost(requestParam.getUrl());
    	  logger.debug("url:"+requestParam.getUrl());
    	  if(requestParam.isProxy()){/*
    		    String userName=PropFileUtil.get("PROXY_USERNAME");
    			String password =PropFileUtil.get("PROXY_PASSWORD");
    			if((!Strings.isNullOrEmpty(userName))&&(!Strings.isNullOrEmpty(password))){
    				CredentialsProvider credsProvider = new BasicCredentialsProvider();
    				credsProvider.setCredentials(
    						new AuthScope(PropFileUtil.get("PROXY_HOST"), Integer.valueOf(PropFileUtil.get("PROXY_PORT")),"macn"),
    						new UsernamePasswordCredentials(userName, password));
    				clientBuilder.setDefaultCredentialsProvider(credsProvider);
    			}
    		  HttpHost proxy = new HttpHost(PropFileUtil.get("PROXY_HOST"), Integer.valueOf(PropFileUtil.get("PROXY_PORT")));
    		  RequestConfig config = RequestConfig.custom()
    				  .setProxy(proxy)
    				  .build();
    		  post.setConfig(config);
    	  */}
    	  if(requestParam.getUrl().contains("https")){
    		  clientBuilder.setSSLSocketFactory(setHttps());
    	  }
    	     httpclient = clientBuilder.build();
			Map<String,String> heads =requestParam.getHeads();
	  		if(heads!=null){
	  			Iterator<String> keys =heads.keySet().iterator();
	  			while(keys.hasNext()){
	  				String key = keys.next();
	  				post.addHeader(key,heads.get(key));
	  			}
	  		}
          HttpEntity entity = new StringEntity((String) requestParam.getContext(),Charset.forName(requestParam.getEncoding()));
          post.setEntity(entity);
          response = httpclient.execute(post);
          reponse.setCode(response.getStatusLine().getStatusCode());
          String content =EntityUtils.toString(response.getEntity(),Charset.forName(requestParam.getEncoding()));
          EntityUtils.consume(entity);
          reponse.setContent(content);
          logger.debug("code"+response.getStatusLine()+",content:"+content);
      } catch(Exception e){
    	  reponse.setCode(404);
    	  reponse.setContent(e.getMessage());
    	  logger.error("HttpUtil:post",e);
      	  System.out.println("error:"+e.getMessage());
      }finally {
          try {
        	   if(response!=null){   
        		   response.close();
        	   }
        	   if(httpclient!=null){   
        		   httpclient.close();
        	   }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      }
	
		return reponse;
	}
   @SuppressWarnings("deprecation")
public static HttpResponser postParamByHttpClient(HttpRequestParam requestParam){
	   HttpResponser reponse = new HttpResponser(); 
      HttpClientBuilder clientBuilder = HttpClients.custom();
      CloseableHttpClient httpclient = null;
      CloseableHttpResponse response = null;
      try {
    	  HttpPost post = new HttpPost(requestParam.getUrl());
    	  logger.debug("url:"+requestParam.getUrl());
    	  if(requestParam.isProxy()){/*
    		    String userName=PropFileUtil.get("PROXY_USERNAME");
    			String password =PropFileUtil.get("PROXY_PASSWORD");
    			if((!Strings.isNullOrEmpty(userName))&&(!Strings.isNullOrEmpty(password))){
    				CredentialsProvider credsProvider = new BasicCredentialsProvider();
    				credsProvider.setCredentials(
    						new AuthScope(PropFileUtil.get("PROXY_HOST"), Integer.valueOf(PropFileUtil.get("PROXY_PORT")),"macn"),
    						new UsernamePasswordCredentials(userName, password));
    				clientBuilder.setDefaultCredentialsProvider(credsProvider);
    			}
    		  HttpHost proxy = new HttpHost(PropFileUtil.get("PROXY_HOST"), Integer.valueOf(PropFileUtil.get("PROXY_PORT")));
    		  RequestConfig config = RequestConfig.custom()
    				  .setProxy(proxy)
    				  .build();
    		  post.setConfig(config);
    	  */}
    	  if(requestParam.getUrl().contains("https")){
    		  clientBuilder.setSSLSocketFactory(setHttps());
    	  }
    	     httpclient = clientBuilder.build();
			Map<String,String> heads =requestParam.getHeads();
	  		if(heads!=null){
	  			Iterator<String> keys =heads.keySet().iterator();
	  			while(keys.hasNext()){
	  				String key = keys.next();
	  				post.addHeader(key,heads.get(key));
	  			}
	  		}
	  		if(requestParam.getContext()!=null){	
	  			HttpEntity entity = new StringEntity((String) requestParam.getContext(),Charset.forName(requestParam.getEncoding()));
	  			post.setEntity(entity);
	  		}
	  	  Map<String,String> params = requestParam.getParams();
	  	  if(params!=null){
	  		  List<NameValuePair> pairs = Lists.newArrayList();
	  		  Set<String> keySet = params.keySet();
	  		  for(String key:keySet){
	  			  pairs.add(new BasicNameValuePair(key,params.get(key)));
	  		  }
	  		 
	  		  post.setEntity(new UrlEncodedFormEntity(pairs,requestParam.getEncoding()));
	  	  }
	 
          response = httpclient.execute(post);
          reponse.setCode(response.getStatusLine().getStatusCode());
          String content =EntityUtils.toString(response.getEntity(),Charset.forName(requestParam.getEncoding()));
          EntityUtils.consume(response.getEntity());
          reponse.setContent(content);
          logger.debug("code"+response.getStatusLine()+",content:"+content);
      } catch(Exception e){
    	  reponse.setCode(404);
    	  reponse.setContent(e.getMessage());
    	  logger.error("HttpUtil:post",e);
      	  System.out.println("error:"+e.getMessage());
      }finally {
          try {
        	   if(response!=null){   
        		   response.close();
        	   }
        	   if(httpclient!=null){   
        		   httpclient.close();
        	   }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      }
	
		return reponse;
	}
   
   public static HttpResponser getByHttpClient(HttpRequestParam requestParam){
	   HttpResponser reponse = new HttpResponser(); 
      HttpClientBuilder clientBuilder = HttpClients.custom();
      CloseableHttpClient httpclient = null;
      CloseableHttpResponse response = null;
      try {
    	  HttpGet post = new HttpGet(requestParam.getUrl());
    	  logger.debug("url:"+requestParam.getUrl());
    	  if(requestParam.isProxy()){/*
    		    String userName=PropFileUtil.get("PROXY_USERNAME");
    			String password =PropFileUtil.get("PROXY_PASSWORD");
    			if((!Strings.isNullOrEmpty(userName))&&(!Strings.isNullOrEmpty(password))){
    				CredentialsProvider credsProvider = new BasicCredentialsProvider();
    				credsProvider.setCredentials(
    						new AuthScope(PropFileUtil.get("PROXY_HOST"), Integer.valueOf(PropFileUtil.get("PROXY_PORT")),"macn"),
    						new UsernamePasswordCredentials(userName, password));
    				clientBuilder.setDefaultCredentialsProvider(credsProvider);
    			}
    		  HttpHost proxy = new HttpHost(PropFileUtil.get("PROXY_HOST"), Integer.valueOf(PropFileUtil.get("PROXY_PORT")));
    		  RequestConfig config = RequestConfig.custom()
    				  .setProxy(proxy)
    				  .build();
    		  post.setConfig(config);
    	  */}
    	  if(requestParam.getUrl().contains("https")){
    		  clientBuilder.setSSLSocketFactory(setHttps());
    	  }
    	     httpclient = clientBuilder.build();
			Map<String,String> heads =requestParam.getHeads();
	  		if(heads!=null){
	  			Iterator<String> keys =heads.keySet().iterator();
	  			while(keys.hasNext()){
	  				String key = keys.next();
	  				post.addHeader(key,heads.get(key));
	  			}
	  		}
          response = httpclient.execute(post);
          reponse.setCode(response.getStatusLine().getStatusCode());
          String content =EntityUtils.toString(response.getEntity(),Charset.forName(requestParam.getEncoding()));
          EntityUtils.consume(response.getEntity());
          reponse.setContent(content);
          logger.debug("code"+response.getStatusLine()+",content:"+content);
      } catch(Exception e){
    	  reponse.setCode(404);
    	  reponse.setContent(e.getMessage());
    	  logger.error("HttpUtil:post",e);
      	  System.out.println("error:"+e.getMessage());
      }finally {
          try {
        	   if(response!=null){   
        		   response.close();
        	   }
        	   if(httpclient!=null){   
        		   httpclient.close();
        	   }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      }
	
		return reponse;
	}
}
