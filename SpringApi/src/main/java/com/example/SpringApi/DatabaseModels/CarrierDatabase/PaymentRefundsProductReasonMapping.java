package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import java.util.Date;

public class PaymentRefundsProductReasonMapping {
    // primary key - auto increment
    private int id;

    private String reason;

    // Logging
    private String notes;
    private Date createdAt;

    // Fk's
    private int refundId;
    private int productId;
    private Product product;
    private PaymentRefund refund;

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getRefundId() {
        return refundId;
    }

    public void setRefundId(int refundId) {
        this.refundId = refundId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public PaymentRefund getRefund() {
        return refund;
    }

    public void setRefund(PaymentRefund refund) {
        this.refund = refund;
    }
}