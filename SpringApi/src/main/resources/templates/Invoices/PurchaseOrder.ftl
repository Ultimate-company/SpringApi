<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Purchase Order Invoice</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 800px;
            margin: 20px auto;
            background-color: #fff;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        h3 {
            text-align: center;
            margin-bottom: 20px;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .company-logo {
            width: 100px;
            height: auto;
        }
        .company-info {
            text-align: right;
        }
        .company-info h2 {
            margin: 0;
        }
        .section {
            margin-bottom: 30px;
        }
        .section-title {
            font-size: 18px;
            font-weight: bold;
            margin-bottom: 10px;
        }
        .section-content {
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .address {
            font-style: italic;
        }
        .table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        .table th, .table td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }
        .table th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
<div class="container">

    <!-- Header -->
    <div class="header">
        <div class="company-logo">
            <img src="${companyLogo}" alt="Company Logo" height="100" width="100"/>
        </div>
        <div class="company-info">
            <h2>${companyName}</h2>
            <p>${website}</p>
            <p>${fullAddress}</p>
        </div>
    </div><hr />

    <h3>Purchase Order Invoice</h3>


    <!-- Purchase order details -->
    <div class="section">
        <div class="section-title">Purchase Order Details</div>
        <div class="section-content">
            <p><strong>Purchase Order ID:</strong> ${purchaseOrder.purchaseOrderId}</p>
            <p><strong>Expected Shipment Date:</strong> ${purchaseOrder.expectedShipmentDate}</p>
            <p><strong>Vendor Number:</strong> ${purchaseOrder.vendorNumber}</p>
            <p><strong>Terms and Conditions:</strong></p>
            <div class="terms">
                ${purchaseOrder.termsConditionsHtml}
            </div>
            <p><strong>Order Receipt:</strong> ${purchaseOrder.orderReceipt}</p>
            <p><strong>Approved:</strong> ${purchaseOrder.approved?string('Yes', 'No')}</p>
        </div>
    </div>

    <!-- Shipping address -->
    <div class="section">
        <div class="section-title">Shipping Address</div>
        <div class="section-content address">
            <p>${shippingAddress.nameOnAddress}</p>
            <p>${shippingAddress.line1}, ${shippingAddress.line2}</p>
            <p>${shippingAddress.city}, ${shippingAddress.state}, ${shippingAddress.zipCode}</p>
        </div>
    </div>

    <!-- Lead purchase order is associated to-->
    <div class="section">
        <div class="section-title">Lead Information</div>
        <div class="section-content">
            <p><strong>Company:</strong> ${lead.company}</p>
            <p><strong>Contact Name:</strong> ${lead.firstName} ${lead.lastName}</p>
            <p><strong>Email:</strong> ${lead.email}</p>
            <p><strong>Phone:</strong> ${lead.phone}</p>
            <p><strong>Title:</strong> ${lead.title}</p>
            <p><strong>Website:</strong> <a href="${lead.website}">${lead.website}</a></p>
        </div>
    </div>

    <!-- Purchase Order Created By -->
    <div class="section">
        <div class="section-title">Purchase Order Created By</div>
        <div class="section-content">
            <p><strong>Name:</strong> ${purchaseOrderCreatedBy.firstName} ${purchaseOrderCreatedBy.lastName}</p>
            <p><strong>Email:</strong> ${purchaseOrderCreatedBy.loginName}</p>
            <p><strong>Role:</strong> ${purchaseOrderCreatedBy.role}</p>
            <p><strong>Phone:</strong> ${purchaseOrderCreatedBy.phone}</p>
        </div>
    </div>

    <!-- Purchase Order Approved By -->
    <div class="section">
        <div class="section-title">Purchase Order Approved By</div>
        <div class="section-content">
            <p><strong>Name:</strong> ${purchaseOrderApprovedBy.firstName} ${purchaseOrderApprovedBy.lastName}</p>
            <p><strong>Email:</strong> ${purchaseOrderApprovedBy.loginName}</p>
            <p><strong>Role:</strong> ${purchaseOrderApprovedBy.role}</p>
            <p><strong>Phone:</strong> ${purchaseOrderApprovedBy.phone}</p>
        </div>
    </div>

    <!-- Product id, quantity, price mapping -->
    <div class="section">
        <div class="section-title">Product Details</div>
        <div class="section-content">
            <table class="table">
                <thead>
                <tr>
                    <th>Product</th>
                    <th>Quantity</th>
                </tr>
                </thead>
                <tbody>
                <#list purchaseOrdersProductQuantityMaps as map>
                    <tr>
                        <td>${map.productId}</td>
                        <td>${map.quantity}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>