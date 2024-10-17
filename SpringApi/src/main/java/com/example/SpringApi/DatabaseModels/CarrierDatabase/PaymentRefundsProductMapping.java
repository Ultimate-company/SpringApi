package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import java.util.Date;

public class PaymentRefundsProductMapping {
    // primary key - auto increment
    private int id;

    private double pricing;
    private int quantity;

    // Fk's
    private int refundId;
    private int productId;
    private Product product;
    private PaymentRefund refund;

    // Logging
    private String notes;
    private Date createdAt;

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPricing() {
        return pricing;
    }

    public void setPricing(double pricing) {
        this.pricing = pricing;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
}