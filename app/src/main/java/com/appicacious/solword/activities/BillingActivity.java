package com.appicacious.solword.activities;

import static com.android.billingclient.api.Purchase.PurchaseState.PURCHASED;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.appicacious.solword.R;
import com.appicacious.solword.architecture.BillingViewModel;
import com.appicacious.solword.billing.BillingConstants;
import com.appicacious.solword.billing.BillingManager;
import com.appicacious.solword.billing.MyPurchase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.Objects;

public abstract class BillingActivity extends AdActivity implements
        BillingManager.BillingUpdatesListener, AcknowledgePurchaseResponseListener {

    private static final String TAG = BillingActivity.class.getSimpleName();
    private BillingViewModel billingViewModel;
    private BillingManager mBillingManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        billingViewModel = new ViewModelProvider(this).get(BillingViewModel.class);

        billingViewModel.getIsPurchasedLiveData().observe(this, isPurchased -> {
            Log.d(TAG, "onCreate: isPurchased=" + isPurchased);
            if (Boolean.TRUE.equals(isPurchased)) {
                // Destroy ad Sdk.
                destroyAdSdk();
//                // Fragments may use this to destroy ads initialized by them
//                mainViewModel.setAdNetworkInitialized(false);
            } else {
                initAppLovinSdk();
            }
        });

        billingViewModel.getIsAcknowledgedLiveData().observe(this, isAcknowledged -> {
            Log.d(TAG, "onCreate: isAcknowledged=" + isAcknowledged);
            if (isAcknowledged != null && isAcknowledged) {
                Toast.makeText(getApplicationContext(), getString(R.string.label_purchased),
                        Toast.LENGTH_SHORT).show();
            }
        });

        initBillingManager();

    }

    @Override
    public boolean isPurchased() {
        return billingViewModel.isPurchased();
    }

    @Override
    public boolean isPurchased_Billing() {
        return billingViewModel.isFirstQueryResponsePending() || billingViewModel.isPurchased();
    }

    @Override
    public void queryPurchases() {
        Log.d(TAG, "queryPurchases: called");
        initBillingManagerOrExecute(() -> {
            // Added on 28/07/2020 to fix the bug where app won't start when offline.
            /* If sku details are null, it means first we need to query sku details which in
             turn would automatically query purchases as well. */
            if (billingViewModel.getProductDetailsList().isEmpty()) {
                Log.d(TAG, "queryPurchases: empty product details");
                queryProductDetailsAsync();
            } else {
                Log.d(TAG, "queryPurchases: started");
                mBillingManager.queryPurchases();
            }
        });
    }

    public void queryProductDetailsAsync() {
        initBillingManagerOrExecute(() -> mBillingManager.queryProductDetailsAsync());
    }

    private void initBillingManagerOrExecute(Runnable runnable) {
        Log.d(TAG, "initBillingManagerOrExecute: called");
        if (mBillingManager != null) {
            runnable.run();
        } else {
            initBillingManager();
        }
    }

    private void initBillingManager() {
        Log.d(TAG, "initBillingManager: called with billing manager=" + mBillingManager);
        if (mBillingManager == null) mBillingManager = new BillingManager(this);
    }

    @Override
    public void initConsumeFlow() {
        List<Purchase> purchases = billingViewModel.getPurchases();
        Log.d(TAG, "initConsumeFlow: purchases=" + purchases);
        if (!purchases.isEmpty()) {
            MyPurchase myPurchase = getMyPurchase(purchases.get(0));
            Log.d(TAG, "initConsumeFlow: myPurchase=" + myPurchase);
            if (myPurchase != null) {
                if (mBillingManager != null)
                    mBillingManager.initiateConsumeFlow(myPurchase.getPurchaseToken());
            }
        }
    }

    @Override
    public void removeAds() {
        ProductDetails productDetails = getProductDetails();
        Log.d(TAG, "removeAds: productDetails=" + productDetails);
        if (productDetails != null) {
            billingViewModel.setPurchaseInitiated(true);
            if (mBillingManager != null) mBillingManager.initiatePurchaseFlow(productDetails);
        } else {
            Log.d(TAG, "removeAds: no in-app products found!");
        }
    }

    @Override
    @Nullable
    public ProductDetails getProductDetails() {
        List<ProductDetails> detailsList = billingViewModel.getProductDetailsList();
        return detailsList.isEmpty() ? null : detailsList.get(0);
    }

    @Nullable
    public static MyPurchase getMyPurchase(@Nullable Purchase purchase) {
        MyPurchase myPurchase = null;
        if (purchase != null && !TextUtils.isEmpty(purchase.getOriginalJson())) {
            try {
                myPurchase = new Gson().fromJson(purchase.getOriginalJson(), MyPurchase.class);
            } catch (JsonSyntaxException e) {
                Log.d(TAG, "getMyPurchase: error=" + e.getMessage());
            }
        }
        return myPurchase;
    }

    protected void updateUI(boolean isPurchased) {
        Log.d(TAG, "updateUI: called with isPurchased=" + isPurchased);
        if (isPurchased) {
            // Destroy ads of MainActivity.
            destroyAdSdk();
            // This will destroy ads initialized by fragments
            mainViewModel.setAdNetworkInitialized(false);
        }
        else {
//            initAppLovinSdk();
        }
    }



//---------------IMPLEMENTED BILLING METHODS---------------//

    @Override
    public void onBillingClientSetupFinished() {
        Log.d(TAG, "onBillingClientSetupFinished: called");
        // TODO: 28-Jul-20 See why this app active check is necessary here
        if (isAppActive()) queryProductDetailsAsync();
    }

    @Override
    public void onBillingClientSetupFailed(int responseCode) {
        Log.d(TAG, "onBillingClientSetupFailed: called with " +
                BillingConstants.getResponseCodeStrByCode(responseCode));
        // TODO: 12-Mar-20 Can it be OK here in the failed callback???
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            updateUI(false);
        }
    }

    /**
     * The purpose of this method is to find a new purchase, if any, and get it acknowledged.
     * <p>
     * This is called when a new purchase is made
     */
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult,
                                   @NonNull List<Purchase> purchases) {
        Log.d(TAG, "onPurchasesUpdated: called with response code: " +
                BillingConstants.getResponseCodeStrByCode(billingResult.getResponseCode())
                + ", and purchases: " + purchases);

        Purchase newPurchase = null;
        if (!purchases.isEmpty()) {
            for (Purchase purchase : purchases) {
//                Log.d(TAG, "onPurchasesUpdated: analysing purchase=" + purchase);
                MyPurchase myPurchase = getMyPurchase(purchase);
                Purchase currentPurchase = billingViewModel.getCurrentPurchase();
                Log.d(TAG, "onPurchasesUpdated: comparing purchase=" + purchase + "\nwith current=" + currentPurchase);
//                if (purchase != null) {
//                    long currentPurchaseTime = currentPurchase == null ? 0 :
//                            currentPurchase.getPurchaseTime();
//                    if (purchase.getPurchaseTime() > currentPurchaseTime) {
//                        newPurchase = purchase;
//                        break;
//                    }
//                }

                if (purchase != null && /*myPurchase != null &&*/
                        purchase.getPurchaseState() == PURCHASED) {
                    if (!Objects.equals(purchase, billingViewModel.getCurrentPurchase())) {
                        newPurchase = purchase;
                        break;
                    }
                }
            }
            if (newPurchase != null) {
                Log.d(TAG, "onPurchasesUpdated: new purchase found=" + newPurchase);

                if (!newPurchase.isAcknowledged()) {
                    // Unacknowledged purchase will be sent for acknowledgement
                    // This means a new purchase
                    final Purchase newPurchaseFinal = newPurchase;
                    initBillingManagerOrExecute(() -> mBillingManager.acknowledgePurchase(
                            newPurchaseFinal, this));
                } else {
                    // If already acknowledged, complete the acknowledgement procedure right here.
                    billingViewModel.setPurchaseInitiated(false);
                    billingViewModel.setAcknowledged(true);

//                    Toast.makeText(this, getString(R.string.subs_plan_sub_label_2),
//                            Toast.LENGTH_SHORT).show();

                    queryPurchases();
                }
            } else {
                // Ideally, we shouldn't reach here
                Log.d(TAG, "onPurchasesUpdated: null purchase found??");
                queryPurchases();
            }
        } else {
            // Ideally, we shouldn't reach here
            Log.d(TAG, "onPurchasesUpdated: empty purchase list??");
            queryPurchases();
        }
    }

    @Override
    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult,
                                         @NonNull List<Purchase> purchases) {
        Log.d(TAG, "onQueryPurchasesResponse: called with " + purchases);
//        List<Purchase> acknowledgedPurchases = new ArrayList<>();
        Purchase acknowledged = null;
        Purchase unacknowledged = null;

        for (Purchase purchase : purchases) {
            Log.d(TAG, "onQueryPurchasesResponse: analysing purchase=" + purchase +
                    " with state=" + (purchase != null ? String.valueOf(purchase.getPurchaseState()) : "null"));
            MyPurchase myPurchase = getMyPurchase(purchase);
            if (purchase != null && /*myPurchase != null &&*/
                    purchase.getPurchaseState() == PURCHASED) {
                if (purchase.isAcknowledged()) {
                    acknowledged = purchase;
//                    acknowledgedPurchases.add(purchase);
                } else {
                    unacknowledged = purchase;
                }
            }
        }

        if (unacknowledged != null) {
            Log.d(TAG, "onQueryPurchasesResponse: found unacknowledged purchase=" + unacknowledged);
            /* We restart acknowledgement process here only if the purchase is unacknowledged even
            after the purchase initiation process is completed. During the said process,
            acknowledgement originally gets started in onPurchasesUpdated() and ends in
            acknowledgement callback if the result is OK. But in case of non-ok result, we will be
            forwarded to this method. At that time, isPurchaseInitiated() would be true and we won't
            want to restart acknowledgment at that point. Hence, we don't restart, we rather reset
            the flag to mark the purchase process as complete.
            However, next time we end up here and the purchase is still unacknowledged, we restart
            the acknowledgement process.*/
            if (!billingViewModel.isPurchaseInitiated()) {
                final Purchase finalUnacknowledged = unacknowledged;
                initBillingManagerOrExecute(() -> mBillingManager.acknowledgePurchase(
                        finalUnacknowledged, this));
            } else {
                /* As stated above in detail, we need to reset this flag in this method only when we
                initiated a purchase and acknowledgement process ended up in a Non-Ok response. So
                we turn it off here. In case of OK response, it would be reset in acknowledgement
                callback. */
                billingViewModel.setPurchaseInitiated(false);
            }
        }

        // We store it whether it is null or acknowledged.
        Log.d(TAG, "onQueryPurchasesResponse: acknowledged=" + acknowledged);
        billingViewModel.setCurrentPurchase(acknowledged);
        // TODO: 17/07/2022 Remove this code that was required for consumption of purchases
        billingViewModel.setPurchases(purchases);
        updateUI(isPurchased());
    }

    @Override
    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
        int resCode = billingResult.getResponseCode();
        Log.d(TAG, "onAcknowledgePurchaseResponse: resCode= " + resCode + ", response="
                + BillingConstants.getResponseCodeStrByCode(resCode));

        /* In case of any non-ok response, we query purchases only if this callback was in response
        to an initiated purchase, just so that the purchase process is completed. If this callback
        receives non-ok afterwards, we don't query purchase unnecessarily.

        In other words, when we initiate a purchase, we query purchases whether we receive an OK
        or Non-Ok acknowledgement response. */
        if (resCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "onAcknowledgePurchaseResponse: OK");
            billingViewModel.setPurchaseInitiated(false);
            billingViewModel.setAcknowledged(true);
//            resumeRunnable = () -> billingViewModel.setAcknowledged(true);
            queryPurchases();
//            Log.d(TAG, "onAcknowledgePurchaseResponse: query purchase called");

        } else {
            Log.d(TAG, "onAcknowledgePurchaseResponse: not OK");
            if (billingViewModel.isPurchaseInitiated()) {
            /* The flag will be turned off in onQueryPurchasesUpdated() in order to avoid repeated
             calls to BillingManager.acknowledgePurchase(). */
                queryPurchases();
            }
        }
    }

    @Override
    public void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                         @NonNull List<ProductDetails> productDetails) {
        Log.d(TAG, "onProductDetailsResponse: producDetails list=" + productDetails);
        int responseCode = billingResult.getResponseCode();
        String responseCodeStr = BillingConstants.getResponseCodeStrByCode(responseCode);
        Log.d(TAG, "onProductDetailsResponse: called with response code " + responseCodeStr);
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            billingViewModel.setProductDetailsList(productDetails);
            if (!productDetails.isEmpty()) {
                for (ProductDetails details : productDetails) {
                    ProductDetails.OneTimePurchaseOfferDetails oneTimeDetails = details
                            .getOneTimePurchaseOfferDetails();
                    String priceStr = null;
                    String currCode = null;
                    long priceInt = 0;
                    if (oneTimeDetails != null) {
                        priceStr = oneTimeDetails.getFormattedPrice();
                        currCode = oneTimeDetails.getPriceCurrencyCode();
                        priceInt = oneTimeDetails.getPriceAmountMicros();
                    }
                    Log.d(TAG, String.format("onSkuDetailsResponse: title=%s, description=%s, " +
                                    "priceStr=%s, currCode=%s, priceInt=%d", details.getTitle(),
                            details.getDescription(), priceStr, currCode, priceInt));
                }
                queryPurchases();
            } else {
//                updateUI(false);
            }
        } else {
            // Added on 28/07/2020 to fix the bug where app won't start when offline.
            updateUI(false);
        }
    }

    @Override
    public void onPurchasesFailed(@NonNull BillingResult billingResult) {
        Log.d(TAG, "onPurchasesFailed: Response code=" +
                BillingConstants.getResponseCodeStrByCode(billingResult.getResponseCode()));
        billingViewModel.setPurchaseInitiated(false);
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
            Toast.makeText(this, "Payment failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConsumeResponse(int resCode) {
        Log.d(TAG, "onConsumeResponse: response=" + BillingConstants.getResponseCodeStrByCode(
                resCode));
        if (resCode == BillingClient.BillingResponseCode.OK)
            billingViewModel.setCurrentPurchase(null);
    }
}
