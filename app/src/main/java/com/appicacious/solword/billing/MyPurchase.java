package com.appicacious.solword.billing;

public class MyPurchase {

    private String orderId;
    private String packageName;
    private String productId;
    private long purchaseTime;
    private int purchaseState;
    private String purchaseToken;
    private int quantity;
    private boolean acknowledged;


    public String getOrderId() {
        return orderId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getProductId() {
        return productId;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public int getPurchaseState() {
        return purchaseState;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    @Override
    public String toString() {
        return "MyPurchase{" +
                "orderId='" + orderId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", productId='" + productId + '\'' +
                ", purchaseTime=" + purchaseTime +
                ", purchaseState=" + purchaseState +
                ", purchaseToken='" + purchaseToken + '\'' +
                ", quantity=" + quantity +
                ", acknowledged=" + acknowledged +
                '}';
    }
}
