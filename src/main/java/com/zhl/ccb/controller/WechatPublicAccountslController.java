package com.zhl.ccb.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import mjson.Json;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zhl.ccb.dao.NfcOrderWaterMapper;
import com.zhl.ccb.service.CcbService;
import com.zhl.ccb.utils.Constants;
import com.zhl.ccb.utils.DSDES;
import com.zhl.ccb.utils.JsonUtil;
import com.zhl.ccb.utils.MD5;

@Controller  
@RequestMapping(value = "/nfc/channel")
public class WechatPublicAccountslController {
	Logger logger = Logger.getLogger(WechatPublicAccountslController.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	@Autowired
	private CcbService ccbService;
	@Autowired
    private NfcOrderWaterMapper nfcOrderWaterMapper; 
	
	/**
	 * NFC-微信公众号支付（发起支付时使用）---TEST
	 * 注：测试环境本地测试时的流程，真正提供该接口的时候，流程要重新设计
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/publicAccountsPayTest")
	public String publicAccountsPayTest(HttpServletRequest request,Model model){
 		String orderNo = request.getParameter("orderNo");
		String amount = request.getParameter("amount");
		logger.info("中互联_中信银行服务---收到请求(公众号支付接口)参数：orderNo="+orderNo);
		logger.info("中互联_中信银行服务---收到请求(公众号支付接口)参数：amount="+amount);
		
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_010131);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("payAccessType",Constants.pay_access_type_02);//接入支付类型
		reqMap.put("backEndUrl",Constants.getReadProperties("ccb", "ccb.online.notifyUrl"));//后台通知地址
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.wechat.active.merId"));//商户编号
		reqMap.put("secMerId","998800000083600");//分账子商户号
		reqMap.put("termId","WEB");//终端编号
		reqMap.put("termIp",Constants.getNetAddressIp());//终端IP
		reqMap.put("orderId",orderNo);//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("productId","");//商品ID
		reqMap.put("orderBody","中互联公众号支付测试");//商品描述2                                                              
		reqMap.put("orderDetail","测试");//商品详情
		reqMap.put("orderGoodsTag","");//商品标记
		reqMap.put("orderSubOpenid","o1MV4wOfD0znYFvq8mktFH0Fui5M");//用户子标识
		reqMap.put("txnAmt",amount);//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("accountFlag","Y");//分账标识
		reqMap.put("secMerFeeRate","0.003");//分账子商户交易费率
		reqMap.put("attach","");//附加数据
		reqMap.put("limitPay","");//指定支付方式
		reqMap.put("needBankType","");//是否需要获取支付行类型
		//此处做签名
		String respStr = MD5.request(orderNo,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.active.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderNo,respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.md5key"));
		String json = JsonUtil.getMapToJson(respMap);
		logger.info("中互联_中信银行服务---收到请求(公众号支付接口)参数：json="+json);
		if(respMap.get("respCode").toString().equals("0000")){//调用成功
			String appId = respMap.get("appId").toString();//公众号ID
			String timeStamp = respMap.get("timeStamp").toString();//时间戳
			String nonceStr = respMap.get("nonceStr").toString();//随机字符串
			String _package = respMap.get("package").toString();;//订单详情扩展字符串
			String signType = respMap.get("signType").toString();//签名方式
			String paySign = respMap.get("paySign").toString();//签名
			model.addAttribute("appId", appId);
			model.addAttribute("timeStamp", timeStamp);
			model.addAttribute("nonceStr", nonceStr);
			model.addAttribute("_package", _package);
			model.addAttribute("signType", signType);
			model.addAttribute("paySign", paySign);
		}
		return "/publicAccounts/pay";
	}
	/**
	 * NFC-支付宝收银（发起支付时使用）---TEST
	 * 注：测试环境本地测试时的流程，真正提供该接口的时候，流程要重新设计
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/aliPayTest")
	public String aliPayTest(HttpServletRequest request,Model model){
 		String orderNo = request.getParameter("orderNo");
		String amount = request.getParameter("amount");
		logger.info("中互联_中信银行服务---收到请求(支付宝收银接口)参数：orderNo="+orderNo);
		logger.info("中互联_中信银行服务---收到请求(支付宝收银接口)参数：amount="+amount);
		
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_010303);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("backEndUrl",Constants.getReadProperties("ccb", "ccb.online.notifyUrl"));//后台通知地址
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户标编号
		reqMap.put("secMerId","888800000011672");//分账子账户号
		reqMap.put("termId","");//终端编号
		reqMap.put("termIp",Constants.getNetAddressIp());//客户端真实IP
		reqMap.put("orderId",orderNo);//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("orderBody","支付宝收银台");//商品描述
		reqMap.put("orderDetail","支付宝二清交易创建接口");//商品详细
		reqMap.put("txnAmt",amount);//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("accountFlag","Y");//分账标识
		reqMap.put("secMerFeeRate","0.003");//分账子商户交易费率
		reqMap.put("buyerLogonId","15010001161");//买家支付宝账号
		reqMap.put("buyerId","");//买家支付宝唯一用户号
		reqMap.put("disablePayChannels","");//要禁用的信用渠道(禁用支付渠道creditCard：信用卡credit_group：所有信用渠道，包含（信用卡，花呗）)
		reqMap.put("needBankType","");//是否需要获取支付行类型

		//此处做签名
		String respStr = MD5.request(orderNo,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderNo,respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		String json = JsonUtil.getMapToJson(respMap);
		logger.info("中互联_中信银行服务---收到请求(支付宝收银接口)参数：json="+json);
		if(respMap.get("respCode").toString().equals("0000")){//调用成功
			model.addAttribute("tradeNO", respMap.get("alipayTradeNo").toString());
		}
		return "/publicAccounts/alipay";
	}
	/**
	 * NFC-微信公众号支付（发起支付时使用）
	 * 注：测试环境本地测试时的流程，真正提供该接口的时候，流程要重新设计
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/publicAccountsPay")
	public String publicAccountsPay(HttpServletRequest request,Model model){
		String mId = request.getParameter("mId");
		String data = request.getParameter("data");
		String sign = request.getParameter("sign");
		String resType = request.getParameter("resType");
		logger.info("中互联_中信银行服务---收到请求(公众号支付接口)参数：mId="+mId);
		logger.info("中互联_中信银行服务---收到请求(公众号支付接口)参数：data="+data);
		logger.info("中互联_中信银行服务---收到请求(公众号支付接口)参数：sign="+sign);
		logger.info("中互联_中信银行服务---收到请求(公众号支付接口)参数：resType="+resType);
		if(mId == null || "".equals(mId)){
			model.addAttribute("errorFrom", "mId is not null");
			return "/publicAccounts/payError";
		}else if(data == null || "".equals(data)){
			model.addAttribute("errorFrom", "data is not null");
			return "/publicAccounts/payError";
		}else if(sign == null || "".equals(sign)){
			model.addAttribute("errorFrom", "sign is not null");
			return "/publicAccounts/payError";
		}else if(resType == null || "".equals(resType)){
			model.addAttribute("errorFrom", "resType is not null");
			return "/publicAccounts/payError";
		}else{
			//解析数据
			try {
				//DES解密
				String dataDeCoding = DSDES.getWhiteData(Constants.getReadProperties("ccb", "DES.KEY").getBytes(), DSDES.hex2byte(data));
				String dataSign = MD5.MD5Encode(dataDeCoding);
				if(dataSign.equals(sign)){
					Map<String, String> map = JsonUtil.getJsonToMapStr(dataDeCoding);
					model.addAttribute("appId", map.get("appId"));
					model.addAttribute("timeStamp", map.get("timeStamp"));
					model.addAttribute("nonceStr", map.get("nonceStr"));
					model.addAttribute("_package", map.get("_package"));
					model.addAttribute("signType", map.get("signType"));
					model.addAttribute("paySign", map.get("paySign"));
				}else{
					model.addAttribute("errorFrom", "verify sign is error");
					return "/publicAccounts/payError";
				}
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("errorFrom", e.getMessage());
				return "/publicAccounts/payError";
			}
			return "/publicAccounts/pay";
		}
	}
	/**
	 * NFC-支付宝收银台支付（发起支付时使用）
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/alipayCashierDesk")
	public String alipayCashierDesk(HttpServletRequest request,Model model){
		String mId = request.getParameter("mId");
		String data = request.getParameter("data");
		String sign = request.getParameter("sign");
		String resType = request.getParameter("resType");
		logger.info("中互联_中信银行服务---收到请求(支付宝收银台支付接口)参数：mId="+mId);
		logger.info("中互联_中信银行服务---收到请求(支付宝收银台支付接口)参数：data="+data);
		logger.info("中互联_中信银行服务---收到请求(支付宝收银台支付接口)参数：sign="+sign);
		logger.info("中互联_中信银行服务---收到请求(支付宝收银台支付接口)参数：resType="+resType);
		if(mId == null || "".equals(mId)){
			model.addAttribute("errorFrom", "mId is not null");
			return "/publicAccounts/payError";
		}else if(data == null || "".equals(data)){
			model.addAttribute("errorFrom", "data is not null");
			return "/publicAccounts/payError";
		}else if(sign == null || "".equals(sign)){
			model.addAttribute("errorFrom", "sign is not null");
			return "/publicAccounts/payError";
		}else if(resType == null || "".equals(resType)){
			model.addAttribute("errorFrom", "resType is not null");
			return "/publicAccounts/payError";
		}else{
			//解析数据
			try {
				//DES解密
				String dataDeCoding = DSDES.getWhiteData(Constants.getReadProperties("ccb", "DES.KEY").getBytes(), DSDES.hex2byte(data));
				String dataSign = MD5.MD5Encode(dataDeCoding);
				if(dataSign.equals(sign)){
					Map<String, String> map = JsonUtil.getJsonToMapStr(dataDeCoding);
					model.addAttribute("tradeNO", map.get("tradeNO").toString());
				}else{
					model.addAttribute("errorFrom", "verify sign is error");
					return "/publicAccounts/payError";
				}
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("errorFrom", e.getMessage());
				return "/publicAccounts/payError";
			}
			return "/publicAccounts/alipay";
		}
	}
}
