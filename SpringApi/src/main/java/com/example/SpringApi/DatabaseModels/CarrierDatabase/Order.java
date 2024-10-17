package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;



public class Order {
    public enum OrderStatus {
        PendingPickup(1),
        Transit(2),
        Delivered(3),
        Refunded(4);

        private final int value;

        OrderStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    private int orderId;
    private OrderStatus status;
    private Timestamp purchaseTimeStamp;
    private Date estimatedDeliveryDate;
    private boolean isCancelled;
    private String cancellationReason;
    private String shipRocketOrderId;
    private String notes;
    private Timestamp createdAt;
    private int pickupLocationId;
    private Integer paymentId;
    private int customerId;
    private int billingId;
    private int customerAddressId;
    private int billingAddressId;
    private Address billingAddress;
    private Address customerAddress;
    private PaymentInfo payment;
    private PickupLocation pickupLocation;
    private List<OrdersProductMapping> ordersProductMappings;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Timestamp getPurchaseTimeStamp() {
        return purchaseTimeStamp;
    }

    public void setPurchaseTimeStamp(Timestamp purchaseTimeStamp) {
        this.purchaseTimeStamp = purchaseTimeStamp;
    }

    public Date getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getShipRocketOrderId() {
        return shipRocketOrderId;
    }

    public void setShipRocketOrderId(String shipRocketOrderId) {
        this.shipRocketOrderId = shipRocketOrderId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getPickupLocationId() {
        return pickupLocationId;
    }

    public void setPickupLocationId(int pickupLocationId) {
        this.pickupLocationId = pickupLocationId;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getBillingId() {
        return billingId;
    }

    public void setBillingId(int billingId) {
        this.billingId = billingId;
    }

    public int getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(int customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public int getBillingAddressId() {
        return billingAddressId;
    }

    public void setBillingAddressId(int billingAddressId) {
        this.billingAddressId = billingAddressId;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }


    public Address getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(Address customerAddress) {
        this.customerAddress = customerAddress;
    }

    public PaymentInfo getPayment() {
        return payment;
    }

    public void setPayment(PaymentInfo payment) {
        this.payment = payment;
    }

    public PickupLocation getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(PickupLocation pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public List<OrdersProductMapping> getOrdersProductMappings() {
        return ordersProductMappings;
    }

    public void setOrdersProductMappings(List<OrdersProductMapping> ordersProductMappings) {
        this.ordersProductMappings = ordersProductMappings;
    }
}