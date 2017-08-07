package com.zhl.ccb.model;

import java.math.BigDecimal;
import java.util.Date;

public class NfcMerch {
    private String merchNo;

    private String merchName;

    private String merchKey;

    private String channel;

    private BigDecimal merchRate;

    private Date createdTime;

    public String getMerchNo() {
        return merchNo;
    }

    public void setMerchNo(String merchNo) {
        this.merchNo = merchNo == null ? null : merchNo.trim();
    }

    public String getMerchName() {
        return merchName;
    }

    public void setMerchName(String merchName) {
        this.merchName = merchName == null ? null : merchName.trim();
    }

    public String getMerchKey() {
        return merchKey;
    }

    public void setMerchKey(String merchKey) {
        this.merchKey = merchKey == null ? null : merchKey.trim();
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel == null ? null : channel.trim();
    }

    public BigDecimal getMerchRate() {
        return merchRate;
    }

    public void setMerchRate(BigDecimal merchRate) {
        this.merchRate = merchRate;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

}