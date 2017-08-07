package com.zhl.ccb.service.Impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mjson.Json;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.zhl.ccb.dao.NfcOrderWaterMapper;
import com.zhl.ccb.model.NfcOrderWater;
import com.zhl.ccb.service.CcbService;
import com.zhl.ccb.utils.Constants;
import com.zhl.ccb.utils.DSDES;
import com.zhl.ccb.utils.DateUtil;
import com.zhl.ccb.utils.HttpHelp;
import com.zhl.ccb.utils.HttpRequestParam;
import com.zhl.ccb.utils.HttpResponser;
import com.zhl.ccb.utils.JsonUtil;
import com.zhl.ccb.utils.MD5;

@Service("CcbService")
public class CcbServiceImpl implements CcbService{
	@Autowired
    private NfcOrderWaterMapper nfcOrderWaterMapper; 
	
	Logger logger = Logger.getLogger(CcbServiceImpl.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	/**
	 * 主扫订单预支付（消费）交易接口(微信)---(中互联被扫)
	 * @param mapParam
	 * @return
	 */
	public Map<String,Object> zhlNfcWechatPassive(Map<String,Object> mapParam){
		Map<String,Object> jsonRetMap = null;
		//保存订单流水表
		NfcOrderWater water = this.getShareOrderWater(mapParam);
		water.setStatus(Constants.nfc_pay_status_3);
		water.setMessage(Constants.nfc_pay_status_3_context);
		//计算应收手续费
		BigDecimal totalFee = water.getTotalFee();
		BigDecimal merchRate = Constants.getStrToBigDecimal(mapParam.get("merch_rate").toString());
		BigDecimal merchFee =  totalFee.multiply(merchRate);
    	water.setMerchFee(merchFee);//交易金额与手续费率相乘保留2位小数
		water.setEndTime(null);
		nfcOrderWaterMapper.insertSelective(water);
		//组装传递给通道方的报文
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding", Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod", Constants.sing_method_MD5);//签名方式
		reqMap.put("txnType", Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType", Constants.trade_sub_type_010130);//交易子类型
		reqMap.put("channelType", Constants.channel_type_6002);//接入渠道
		reqMap.put("payAccessType", Constants.pay_access_type_02);//接入支付类型
		reqMap.put("backEndUrl", Constants.getReadProperties("ccb", "ccb.online.notifyUrl"));//后台通知地址
		//不分账
		if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"))){//不分账
			reqMap.put("merId", water.getMerchNo());//商户标编号
			reqMap.put("secMerId", "");//分账子账户号
			reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			reqMap.put("secMerFeeRate", "");//分账子商户交易费率
		}else{//分账
			reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));//商户标编号
			reqMap.put("secMerId", water.getMerchNo());//分账子账户号
			reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			reqMap.put("secMerFeeRate", mapParam.get("merch_rate").toString());//分账子商户交易费率---使用分账功能时上传，是分账子商户secMerId的交易费率（若费率是0.3%，此值传0.003，小数点后最多四位），此费率值不能比平台商户merId的费率低
		}
		reqMap.put("termId", "WEB");//终端编号
		reqMap.put("termIp", Constants.getNetAddressIp());//终端IP
		reqMap.put("orderId", water.getMerOrderNo());//商户订单号
		reqMap.put("orderTime", sdf.format(water.getCreatedTime()));//交易起始日期
		reqMap.put("productId", "");//商品ID
		reqMap.put("orderBody", mapParam.get("product_name").toString());//商品描述
		reqMap.put("orderDetail", mapParam.get("product_desc").toString());//商品详情
		reqMap.put("orderGoodsTag", "");//商品标记
		reqMap.put("txnAmt", Constants.fromYuanToFen(mapParam.get("total_fee").toString()));//交易金额
		reqMap.put("currencyType", Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("attach", "");//附加数据
		if(mapParam.get("limit_pay")!=null){
			if(mapParam.get("limit_pay").toString().equals("no_credit")){
				reqMap.put("limitPay", mapParam.get("limit_pay").toString());//指定支付方式no_credit
			}else{
				reqMap.put("limitPay", "");//指定支付方式no_credit
			}
		}else{
			reqMap.put("limitPay", "");//指定支付方式no_credit
		}
		reqMap.put("needBankType", "");//是否需要获取支付行类型
		//请求通道
		String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			NfcOrderWater updateWater = new NfcOrderWater();
			updateWater.setMerOrderNo(respMap.get("orderId").toString());
			if(respMap.get("respCode").toString().equals("0000")){
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_3,Constants.nfc_pay_status_3_context);
				jsonRetMap.put("code_url", respMap.get("codeUrl"));
				jsonRetMap.put("code_img_url", "");
				jsonRetMap.put("merch_fee", water.getMerchFee().toString());
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_9,respMap.get("respMsg").toString());
				updateWater.setStatus(Constants.nfc_pay_status_9);
				updateWater.setMessage(respMap.get("respMsg").toString());
			}
			//最后时间
			String endDate = DateUtil.format(new Date());
			try {
				if(respMap.get("txnSeqId") != null){
					updateWater.setChannelOrderNo(respMap.get("txnSeqId").toString());
				}
				updateWater.setEndTime(DateUtil.strToDate(endDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//修改订单状态
			nfcOrderWaterMapper.updateByPrimaryKeySelective(updateWater);
			return jsonRetMap;
		}
	}
	/**
	 * 支付宝二清主扫消费接口---(中互联被扫)
	 * @param mapParam
	 * @return
	 */
	public Map<String, Object> zhlNfcAlipayPassive(Map<String, Object> mapParam){
		Map<String,Object> jsonRetMap = null;
		//保存订单流水表
		NfcOrderWater water = this.getShareOrderWater(mapParam);
		water.setStatus(Constants.nfc_pay_status_3);
		water.setMessage(Constants.nfc_pay_status_3_context);
		//计算应收手续费
		BigDecimal totalFee = water.getTotalFee();
		BigDecimal merchRate = Constants.getStrToBigDecimal(mapParam.get("merch_rate").toString());
		BigDecimal merchFee =  totalFee.multiply(merchRate);
    	water.setMerchFee(merchFee);//交易金额与手续费率相乘保留2位小数
		water.setEndTime(null);
		nfcOrderWaterMapper.insertSelective(water);
		//组装传递给通道方的报文
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding", Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod", Constants.sing_method_MD5);//签名方式
		reqMap.put("txnType", Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType", Constants.trade_sub_type_010302);//交易子类型
		reqMap.put("channelType", Constants.channel_type_6002);//接入渠道
		reqMap.put("backEndUrl", Constants.getReadProperties("ccb", "ccb.online.notifyUrl"));//后台通知地址
		//不分账
		if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.alipay.merId"))){//不分账
			reqMap.put("merId", water.getMerchNo());//商户标编号
			reqMap.put("secMerId", "");//分账子账户号
			reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			reqMap.put("secMerFeeRate", "");//分账子商户交易费率
		}else{//分账
			reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户标编号
			reqMap.put("secMerId", water.getMerchNo());//分账子账户号
			reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			reqMap.put("secMerFeeRate", mapParam.get("merch_rate").toString());//分账子商户交易费率---使用分账功能时上传，是分账子商户secMerId的交易费率（若费率是0.3%，此值传0.003，小数点后最多四位），此费率值不能比平台商户merId的费率低
		}
		reqMap.put("termId", "WEB");//终端编号
		reqMap.put("termIp", Constants.getNetAddressIp());//终端IP
		reqMap.put("orderId", water.getMerOrderNo());//商户订单号
		reqMap.put("orderTime", sdf.format(water.getCreatedTime()));//交易起始日期
		reqMap.put("orderBody", mapParam.get("product_name").toString());//商品描述
		reqMap.put("orderDetail", mapParam.get("product_desc").toString());//商品详情
		reqMap.put("txnAmt", Constants.fromYuanToFen(mapParam.get("total_fee").toString()));//交易金额
		reqMap.put("currencyType", Constants.trade_currency_type_CNY);//交易币种
		//此处做签名
		String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			NfcOrderWater updateWater = new NfcOrderWater();
			updateWater.setMerOrderNo(respMap.get("orderId").toString());
			if(respMap.get("respCode").toString().equals("0000")){
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_3,Constants.nfc_pay_status_3_context);
				jsonRetMap.put("code_url", respMap.get("codeUrl"));
				jsonRetMap.put("code_img_url", "");
				jsonRetMap.put("merch_fee", water.getMerchFee());
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_9,respMap.get("respMsg").toString());
				updateWater.setStatus(Constants.nfc_pay_status_9);
				updateWater.setMessage(respMap.get("respMsg").toString());
			}
			//最后时间
			String endDate = DateUtil.format(new Date());
			try {
				updateWater.setEndTime(DateUtil.strToDate(endDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//修改订单状态
			if(respMap.get("seqId") != null){
				//特殊接口处理请注意！！！！！！！
				updateWater.setChannelOrderNo(respMap.get("seqId").toString());
			}
			nfcOrderWaterMapper.updateByPrimaryKeySelective(updateWater);
			return jsonRetMap;
		}
	}
	/**
	 * 线下-被扫订单支付（消费）交易接口---(中互联主扫)
	 * @param mapParam
	 * @return
	 */
	public Map<String,Object> zhlNfcActive(Map<String,Object> mapParam){
		Map<String, Object> respMap = null;
		Map<String,Object> jsonRetMap = null;
		//保存订单流水表
		NfcOrderWater water = this.getShareOrderWater(mapParam);
		water.setStatus(Constants.nfc_pay_status_0);
		water.setMessage(Constants.nfc_pay_status_0_context);
		//计算应收手续费
		BigDecimal totalFee = water.getTotalFee();
		BigDecimal merchRate = Constants.getStrToBigDecimal(mapParam.get("merch_rate").toString());
		BigDecimal merchFee =  totalFee.multiply(merchRate);
    	water.setMerchFee(merchFee);//交易金额与手续费率相乘保留2位小数
		water.setEndTime(null);
		nfcOrderWaterMapper.insertSelective(water);
		//组装传递给通道方的报文
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("stdmsgtype",Constants.trade_type_48);//消息类型
		reqMap.put("std400chnl",Constants.channel_type_6005);//接入渠道
		reqMap.put("stdpaytype",Constants.pay_access_type_02);//接入支付类型
		//判断是wechat支付还是alipay支付
		if(water.getNfcMerch().equals(Constants.wechat_nfc_merch)){
			reqMap.put("stdprocode",Constants.trade_sub_type_481000);//交易码
			if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"))){//不分账
				reqMap.put("stdmercno", water.getMerchNo());//商户编号
				reqMap.put("stdmercno2", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
				reqMap.put("secMerFeeRate", "");//分账子商户交易费率
			}else{//分账
				reqMap.put("stdmercno", Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));//商户编号
				reqMap.put("stdmercno2", water.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
				reqMap.put("secMerFeeRate", mapParam.get("merch_rate").toString());//分账子商户交易费率---使用分账功能时上传，是分账子商户secMerId的交易费率（若费率是0.3%，此值传0.003，小数点后最多四位），此费率值不能比平台商户merId的费率低
			}
		}
		if(water.getNfcMerch().equals(Constants.alipay_nfc_merch)){
			reqMap.put("stdprocode",Constants.trade_sub_type_481003);//交易码
			if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.alipay.merId"))){//不分账
				reqMap.put("stdmercno", water.getMerchNo());//商户编号
				reqMap.put("stdmercno2", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
				reqMap.put("secMerFeeRate", "");//分账子商户交易费率
			}else{//分账
				reqMap.put("stdmercno", Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户编号
				reqMap.put("stdmercno2", water.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
				reqMap.put("secMerFeeRate", mapParam.get("merch_rate").toString());//分账子商户交易费率---使用分账功能时上传，是分账子商户secMerId的交易费率（若费率是0.3%，此值传0.003，小数点后最多四位），此费率值不能比平台商户merId的费率低
			}
		}
		reqMap.put("stdtermid",mapParam.get("terminal_no").toString());//终端编号
		reqMap.put("stdorderid",water.getMerOrderNo());//商户订单号
		reqMap.put("stdbegtime",sdf.format(water.getCreatedTime()));//交易起始时间
		reqMap.put("stdendtime","");//交易结束时间
		reqMap.put("std400memo",mapParam.get("product_name").toString());//商品描述
		reqMap.put("stdtranamt",Constants.fromYuanToFen(mapParam.get("total_fee").toString()));//交易金额
		reqMap.put("stddiscamt","0");//不可优惠金额
		reqMap.put("stdtrancur",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("stdauthid",mapParam.get("auth_code").toString());//授权码
		reqMap.put("stdqytype","");//消费类型
		//此处做签名
		if(water.getNfcMerch().equals(Constants.wechat_nfc_merch)){
			String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.offline.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
			//解析返回报文
			respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		}
		if(water.getNfcMerch().equals(Constants.alipay_nfc_merch)){
			String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.offline.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
			//解析返回报文
			respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		}
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			NfcOrderWater updateWater = new NfcOrderWater();
			updateWater.setMerOrderNo(respMap.get("stdorderid").toString());
			if(respMap.get("std400mgid").toString().equals("0000")){
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_1,Constants.nfc_pay_status_1_context);
				jsonRetMap.put("merch_fee", water.getMerchFee().toString());
				updateWater.setStatus(Constants.nfc_pay_status_1);
				updateWater.setMessage(Constants.nfc_pay_status_1_context);
			}else if(respMap.get("std400mgid").toString().equals("0003")||respMap.get("std400mgid").toString().equals("0005")){
				//交易通讯超时（银行系统异常），请发起查询交易
				//交易已受理，请稍后查询交易结果
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_0,Constants.nfc_pay_status_0_context);
				jsonRetMap.put("merch_fee", water.getMerchFee().toString());
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_9,respMap.get("stdrtninfo").toString());
				jsonRetMap.put("merch_fee", water.getMerchFee().toString());
				updateWater.setStatus(Constants.nfc_pay_status_9);
				updateWater.setMessage(respMap.get("stdrtninfo").toString());
			}
			//最后时间
			String endDate = DateUtil.format(new Date());
			try {
				updateWater.setEndTime(DateUtil.strToDate(endDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//修改订单状态
			if(respMap.get("stdrefnum") != null){
				//特殊接口处理请注意！！！！！！！
				updateWater.setChannelOrderNo(respMap.get("stdrefnum").toString());
			}
			nfcOrderWaterMapper.updateByPrimaryKeySelective(updateWater);
			return jsonRetMap;
		}
	}
	/**
	 * 线上-交易状态查询接口
	 * @param nfcOrderWater
	 * @return
	 */
	public void zhlNfcOnlineQuery(NfcOrderWater nfcOrderWater) {
		NfcOrderWater water = new NfcOrderWater();
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_38);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_383000);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("payAccessType",Constants.pay_access_type_02);//接入支付类型
		if(nfcOrderWater.getNfcType().equals(Constants.nfc_channelPay)){//公众号支付
			if(nfcOrderWater.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.public.merId"))){//不分账
				reqMap.put("merId", nfcOrderWater.getMerchNo());//商户标编号
				reqMap.put("secMerId", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			}else{//分账
				reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.wechat.public.merId"));//商户标编号
				reqMap.put("secMerId", nfcOrderWater.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			}
		}else{//微信扫码支付
			if(nfcOrderWater.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"))){//不分账
				reqMap.put("merId", nfcOrderWater.getMerchNo());//商户标编号
				reqMap.put("secMerId", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			}else{//分账
				reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));//商户标编号
				reqMap.put("secMerId", nfcOrderWater.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			}
		}
		reqMap.put("origOrderId",nfcOrderWater.getChannelOrderNo());//原始商户订单号
		reqMap.put("origOrderTime",sdf.format(nfcOrderWater.getCreatedTime()));//原始商户交易时间
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("fetchOrderNo","");//是否获取订单号标识
		reqMap.put("wxOrderNo","");//微信订单号
		Map<String, Object> respMap = null;
		if(nfcOrderWater.getNfcType().equals(Constants.nfc_channelPay)){//公众号支付
			//此处做签名
			String respStr = MD5.request(nfcOrderWater.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
			//解析返回报文
			respMap = MD5.getResp(nfcOrderWater.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
		}else{
			//此处做签名
			String respStr = MD5.request(nfcOrderWater.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
			//解析返回报文
			respMap = MD5.getResp(nfcOrderWater.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		}
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			logger.info("订单号："+nfcOrderWater.getOrderNo()+"线上-交易状态查询接口，中信通道返回数据验签失败");
		}else{
			//water.setMerOrderNo(respMap.get("origOrderId").toString());
			water.setMerOrderNo(nfcOrderWater.getMerOrderNo());
			if(respMap.get("respCode").toString().equals("0000")){
				if(nfcOrderWater.getRefundOrderNo() != null && !"".equals(nfcOrderWater.getRefundOrderNo())){//认为是退货订单查询
					if(respMap.get("origRespCode").toString().equals("SUCCESS")){//退款成功
						water.setStatus(Constants.nfc_pay_status_1);
						water.setMessage(Constants.nfc_pay_status_1_context_refund_RE);
					}else if(respMap.get("origRespCode").toString().equals("FAIL")){//退款失败
						water.setStatus(Constants.nfc_pay_status_9);
						water.setMessage(Constants.nfc_pay_status_9_context);
					}else if(respMap.get("origRespCode").toString().equals("PROCESSING")){//退款处理中
						water.setStatus(Constants.nfc_pay_status_2);
						water.setMessage(Constants.nfc_pay_status_2_context);
					}else if(respMap.get("origRespCode").toString().equals("NOTSURE")){//未确定，需要商户原退款单号重新发起
						water.setStatus(Constants.nfc_pay_status_29);
						water.setMessage(Constants.nfc_pay_status_29_context);
					}else if(respMap.get("origRespCode").toString().equals("CHANGE")){//转入代发，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款
						water.setStatus(Constants.nfc_pay_status_5);
						water.setMessage(Constants.nfc_pay_status_5_context);
					}else{
						water.setStatus(Constants.nfc_pay_status_9);
						water.setMessage(Constants.nfc_pay_status_9_context);
					}
				}else{
					if(respMap.get("origRespCode").toString().equals("SUCCESS")){//订单那支付成功
						water.setStatus(Constants.nfc_pay_status_1);
						water.setMessage(Constants.nfc_pay_status_1_context);
					}else if(respMap.get("origRespCode").toString().equals("NOTPAY")){//订单未支付
						water.setStatus(Constants.nfc_pay_status_3);
						water.setMessage(Constants.nfc_pay_status_3_context);
					}else{
						water.setStatus(Constants.nfc_pay_status_9);
						water.setMessage(respMap.get("respMsg").toString());
					}
				}
			}else{
				water.setStatus(Constants.nfc_pay_status_9);
				water.setMessage(respMap.get("respMsg").toString());
			}
			if(!water.getStatus().equals(nfcOrderWater.getStatus())){
				try {
					//最后时间
					String endDate = DateUtil.format(new Date());
					water.setEndTime(DateUtil.strToDate(endDate));
					nfcOrderWaterMapper.updateByPrimaryKeySelective(water);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
     * 线上-支付宝二清交易查询接口
     * @param nfcOrderWater
	 * @return
     */
	public void zhlNfcAlipayOnlineQuery(NfcOrderWater nfcOrderWater) {
		NfcOrderWater water = new NfcOrderWater();
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_38);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_381004);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		if(nfcOrderWater.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.alipay.merId"))){//不分账
			reqMap.put("merId", nfcOrderWater.getMerchNo());//商户标编号
			reqMap.put("secMerId", "");//分账子账户号
			reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
		}else{//分账
			reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户标编号
			reqMap.put("secMerId", nfcOrderWater.getMerchNo());//分账子账户号
			reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
		}
//		if(nfcOrderWater.getNfcType().equals(Constants.nfc_refund)){//退货查询
//			reqMap.put("seqId",nfcOrderWater.getRefundOrderNo());//中信流水号
//		}else{
			reqMap.put("seqId",nfcOrderWater.getChannelOrderNo());//原始商户订单号
//		}
		
		reqMap.put("fetchOrderNo","Y");//是否获取订单号标识
		//此处做签名
		String respStr = MD5.request(nfcOrderWater.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(nfcOrderWater.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			logger.info("订单号："+nfcOrderWater.getOrderNo()+"线上-支付宝二清交易查询接口，中信通道返回数据验签失败");
		}else{
			//water.setMerOrderNo(respMap.get("orderId").toString());
			water.setMerOrderNo(nfcOrderWater.getMerOrderNo());
			if(respMap.get("respCode").toString().equals("0000") || respMap.get("respCode").toString().equals("7000") || respMap.get("respCode").toString().equals("6001")){
				if(respMap.get("txnState").toString().equals("00")){//交易成功
					water.setStatus(Constants.nfc_pay_status_1);
					water.setMessage(Constants.nfc_pay_status_1_context);
				}else if(respMap.get("txnState").toString().equals("01")){//交易成功（已发生退货）
					water.setStatus(Constants.nfc_pay_status_1);
					water.setMessage(Constants.nfc_pay_status_1_context_refund_RE);
				}else if(respMap.get("txnState").toString().equals("02")){//交易已关闭
					water.setStatus(Constants.nfc_pay_status_9);
					water.setMessage(Constants.nfc_pay_status_9_context_close);
				}else if(respMap.get("txnState").toString().equals("04")){//预支付交易成功
					water.setStatus(Constants.nfc_pay_status_3);
					water.setMessage(Constants.nfc_pay_status_3_context);
				}else if(respMap.get("txnState").toString().equals("06")){//用户正在支付中
					water.setStatus(Constants.nfc_pay_status_0);
					water.setMessage(Constants.nfc_pay_status_0_context);
				}else if(respMap.get("txnState").toString().equals("08")){//交易受理成功（退货交易时使用）
					water.setStatus(Constants.nfc_pay_status_2);
					water.setMessage(Constants.nfc_pay_status_2_context);
				}else if(respMap.get("txnState").toString().equals("99")){//交易失败
					water.setStatus(Constants.nfc_pay_status_9);
					water.setMessage(Constants.nfc_pay_status_9_context);
				}else{
					water.setStatus(Constants.nfc_pay_status_9);
					water.setMessage(respMap.get("respMsg").toString());
				}
			}else{
				water.setStatus(Constants.nfc_pay_status_9);
				water.setMessage(respMap.get("respMsg").toString());
			}
			if(!water.getStatus().equals(nfcOrderWater.getStatus())){
				try {
					//最后时间
					String endDate = DateUtil.format(new Date());
					water.setEndTime(DateUtil.strToDate(endDate));
					nfcOrderWaterMapper.updateByPrimaryKeySelective(water);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 线下-交易状态查询接口
	 * @param nfcOrderWater
	 * @return
	 */
	public void zhlOffLineQuery(NfcOrderWater nfcOrderWater){
		Map<String, Object> respMap = null;
		NfcOrderWater water = new NfcOrderWater();
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方式
		reqMap.put("stdmsgtype",Constants.trade_type_38);//消息类型
		reqMap.put("std400chnl",Constants.channel_type_6005);//接入渠道
		reqMap.put("stdpaytype",Constants.pay_access_type_02);//接入支付类型
		//判断是wechat支付还是alipay支付
		if(nfcOrderWater.getNfcMerch().equals(Constants.wechat_nfc_merch)){//微信
			reqMap.put("stdprocode",Constants.trade_sub_type_381000);//交易码
			if(nfcOrderWater.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"))){//不分账
				reqMap.put("stdmercno", nfcOrderWater.getMerchNo());//商户编号
				reqMap.put("stdmercno2", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			}else{//分账
				reqMap.put("stdmercno", Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));//商户编号
				reqMap.put("stdmercno2", nfcOrderWater.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			}
		}
		if(nfcOrderWater.getNfcMerch().equals(Constants.alipay_nfc_merch)){//支付宝
			reqMap.put("stdprocode",Constants.trade_sub_type_381003);//交易码
			if(nfcOrderWater.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.alipay.merId"))){//不分账
				reqMap.put("stdmercno", nfcOrderWater.getMerchNo());//商户编号
				reqMap.put("stdmercno2", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			}else{//分账
				reqMap.put("stdmercno", Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户编号
				reqMap.put("stdmercno2", nfcOrderWater.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			}
		}
//		if(nfcOrderWater.getNfcType().equals(Constants.nfc_refund)){//退货查询
//			reqMap.put("orgorderid",nfcOrderWater.getRefundOrderNo());//原始商户订单号
//		}else{
			reqMap.put("orgorderid",nfcOrderWater.getChannelOrderNo());//原始商户订单号
//		}
		reqMap.put("stdorderid",nfcOrderWater.getMerOrderNo());//商户订单号
		reqMap.put("stdbegtime", sdf.format(new Date()));//交易起始日期
		reqMap.put("stdtermid","");//终端编号
		//此处做签名
		if(nfcOrderWater.getNfcMerch().equals(Constants.wechat_nfc_merch)){
			String respStr = MD5.request(nfcOrderWater.getOrderNo(),Constants.getReadProperties("ccb", "ccb.offline.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
			//解析返回报文
			respMap = MD5.getResp(nfcOrderWater.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		}
		if(nfcOrderWater.getNfcMerch().equals(Constants.alipay_nfc_merch)){
			String respStr = MD5.request(nfcOrderWater.getOrderNo(),Constants.getReadProperties("ccb", "ccb.offline.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
			//解析返回报文
			respMap = MD5.getResp(nfcOrderWater.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		}
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			logger.info("订单号："+nfcOrderWater.getOrderNo()+"线下-交易状态查询接口，中信通道返回数据验签失败");
		}else{
			//water.setMerOrderNo(respMap.get("stdorderid").toString());
			water.setMerOrderNo(nfcOrderWater.getMerOrderNo());
			if(respMap.get("srg400mgid").toString().equals("0000")){
				if(nfcOrderWater.getRefundOrderNo() != null && !"".equals(nfcOrderWater.getRefundOrderNo())){//认为是退货订单查询
					water.setStatus(Constants.nfc_pay_status_1);
					water.setMessage(Constants.nfc_pay_status_1_context_refund_RE);
				}else{
					water.setStatus(Constants.nfc_pay_status_1);
					water.setMessage(Constants.nfc_pay_status_1_context);
				}
			}else if(respMap.get("srg400mgid").toString().equals("0005")){
				water.setStatus(Constants.nfc_pay_status_2);
				water.setMessage(Constants.nfc_pay_status_2_context);
			}else if(respMap.get("srg400mgid").toString().equals("0019")||respMap.get("srg400mgid").toString().equals("6001")){
				//0019未支付，6001等待客户输密码
				water.setStatus(Constants.nfc_pay_status_3);
			}else{
				water.setStatus(Constants.nfc_pay_status_9);
				water.setMessage(respMap.get("stdrtninfo").toString());
			}
			if(!water.getStatus().equals(nfcOrderWater.getStatus())){
				try {
					//最后时间
					String endDate = DateUtil.format(new Date());
					water.setEndTime(DateUtil.strToDate(endDate));
					nfcOrderWaterMapper.updateByPrimaryKeySelective(water);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 线上微信退货接口
	 * @param channelRefundOrderNo
	 * @param nfcOrderWater
	 * @param mapParam
	 * @return
	 */
	public Map<String,Object> zhlOnLineWechatRefund(String channelRefundOrderNo,NfcOrderWater nfcOrderWater,Map<String,Object> mapParam){
		Map<String,Object> jsonRetMap = null;
		//保存订单流水表
		NfcOrderWater water = this.getShareOrderWater(mapParam);
		water.setRefundOrderNo(channelRefundOrderNo);
		water.setStatus(Constants.nfc_pay_status_2);
		water.setMessage(Constants.nfc_pay_status_2_context);
		water.setEndTime(null);
		nfcOrderWaterMapper.insertSelective(water);
		//组装传递给通道方的报文
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_04);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_040441);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("payAccessType",Constants.pay_access_type_02);//接入支付类型
		if(nfcOrderWater.getNfcType().equals(Constants.nfc_channelPay)){//公众号支付
			if(nfcOrderWater.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.public.merId"))){//不分账
				reqMap.put("merId", nfcOrderWater.getMerchNo());//商户标编号
				reqMap.put("secMerId", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			}else{//分账
				reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.wechat.public.merId"));//商户标编号
				reqMap.put("secMerId", nfcOrderWater.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			}
		}else{
			//不分账
			if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"))){//不分账
				reqMap.put("merId", water.getMerchNo());//商户标编号
				reqMap.put("secMerId", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			}else{//分账
				reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));//商户标编号
				reqMap.put("secMerId", water.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			}
		}
		reqMap.put("termId","");//终端编号
		reqMap.put("origTxnSeqId",channelRefundOrderNo);//原商户预支付订单号
		reqMap.put("origSettleDate",sdf.format(nfcOrderWater.getCreatedTime()));//原商户支付订单日期
		reqMap.put("orderId",water.getMerOrderNo());//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//商户订单发送时间
		reqMap.put("txnAmt",Constants.fromYuanToFen(mapParam.get("refund_fee").toString()));//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//币种
		Map<String, Object> respMap = null;
		if(nfcOrderWater.getNfcType().equals(Constants.nfc_channelPay)){//公众号支付
			//此处做签名
			String respStr = MD5.request(nfcOrderWater.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
			//解析返回报文
			respMap = MD5.getResp(nfcOrderWater.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
		}else{
			//请求通道
			String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
			//解析返回报文
			respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		}
		
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			NfcOrderWater updateWater = new NfcOrderWater();
			updateWater.setMerOrderNo(respMap.get("orderId").toString());
			if(respMap.get("respCode").toString().equals("0000")){//交易成功
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_1,Constants.nfc_pay_status_1_context_refund_RE);
				updateWater.setStatus(Constants.nfc_pay_status_1);
				updateWater.setMessage(Constants.nfc_pay_status_1_context_refund_RE);
			}else if(respMap.get("respCode").toString().equals("0001")){//交易已受理，请稍后查询交易结果
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_2,Constants.nfc_pay_status_2_context);
			}else{//交易失败
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_9,respMap.get("respMsg").toString());
				updateWater.setStatus(Constants.nfc_pay_status_9);
				updateWater.setMessage(respMap.get("respMsg").toString());
			}
			//最后时间
			String endDate = DateUtil.format(new Date());
			try {
				updateWater.setEndTime(DateUtil.strToDate(endDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//修改订单状态
			if(respMap.get("txnSeqId") != null){
				//特殊接口处理请注意！！！！！！！
				updateWater.setChannelOrderNo(respMap.get("txnSeqId").toString());
			}
			//修改订单状态
			nfcOrderWaterMapper.updateByPrimaryKeySelective(updateWater);
			return jsonRetMap;
		}
	}
	/**
	 * 线上支付宝二清退货接口
	 * @param channelRefundOrderNo
	 * @param nfcOrderWater
	 * @param mapParam
	 * @return
	 */
	public Map<String,Object> zhlOnLineAlipayRefund(String channelRefundOrderNo,NfcOrderWater nfcOrderWater,Map<String,Object> mapParam){
		Map<String,Object> jsonRetMap = null;
		//保存订单流水表
		NfcOrderWater water = this.getShareOrderWater(mapParam);
		water.setRefundOrderNo(channelRefundOrderNo);
		water.setStatus(Constants.nfc_pay_status_2);
		water.setMessage(Constants.nfc_pay_status_2_context);
		water.setEndTime(null);
		nfcOrderWaterMapper.insertSelective(water);
		//组装传递给通道方的报文
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_04);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_040303);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		//不分账
		if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.alipay.merId"))){//不分账
			reqMap.put("merId", water.getMerchNo());//商户标编号
			reqMap.put("secMerId", "");//分账子账户号
			reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
		}else{//分账
			reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户标编号
			reqMap.put("secMerId", water.getMerchNo());//分账子账户号
			reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
		}
		reqMap.put("termId","");//终端编号
		reqMap.put("seqId",channelRefundOrderNo);//中信流水号
		reqMap.put("orderId",water.getMerOrderNo());//退款订单号
		reqMap.put("orderTime",sdf.format(new Date()));//退款时间
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("txnAmt",Constants.fromYuanToFen(mapParam.get("refund_fee").toString()));//退款金额
		//请求通道
		String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			NfcOrderWater updateWater = new NfcOrderWater();
			updateWater.setMerOrderNo(respMap.get("orderId").toString());
			if(respMap.get("respCode").toString().equals("0000")){//交易成功
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_1,Constants.nfc_pay_status_1_context_refund_RE);
				updateWater.setStatus(Constants.nfc_pay_status_1);
				updateWater.setMessage(Constants.nfc_pay_status_1_context_refund_RE);
			}else if(respMap.get("respCode").toString().equals("0001")){//交易已受理，请稍后查询交易结果
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_2,Constants.nfc_pay_status_2_context);
			}else{//交易失败
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_9,respMap.get("respMsg").toString());
				updateWater.setStatus(Constants.nfc_pay_status_9);
				updateWater.setMessage(respMap.get("respMsg").toString());
			}
			//最后时间
			String endDate = DateUtil.format(new Date());
			try {
				updateWater.setEndTime(DateUtil.strToDate(endDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//修改订单状态
			if(respMap.get("seqId") != null){
				//特殊接口处理请注意！！！！！！！
				updateWater.setChannelOrderNo(respMap.get("seqId").toString());
			}
			nfcOrderWaterMapper.updateByPrimaryKeySelective(updateWater);
			return jsonRetMap;
		}
	}
	/**
	 * 线下退货接口
	 * @param channelRefundOrderNo
	 * @param nfcOrderWater
	 * @param mapParam
	 * @return
	 */
	public Map<String,Object> zhlOffLineRefund(String channelRefundOrderNo,NfcOrderWater nfcOrderWater,Map<String,Object> mapParam){
		Map<String, Object> respMap = null;
		Map<String,Object> jsonRetMap = null;
		//保存订单流水表
		NfcOrderWater water = this.getShareOrderWater(mapParam);
		water.setRefundOrderNo(channelRefundOrderNo);
		water.setStatus(Constants.nfc_pay_status_2);
		water.setMessage(Constants.nfc_pay_status_2_context);
		water.setEndTime(null);
		nfcOrderWaterMapper.insertSelective(water);
		//组装传递给通道方的报文
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("signMethod", Constants.sing_method_MD5);//签名方法
		reqMap.put("stdmsgtype", Constants.trade_type_48);//消息类型
		//判断是wechat支付还是alipay支付
		if(water.getNfcMerch().equals(Constants.wechat_nfc_merch)){
			reqMap.put("stdprocode",Constants.trade_sub_type_483000);//交易码
			if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"))){//不分账
				reqMap.put("stdmercno", water.getMerchNo());//商户编号
				reqMap.put("stdmercno2", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			}else{//分账
				reqMap.put("stdmercno", Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));//商户编号
				reqMap.put("stdmercno2", water.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			}
		}
		if(water.getNfcMerch().equals(Constants.alipay_nfc_merch)){
			reqMap.put("stdprocode",Constants.trade_sub_type_483003);//交易码
			if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.alipay.merId"))){//不分账
				reqMap.put("stdmercno", water.getMerchNo());//商户编号
				reqMap.put("stdmercno2", "");//分账子账户号
				reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			}else{//分账
				reqMap.put("stdmercno", Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户编号
				reqMap.put("stdmercno2", water.getMerchNo());//分账子账户号
				reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			}
		}
		reqMap.put("std400chnl", Constants.channel_type_6005);//接入渠道
		reqMap.put("stdpaytype", Constants.pay_access_type_02);//接入支付类型
		reqMap.put("stdaddress", Constants.getReadProperties("ccb", "ccb.online.notifyUrl"));//后台通知地址
		reqMap.put("stdtermid", "");//终端编号
		reqMap.put("orgrefnum", channelRefundOrderNo);//原支付交易的中信订单号
		reqMap.put("stdorderid", water.getMerOrderNo());//商户订单号
		reqMap.put("stdbegtime", sdf.format(new Date()));//商户订单发送时间
		reqMap.put("stdtranamt", Constants.fromYuanToFen(mapParam.get("refund_fee").toString()));//交易金额
		reqMap.put("stdtrancur", Constants.trade_currency_type_CNY);//币种
		//此处做签名
		if(water.getNfcMerch().equals(Constants.wechat_nfc_merch)){
			String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.offline.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
			//解析返回报文
			respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		}
		if(water.getNfcMerch().equals(Constants.alipay_nfc_merch)){
			String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.offline.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
			//解析返回报文
			respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		}
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			NfcOrderWater updateWater = new NfcOrderWater();
			updateWater.setMerOrderNo(respMap.get("stdorderid").toString());
			if(respMap.get("std400mgid").toString().equals("0000")){
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_1,Constants.nfc_pay_status_1_context_refund_RE);
				updateWater.setStatus(Constants.nfc_pay_status_1);
				updateWater.setMessage(Constants.nfc_pay_status_1_context_refund_RE);
			}else if(respMap.get("std400mgid").toString().equals("0003")||respMap.get("srg400mgid").toString().equals("0005")){
				//交易通讯超时（银行系统异常），请发起查询交易
				//交易已受理，请稍后查询交易结果
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_2,Constants.nfc_pay_status_2_context);
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_9,respMap.get("stdrtninfo").toString());
				updateWater.setStatus(Constants.nfc_pay_status_9);
				updateWater.setMessage(respMap.get("stdrtninfo").toString());
			}
			//最后时间
			String endDate = DateUtil.format(new Date());
			try {
				updateWater.setEndTime(DateUtil.strToDate(endDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//修改订单状态
			if(respMap.get("stdrefnum") != null){
				//特殊接口处理请注意！！！！！！！
				updateWater.setChannelOrderNo(respMap.get("stdrefnum").toString());
			}
			nfcOrderWaterMapper.updateByPrimaryKeySelective(updateWater);
			return jsonRetMap;
		}
	}
	/**
	 * 分账子账户入账接口
	 * @param mapParam
	 * @return
	 */
	public Map<String,Object> ccbMerchJoin(Map<String,Object> mapParam){
		Map<String,Object> jsonRetMap = null;
		Map<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_09);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_900030);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("mchtNm",mapParam.get("mchtNm"));//商户名称
		reqMap.put("mchtCnAbbr",mapParam.get("mchtCnAbbr"));//商户简称
		reqMap.put("customSerPhone",mapParam.get("customSerPhone"));//客服电话
		reqMap.put("contact",mapParam.get("contact"));//联系人姓名
		reqMap.put("contactPhone",mapParam.get("contactPhone"));//联系人电话
		reqMap.put("contactEmail",mapParam.get("contactEmail"));//联系人邮箱
		reqMap.put("business",mapParam.get("business"));//经营类目
		reqMap.put("agntBrhLicenceNo",mapParam.get("agntBrhLicenceNo"));//营业执照编号
		reqMap.put("agntBrhObic",mapParam.get("agntBrhObic"));//组织机构代码证号
		reqMap.put("agntBrhTaxNo",mapParam.get("agntBrhTaxNo"));//税务登记号
		reqMap.put("agntBrhManager",mapParam.get("agntBrhManager"));//法人姓名
		reqMap.put("agntBrhIdeNo",mapParam.get("agntBrhIdeNo"));//法人身份证
		reqMap.put("merchantRemark",mapParam.get("merchantRemark"));//商户备注
		reqMap.put("mchtProvince",mapParam.get("mchtProvince"));//省份
		reqMap.put("mchtCity",mapParam.get("mchtCity"));//市
		reqMap.put("addr",mapParam.get("addr"));//详细地址
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));//平台商户中信子商户
		reqMap.put("thirdMchtNo",mapParam.get("thirdMchtNo"));//第三方平台子商户
		reqMap.put("isOrNotD0",mapParam.get("isOrNotD0"));//是否是D+0
		reqMap.put("isOrNotZxMchtNo",mapParam.get("isOrNotZxMchtNo"));//是否中信银行账户
		reqMap.put("acctType",mapParam.get("acctType"));//账户类型
		reqMap.put("settleAcctNm",mapParam.get("settleAcctNm"));//结算户名
		reqMap.put("settleAcct",mapParam.get("settleAcct"));//结算账户
		reqMap.put("accIdeNo",mapParam.get("accIdeNo"));//开户身份证
		reqMap.put("accPhone",mapParam.get("accPhone"));//开户手机号
		reqMap.put("settleBankAllName",mapParam.get("settleBankAllName"));//收款行全称
		reqMap.put("settleBankCode",mapParam.get("settleBankCode"));//收款行行号
		reqMap.put("apType",mapParam.get("apType"));//申请类型
		reqMap.put("mchtState",mapParam.get("mchtState"));//商户状态
		reqMap.put("secMerId",mapParam.get("secMerId"));//分账子商户
		String ObjectJson = JsonUtil.getMapToJson(reqMap);
		Map<String,String> map = JsonUtil.getJsonToMapStr(ObjectJson);
		//此处做签名
		String respStr = MD5.request(mapParam.get("order_no").toString(),Constants.getReadProperties("ccb", "ccb.online.url"),map,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(mapParam.get("order_no").toString(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			if(respMap.get("resultCode").toString().equals("0000")){//入驻成功
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(mapParam.get("order_no").toString(),Constants.nfc_pay_status_1,Constants.nfc_pay_status_1_context);
				jsonRetMap.put("secMerId", respMap.get("secMerId"));
				jsonRetMap.put("thirdMchtNo", respMap.get("thirdMchtNo"));
			}else{//入驻失败
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(mapParam.get("order_no").toString(),Constants.nfc_pay_status_9,respMap.get("resultMsg").toString());
				jsonRetMap.put("secMerId", respMap.get("secMerId"));
				jsonRetMap.put("thirdMchtNo", respMap.get("thirdMchtNo"));
			}
			return jsonRetMap;
		}
	}
	/**
	 * 分账子商户查询
	 * @param mapParam
	 * @return
	 */
	public Map<String,Object> ccbMerchSearch(Map<String,Object> mapParam){
		Map<String,Object> jsonRetMap = null;
		Map<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_10);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_900040);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("secMerId",mapParam.get("secMerId"));
		reqMap.put("thirdMchtNo",mapParam.get("thirdMchtNo"));
		reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));
		String ObjectJson = JsonUtil.getMapToJson(reqMap);
		Map<String,String> map = JsonUtil.getJsonToMapStr(ObjectJson);
		//此处做签名
		String respStr = MD5.request(mapParam.get("order_no").toString(),Constants.getReadProperties("ccb", "ccb.online.url"),map,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(mapParam.get("order_no").toString(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
		if(respMap.get("result_code") != null && respMap.get("result_code").equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(mapParam.get("order_no").toString(),Constants.nfc_pay_status_1,respMap.get("resultMsg").toString());
			respMap.remove("encoding");
			respMap.remove("signMethod");
			respMap.remove("signAture");
			respMap.remove("txnType");
			respMap.remove("txnSubType");
			respMap.remove("channelType");
			respMap.remove("merId");
			respMap.remove("resultCode");
			respMap.remove("resultMsg");
			jsonRetMap.putAll(respMap);
			return jsonRetMap;
		}
	}
	/**
	 * 微信公众号支付
	 * @throws Exception 
	 * @throws UnsupportedEncodingException 
	 */
	public Map<String,Object> zhlNfcWechatPublicAccountsPay(Map<String,Object> mapParam) throws Exception {
		Map<String,Object> jsonRetMap = new HashMap<String, Object>();
		//保存订单流水表
		NfcOrderWater water = this.getShareOrderWater(mapParam);
		water.setStatus(Constants.nfc_pay_status_3);
		water.setMessage(Constants.nfc_pay_status_3_context);
		//计算应收手续费
		BigDecimal totalFee = water.getTotalFee();
		BigDecimal merchRate = Constants.getStrToBigDecimal(mapParam.get("merch_rate").toString());
		BigDecimal merchFee =  totalFee.multiply(merchRate);
    	water.setMerchFee(merchFee);//交易金额与手续费率相乘保留2位小数
		water.setEndTime(null);
		nfcOrderWaterMapper.insertSelective(water);
		//组装传递给通道方的报文
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_010131);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("payAccessType",Constants.pay_access_type_02);//接入支付类型
		reqMap.put("backEndUrl",Constants.getReadProperties("ccb", "ccb.online.notifyUrl"));//后台通知地址
		//不分账
		if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.wechat.public.merId"))){//不分账
			reqMap.put("merId", water.getMerchNo());//商户编号
			reqMap.put("secMerId", "");//分账子账户号
			reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			reqMap.put("secMerFeeRate", "");//分账子商户交易费率
		}else{//分账
			reqMap.put("merId", Constants.getReadProperties("ccb", "ccb.wechat.public.merId"));//商户编号
			reqMap.put("secMerId", water.getMerchNo());//分账子账户号
			reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			reqMap.put("secMerFeeRate", mapParam.get("merch_rate").toString());//分账子商户交易费率---使用分账功能时上传，是分账子商户secMerId的交易费率（若费率是0.3%，此值传0.003，小数点后最多四位），此费率值不能比平台商户merId的费率低
		}
		reqMap.put("termId","WEB");//终端编号
		reqMap.put("termIp",Constants.getNetAddressIp());//终端IP
		reqMap.put("orderId",water.getMerOrderNo());//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("productId","");//商品ID
		reqMap.put("orderBody",mapParam.get("product_name").toString());//商品描述                                                              
		reqMap.put("orderDetail",mapParam.get("product_desc").toString());//商品详情
		reqMap.put("orderGoodsTag","");//商品标记
		reqMap.put("orderSubOpenid",mapParam.get("openId").toString());//用户子标识
		reqMap.put("txnAmt",Constants.fromYuanToFen(mapParam.get("total_fee").toString()));//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("attach","");//附加数据
		if(mapParam.get("limit_pay")!=null){
			if(mapParam.get("limit_pay").toString().equals("no_credit")){
				reqMap.put("limitPay", mapParam.get("limit_pay").toString());//指定支付方式no_credit
			}else{
				reqMap.put("limitPay", "");//指定支付方式no_credit
			}
		}else{
			reqMap.put("limitPay", "");//指定支付方式no_credit
		}
		reqMap.put("needBankType","");//是否需要获取支付行类型
		//此处做签名
		String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			NfcOrderWater updateWater = new NfcOrderWater();
			updateWater.setMerOrderNo(reqMap.get("orderId"));
			if(respMap.get("respCode").toString().equals("0000")){//调用通道成功
				//拼写返回的JSAPI调用参数
				Map<String,String> jsApiMap = new HashMap<String, String>();
				jsApiMap.put("appId", respMap.get("appId").toString());//公众号ID
				jsApiMap.put("timeStamp", respMap.get("timeStamp").toString());//时间戳
				jsApiMap.put("nonceStr", respMap.get("nonceStr").toString());//随机字符串
				jsApiMap.put("_package", respMap.get("package").toString());//订单详情扩展字符串
				jsApiMap.put("signType", respMap.get("signType").toString());//签名方式
				jsApiMap.put("paySign", respMap.get("paySign").toString());//签名
				String jsApiJson = JsonUtil.getMapToJsonStr(jsApiMap);
				//组装返回给下游的参数
				jsonRetMap.put("mId", water.getMerchNo());
				jsonRetMap.put("data", DSDES.getBlackData(Constants.getReadProperties("ccb", "DES.KEY").getBytes(), jsApiJson.getBytes("utf-8")));
				jsonRetMap.put("sign", MD5.MD5Encode(jsApiJson));
				jsonRetMap.put("resType", "1");
				jsonRetMap.put("url", Constants.getReadProperties("ccb", "nfc.channelWechatPayUrl"));
				String json = JsonUtil.getMapToJson(jsonRetMap);
				logger.info("(微信公众号支付接口：)订单号："+water.getOrderNo()+",发送给通道的数据："+json);
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_9,respMap.get("respMsg").toString());
				updateWater.setStatus(Constants.nfc_pay_status_9);
				updateWater.setMessage(respMap.get("respMsg").toString());
			}
			//最后时间
			String endDate = DateUtil.format(new Date());
			try {
				if(respMap.get("txnSeqId") != null){
					updateWater.setChannelOrderNo(respMap.get("txnSeqId").toString());
				}
				updateWater.setEndTime(DateUtil.strToDate(endDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//修改订单状态
			nfcOrderWaterMapper.updateByPrimaryKeySelective(updateWater);
			return jsonRetMap;
		}
	}
	/**
	 * 支付宝二清交易创建接口
	 * @throws Exception 
	 * @throws  
	 */
	public Map<String,Object> zhlNfcAlipayTradeCreatePay(Map<String,Object> mapParam) throws Exception{
		Map<String,Object> jsonRetMap = new HashMap<String, Object>();
		//保存订单流水表
		NfcOrderWater water = this.getShareOrderWater(mapParam);
		water.setStatus(Constants.nfc_pay_status_3);
		water.setMessage(Constants.nfc_pay_status_3_context);
		//计算应收手续费
		BigDecimal totalFee = water.getTotalFee();
		BigDecimal merchRate = Constants.getStrToBigDecimal(mapParam.get("merch_rate").toString());
		BigDecimal merchFee =  totalFee.multiply(merchRate);
    	water.setMerchFee(merchFee);//交易金额与手续费率相乘保留2位小数
		water.setEndTime(null);
		nfcOrderWaterMapper.insertSelective(water);
		//组装传递给通道方的报文
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("encoding",Constants.charset_UTF_8);//编码方式
		reqMap.put("signMethod",Constants.sing_method_MD5);//签名方法
		reqMap.put("txnType",Constants.trade_type_01);//交易类型
		reqMap.put("txnSubType",Constants.trade_sub_type_010303);//交易子类型
		reqMap.put("channelType",Constants.channel_type_6002);//接入渠道
		reqMap.put("backEndUrl",Constants.getReadProperties("ccb", "ccb.online.notifyUrl"));//后台通知地址
		//不分账
		if(water.getMerchNo().equals(Constants.getReadProperties("ccb", "ccb.alipay.merId"))){//不分账
			reqMap.put("merId", water.getMerchNo());//商户编号
			reqMap.put("secMerId", "");//分账子账户号
			reqMap.put("accountFlag", "");//分账标识 Y为分账交易，secMerId必传
			reqMap.put("secMerFeeRate", "");//分账子商户交易费率
		}else{//分账
			reqMap.put("merId",Constants.getReadProperties("ccb", "ccb.alipay.merId"));//商户标编号
			reqMap.put("secMerId", water.getMerchNo());//分账子账户号
			reqMap.put("accountFlag", "Y");//分账标识 Y为分账交易，secMerId必传
			reqMap.put("secMerFeeRate", mapParam.get("merch_rate").toString());//分账子商户交易费率---使用分账功能时上传，是分账子商户secMerId的交易费率（若费率是0.3%，此值传0.003，小数点后最多四位），此费率值不能比平台商户merId的费率低
		}
		reqMap.put("termId","");//终端编号
		reqMap.put("termIp",Constants.getNetAddressIp());//客户端真实IP
		reqMap.put("orderId",water.getMerOrderNo());//商户订单号
		reqMap.put("orderTime",sdf.format(new Date()));//交易起始时间
		reqMap.put("orderBody",mapParam.get("product_name").toString());//商品描述
		reqMap.put("orderDetail",mapParam.get("product_desc").toString());//商品详细
		reqMap.put("txnAmt",Constants.fromYuanToFen(mapParam.get("total_fee").toString()));//交易金额
		reqMap.put("currencyType",Constants.trade_currency_type_CNY);//交易币种
		reqMap.put("buyerLogonId",mapParam.get("buyerLogonId").toString());//买家支付宝账号
		reqMap.put("buyerId",mapParam.get("buyerId").toString());//买家支付宝唯一用户号
		if(mapParam.get("limit_pay")!=null){
			if(mapParam.get("limit_pay").toString().equals("no_credit")){
				reqMap.put("disablePayChannels", "credit_group");//指定支付方式no_credit
			}else{
				reqMap.put("disablePayChannels", "");//指定支付方式no_credit
			}
		}else{
			reqMap.put("disablePayChannels", "");//要禁用的信用渠道(禁用支付渠道creditCard：信用卡credit_group：所有信用渠道，包含（信用卡，花呗）)
		}
		reqMap.put("needBankType","");//是否需要获取支付行类型
		//此处做签名
		String respStr = MD5.request(water.getOrderNo(),Constants.getReadProperties("ccb", "ccb.online.url"),reqMap,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		//解析返回报文
		Map<String, Object> respMap = MD5.getResp(water.getOrderNo(),respStr,Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
		if(respMap.get("result_code") != null && respMap.get("result_code").toString().equals("9")){//说明通道返回数据验签失败
			return respMap;
		}else{
			NfcOrderWater updateWater = new NfcOrderWater();
			updateWater.setMerOrderNo(reqMap.get("orderId"));
			if(respMap.get("respCode").toString().equals("0000")){//调用通道成功
				//拼写返回的JSAPI调用参数
				Map<String,String> jsApiMap = new HashMap<String, String>();
				jsApiMap.put("tradeNO", respMap.get("alipayTradeNo").toString());//支付宝交易号
				String jsApiJson = JsonUtil.getMapToJsonStr(jsApiMap);
				//组装返回给下游的参数
				jsonRetMap.put("mId", water.getMerchNo());
				jsonRetMap.put("data", DSDES.getBlackData(Constants.getReadProperties("ccb", "DES.KEY").getBytes(), jsApiJson.getBytes("utf-8")));
				jsonRetMap.put("sign", MD5.MD5Encode(jsApiJson));
				jsonRetMap.put("resType", "1");
				jsonRetMap.put("url", Constants.getReadProperties("ccb", "nfc.channelAliPayUrl"));
				String json = JsonUtil.getMapToJson(jsonRetMap);
				logger.info("(支付宝二清交易创建接口：)订单号："+water.getOrderNo()+",发送给通道的数据："+json);
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(water.getOrderNo(),Constants.nfc_pay_status_9,respMap.get("respMsg").toString());
				updateWater.setStatus(Constants.nfc_pay_status_9);
				updateWater.setMessage(respMap.get("respMsg").toString());
			}
			//最后时间
			String endDate = DateUtil.format(new Date());
			try {
				if(respMap.get("seqId") != null){
					updateWater.setChannelOrderNo(respMap.get("seqId").toString());
				}
				updateWater.setEndTime(DateUtil.strToDate(endDate));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//修改订单状态
			nfcOrderWaterMapper.updateByPrimaryKeySelective(updateWater);
			return jsonRetMap;
		}
	}
	/**
	 * 下载对账单
	 */
	public String ccbTransBill(Map<String,String> mapParam){
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("userName",mapParam.get("userName"));//用户账号
		reqMap.put("userPwd",MD5.MD5Encode(mapParam.get("userPwd")));//API密码
		reqMap.put("date",mapParam.get("date"));//对账单日期
		reqMap.put("statementType",mapParam.get("statementType"));//对账单类型
		String json = JsonUtil.getMapToJsonStr(reqMap);
//		logger.info("(中信银行对账单下载接口：),发送给通道的数据："+json);
//		HttpRequestParam param = new HttpRequestParam();
//		param.setUrl(Constants.getReadProperties("ccb","ccb.check.url"));
//		Map<String,String> heads = Maps.newHashMap();
//		heads.put("Content-Type", "application/json;charset=UTF-8");
//		param.setContext(json);
//		param.setHeads(heads);
//		HttpResponser resp=HttpHelp.postParamByHttpClient(param);
//		logger.info("(中信银行对账单下载接口：),通道返回的数据："+resp.getContent());
		return json;
	}
	
	/**
     * 构建共用订单流水对象
     * @param mapParam
     * @return
     */
    public NfcOrderWater getShareOrderWater(Map<String,Object> mapParam){
    	NfcOrderWater water = new NfcOrderWater();
    	water.setMerOrderNo(Constants.getUUID());
    	if(mapParam.get("order_no") != null){
    		water.setOrderNo(mapParam.get("order_no").toString());
		}
    	if(mapParam.get("refund_order_no") != null){
    		water.setRefundOrderNo(mapParam.get("refund_order_no").toString());
		}
    	if(mapParam.get("sub_merch_no") != null){
			water.setSubMerchNo(mapParam.get("sub_merch_no").toString());
		}
    	if(mapParam.get("merch_no") != null){
			water.setMerchNo(mapParam.get("merch_no").toString());
		}
		if(mapParam.get("merch_channel") != null){
			water.setMerchChannel(mapParam.get("merch_channel").toString());
		}
		if(mapParam.get("nfc_type") != null){
			water.setNfcType(mapParam.get("nfc_type").toString());
		}
		if(mapParam.get("nfc_merch") != null){
			water.setNfcMerch(mapParam.get("nfc_merch").toString());
		}
		if(mapParam.get("total_fee") != null){
			water.setTotalFee(Constants.getStrToBigDecimal(mapParam.get("total_fee").toString()));
		}
		if(mapParam.get("refund_fee") != null){
			water.setRefundFee(Constants.getStrToBigDecimal(mapParam.get("refund_fee").toString()));
		}
		if(mapParam.get("refund_channe") != null){
			water.setRefundChanne(mapParam.get("refund_channe").toString());
		}
		if(mapParam.get("currency") != null){
			water.setCurrency(mapParam.get("currency").toString());
		}
		if(mapParam.get("notify_url") != null){
			water.setNotifyUrl(mapParam.get("notify_url").toString());
		}
		if(mapParam.get("remark") != null){
			water.setRemark(mapParam.get("remark").toString());
		}
		String strDate = DateUtil.format(new Date());
		try {
			water.setCreatedTime(DateUtil.strToDate(strDate));
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return water;
    }
	
}
