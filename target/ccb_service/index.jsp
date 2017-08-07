<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%> 
<!-- <script language="JavaScript" src="js/jquery.js"></script> -->
<% 
String path = request.getContextPath(); 
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/"; 
%> 
  
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"> 
<html> 
<head> 
	<base href="<%=basePath%>"> 
	<title>微信公众号支付样例</title> 
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0"> 
		<!-- 
		<link rel="stylesheet" type="text/css" href="styles.css"> 
		--> 
</head> 
<body> 
	<%
		String orderNo = System.currentTimeMillis()+"";
	%>
	<form action="nfc/channel/publicAccountsPayTest" method="POST"> 
		订单号：<input type="text" name="orderNo" value="<%=orderNo%>"/>
	   	金额 : <input type="text" name="amount" value="1"/>
		<input type="submit" value="公众号支付"/> 
	</form> 
</body> 
</html>