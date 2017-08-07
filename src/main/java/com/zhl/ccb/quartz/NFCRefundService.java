package com.zhl.ccb.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhl.ccb.dao.NfcOrderWaterMapper;
import com.zhl.ccb.model.NfcOrderWater;
import com.zhl.ccb.service.CcbService;
import com.zhl.ccb.utils.Constants;

public class NFCRefundService {
	@Autowired
	private NfcOrderWaterMapper nfcOrderWaterMapper;
	@Autowired
	private CcbService ccbService;
	
	SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Log logger = LogFactory.getLog(NFCRefundService.class);
	public void status(){
		logger.info("==============================进入NFC-中信服务退货查询定时器==============================");
		try{
			//查询中信退货流水
			List<NfcOrderWater> waterList = nfcOrderWaterMapper.selRefundWater();
			logger.info("==============================NFC-中信服务退货查询定时任务准备执行的数量："+waterList.size());
			for(NfcOrderWater water : waterList){
				//跟就此退货数据，推算出此笔退货的交易是线上还是线下，支付宝还是微信
				NfcOrderWater oldWater = nfcOrderWaterMapper.selOldTransRefundWater(water.getRefundOrderNo());
				if(oldWater != null){
					if(oldWater.getNfcMerch().equals(Constants.wechat_nfc_merch)){//微信
						if(oldWater.getNfcType().equals(Constants.nfc_passive)){//被扫
							ccbService.zhlNfcOnlineQuery(water);
						}else if(oldWater.getNfcType().equals(Constants.nfc_active)){//主扫
							ccbService.zhlOffLineQuery(water);
						}else if(oldWater.getNfcType().equals(Constants.nfc_channelPay)){//公会号支付
							water.setNfcType(oldWater.getNfcType());
							ccbService.zhlNfcOnlineQuery(water);
						}
					}else if(oldWater.getNfcMerch().equals(Constants.alipay_nfc_merch)){//支付宝
						if(oldWater.getNfcType().equals(Constants.nfc_passive)){//被扫
							ccbService.zhlNfcAlipayOnlineQuery(water);
						}else if(oldWater.getNfcType().equals(Constants.nfc_active)){//主扫
							ccbService.zhlOffLineQuery(water);
						}else if(water.getNfcType().equals(Constants.nfc_channelPay)){//收银台
							ccbService.zhlNfcAlipayOnlineQuery(water);
						}
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
	}
}
