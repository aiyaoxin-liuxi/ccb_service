package com.zhl.ccb.model;

import java.math.BigDecimal;
import java.util.Date;

public class NfcOrder {
    private String orderNo;

    private String refundOrderNo;

    private String subMerchNo;

    private String merchNo;

    private String merchChannel;

    private String nfcType;

    private String nfcMerch;

    private BigDecimal totalFee;

    private BigDecimal refundFee;

    private String refundChanne;

    private String currency;

    private String notifyUrl;

    private String remark;

    private Date createdTime;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo == null ? null : orderNo.trim();
    }

    public String getRefundOrderNo() {
        return refundOrderNo;
    }

    public void setRefundOrderNo(String refundOrderNo) {
        this.refundOrderNo = refundOrderNo == null ? null : refundOrderNo.trim();
    }

    public String getSubMerchNo() {
        return subMerchNo;
    }

    public void setSubMerchNo(String subMerchNo) {
        this.subMerchNo = subMerchNo == null ? null : subMerchNo.trim();
    }

    public String getMerchNo() {
        return merchNo;
    }

    public void setMerchNo(String merchNo) {
        this.merchNo = merchNo == null ? null : merchNo.trim();
    }

    public String getMerchChannel() {
        return merchChannel;
    }

    public void setMerchChannel(String merchChannel) {
        this.merchChannel = merchChannel == null ? null : merchChannel.trim();
    }

    public String getNfcType() {
        return nfcType;
    }

    public void setNfcType(String nfcType) {
        this.nfcType = nfcType == null ? null : nfcType.trim();
    }

    public String getNfcMerch() {
        return nfcMerch;
    }

    public void setNfcMerch(String nfcMerch) {
        this.nfcMerch = nfcMerch == null ? null : nfcMerch.trim();
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public BigDecimal getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(BigDecimal refundFee) {
        this.refundFee = refundFee;
    }

    public String getRefundChanne() {
        return refundChanne;
    }

    public void setRefundChanne(String refundChanne) {
        this.refundChanne = refundChanne == null ? null : refundChanne.trim();
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency == null ? null : currency.trim();
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl == null ? null : notifyUrl.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}