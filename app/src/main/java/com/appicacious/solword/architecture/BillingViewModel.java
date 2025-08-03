package com.appicacious.solword.architecture;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BillingViewModel extends AndroidViewModel {


    private List<ProductDetails> productDetailsList;
    private List<Purchase> purchases;
    private Purchase currentPurchase;
    private boolean isPurchaseInitiated;

    // TODO: 03/07/2022 Change its name to relate it with 'acknowledgement in process'
    private boolean isAcknowledgementInProcess;

    // This flag tells us whether query response has been received at least once or not
    private boolean isFirstQueryResponsePending = true;


    private final MutableLiveData<Boolean> isAcknowledgedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPurchasedLiveData = new MutableLiveData<>();


    public BillingViewModel(@NonNull Application application) {
        super(application);
    }

    public boolean isPurchased() {
        return getCurrentPurchase() != null;
    }

    public Purchase getCurrentPurchase() {
        return currentPurchase;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }

    @NonNull
    public List<Purchase> getPurchases() {
        if (purchases == null) {
            purchases = new ArrayList<>();
        } else {
            Collections.sort(purchases, (p1, p2) ->
                    Long.compare(p2.getPurchaseTime(), p1.getPurchaseTime()));
        }
        return purchases;
    }

    /**
     * It is called as a result of {@link BillingClient#queryPurchasesAsync(QueryPurchasesParams,
     * PurchasesResponseListener)}  and it tells us whether any acknowledged purchase is in place
     * or not. This method also pushes an update to relevant LiveData observer.
     *
     * @param currentPurchase the purchase to be stored:
     *                        1) Null means there is no purchase and app is ad-supported
     *                        2) Non-null means the purchase is with the state of PURCHASED and is
     *                        acknowledged, which in turn means that the app is ad-free
     */
    public void setCurrentPurchase(@Nullable Purchase currentPurchase) {
        if (isFirstQueryResponsePending) isFirstQueryResponsePending = false;
        this.currentPurchase = currentPurchase;
        isPurchasedLiveData.postValue(isPurchased());
    }

    @NonNull
    public List<ProductDetails> getProductDetailsList() {
        return productDetailsList == null ? new ArrayList<>() : productDetailsList;
    }

    public void setProductDetailsList(List<ProductDetails> productDetails) {
        productDetailsList = productDetails;
    }


    public boolean isPurchaseInitiated() {
        return isPurchaseInitiated;
    }

    public void setPurchaseInitiated(boolean purchaseInitiated) {
        isPurchaseInitiated = purchaseInitiated;
    }


    public boolean isFirstQueryResponsePending() {
        return isFirstQueryResponsePending;
    }

    public boolean isAcknowledgementInProcess() {
        return isAcknowledgementInProcess;
    }

    public void setAcknowledgementInProcess(boolean acknowledgementInProcess) {
        isAcknowledgementInProcess = acknowledgementInProcess;
    }

    public LiveData<Boolean> getIsPurchasedLiveData() {
        return isPurchasedLiveData;
    }

    public LiveData<Boolean> getIsAcknowledgedLiveData() {
        return isAcknowledgedLiveData;
    }

    public void setAcknowledged(boolean isAcknowledged) {
//        if (isAcknowledged) this.lastPurchaseTime = System.currentTimeMillis();
        this.isAcknowledgedLiveData.postValue(isAcknowledged);
    }
}
