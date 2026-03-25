package com.ddmo.app.dto;

import java.math.BigDecimal;

public class CustomerRequest {
    private String name;
    private String phone;
    private String verifyCode;
    private BigDecimal initialRechargeAmount;
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public BigDecimal getInitialRechargeAmount() {
        return initialRechargeAmount;
    }

    public void setInitialRechargeAmount(BigDecimal initialRechargeAmount) {
        this.initialRechargeAmount = initialRechargeAmount;
    }
}
