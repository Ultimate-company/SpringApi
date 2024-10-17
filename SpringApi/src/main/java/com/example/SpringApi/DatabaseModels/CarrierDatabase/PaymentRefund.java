package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import java.sql.Timestamp;
import java.util.List;

public class PaymentRefund {
    // primary key - auto increment
    private int paymentRefundId;
    private String refundId;
    private String status;
    private String speed;
    private double amount;
    //Logging
    private String notes;
    private Timestamp createdAt;
    //Fk's
    private int paymentId;
    private int razorpayId;
    private PaymentInfo payment;
    //Navigation
    private List<PaymentRefundsProductMapping> paymentRefundsProductMappings;
    private List<PaymentRefundsProductReasonMapping> paymentRefundsProductReasonMappings;
}