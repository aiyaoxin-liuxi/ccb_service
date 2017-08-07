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
	<title>微信公众号支付对外流程测试样例</title> 
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0"> 
		<!-- 
		<link rel="stylesheet" type="text/css" href="styles.css"> 
		--> 
</head> 
<body> 
	<!-- http://weixin.zhonghulian.cn/ccb_service/nfc/channel/publicAccountsPay -->
	<form action="http://channel.zhonghulian.cn/ccb_service/nfc/channel/publicAccountsPay" method="POST"> 
		商户号：<input type="text" name="mId" value="888800000011672"/>
	   	主数据: <input type="text" name="data" value="8A7D7C076B8183496EB646A73885F2EE94AD478CD4327C53703532B3FA13324D2EF34712EF5DFF22CF65F429E2A3E17DCBD397C10C7B154A38CAE754CEBA9AE10EC7F47509C0E75B1BE4A8DEFF721333C8B6EDF0C6B4A981AE33B747AFEB4B5E19F25BC3C75AC1B4C7D1F4C7AEFCF73809FEDB6036E51CFA0DE0EBD96FC9ED9F571220E211F280727F0CA5D31E50CC981E40E0B822C10D8E801553C7F476D74EA279E4941977627F041A7E19928FF4F354088216DD8E8405A44EE2B73E5DCAE67F4AFC7A1BDA4F01CE37D3A5CCD315500E9983B93952E7D3904BDC42BAD24CA99C2D1982C8EC67C5"/>
	   	加密数据：<input type="text" name="sign" value="2737739300e0ea2f244c8f3b382683a6"/>
	   	状态: <input type="text" name="resType" value="1"/>
		<input type="submit" value="支付"/> 
	</form> 
</body> 
</html>