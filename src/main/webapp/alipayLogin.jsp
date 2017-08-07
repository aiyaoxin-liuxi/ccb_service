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
<title>支付宝授权！！</title>
</head>
<body>
<!-- https://openauth.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=2016080500176123&scope=auth_base&redirect_uri=http%3A%2F%2Fweixin.zhonghulian.cn%2Fccb_service%2FalipayUser.jsp&state=accredit -->
<!-- https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2017050507123024&scope=auth_base&redirect_uri=http%3A%2F%2Fweixin.zhonghulian.cn%2Fccb_service%2FalipayUser.jsp&state=accredit -->

<a href="https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2017050507123024&scope=auth_base&redirect_uri=http%3A%2F%2Fweixin.zhonghulian.cn%2Fccb_service%2FalipayUser.jsp&state=accredit">支付宝第三方登录</a><br/>
</body>
</html>