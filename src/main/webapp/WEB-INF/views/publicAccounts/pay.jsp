<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*"
	pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>微信公众号支付</title>
<!-- ${appId }
${timeStamp }
${nonceStr }
${_package }
${signType }
${paySign } -->
<script type="text/javascript">
function callpay()
{
	
	if (typeof WeixinJSBridge == "undefined") {
	      if (document.addEventListener) {
	        document.addEventListener('WeixinJSBridgeReady', onBridgeReady,
	            false);
	      } else if (document.attachEvent) {
	        document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
	        document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
	      }
	    } else {
	      onBridgeReady();
	    }
}

function onBridgeReady() {
	var appId = '${appId }';
	var timeStamp = '${timeStamp }';
	var nonceStr = '${nonceStr }';
	var _package = '${_package }';
	var signType = '${signType }';
	var paySign = '${paySign }';
	
	//alert("appId="+appId+",timeStamp="+timeStamp+",nonceStr="+nonceStr+",package="+_package+",signType="+signType+",paySign="+paySign);
    WeixinJSBridge.invoke('getBrandWCPayRequest', {
      "appId" : ""+appId+"",
      "timeStamp" : ""+timeStamp+"",
      "nonceStr" : ""+nonceStr+"",
      "package" : ""+_package+"",
      "signType" : ""+signType+"",
      "paySign" : ""+paySign+""
    }, function(res) { // 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回  ok，但并不保证它绝对可靠。  
      //alert(res.err_msg);
      if (res.err_msg == "get_brand_wcpay_request:ok") {
        //alert("支付成功");
        WeixinJSBridge.call('closeWindow');
      }
      if (res.err_msg == "get_brand_wcpay_request:cancel") {
        //alert("交易取消");
    	WeixinJSBridge.call('closeWindow');
      }
      if (res.err_msg == "get_brand_wcpay_request:fail") {
        //alert("支付失败");
    	WeixinJSBridge.call('closeWindow');
      }
    });
  }
</script>
</head>

<body onload="callpay()">
	<font color="red" size="10"></font>
	<!-- <div style="text-align: center;margin-top: 50px;"><h1><button type="button" style="display:inline-block;width:600px;height:200px;border-radius:30px;font-size:50px" onclick="callpay()">确认支付</button></h1></div> -->
</body>
</html>