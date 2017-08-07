<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.Map"%>
<%@ page import="com.alipay.api.*"%>
<%@ page import="com.alipay.api.request.*"%>
<%@ page import="com.alipay.api.response.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>支付宝页面跳转同步通知页面</title>
</head>
<body>
<%
	out.print("lalalalalalala..........");
 	String app_id = request.getParameter("app_id");
 	String auth_code = request.getParameter("auth_code");
 	out.write("app_id = "+app_id);
 	out.write("auth_code = "+auth_code);
 	System.out.println("app_id = "+app_id);
 	System.out.println("auth_code = "+auth_code);
 
 //生成的秘钥
 String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCU0s9dIDk+2qvpGwiwf8poD6wiC9WTjcpcY21vNTFBuDUpuE/rfrhTrRV5h/VrZTLwDs5jQhVpNwrLrBzoOCUzXmsTj4y/qcz7/r5VNbpIr3PD+oU4zTFLrAT/2r2ftTpQuPHaXBKYePCXek07Nhe8qo1ncQ8Wg4TaQjk/ssYfSKELfLE5DxhXDJXe7vtDCKJmVkJ5xOZ1Di+eGSXv429iIW1IW5vVTMB6NqkDWtBbSBt+P3c6g+t1ir3w+/9oXclKWlqfI0URIrr3pnSruZMQbG1pXNM6IUSI+aFbLD0tNTZvWdxCGXWjNrQgmjYw+qbdBVjE226CIqCvW/AZIjHlAgMBAAECggEBAJMtV/1U3MgYIWLKZXGHL3hx511ULsdZtgJgId2U0T2ZGppDuGXAUDIQPeiOWFr5HcLiIaFvvRTsZjS95WiR/dgrS+FwWVVCa4HakAhctfkj6myp2A4wfsvDNr6AaH8FDsK95BB6dblsASMMxEwGVrsyP91Ipg8xybS/QTaJaf2Mp/tZS0OZ8cE2QpVxpinEEpzMPa2ouXUD8gjOs5u4gifewtE31o4/jWSsXDULaa12fKQnscl3hdogHTrm5ZFzH2902zk7EnzMZLRUPt52UmmtkgB7jeMAQHl2X+Fl07UdAxI8JUg/1Y6WVyNw8nozG2q+Q0TJfH5gBos/hzNQkRECgYEAxibUo7ftettrcvyzPArKBFEqtZMvVbHxveoY6IapLhJCPXnm890Uyz3FGu0h6pr/NF3z0df6CTivAKmmatTpv5z6gvmqsZJMZtdDcmr5v1z1ny04Z/4//X0d83aoVca78qwH+Vnw26lYpW3XJe4smipMxoxeZGeeRm85v+7osp8CgYEAwEVcRI/yTlSezHopLx1IIJlqtQoz5WzO94nAA1qcziXLRMnQ4QN1pAqWzo8nSTi5Ww2CLarM8I+4HSuCw+TtxRDAmp7LZgJTUBhDFMJhP1fj1uqkXrnzr6HtPjjs60qJiZmUoCLtsZIs5/dxNtzUEBZ09PzDX7zW5spS75S68PsCgYBk3gVmZc1kuedDfHF8wf0+H1c/YPI6jD+DrnQJgesDAdZoVvKC9uqL3yDD6SxXsZjxvlXMX0XvhcRH8RQ+0609lhpLq+4BQHjV7QRvbRc/G4IJCkrpXRqT3int8lwUdfrcuEfC3c7m8pimdXiM3WYlAB1fALtYHwsJzbq3AlW//wKBgENWx4b3x0xp1KgWzN/EaKOKN9YqOZzLq/0EUKycCrkkgXmZGUAFfykx0DmN55t8c2aRZ5to65gBLeGBgK3tOt0/DcXZgbE7dURjCvN1VKvjs1dtHJ5UkCVeGAIn28pYc60ujeA7+3WqlXG0twEY/GiaBOJcuGyKbdWs3LkuJP8LAoGAVyJ3lVhL3kPkIPaa51ubVaWl9PBeSIuiT4tc8V5lDBfCYQPzl9RDfzWeRgQqBAKRHHGEbeM7thdLCKM4IvT29R7Uyl1dXZ1Ov4cbVPCatBYyQ5jn3MhHZtrHOl0EUk3iChs9ADd/e6bCzxfN2GSfnFHCTWT6I4yC1m0I5UdycXg=";
 //支付宝公钥
 String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvGBVTNVEJFZxX1vh2CpN13d49zbO6hGRWK7mHw3PzdAH7s+n6jDJvkaECi70LeI11NLCrmQH19s1EVrSOzGqJt7zy/Vyk6axsq5PM3HGa0mug63o4YLOCmij3afthXeCa72j0yPF2Kl0bRnIK0a688Kj56JhhUB20p36u90d7t6ksYhjDa5qFTgF2vUP/EJ8iPRawwpiMdsS5kibTtjuIRLWk3tL1AA89t5xq7lTeBicdF1GJ+52alfwPQ8nuQ0npmZZ2EFIVzHgIxFjNQsq7km6Z53iGJVplUrDA9rhE9KM4CW0X/GdsAust/L3H1gbk+4ugYZ+kKy3U8MsYLXBlQIDAQAB";
                     
 //使用auth_code换取接口access_token及用户userId      
  AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","2017050507123024",privateKey,"json","UTF-8",publicKey,"RSA2");//正常环境下的网关
 
  
  //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipaydev.com/gateway.do","2016080500176123",privateKey,"json","UTF-8",publicKey,"RSA2");//沙箱下的网关
  AlipaySystemOauthTokenRequest requestqw = new AlipaySystemOauthTokenRequest();
  requestqw.setCode(auth_code);
  requestqw.setGrantType("authorization_code");
  try {
	  	AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(requestqw);
	    System.out.println("userId="+oauthTokenResponse.getUserId());
	    System.out.println("access_token="+oauthTokenResponse.getAccessToken());
	    out.write("userId = "+oauthTokenResponse.getUserId());
	 	out.write("access_token = "+oauthTokenResponse.getAccessToken());
	    
	} catch (AlipayApiException e) {
	    //处理异常
	    e.printStackTrace();
	}
%>
</body>
</html>