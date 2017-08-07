package com.zhl.ccb.utils;

import java.util.Map;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class RSATester {
	static String publicKey;  
    static String privateKey;  
  
    static {  
        try {  
            Map<String, Object> keyMap = RSAUtils.genKeyPair();  
            publicKey = RSAUtils.getPublicKey(keyMap);  
            privateKey = RSAUtils.getPrivateKey(keyMap);  
            System.err.println("公钥: \n\r" + publicKey);  
            System.err.println("私钥： \n\r" + privateKey);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		test();  
        //testSign();  
	}
	public static byte[] decryptBASE64(String key) throws Exception {               
	       return (new BASE64Decoder()).decodeBuffer(key);               
	    }
	public static String encryptBASE64(byte[] key) throws Exception {               
	       return (new BASE64Encoder()).encodeBuffer(key);               
	    }
	static void test() throws Exception {  
        System.err.println("公钥加密——私钥解密");  
        String source = "qqqqqqqqqqqqqqqqqqwwwwwwwwwwwwwwwwwwwwwwwwwwww";  
        System.out.println("\r加密前文字：\r\n" + source);  
        byte[] data = source.getBytes();  
        
        //String publicKey_str = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCWCRQZQpO6t3n1Fp5bbOH1SE/bO/glwP1rDrSqTvvD+lBC9D59KfY+agtvaIHnh93s4iMdWeLnPSOxvjkJvsr5n/cZgbrxKMT0rWFNR44qjtllkQVuvaBcXaY2j6bHk+npQ0vYpoahjpHLXdUTGhcM+Qfrn5HREq+cChC/ae/DrwIDAQAB";
        //byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey_str);  
        byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
        System.out.println("加密后文字：\r\n" + new String(encodedData)); 
//        String aaa = encryptBASE64(encodedData);
//        System.out.println("aaa:"+aaa);
//        String privateKey_str = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJYJFBlCk7q3efUWnlts4fVIT9s7+CXA/WsOtKpO+8P6UEL0Pn0p9j5qC29ogeeH3eziIx1Z4uc9I7G+OQm+yvmf9xmBuvEoxPStYU1HjiqO2WWRBW69oFxdpjaPpseT6elDS9imhqGOkctd1RMaFwz5B+ufkdESr5wKEL9p78OvAgMBAAECgYEAhMIHvMpCeec7zPh0qyVPU4Dm2HDaBF4eXwqmJkS5VhI5zuTcHlhxAY1IDQ6GU97PKnp249PTiRV6bh6Jzeu7IYHLzGxV1fz7KC8reO/nxM14SdViId7B5VgHcvyPC9iqzaPL9tXXvRof2jE1xxkF0sKTPf6ycR57/Ot3x8Q3+qECQQDsbci+RuIVtMJq3nzj7owv4UqsEcYaGblxhq8tgr2MQ6P7MrbnsleFv0i5TRf1b6SXF5/5kQQ9GkyX2o1r66mpAkEAonSDXM4UPGMkAhhntsgh3cJZAjGmtckZ+7P6G9fgo+m7hLY1K5nxORq/qjDWHEDeEGJY78AwhQoK6H3/OZHJlwJBAIb7Bqjbfyg1UuoAq/zVrGOJlEA4xYkRNraB1nF9Owphc7Vuds5BG4bYdoSUZyFQ4/7/Fe49xNW9tgzcUfFTUkECQCauoZzWgSED5k4Na5ytOMsp/LG2CuEcOnChoTtCAv5kqD7od+6i4DpdsSegK0tc8sNp941W8Pastn43ii82FI8CQQDdJtFW8x0pZ+IiF3OA6R/HWRxFj41qUfri+o46dkcA965P37WCg6AP5wvR1FfjOQ8ZXTBjylyQB2By0VbzA4hB";
        
        
//        byte[] bbb = decryptBASE64(aaa);
//        byte[] decodedData = RSAUtils.decryptByPrivateKey(bbb, privateKey_str);
        byte[] decodedData = RSAUtils.decryptByPrivateKey(encodedData, privateKey);  
        String target = new String(decodedData);  
        System.out.println("解密后文字: \r\n" + target);  
    } 
	
//	static void testSign() throws Exception {  
//        System.err.println("私钥加密——公钥解密");  
//        String source = "这是一行测试RSA数字签名的无意义文字";  
//        System.out.println("原文字：\r\n" + source);  
//        byte[] data = source.getBytes();  
//        byte[] encodedData = RSAUtils.encryptByPrivateKey(data, privateKey);  
//        System.out.println("加密后：\r\n" + new String(encodedData));  
//        byte[] decodedData = RSAUtils.decryptByPublicKey(encodedData, publicKey);  
//        String target = new String(decodedData);  
//        System.out.println("解密后: \r\n" + target);  
//        System.err.println("私钥签名——公钥验证签名");  
//        String sign = RSAUtils.sign(encodedData, privateKey);  
//        System.err.println("签名:\r" + sign);  
//        boolean status = RSAUtils.verify(encodedData, publicKey, sign);  
//        System.err.println("验证结果:\r" + status);  
//    }
}
