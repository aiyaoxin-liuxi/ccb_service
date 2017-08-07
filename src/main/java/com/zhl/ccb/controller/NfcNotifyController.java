package com.zhl.ccb.controller;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mjson.Json;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.zhl.ccb.dao.NfcMerchMapper;
import com.zhl.ccb.dao.NfcOrderWaterMapper;
import com.zhl.ccb.model.NfcMerch;
import com.zhl.ccb.model.NfcOrderWater;
import com.zhl.ccb.utils.Constants;
import com.zhl.ccb.utils.DateUtil;
import com.zhl.ccb.utils.EncodeUtils;
import com.zhl.ccb.utils.JsonUtil;
import com.zhl.ccb.utils.MD5;

@Controller
@RequestMapping(value="/zhl")
public class NfcNotifyController {
	Logger logger = Logger.getLogger(NfcNotifyController.class);
	@Autowired
    private NfcOrderWaterMapper nfcOrderWaterMapper; 
	@Autowired
    private NfcMerchMapper nfcMerchMapper;
	
	@RequestMapping(method=RequestMethod.POST,value = "/ccbNotifyUrl")
	public String ccbNotifyUrl(HttpServletRequest request, HttpServletResponse response){
		Map<String,String> returnMap = new HashMap<String, String>();
		returnMap.put("respCode", "9999");
		returnMap.put("respMsg", "ERROR");
		String returnParam = "";
		String json = "";
		String b64ReqStr = "";
		String respB64Str = request.getParameter("sendData");
		logger.info("接收中信异步通知参数respB64Str="+respB64Str);
		//base64解码,并对一些特殊字符进行置换
		byte [] respJsBs = Base64.decodeBase64(respB64Str.replaceAll("#","+"));
		String respJsStr = null;
		try {
			respJsStr = new String(respJsBs,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		//解析json
		Json respJs = Json.read(respJsStr);
		logger.info("接收中信异步通知参数解析json="+respJs);
		//转成map方便排序
		SortedMap<String, Object> sm = new TreeMap<String, Object>(respJs.asMap());
		//按序拼接
		StringBuilder sb = new StringBuilder();
		for(Entry<String, Object> sme : sm.entrySet()){
			//排除signAture字段
			if("signAture".equals(sme.getKey()))
				continue;
			String v = (String)sme.getValue();
			//空字段不参加验签
			if(null == v || v.length()==0)
				continue;
			sb.append("&").append(sme.getKey()).append("=").append(v);
		}
		//判断是支付宝还是微信的交易
		NfcOrderWater nfcOrderWater = nfcOrderWaterMapper.selectByPrimaryKey(sm.get("orderId").toString());
		if(nfcOrderWater == null){
			logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",无此交易数据，不做操作");
		}else{
			if(nfcOrderWater.getNfcMerch().equals(Constants.wechat_nfc_merch)){
				//区分中互联被扫和中互联通道支付
				if(nfcOrderWater.getNfcType().equals(Constants.nfc_channelPay)){
					logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",异步通知微信公众号交易");
					sb.append("&key=").append(Constants.getReadProperties("ccb", "ccb.wechat.public.md5key"));
				}else{
					logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",异步通知微信交易");
					sb.append("&key=").append(Constants.getReadProperties("ccb", "ccb.wechat.active.passive.md5key"));
				}
			}
			if(nfcOrderWater.getNfcMerch().equals(Constants.alipay_nfc_merch)){
				logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",异步通知支付宝交易");
				sb.append("&key=").append(Constants.getReadProperties("ccb", "ccb.alipay.md5key"));
			}
			logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",加签报文="+sb.substring(1));
			try {
				String signAture = MD5.MD5Encode(sb.substring(1)).toUpperCase();
				logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",本地加签后的签名："+signAture);
				String respSign = respJs.at("signAture").toString();
				respSign=respSign.substring(1,respSign.length()-1);
				logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",接收报文中的签名："+respSign);
				if (respSign.equals(signAture)){
					logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",md5 OK!");
					if(nfcOrderWater.getStatus().equals(Constants.nfc_pay_status_1) || nfcOrderWater.getStatus().equals(Constants.nfc_pay_status_9)){
						//该订单的状态为成功或失败，无需做处理
			    		logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",该订单的状态为成功或失败，无需做处理！！！");
					}else{
						if(sm.get("respCode").equals("0000")){//交易成功
							logger.info("(接收中信异步通知接口：)订单号：orderid="+sm.get("orderId")+",订单交易成功");
							nfcOrderWater.setStatus(Constants.nfc_pay_status_1);
							nfcOrderWater.setMessage(Constants.nfc_pay_status_1_context);
						}else{//交易失败
							logger.info("(接收中信异步通知接口：)订单号：orderid="+sm.get("orderId")+",订单交易失败");
							nfcOrderWater.setStatus(Constants.nfc_pay_status_9);
							nfcOrderWater.setMessage(sm.get("respMsg").toString());
						}
						//保存中信平台交易流水号
						if(sm.get("seqId") != null){
							nfcOrderWater.setChannelOrderNo(sm.get("seqId").toString());
						}
						if(sm.get("txnSeqId") != null){
							nfcOrderWater.setChannelOrderNo(sm.get("txnSeqId").toString());
						}
						//最后时间
						String endDate = DateUtil.format(new Date());
						try {
							nfcOrderWater.setEndTime(DateUtil.strToDate(endDate));
						} catch (Exception e) {
							e.printStackTrace();
						}
						nfcOrderWaterMapper.updateByPrimaryKeySelective(nfcOrderWater);
						//组装异步通知参数 准备发送给商户
						if(nfcOrderWater.getNotifyUrl() != null && !"".equals(nfcOrderWater.getNotifyUrl())){
							Map<String,Object> notityMap = new LinkedHashMap<String, Object>();
							notityMap.put("order_no", nfcOrderWater.getOrderNo());
							notityMap.put("total_fee", nfcOrderWater.getTotalFee());
							notityMap.put("result_code", nfcOrderWater.getStatus());
							notityMap.put("message", nfcOrderWater.getMessage());
							notityMap.put("end_time", DateUtil.format(nfcOrderWater.getEndTime()));
							String paramStr = Constants.getBuildPayParams(notityMap);
							String signStr = "";
							NfcMerch nfcMerch = nfcMerchMapper.selectByPrimaryKey(nfcOrderWater.getSubMerchNo());
							String key = nfcMerch.getMerchKey();
							signStr = MD5.encrypt(EncodeUtils.base64Encode(paramStr.getBytes("UTF-8")),key);
							Map<String,Object> m = new LinkedHashMap<String, Object>();
							m.put("order_no", nfcOrderWater.getOrderNo());
							m.put("data", paramStr+"&sign="+signStr);
							Constants.getSendNotifyUrl(nfcOrderWater.getNotifyUrl(),m);
						}
					}
					//返回给中信应答信息(接受成功)
					returnMap.put("respCode", "0000");
					returnMap.put("respMsg", "OK");
					json = JsonUtil.getMapToJsonStr(returnMap);
					b64ReqStr = Base64.encodeBase64String(json.getBytes("utf-8")).replaceAll("\\+", "#");
					returnParam = "sendData="+b64ReqStr;
				}else{
					logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",md5 ERROR!");
					//返回给中信应答信息(接受失败)
					json = JsonUtil.getMapToJsonStr(returnMap);
					b64ReqStr = Base64.encodeBase64String(json.getBytes("utf-8")).replaceAll("\\+", "#");
					returnParam = "sendData="+b64ReqStr;
				} 
			} catch (Exception ex) {
				logger.error("(接收中信异步通知接口：)订单号："+sm.get("orderId")+ex.getMessage(), ex);
				try {
					//返回给中信应答信息(接受失败)
					json = JsonUtil.getMapToJsonStr(returnMap);
					b64ReqStr = Base64.encodeBase64String(json.getBytes("utf-8")).replaceAll("\\+", "#");
					returnParam = "sendData="+b64ReqStr;
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			}
		}
		logger.info("(接收中信异步通知接口：)订单号："+sm.get("orderId")+",返回给中信的数据：returnParam="+returnParam);
		return returnParam;
	}
}
