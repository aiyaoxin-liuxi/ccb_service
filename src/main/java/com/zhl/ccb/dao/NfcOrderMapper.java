package com.zhl.ccb.dao;

import com.zhl.ccb.model.NfcOrder;

public interface NfcOrderMapper {
    int deleteByPrimaryKey(String orderNo);

    int insert(NfcOrder record);

    int insertSelective(NfcOrder record);

    NfcOrder selectByPrimaryKey(String orderNo);

    int updateByPrimaryKeySelective(NfcOrder record);

    int updateByPrimaryKey(NfcOrder record);
}