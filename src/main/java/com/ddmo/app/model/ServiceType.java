package com.ddmo.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceType {
    private String id;
    private String name;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;

    public ServiceType() {
    }

    public ServiceType(String id, String name, BigDecimal price, String status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

