package test;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

//import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.google.common.collect.Maps;
import com.zhl.ccb.utils.HttpHelp;
import com.zhl.ccb.utils.HttpRequestParam;
import com.zhl.ccb.utils.HttpResponser;
import com.zhl.ccb.utils.JsonUtil;

public class SendTest {
	public static void main(String[] args) throws UnknownHostException {
		/**
		 * 模拟充值时通道方调用我的异步通知
		 */
		Map<String,String> map = new HashMap<String,String>();
		map.put("orderId", "panTest_1483587715715");//收银台订单号（外部通道订单号）
		map.put("oriordercore", "170105114227");//我方调用通道传过去的订单号（下游系统上送的订单号）
		map.put("paystatus", "000000");//支付结果
		map.put("amount", "5500");//支付金额，单位为分
		map.put("plattime", "");//交易时间）
		HttpRequestParam http = new HttpRequestParam();
		http.setUrl("http://localhost:8080/ccb_service/nfc/ccb/nfcPay");
		Map<String,String> heads = Maps.newHashMap();
		heads.put("Content-Type", "application/json;charset=UTF-8");
		http.setContext(JsonUtil.getMapToJsonStr(map));
		System.out.println("订单号："+map.get("orderId")+"发送商户的报文："+JsonUtil.getMapToJsonStr(map));
		http.setHeads(heads);
		HttpResponser resp=HttpHelp.postParamByHttpClient(http);
		System.out.println("订单号："+map.get("orderId")+"发送商户结果："+resp.getContent());
		
		
		//System.out.println(org.pub.util.uuid.KeySn.getKey());

		//http://blog.csdn.net/heng_ji/article/details/39007227

	}
}
