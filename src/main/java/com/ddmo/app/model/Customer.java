package com.ddmo.app.model;

import java.time.LocalDateTime;

public class Customer {
    private String id;
    private String name;
    private String phone;
    private String verifyCode;
    private String remark;
    private String status;
    private LocalDateTime createdAt;

    public Customer() {
    }

    public Customer(String id, String name, String phone, String verifyCode, String remark, String status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.verifyCode = verifyCode;
        this.remark = remark;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
