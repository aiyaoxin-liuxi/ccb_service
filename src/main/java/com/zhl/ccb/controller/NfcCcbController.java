package com.zhl.ccb.controller;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonSyntaxException;
import com.zhl.ccb.dao.NfcOrderWaterMapper;
import com.zhl.ccb.model.NfcOrderWater;
import com.zhl.ccb.service.CcbService;
import com.zhl.ccb.service.CcbTransBillService;
import com.zhl.ccb.utils.Constants;
import com.zhl.ccb.utils.JsonUtil;

@Controller  
@RequestMapping(value = "/nfc",produces = {"application/json;charset=UTF-8"})
public class NfcCcbController {
	Logger logger = Logger.getLogger(NfcCcbController.class);
	@Autowired
	private CcbService ccbService;
	@Autowired
	private CcbTransBillService ccbTransBillService;
	@Autowired
    private NfcOrderWaterMapper nfcOrderWaterMapper; 
	
	/**
	 * NFC支付
	 * 包含：1、中互联微信被扫2、中互联支付宝被扫3、中互联微信主扫4、中互联支付宝主扫
	 * @param json
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", value = "/nfcPay")
	public @ResponseBody String nfcPay(@RequestBody String json){
		logger.info("中互联_中信银行服务---收到请求(无线通讯支付接口)参数：" + json);
		long l = System.currentTimeMillis();
		Map<String,Object> jsonRetMap = null;
		String orderNo = "";
		try{
			Map<String,Object> jsonMap = JsonUtil.getJsonToMap(json);
			orderNo = jsonMap.get("order_no").toString();
			//通讯类型
			String nfc_type = jsonMap.get("nfc_type").toString();
			//通讯服务商
			String nfc_merch = jsonMap.get("nfc_merch").toString();
			if(nfc_type.equals(Constants.nfc_passive) && nfc_merch.equals(Constants.wechat_nfc_merch)){//中互联微信被扫
				jsonRetMap = ccbService.zhlNfcWechatPassive(jsonMap);
			}else if(nfc_type.equals(Constants.nfc_passive) && nfc_merch.equals(Constants.alipay_nfc_merch)){//中互联支付宝被扫
				jsonRetMap = ccbService.zhlNfcAlipayPassive(jsonMap);
			}else if(nfc_type.equals(Constants.nfc_active) && nfc_merch.equals(Constants.wechat_nfc_merch)){//中互联微信主扫
				jsonRetMap = ccbService.zhlNfcActive(jsonMap);
			}else if(nfc_type.equals(Constants.nfc_active) && nfc_merch.equals(Constants.alipay_nfc_merch)){//中互联支付宝主扫
				jsonRetMap = ccbService.zhlNfcActive(jsonMap);
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"请检查参数【nfc_merch】【nfc_type】");
			}
		}catch(JsonSyntaxException e){
			logger.error("中互联_中信银行服务---收到请求(无线通讯支付接口)转换JSON异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---收到请求(无线通讯支付接口)转换JSON异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			logger.error("中互联_中信银行服务---收到请求(无线通讯支付接口)接收参数异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---收到请求(无线通讯支付接口)接收参数异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}
		logger.info("耗时：中互联_中信银行服务---返回请求(无线通讯支付接口):("+ orderNo +"):"+(System.currentTimeMillis()-l));
		logger.info("中互联_中信银行服务---返回请求(无线通讯支付接口)：服务端->客户端(" + orderNo + ")参数：" + JsonUtil.getMapToJson(jsonRetMap));
		return JsonUtil.getMapToJson(jsonRetMap);
	}
	/**
	 * NFC退货
	 * 包含：1、线上微信退货2、线上支付宝二清退货3、线下微信退货4.线下支付宝退货
	 * @param json
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", value = "/nfcRefund")
	public @ResponseBody String nfcRefund(@RequestBody String json){
		logger.info("中互联_中信银行服务---收到请求(无线通讯退货接口)参数：" + json);
		long l = System.currentTimeMillis();
		Map<String,Object> jsonRetMap = null;
		String orderNo = "";
		try{
			Map<String,Object> jsonMap = JsonUtil.getJsonToMap(json);
			orderNo = jsonMap.get("order_no").toString();
			//下游传入的退货订单号
			String refundOrderNo = jsonMap.get("refund_order_no").toString();
			//退货渠道
			String merchChannel = jsonMap.get("merch_channel").toString();
			//通讯类型
			String nfcType = jsonMap.get("nfc_type").toString();
			//通讯服务商
			String nfcMerch = jsonMap.get("nfc_merch").toString();
			/**
			 * 根据下游传入的refund_order_no退货订单号，找到原支付交易数据 
			 * 中互联被扫相当于中信主扫
			 * 中互联主扫相当于中信被扫
			 */
			NfcOrderWater nfcOrderWater = nfcOrderWaterMapper.selOldRefundWater(refundOrderNo,merchChannel,nfcMerch);
			if(nfcOrderWater != null){
				if(nfcOrderWater.getNfcMerch().equals(Constants.wechat_nfc_merch) && nfcType.equals(Constants.nfc_refund)){//微信
					if(nfcOrderWater.getNfcType().equals(Constants.nfc_passive) || nfcOrderWater.getNfcType().equals(Constants.nfc_channelPay)){//线上微信退货
						String channelRefundOrderNo = nfcOrderWater.getMerOrderNo();
						jsonRetMap = ccbService.zhlOnLineWechatRefund(channelRefundOrderNo,nfcOrderWater,jsonMap);
					}
					if(nfcOrderWater.getNfcType().equals(Constants.nfc_active)){//线下微信退货
						String channelRefundOrderNo = nfcOrderWater.getChannelOrderNo();
						jsonRetMap = ccbService.zhlOffLineRefund(channelRefundOrderNo, nfcOrderWater, jsonMap);
					}
				}else if(nfcOrderWater.getNfcMerch().equals(Constants.alipay_nfc_merch) && nfcType.equals(Constants.nfc_refund)){//支付宝
					if(nfcOrderWater.getNfcType().equals(Constants.nfc_passive) || nfcOrderWater.getNfcType().equals(Constants.nfc_channelPay)){//线上支付宝退货
						String channelRefundOrderNo = nfcOrderWater.getChannelOrderNo();
						jsonRetMap = ccbService.zhlOnLineAlipayRefund(channelRefundOrderNo,nfcOrderWater,jsonMap);
					}
					if(nfcOrderWater.getNfcType().equals(Constants.nfc_active)){//线下支付宝退货
						String channelRefundOrderNo = nfcOrderWater.getChannelOrderNo();
						jsonRetMap = ccbService.zhlOffLineRefund(channelRefundOrderNo, nfcOrderWater, jsonMap);
					}
				}else{
					jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"请检查参数【nfc_merch】【nfc_type】");
				}
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---收到请求(无线通讯退货接口):此订单不存在！");
			}
		}catch(JsonSyntaxException e){
			logger.error("中互联_中信银行服务---收到请求(无线通讯退货接口)转换JSON异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---收到请求(无线通讯退货接口)转换JSON异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			logger.error("中互联_中信银行服务---收到请求(无线通讯退货接口)接收参数异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---收到请求(无线通讯退货接口)接收参数异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}
		logger.info("耗时：中互联_中信银行服务---返回请求(无线通讯退货接口):("+ orderNo +"):"+(System.currentTimeMillis()-l));
		logger.info("中互联_中信银行服务---返回请求(无线通讯退货接口)：服务端->客户端(" + orderNo + ")参数：" + JsonUtil.getMapToJson(jsonRetMap));
		return JsonUtil.getMapToJson(jsonRetMap);
	}
	/**
	 * 中信银行分账子账户入驻
	 * @param json
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", value = "/nfcMerchJoin")
	public @ResponseBody String nfcMerchJoin(@RequestBody String json){
		logger.info("中互联_中信银行服务---收到请求(无线通讯分账子账户入驻接口)参数：" + json);
		Map<String,Object> jsonRetMap = null;
		String orderNo = "";
		try{
			Map<String,Object> jsonMap = JsonUtil.getJsonToMap(json);
			orderNo = jsonMap.get("order_no").toString();
			jsonRetMap = ccbService.ccbMerchJoin(jsonMap);
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(JsonSyntaxException e){
			logger.error("中互联_中信银行服务---分账子账户入驻接口转换JSON异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---分账子账户入驻接口转换JSON异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			logger.error("中互联_中信银行服务---分账子账户入驻接口接收参数异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---分账子账户入驻接口接收参数异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}
	}
	/**
	 * 中信银行分账子账户查询
	 * @param json
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", value = "/nfcMerchSearch")
	public @ResponseBody String nfcMerchSearch(@RequestBody String json){
		logger.info("中互联_中信银行服务---收到请求(无线通讯分账子账户查询接口)参数：" + json);
		Map<String,Object> jsonRetMap = null;
		String orderNo = "";
		try{
			Map<String,Object> jsonMap = JsonUtil.getJsonToMap(json);
			orderNo = jsonMap.get("order_no").toString();
			jsonRetMap = ccbService.ccbMerchSearch(jsonMap);
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(JsonSyntaxException e){
			logger.error("中互联_中信银行服务---分账子账户查询接口转换JSON异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---分账子账户查询接口转换JSON异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			logger.error("中互联_中信银行服务---分账子账户查询接口接收参数异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---分账子账户查询接口接收参数异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}
	}
	/**
	 * 中信银行通道支付
	 * 微信公众号支付
	 * 支付宝收银台支付
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", value = "/nfcChannelPay")
	public @ResponseBody String nfcChannelPay(@RequestBody String json){
		logger.info("中互联_中信银行服务---收到请求(无线通讯通道支付接口)参数：" + json);
		Map<String,Object> jsonRetMap = null;
		String orderNo = "";
		try{
			Map<String,Object> jsonMap = JsonUtil.getJsonToMap(json);
			orderNo = jsonMap.get("order_no").toString();
			if(jsonMap.get("nfc_merch").toString().equals(Constants.wechat_nfc_merch)){//微信
				jsonRetMap = ccbService.zhlNfcWechatPublicAccountsPay(jsonMap);
			}else if(jsonMap.get("nfc_merch").toString().equals(Constants.alipay_nfc_merch)){//支付宝
				jsonRetMap = ccbService.zhlNfcAlipayTradeCreatePay(jsonMap);
			}else{
				jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---通道支付nfc_merch is error");
			}
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(JsonSyntaxException e){
			logger.error("中互联_中信银行服务---微信公众号支付接口转换JSON异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---通道支付接口转换JSON异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			logger.error("中互联_中信银行服务---微信公众号支付接口收参数异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"中互联_中信银行服务---通道支付接口接收参数异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}
	}
	/**
	 * 对账文件下载导入
	 * @param json
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", value = "/nfcTransBill")
	public @ResponseBody String nfcTransBill(@RequestBody String json){
		logger.info("收到请求(对账数据重导接口)：参数：" + json);
		long l = System.currentTimeMillis();
		Map<String,Object> jsonRetMap = null;
		String returnString = "";
		String orderNo = "";
		try{
			Map<String,Object> jsonMap = JsonUtil.getJsonToMap(json);
				orderNo = jsonMap.get("order_no").toString();
				returnString = ccbTransBillService.status(jsonMap.get("date_time").toString());
				logger.info("耗时：对账数据重导接口("+ orderNo +"):"+(System.currentTimeMillis()-l));
				logger.info("收到请求(对账数据重导接口)：服务端->客户端(" + orderNo + ")参数：" + returnString);
				return returnString;
		}catch(JsonSyntaxException e){
			logger.error("对账数据重导接口转换JSON异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"对账数据重导接口转换JSON异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			logger.error("对账数据重导接口接收参数异常："+orderNo+","+e.getMessage());
			jsonRetMap = JsonUtil.getReturnNFCMessageHead(orderNo,Constants.nfc_pay_status_9,"对账数据重导接口接收参数异常");
			return JsonUtil.getMapToJson(jsonRetMap);
		}
	}
	
}
