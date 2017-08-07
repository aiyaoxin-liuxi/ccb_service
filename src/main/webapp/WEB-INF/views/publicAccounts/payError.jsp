<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<!-- Viewport metatags -->
<meta name="HandheldFriendly" content="true" />
<meta name="MobileOptimized" content="320" />
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<link rel="stylesheet" type="text/css" href="<%=basePath%>css/dandelion.css"  media="screen" />

<title>错误</title>

</head>
	<body>
		<div id="da-wrapper" class="fluid">
        <!-- Content -->
        <div id="da-content">
            <!-- Container -->
            <div class="da-container clearfix">
            	<div id="da-error-wrapper">
                   	<div id="da-error-pin"></div>
                    <div id="da-error-code">
                    	error <span>参数错误</span>
					</div>
                	<h1 class="da-error-heading">${errorFrom }</h1>
                    <p>请检查参数</p>
                </div>
            </div>
        </div>
        <!-- Footer -->
        <div id="da-footer">
        	<div class="da-container clearfix">
           	<p> 2017 5 All Rights Reserved<a href="http://www.mycodes.net/" target="_blank">跳转地址名称</a></div>
        </div>
    </div>
	</body>
</html>