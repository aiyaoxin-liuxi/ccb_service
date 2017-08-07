package com.zhl.ccb.dao;

import java.util.List;

import com.zhl.ccb.model.NfcOrderWater;
public interface NfcOrderWaterMapper {
    int deleteByPrimaryKey(String merOrderNo);

    int insert(NfcOrderWater record);

    int insertSelective(NfcOrderWater record);

    NfcOrderWater selectByPrimaryKey(String merOrderNo);

    int updateByPrimaryKeySelective(NfcOrderWater record);

    int updateByPrimaryKey(NfcOrderWater record);
    
    List<NfcOrderWater> selectAll();
    List<NfcOrderWater> selNfcMerch();
    List<NfcOrderWater> selTransWater();
    List<NfcOrderWater> selRefundWater();
    //根据下游传入的退货订单，查询对应的原支付数据
    NfcOrderWater selOldRefundWater(String orderNo,String merchChannel,String nfcMerch);
    NfcOrderWater selOldTransRefundWater(String refundOrderNo);
    
    List<NfcOrderWater> selRfOrderWater(String createdTime, String merchChannel,String status);
}