package com.zhl.ccb.dao;

import java.util.Date;
import java.util.List;

import com.zhl.ccb.model.NfcTransBillWater;

public interface NfcTransBillWaterMapper {
    int deleteByPrimaryKey(String id);
    int deleteTransBillWater(Date date);
    
    int insert(NfcTransBillWater record);
    int insertSelective(NfcTransBillWater record);

    
    int updateByPrimaryKeySelective(NfcTransBillWater record);
    int updateByPrimaryKey(NfcTransBillWater record);
    
    
    NfcTransBillWater selectByPrimaryKey(String id);
    List<NfcTransBillWater> selectCcbTransBillWater(Date createdTime,String merchChannel,String status);
}