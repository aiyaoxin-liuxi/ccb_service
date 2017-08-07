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

public class NFCqueryService {
	@Autowired
	private NfcOrderWaterMapper nfcOrderWaterMapper;
	@Autowired
	private CcbService ccbService;
	
	SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Log logger = LogFactory.getLog(NFCqueryService.class);
	public void status(){
		logger.info("==============================进入NFC-中信服务交易查询定时器==============================");
		try{
			//查询中信支付流水
			List<NfcOrderWater> waterList = nfcOrderWaterMapper.selTransWater(); 
			logger.info("==============================NFC-中信服务交易查询定时任务准备执行的数量："+waterList.size());
			for(NfcOrderWater water : waterList){
				//未支付订单暂定半小时超时
				Date date = new Date();
				String start_Time = sdf.format(date); 
				Date startTime =  sdf.parse(start_Time);
				Date endTime = water.getCreatedTime();
				long time = (startTime.getTime() - endTime.getTime())/(1000);
				if(time > 1800){//中互联自动改为超时
					water.setEndTime(startTime);
					water.setStatus(Constants.nfc_pay_status_19);
					water.setMessage(Constants.nfc_pay_status_19_context_ZHL);
					nfcOrderWaterMapper.updateByPrimaryKeySelective(water);
				}else{
					if(water.getNfcMerch().equals(Constants.wechat_nfc_merch)){//微信
						if(water.getNfcType().equals(Constants.nfc_passive)){//被扫
							ccbService.zhlNfcOnlineQuery(water);
						}else if(water.getNfcType().equals(Constants.nfc_active)){//主扫
							ccbService.zhlOffLineQuery(water);
						}else if(water.getNfcType().equals(Constants.nfc_channelPay)){//公众号支付
							ccbService.zhlNfcOnlineQuery(water);
						}
					}else if(water.getNfcMerch().equals(Constants.alipay_nfc_merch)){//支付宝
						if(water.getNfcType().equals(Constants.nfc_passive)){//被扫
							ccbService.zhlNfcAlipayOnlineQuery(water);
						}else if(water.getNfcType().equals(Constants.nfc_active)){//主扫
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
