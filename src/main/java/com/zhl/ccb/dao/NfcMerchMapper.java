package com.zhl.ccb.dao;

import com.zhl.ccb.model.NfcMerch;

public interface NfcMerchMapper {
    int deleteByPrimaryKey(String merchNo);

    int insert(NfcMerch record);

    int insertSelective(NfcMerch record);

    NfcMerch selectByPrimaryKey(String merchNo);

    int updateByPrimaryKeySelective(NfcMerch record);

    int updateByPrimaryKey(NfcMerch record);
}