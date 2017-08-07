package com.zhl.ccb.service;

import java.util.Map;

import com.zhl.ccb.model.NfcOrderWater;

public interface CcbService {
	/**
	 * 主扫订单预支付（消费）交易接口(微信)---(中互联被扫)
	 */
	public Map<String,Object> zhlNfcWechatPassive(Map<String,Object> mapParam);
	/**
	 * 支付宝二清主扫消费接口---(中互联被扫)
	 */
	public Map<String,Object> zhlNfcAlipayPassive(Map<String,Object> mapParam);
	/**
	 * 线下-被扫订单支付（消费）交易接口---(中互联主扫)
	 */
	public Map<String,Object> zhlNfcActive(Map<String,Object> mapParam);
	/**
	 * 线上-交易状态查询接口
	 */
	public void zhlNfcOnlineQuery(NfcOrderWater nfcOrderWater);
	/**
	 * 线上-支付宝二清交易查询接口
	 */
	public void zhlNfcAlipayOnlineQuery(NfcOrderWater nfcOrderWater);
	/**
	 * 线下-交易状态查询接口
	 */
	public void zhlOffLineQuery(NfcOrderWater nfcOrderWater);
	/**
	 * 线上微信退货接口
	 */
	public Map<String,Object> zhlOnLineWechatRefund(String channelRefundOrderNo,NfcOrderWater nfcOrderWater,Map<String,Object> mapParam);
	/**
	 * 线上支付宝二清退货接口
	 */
	public Map<String,Object> zhlOnLineAlipayRefund(String channelRefundOrderNo,NfcOrderWater nfcOrderWater,Map<String,Object> mapParam);
	/**
	 * 线下退货接口
	 */
	public Map<String,Object> zhlOffLineRefund(String channelRefundOrderNo,NfcOrderWater nfcOrderWater,Map<String,Object> mapParam);
	/**
	 * 分账子账户入账接口
	 */
	public Map<String,Object> ccbMerchJoin(Map<String,Object> mapParam);
	/**
	 * 分账子商户查询
	 */
	public Map<String,Object> ccbMerchSearch(Map<String,Object> mapParam);  	
	/**
	 * 微信公众号支付
	 */
	public Map<String,Object> zhlNfcWechatPublicAccountsPay(Map<String,Object> mapParam) throws Exception;
	/**
	 * 支付宝二清交易创建接口
	 */
	public Map<String,Object> zhlNfcAlipayTradeCreatePay(Map<String,Object> mapParam) throws Exception;
	/**
	 * 下载对账单
	 */
	public String ccbTransBill(Map<String,String> mapParam);
	/**
     * 构建共用订单流水对象
     * @param mapParam
     */
	public NfcOrderWater getShareOrderWater(Map<String,Object> mapParam);
}
