package com.zhl.ccb.service.Impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mjson.Json;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.google.common.collect.Maps;
import com.zhl.ccb.utils.Constants;
import com.zhl.ccb.utils.HttpHelp;
import com.zhl.ccb.utils.HttpRequestParam;
import com.zhl.ccb.utils.HttpResponser;
import com.zhl.ccb.utils.JsonUtil;
import com.zhl.ccb.utils.MD5;

@Service("CcbTestService")
public class CcbTestService {
	Logger logger = Logger.getLogger(CcbTestService.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	/**
	 * 主扫订单预支付（消费）交易接口(微信)---(中互联被扫)
	 */
	public void zhlNfcWechatPassive(){
		String orderId = System.currentTimeMillis()+"";
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding", Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod", Constants.sing_method_MD5);//签名方式
		reqMap.put("txnType", Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType", Constants.trade_sub_type_010130);//交易子类型
		reqMap.put("channelType", Constants.channel_type_6002);//接入渠道
		reqMap.put("payAccessType", Constants.pay_access_type_02);//接入支付类型
		reqMap.put("backEndUrl", "http://www.baidu.com");//后台通知地址
		reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.wechat.passive.merId"));//商户标编号
		reqMap.put("secMerId", "998800000083600");//分账子账户号
		reqMap.put("termId", "WEB");//终端编号
		reqMap.put("termIp", Constants.getNetAddressIp());//终端IP
		reqMap.put("orderId", orderId);//商户订单号
		reqMap.put("orderTime", sdf.format(new Date()));//交易起始日期
		reqMap.put("productId", "");//商品ID
		reqMap.put("orderBody", "主扫订单预支付（消费）交易接口(微信)");//商品描述
		reqMap.put("orderDetail", "");//商品详情
		reqMap.put("orderGoodsTag", "");//商品标记
		reqMap.put("txnAmt", "1");//交易金额
		reqMap.put("currencyType", Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
		reqMap.put("secMerFeeRate", "0.003");//分账子商户交易费率
		reqMap.put("attach", "");//附加数据
		reqMap.put("limitPay", "no_credit");//指定支付方式
		reqMap.put("needBankType", "");//是否需要获取支付行类型
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.passive.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.wechat.passive.md5key"));
		System.out.println("!!!!!!!!!!!!!!!="+Json.make(respMap).toString());
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
		System.out.println("#############="+respMap);
	}
	/**
	 * 支付宝二清主扫消费---(中互联被扫)
	 */
	public void zhlNfcAlipayPassive(){
		String orderId = System.currentTimeMillis()+"";
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding", Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod", Constants.sing_method_MD5);//签名方式
		reqMap.put("txnType", Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType", Constants.trade_sub_type_010302);//交易子类型
		reqMap.put("channelType", Constants.channel_type_6002);//接入渠道
		reqMap.put("backEndUrl", "http://www.baidu.com");//后台通知地址
		reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户标编号
		reqMap.put("secMerId", "998800000083600");//分账子账户号
		reqMap.put("termId", "WEB");//终端编号
		reqMap.put("termIp", Constants.getNetAddressIp());//终端IP
		reqMap.put("orderId", orderId);//商户订单号
		reqMap.put("orderTime", sdf.format(new Date()));//交易起始日期
		reqMap.put("orderBody", "支付宝二清主扫消费");//商品描述
		reqMap.put("orderDetail", "");//商品详情
		reqMap.put("txnAmt", "1");//交易金额
		reqMap.put("currencyType", Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
		reqMap.put("secMerFeeRate", "");//分账子商户交易费率
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		System.out.println("!!!!!!!!!!!!!!!="+Json.make(respMap).toString());
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
		System.out.println("#############="+respMap);
	}
	/**
	 * 线上交易状态查询接口
	 */
	public void zhlNfcOnlineQuery(){
		String orderId = "8022017052216245522479586";
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_38);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_383000);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("payAccessType",Constants.pay_access_type_02);//接入支付类型
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.wechat.public.merId"));//商户编号
		reqMap.put("secMerId","888800000011672");//分账子商户号
		reqMap.put("origOrderId",orderId);//原始商户订单号
		reqMap.put("origOrderTime",sdf.format(new Date()));//原始商户交易时间
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("fetchOrderNo","");//是否获取订单号标识
		reqMap.put("wxOrderNo","");//微信订单号
		reqMap.put("accountFlag","Y");//分账标识
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
		System.out.println("!!!!!!!!!!!!!!!="+Json.make(respMap).toString());
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
		System.out.println("#############="+respMap);
	}
	/**
	 * 支付宝二清交易查询接口
	 */
	public void zhlNfcAlipayOnlineQuery(){
		String orderId = "9012017041211243325546383";//1490002572369
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_38);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_381004);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户标编号
		reqMap.put("secMerId","");//分账子账户号
		reqMap.put("seqId","9012017041211243325546383");//中信流水号
		reqMap.put("accountFlag","");//分账标识
		reqMap.put("fetchOrderNo","");//是否获取订单号标识
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		System.out.println("!!!!!!!!!!!!!!!="+Json.make(respMap).toString());
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
		System.out.println("#############="+respMap);
	}
	/**
	 * 线下-被扫订单支付（消费）交易接口---(中互联主扫)
	 */
	public void zhlNfcActive(){
		String orderId = System.currentTimeMillis()+"";
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("stdmsgtype",Constants.trade_type_48);//消息类型
		reqMap.put("stdprocode",Constants.trade_sub_type_481000);//交易码
		reqMap.put("std400chnl",Constants.channel_type_6005);//接入渠道
		reqMap.put("stdpaytype",Constants.pay_access_type_02);//接入支付类型
		reqMap.put("stdmercno",Constants.getReadProperties("ccb", "ccb.wechat.merId"));//商户编号
		reqMap.put("stdmercno2","");//二级商户号
		reqMap.put("stdtermid","11223344");//终端编号
		reqMap.put("stdorderid",orderId);//商户订单号
		reqMap.put("stdbegtime",sdf.format(new Date()));//交易起始时间
		reqMap.put("stdendtime","");//交易结束时间
		reqMap.put("std400memo","被扫订单支付（消费）交易接口");//商品描述
		reqMap.put("stdtranamt","1");//交易金额
		reqMap.put("stddiscamt","");//不可优惠金额
		reqMap.put("stdtrancur","156");//交易币种
		reqMap.put("stdauthid","130263328790255513");//授权码
		reqMap.put("stdqytype","");//消费类型
		reqMap.put("accountFlag","");//分账标识
		reqMap.put("secMerFeeRate","");//分账子商户交易费率
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.offline.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.wechat.md5key"));
		System.out.println("!!!!!!!!!!!!!!!="+Json.make(respMap).toString());
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
		System.out.println("#############="+respMap);
	}
	/**
	 * 线下-交易状态查询接口
	 */
	public void zhlOffLineQuery(){
		String orderId = System.currentTimeMillis()+"";
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方式
		reqMap.put("stdmsgtype",Constants.trade_type_38);//消息类型
		reqMap.put("stdprocode",Constants.trade_sub_type_381000);//交易码
		reqMap.put("std400chnl",Constants.channel_type_6005);//接入渠道
		reqMap.put("stdpaytype",Constants.pay_access_type_02);//接入支付类型
		reqMap.put("stdmercno",Constants.getReadProperties("ccb", "ccb.wechat.public.merId"));//商户编号
		reqMap.put("stdmercno2","888800000011672");//二级商户号
		reqMap.put("stdtermid","");//终端编号
		reqMap.put("orgorderid","8022017051919243549167156");//原支付交易的中信订单号
		reqMap.put("stdorderid","88ca887e1c32446a909ce93479e2fdb5");//商户订单号
		reqMap.put("stdbegtime","");//交易起始日期
		reqMap.put("accountFlag","Y");//分账标识
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.offline.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
		System.out.println("!!!!!!!!!!!!!!!="+Json.make(respMap).toString());
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
		System.out.println("#############="+respMap);
	}
	/**
	 * 线上微信退货接口
	 */
	public void zhlOnLineWechatRefund(){
		String orderId = System.currentTimeMillis()+"";
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_04);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_040441);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6005);//接入渠道
		reqMap.put("payAccessType",Constants.pay_access_type_02);//接入支付类型
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.wechat.passive.merId"));//商户编号
		reqMap.put("secMerId","998800000083600");//分账子商户号
		reqMap.put("termId","");//终端编号
		reqMap.put("origTxnSeqId","1494832636534");//原商户预支付订单号
		reqMap.put("origSettleDate",sdf.format(new Date()));//原商户支付订单日期
		reqMap.put("orderId","");//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//商户订单发送时间
		reqMap.put("txnAmt","1");//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//币种
		reqMap.put("accountFlag","Y");//分账标识
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.passive.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.wechat.passive.md5key"));
		String json = JsonUtil.getMapToJson(respMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
	}
	/**
	 * 线上支付宝二清退货接口
	 */
	public void zhlOnLineAlipayRefund(){
		
	}
	/**
	 * 分账子商户入驻接口
	 */
	public void CcbMerchJoin(){
		String orderId = System.currentTimeMillis()+"";
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_09);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_900030);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("mchtNm","北京中互联软件技术有限公司");//商户名称
		reqMap.put("mchtCnAbbr","中互联软件");//商户简称
		reqMap.put("customSerPhone","13439558911");//客服电话
		reqMap.put("contact","高云侨");//联系人姓名
		reqMap.put("contactPhone","13439558911");//联系人电话
		reqMap.put("contactEmail","");//联系人邮箱
		reqMap.put("business","");//经营类目
		reqMap.put("agntBrhLicenceNo","");//营业执照编号
		reqMap.put("agntBrhObic","");//组织机构代码证号
		reqMap.put("agntBrhTaxNo","");//税务登记号
		reqMap.put("agntBrhManager","");//法人姓名
		reqMap.put("agntBrhIdeNo","");//法人身份证
		reqMap.put("merchantRemark","");//商户备注
		reqMap.put("mchtProvince","");//省份
		reqMap.put("mchtCity","");//市
		reqMap.put("addr","");//详细地址
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.wechat.passive.merId"));//平台商户中信子商户
		reqMap.put("thirdMchtNo","011223344556677");//第三方平台子商户
		reqMap.put("isOrNotD0","2");//是否是D+0
		reqMap.put("isOrNotZxMchtNo","N");//是否中信银行账户
		reqMap.put("acctType","1");//账户类型
		reqMap.put("settleAcctNm","北京中互联软件技术有限公司");//结算户名
		reqMap.put("settleAcct","11050171520000000219");//结算账户
		reqMap.put("accIdeNo","320826197311022439");//开户身份证
		reqMap.put("accPhone","13439558911");//开户手机号
		reqMap.put("settleBankAllName","中国建设银行股份有限公司北京百子湾路支行");//收款行全称
		reqMap.put("settleBankCode","105100023057");//收款行行号
		reqMap.put("apType","1");//申请类型
		reqMap.put("mchtState","0");//商户状态
		reqMap.put("secMerId","");//分账子商户
		reqMap.put("channelCode","");//渠道号
		reqMap.put("managerNo","");//客户经理号

		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.passive.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.wechat.passive.md5key"));
		String json = JsonUtil.getMapToJson(respMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
	}
	/**
	 * 线上商户对账单下载
	 * @throws IOException 
	 */
	public void ccbTransBill() throws IOException{
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("userName",Constants.getReadProperties("ccb","ccb.wechat.active.passive.merId"));//用户账号
		reqMap.put("userPwd",MD5.MD5Encode("fYiVtZ"));//API密码
		reqMap.put("date","20170510");//对账单日期
		reqMap.put("statementType",Constants.statement_type_0);//对账单类型
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("json="+json);
		HttpRequestParam param = new HttpRequestParam();
		param.setUrl(Constants.getReadProperties("ccb","ccb.check.url"));
		Map<String,String> heads = Maps.newHashMap();
		heads.put("Content-Type", "application/zip;charset=UTF-8");
		param.setContext(json);
		param.setHeads(heads);
		HttpResponser resp=HttpHelp.postParamByHttpClient(param);
		System.out.println("result="+resp.getContent());
		

		
	}
	
	
	
	/**
	 * 公众号支付 
	 */
	public void ccbWechatPublicAccounts(){
		String orderId = System.currentTimeMillis()+"";
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
		reqMap.put("orderId",orderId);//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("productId","");//商品ID
		reqMap.put("orderBody","公众号支付测试");//商品描述2                                                              
		reqMap.put("orderDetail","");//商品详情
		reqMap.put("orderGoodsTag","");//商品标记
		reqMap.put("orderSubOpenid","o1MV4wOfD0znYFvq8mktFH0Fui5M");//用户子标识
		reqMap.put("txnAmt","1");//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("accountFlag","Y");//分账标识
		reqMap.put("secMerFeeRate","0.003");//分账子商户交易费率
		reqMap.put("attach","");//附加数据
		reqMap.put("limitPay","");//指定支付方式
		reqMap.put("needBankType","");//是否需要获取支付行类型
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.active.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.md5key"));
		String json = JsonUtil.getMapToJson(respMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
	}
	
	public void ccbH5(){
		String orderId = System.currentTimeMillis()+"";
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_010133);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("payAccessType",Constants.pay_access_type_02);//接入支付类型
		reqMap.put("backEndUrl",Constants.getReadProperties("ccb", "ccb.online.notifyUrl"));//后台通知地址
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.wechat.merId"));//商户编号
		reqMap.put("secMerId","");//分账子商户号
		reqMap.put("termId","WEB");//终端编号
		reqMap.put("termIp",Constants.getNetAddressIp());//终端IP
		reqMap.put("orderId",orderId);//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("orderBody","公众号支付测试");//商品描述
		reqMap.put("orderDetail","");//商品详情
		reqMap.put("orderGoodsTag","");//商品标记
		reqMap.put("txnAmt","1");//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("accountFlag","");//分账标识
		reqMap.put("secMerFeeRate","");//分账子商户交易费率
		reqMap.put("attach","");//附加数据
		reqMap.put("limitPay","");//指定支付方式
		reqMap.put("needBankType","");//是否需要获取支付行类型
		reqMap.put("sceneInfo","wap_url=https://m.jd.com&wap_name=京东官网&618专版");//应用描述
		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.wechat.md5key"));
		String json = JsonUtil.getMapToJson(respMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
	}
	//支付宝二清交易创建接口（支付宝收银台）
	public void alipayTradeCreate(){
		String orderId = System.currentTimeMillis()+"";//1490002572369
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
		reqMap.put("orderId",orderId);//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("orderBody","支付宝收银台");//商品描述
		reqMap.put("orderDetail","支付宝二清交易创建接口");//商品详细
		reqMap.put("txnAmt","1");//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("accountFlag","Y");//分账标识
		reqMap.put("secMerFeeRate","0.003");//分账子商户交易费率
		reqMap.put("buyerLogonId","");//买家支付宝账号
		reqMap.put("buyerId","208810217143598");//买家支付宝唯一用户号
		reqMap.put("disablePayChannels","");//要禁用的信用渠道(禁用支付渠道creditCard：信用卡credit_group：所有信用渠道，包含（信用卡，花呗）)
		reqMap.put("needBankType","");//是否需要获取支付行类型

		//此处做签名
		String respStr = MD5.request(orderId,Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(orderId,respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		System.out.println("!!!!!!!!!!!!!!!="+Json.make(respMap).toString());
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("@@@@@@@@@@@@@@@@="+json);
		System.out.println("#############="+respMap);
	}
	public static void main(String[] args) throws IOException {
		CcbTestService c = new CcbTestService();
		c.alipayTradeCreate();
	}
}
