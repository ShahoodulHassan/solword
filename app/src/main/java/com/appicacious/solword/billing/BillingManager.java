package com.appicacious.solword.billing;


import static com.appicacious.solword.billing.BillingConstants.PID_PREMIUM;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
//import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BillingManager implements PurchasesUpdatedListener, ConsumeResponseListener {

    private static final String TAG = "BillingManager";

    private final AppCompatActivity mActivity;
    private final BillingClient mBillingClient;
    private final BillingUpdatesListener mBillingUpdatesListener;

    public final List<Purchase> mPurchases = new ArrayList<>();
    public boolean mIsServiceConnected, mArePurchasesUpdated;
    private int mBillingResponseCode = BillingResponseCode.SERVICE_DISCONNECTED;


    public BillingManager(AppCompatActivity activity) {
        Log.d(TAG, "Creating Billing client.");
        mActivity = activity;
        if (activity instanceof BillingUpdatesListener) {
            mBillingUpdatesListener = (BillingUpdatesListener) activity;
        } else {
            throw new RuntimeException("Activity doesn't host Billing updates listener");
        }
        mBillingClient = BillingClient.newBuilder(mActivity)
                .enablePendingPurchases()
                .setListener(this)
                .build();

        Log.d(TAG, "Starting setup.");

        // Start setup. This is asynchronous and the specified listener will be called
        // once setup completes.
        // It also starts to report all the new purchases through onPurchasesUpdated() callback.
        executeServiceRequest(new Runnable() {
            @Override
            public void run() {
                // Notifying the listener that billing client is ready
                mBillingUpdatesListener.onBillingClientSetupFinished();
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
//                queryPurchases();
//                queryPurchasesHistoryAsync();
            }
        });
    }

    public void queryProductDetailsAsync() {
        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable queryRequest = () -> {
            // Create product params
            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(Collections.singletonList(
                                            QueryProductDetailsParams.Product.newBuilder()
                                                    .setProductId(PID_PREMIUM)
                                                    .setProductType(BillingClient.ProductType.INAPP)
                                                    .build())
//                                    ImmutableList.of(
//                                            QueryProductDetailsParams.Product.newBuilder()
//                                                    .setProductId(PID_PREMIUM)
//                                                    .setProductType(BillingClient.ProductType.INAPP)
//                                                    .build())
                            ).build();

            // Query the product details async
            mBillingClient.queryProductDetailsAsync(queryProductDetailsParams,
                    (billingResult, productDetails) -> {
                        Log.d(TAG, "onProductDetailsResponse: billingResult=" + billingResult);
                        Log.d(TAG, "onProductDetailsResponse: productDetails= " + productDetails);
                        mBillingUpdatesListener.onProductDetailsResponse(billingResult, productDetails);
                    });
        };

        executeServiceRequest(queryRequest);
    }

    /**
     * Query purchases across various use cases and deliver the result in a formalized way through
     * a listener
     */
    public void queryPurchases() {
        Log.d(TAG, "queryPurchases: called");
        Runnable queryToExecute = () -> {
            long time = System.currentTimeMillis();
            // Create product params
            QueryPurchasesParams queryPurchasesParams =
                    QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build();
            mBillingClient.queryPurchasesAsync(queryPurchasesParams, (billingResult, list) -> {
                Log.d(TAG, "onQueryPurchasesResponse: billingResult=" + billingResult);
                Log.d(TAG, "onQueryPurchasesResponse: purchases=" + list);
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    onQueryPurchasesResponse(billingResult, list);
                } else {
                    Log.w(TAG, "onQueryPurchasesResponse: error response code: "
                            + billingResult.getResponseCode());
                }
            });
        };

        executeServiceRequest(queryToExecute);
    }

    // TODO: 04/07/2022 Arrange to combine onQueryPurchasesResponse and onQueryPurchasesUpdated

    /**
     * Handle a result from querying of purchases and report an updated list to the listener
     */
    private void onQueryPurchasesResponse(@NonNull BillingResult billingResult,
                                          @NonNull List<Purchase> purchases) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (mBillingClient == null || billingResult.getResponseCode() != BillingResponseCode.OK) {
            Log.w(TAG, "Billing client was null or result code (" + billingResult.getResponseCode()
                    + ") was bad - quitting");
            return;
        }

        Log.d(TAG, "Query inventory was successful.");

        // Update the UI and purchases inventory with new list of purchases
        mPurchases.clear();
        onQueryPurchasesUpdated(billingResult, purchases);
    }

    /**
     * Handle a result from querying of purchases and report an updated list to the listener
     */
    private void onQueryPurchasesUpdated(@NonNull BillingResult billingResult,
                                         @NonNull List<Purchase> purchases) {
        Log.d(TAG, "onQueryPurchasesUpdated: purchases=" + purchases);
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingResponseCode.OK) {
            mArePurchasesUpdated = true;
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
            mBillingUpdatesListener.onQueryPurchasesResponse(billingResult, mPurchases);
        } else if (responseCode == BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "onQueryPurchasesUpdated() - user cancelled the purchase flow - skipping");
        } else {
            Log.d(TAG, "onQueryPurchasesUpdated() got unknown resultCode: " +
                    BillingConstants.getResponseCodeStrByCode(responseCode));
        }
    }

    /**
     * Starts a purchase flow
     *
     * @param productDetails contains details of the product intended to be purchased
     */
    public void initiatePurchaseFlow(final ProductDetails productDetails) {
        Log.d(TAG, "initiatePurchaseFlow: called for " + productDetails.getTitle());
        Runnable purchaseFlowRequest = () -> {
            Log.d(TAG, "Launching in-app purchase flow");
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                            Collections.singletonList(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .build())
//                            ImmutableList.of(
//                            BillingFlowParams.ProductDetailsParams.newBuilder()
//                                    .setProductDetails(productDetails)
//                                    .build())
                    ).build();
            mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
        };
        executeServiceRequest(purchaseFlowRequest);
    }


    public void initiateConsumeFlow(String purchaseToken) {
        Runnable consumeFlowRequest = () -> {
            Log.d(TAG, "consumePurchases: called");
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build();
            mBillingClient.consumeAsync(consumeParams, this);
        };
        executeServiceRequest(consumeFlowRequest);
    }


    private void executeServiceRequest(Runnable runnable) {
        Log.d(TAG, "executeServiceRequest: isConnected=" + mIsServiceConnected);
        if (mIsServiceConnected) {
            runnable.run();
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable);
        }
    }


    private void startServiceConnection(final Runnable executeOnSuccess) {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.d(TAG, "Setup finished. Response code: " +
                        BillingConstants.getResponseCodeStrByCode(billingResult.getResponseCode()));
                mBillingResponseCode = billingResult.getResponseCode();
                mIsServiceConnected = mBillingResponseCode == BillingResponseCode.OK;
                if (mIsServiceConnected && executeOnSuccess != null) {
                    executeOnSuccess.run();
                } else {
                    // That's a hacky way of doing it.
                    mBillingUpdatesListener.onBillingClientSetupFailed(mBillingResponseCode);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "Setup finished with disconnection.");
                mBillingResponseCode = BillingResponseCode.SERVICE_DISCONNECTED;
                mIsServiceConnected = false;
//                mBillingUpdatesListener.onBillingClientSetupFailed(mBillingClientResponseCode);
//                executeOnSuccess.run();
            }
        });
    }

    @Override
    public void onConsumeResponse(@NonNull BillingResult billingResult,
                                  @NonNull String purchaseToken) {
        int responseCode = billingResult.getResponseCode();
        String response = BillingConstants.getResponseCodeStrByCode(responseCode);
        Log.d(TAG, String.format("onConsumeResponse: response=%s, purToken=%s", response,
                purchaseToken));
        mBillingUpdatesListener.onConsumeResponse(responseCode);
    }

    /**
     * Handle a callback that purchases were updated from the Billing library
     */
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult,
                                   @Nullable List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingResponseCode.OK) {
            mArePurchasesUpdated = purchases != null;
            if (mArePurchasesUpdated) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
                mBillingUpdatesListener.onPurchasesUpdated(billingResult, mPurchases);
            } else {
                Log.d(TAG, "onPurchasesUpdated: null purchases");
                mBillingUpdatesListener.onPurchasesFailed(billingResult);
            }

        } else if (responseCode == BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
            mBillingUpdatesListener.onPurchasesFailed(billingResult);
        } else {
            Log.d(TAG, "onPurchasesUpdated() got resultCode: " +
                    BillingConstants.getResponseCodeStrByCode(responseCode));
            mBillingUpdatesListener.onPurchasesFailed(billingResult);
        }
    }

    /**
     * Handles the purchase
     * <p>Note: Notice that for each purchase, we check if signature is valid on the client.
     * It's recommended to move this check into your backend.
     * See {@link Security#verifyPurchase(String, String, String)}
     * </p>
     *
     * @param purchase Purchase to be handled
     */
    private void handlePurchase(Purchase purchase) {
        if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
            Log.i(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
            return;
        }

        Log.i(TAG, "Got a verified purchase: " + purchase);

        mPurchases.add(purchase);
    }

    public void acknowledgePurchase(Purchase purchase, AcknowledgePurchaseResponseListener listener) {
        Log.d(TAG, "acknowledgePurchase: started");
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, listener);
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
//        if (BASE_64_ENCODED_PUBLIC_KEY.contains("CONSTRUCT_YOUR")) {
//            throw new RuntimeException("Please update your app's public key at: "
//                    + "BASE_64_ENCODED_PUBLIC_KEY");
//        }
        try {
            return Security.verifyPurchase(BillingConstants.BASE_64_ENCODED_PUBLIC_KEY, signedData,
                    signature);
        } catch (IOException e) {
            Log.e(TAG, "Got an exception trying to validate a purchase: " + e);
            return false;
        }
    }

    /**
     * Listener to the updates that happen when purchases list was updated or consumption of the
     * item was finished
     */
    public interface BillingUpdatesListener {
        void onBillingClientSetupFinished();

        void onBillingClientSetupFailed(int responseCode);

        void onPurchasesUpdated(@NonNull BillingResult billingResult,
                                @NonNull List<Purchase> purchases);

        void onQueryPurchasesResponse(@NonNull BillingResult billingResult,
                                      @NonNull List<Purchase> purchases);

        void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                      @NonNull List<ProductDetails> productDetails);

        void onPurchasesFailed(@NonNull BillingResult billingResult);

        void onConsumeResponse(int resCode);

    }
}
