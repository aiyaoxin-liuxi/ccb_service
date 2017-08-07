package com.zhl.ccb.model;

import java.math.BigDecimal;
import java.util.Date;

public class NfcTransBillWater {
    private String id;

    private String orderNo;

    private String merOrderNo;

    private String merchChannel;

    private BigDecimal totalFee;

    private Date createdTime;

    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo == null ? null : orderNo.trim();
    }

    public String getMerOrderNo() {
        return merOrderNo;
    }

    public void setMerOrderNo(String merOrderNo) {
        this.merOrderNo = merOrderNo == null ? null : merOrderNo.trim();
    }

    public String getMerchChannel() {
        return merchChannel;
    }

    public void setMerchChannel(String merchChannel) {
        this.merchChannel = merchChannel == null ? null : merchChannel.trim();
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}