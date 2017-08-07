package com.zhl.ccb.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import mjson.Json;
import org.apache.commons.codec.binary.Base64;


/**
 * 中信银行杭州分行-移动收单平台接口签名/验签示例代码
 * @author phio 
 * @version 1.0 20160811
 */
public class RSA {
	
	/**
	 * 默认的http连接超时时间
	 */
	private final static int DEFAULT_CONN_TIMEOUT = 10000;	//10s
	/**
	 * 默认的http read超时时间
	 */
	private final static int DEFAULT_READ_TIMEOUT = 120000;	//120s
	/**
	 * 请求的目标URL
	 * 配置在此处仅为演示方便，正式生产代码中，应该做外置可配置处理
	 */
	//private static final String reqUrl = "https://120.27.165.177:8099/MPay/backTransAction.do";
	
	
	
	
	/**RSA加密方式
	 * 用于数据电子签名使用的rsa私钥，由商户自行生成
	 * 配置在此处仅为演示方便，正式生产代码中，商户应该将其外置于安全的地方，且妥善保护该私钥，如有泄露，请第一时间通知我行进行变更！！！
	 */
	private static final String rsaPrivate = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJqjsAJD43b39RoZGO0AxUztv1gqyB5zl8dywdpWiFHay7DFCS8zm0GPil9EBPjjRZQ7r+4v8fFW1j9FGTH6PVHkvAjVcTUC1TLRLISEtRczRI770SCJB/6uNhGkomPy68jaUaA6LVpdsA38RqRLUtrrG6/mjby0XyjJ9i0Bhoy/AgMBAAECgYBHVasj4F1VzBxs5Zbx/aEGU8YozdNy+K/TecSjC7fmyp+b88jA1XWRUL3sJFyG05CNSNtnbQaW5g//L8jnnSAbp2slxaqXcSVnFKpDnyJ2OdG7bYljSh1piAcKEhOYPWJb24kDdY73H1B2cocNuvO25bPT5LQDk5MYj/g0AkAXQQJBAMmMIWwC+kkLffs+5JMYM0kTfyfCMGu/U77a0r0F8cgwylRJlqO6Ti8svyNAQArmVgfsMyEMz7MhUemClVr1I+0CQQDEazPi9UPoN+wHTSPCaZBIsXUtgXvn5Yd1nihnmNYC8YREAjhXoZ1v0gtIak9esVSB0OeqRZSr9vIEApBQS/XbAkEAsWyv22L/rB/2xp/GHouWUkVckcaMU735XjFKhWZfQ2lYJD0n5HhU7YiJtuGPv0ioAY94LycVDM9PSa5QBuN4vQJALGIxClLzslCYpBm5P+AMu3BmEw9USVVjY5+Gb8poaafbhGagBqU9ZxwmcombP8GAYAktoBsy+hzGGUPfClXvywJAOk27nQVMeLvI19PjKwblOBrnfTC+xNce58eXQq79beWNSm3cK6BgvU4U/rtthhoqW/disaeNp/utnHVoisNUCA==";
	private static PrivateKey privateKey = readPriKey(rsaPrivate);
	private static PrivateKey readPriKey(String pk){
		try {
			byte [] kfbs = pk.getBytes("utf-8");
			byte [] keyBytes = Base64.decodeBase64(kfbs);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			return factory.generatePrivate(keySpec);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	/**
	 * 用于数据电子验签使用的rsa公钥，由我行提供
	 * 配置在此处仅为演示方便，正式生产代码中，应该外置于安全的地方。
	 */
	private static final String rsaPublic = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCGU1a93qYpghRXnapLVMxeXvIoova/qx9H4BCOQR0wir6rbCCLjxouxShrhk8kXeWtnZDgvZ8dmLIPQpeuWQOBMjQgZ7uZJEBWlw8cdtvjAasUjl/NYT5WDkTIpA2KZVEzBk2x/I28TO17Bg9r3FFd7tF+KOBpr2OWvjb+spE5vQIDAQAB";
	private static PublicKey publicKey = readPubKey(rsaPublic);
	private static PublicKey readPubKey(String pk){
		try {
			byte [] kfbs = pk.getBytes("utf-8");
			byte [] keyBytes = Base64.decodeBase64(kfbs);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			return factory.generatePublic(keySpec);
		}catch(Exception e){	
			System.out.println(e);
			return null;
		}
	}

	
	/**
	 * 对Map报文进行签名，并发送
	 * @param reqMap
	 * @return
	 */
	public static String request(String reqUrl,Map<String, String> reqMap){
		//将reqMap排序
		SortedMap<String, String> sm = new TreeMap<String, String>(reqMap);
		//按序拼接
		StringBuilder sb = new StringBuilder();
		for(Entry<String, String> sme : sm.entrySet()){
			String v = sme.getValue();
			//空字段不参加签名
			if(null == v || v.length()==0)
				continue;
			sb.append("&").append(sme.getKey()).append("=").append(v);
		}
		//System.out.println(sb.substring(1));
		//使用私钥签名
		try {
			//转成byte数组，按实际情况使用字符集，此处使用utf-8
			byte[] reqBs = sb.substring(1).getBytes("UTF-8");
			Signature signature = Signature.getInstance("SHA1WithRSA");
			signature.initSign(privateKey);
			signature.update(reqBs);
			byte[] signedBs = signature.sign();
			//对签名数据进行base64编码,并对一些特殊字符进行置换
			String signedStr = Base64.encodeBase64String(signedBs);
			//System.out.println(signedStr);
			//将签名信息加入原始请求报文map
			reqMap.put("signAture", signedStr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//将Map转成Json
		Json reqJs = Json.make(reqMap);
		//生成json字符串
		String reqStr = reqJs.toString();
		//System.out.println(reqStr);
		//再将json字符串用base64编码,并对一些特殊字符进行置换
		String b64ReqStr = null;
		try {
			b64ReqStr = Base64.encodeBase64String(reqStr.getBytes("utf-8")).replaceAll("\\+", "#");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		//生成最后的报文
		String finalB64ReqStr = "sendData=" + b64ReqStr;
		System.out.println("req :"+finalB64ReqStr);
		//HTTP POST方式发送报文，并获取返回结果
		String respStr = postReq(reqUrl,finalB64ReqStr);
		return respStr;	
	}
	
	/**
	 * 解析返回的报文，并验签:
	 * @param finalRespStr
	 * @return
	 */
	public static Map<String, Object> getResp(String finalRespStr){
		assert finalRespStr.startsWith("sendData=");
		String respB64Str = finalRespStr.substring(9);
		//base64解码,并对一些特殊字符进行置换
		byte [] respJsBs = Base64.decodeBase64(respB64Str.replaceAll("#","+"));
		String respJsStr = null;
		try {
			respJsStr = new String(respJsBs,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		//解析json
		Json respJs = Json.read(respJsStr);
		//转成map方便排序
		SortedMap<String, Object> sm = new TreeMap<String, Object>(respJs.asMap());
		//按序拼接
		StringBuilder sb = new StringBuilder();
		for(Entry<String, Object> sme : sm.entrySet()){
			//排除signAture字段
			if("signAture".equals(sme.getKey()))
				continue;
			String v = (String)sme.getValue();
			//空字段不参加验签
			if(null == v || v.length()==0)
				continue;
			sb.append("&").append(sme.getKey()).append("=").append(v);
		}
		//System.out.println(sb.substring(1));
		//使用我行提供的公钥验签
		try {
			//将签名转换成byte数组
			String respSign = respJs.at("signAture").toString();
			byte [] respSignBs = Base64.decodeBase64(respSign);
			//将待验签的转成byte数组，按实际情况使用字符集，此处使用utf-8
			byte[] respBs = sb.substring(1).getBytes("UTF-8");
			Signature signature = Signature.getInstance("SHA1WithRSA");
			signature.initVerify(publicKey);
			signature.update(respBs);
			boolean verified = signature.verify(respSignBs);
			if(!verified)
				throw new RuntimeException("response signature verify failed");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return respJs.asMap();
	}
	
	
	/**
	 * http post,有返回String
	 * @param requrl
	 * @param req
	 * @param connTimeOut
	 * @param readTimeOut
	 * @return
	 */
	public static String postReq(String requrl,String req,int connTimeOut,int readTimeOut){
		try {
			HttpURLConnection conn = null;
			try {
				URL url = new URL(requrl);
				conn = (HttpURLConnection)url.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(true);	//POST
				conn.setRequestMethod("POST");
				conn.setUseCaches(false);
				conn.setConnectTimeout(connTimeOut);
				conn.setReadTimeout(readTimeOut);
				conn.connect();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),"utf-8");
			out.write(req);
			out.flush();
			out.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			StringBuilder sb = new StringBuilder();
			char[] buff = new char[2048];
			int cnt = 0;
			while((cnt = in.read(buff))!=-1)
				sb.append(buff,0,cnt);
			in.close();
			String rtStr = sb.toString();
			return rtStr;
		} catch (IOException e) {
			System.out.println(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 标准http post,有返回String
	 * @param requrl
	 * @param req
	 * @return
	 */
	public static String postReq(String requrl,String req){
		return postReq(requrl, req,DEFAULT_CONN_TIMEOUT,DEFAULT_READ_TIMEOUT);
	}
	
	
	public static void main(String[] args) {
		//构建演示用报文！！！注意，此为演示用报文，请勿用于生产！！！
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("backEndUrl", "http://www.baidu.com");	//后台通知地址,商户端接收支付网关异步通知回调地址，此处配置仅做演示
		reqMap.put("channelType", "6002");	//接入渠道
		reqMap.put("currencyType", "156");	//交易币种
		reqMap.put("encoding", "UTF-8");	//编码方式
		reqMap.put("merId", "886600000000004");	//测试商户编号
		reqMap.put("orderBody", "测试产品");	//商品描述
		reqMap.put("orderTime", "20160607143922");	//订单生成时间
		reqMap.put("payAccessType", "02");	//接入支付类型
		reqMap.put("productId", "YLCS888");	//商品ID
		reqMap.put("signMethod", "03");	//签名方法
		reqMap.put("termId", "WEB");	//终端编号
		reqMap.put("termIp", "127.0.0.1");	//终端IP
		reqMap.put("txnAmt", "100");	//交易金额
		reqMap.put("txnSubType", "010130");	//交易子类型
		reqMap.put("txnType", "01");	//交易类型
		reqMap.put("orderId", System.currentTimeMillis()+"");	//商户订单号,此处取当前时间戳仅作演示用，生产环境请勿如此使用
		
		//报文通讯
		String reqUrl = "https://120.27.165.177:8099/MPay/backTransAction.do";
		String respStr = request(reqUrl,reqMap);
		System.out.println("resp:"+respStr);
		//解析返回报文
		Map<String, Object> respMap = getResp(respStr);
		System.out.println(Json.make(respMap).toString());
	}
}
