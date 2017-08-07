<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*"
	pageEncoding="UTF-8"%>
<html>
<script type="text/javascript" src="../../js/jquery-1.7.2.min.js"></script> 
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>支付宝收银支付</title>

<!-- <p id="result">result: </p> -->
<script type="text/javascript">
// 调试时可以通过在页面定义一个元素，打印信息，使用alert方法不够优雅
//function log(obj) {
//    $("#result").append(obj).append(" ").append("<br />");
//}

$(document).ready(function(){
    // 页面载入完成后即唤起收银台
    // 此处${tradeNO}为模板语言语法，实际调用样例类似为tradePpay("2016072621001004200000000752")
    tradePay("${tradeNO}"); 

    // 点击payButton按钮后唤起收银台
    $("#payButton").click(function() {
       tradePay("${tradeNO}");
    });

    // 通过jsapi关闭当前窗口，仅供参考，更多jsapi请访问
    // https://doc.open.alipay.com/docs/doc.htm?treeId=193&articleId=104510&docType=1
    $("#closeButton").click(function() {
       AlipayJSBridge.call('closeWebview');
    });
 });

// 由于js的载入是异步的，所以可以通过该方法，当AlipayJSBridgeReady事件发生后，再执行callback方法
function ready(callback) {
     if (window.AlipayJSBridge) {
         callback && callback();
     } else {
         document.addEventListener('AlipayJSBridgeReady', callback, false);
     }
}

function tradePay(tradeNO) {
    ready(function(){
         // 通过传入交易号唤起快捷调用方式(注意tradeNO大小写严格)
         AlipayJSBridge.call("tradePay", {
              tradeNO: tradeNO
         }, function (data) {
             //log(JSON.stringify(data));
             if ("9000" == data.resultCode) {
                 //alert("支付成功");
                 AlipayJSBridge.call('closeWebview');
                 //window.location.href = "http://www.baidu.com/";
             }else if("8000" == data.resultCode){
            	 //alert("正在处理中");
            	 AlipayJSBridge.call('closeWebview');
             }else if("4000" == data.resultCode){
            	 //alert("订单支付失败");
            	 AlipayJSBridge.call('closeWebview');
             }else if("6001" == data.resultCode){
            	 //alert("用户中途取消");
            	 AlipayJSBridge.call('closeWebview');
             }else if("6002" == data.resultCode){
            	 //alert("网络连接出错");
            	 AlipayJSBridge.call('closeWebview');
             }else if("99" == data.resultCode){
            	 //alert("用户点击忘记密码快捷界面退出");//(only iOS since 9.5)
            	 AlipayJSBridge.call('closeWebview');
             }
         });
    });
}
</script>
</head>

<body>
	<font color="red" size="10"></font>
	<!-- <div style="text-align: center;margin-top: 50px;"><h1><button type="button" style="display:inline-block;width:600px;height:200px;border-radius:30px;font-size:50px" onclick="callpay()">确认支付</button></h1></div> -->
</body>
</html>