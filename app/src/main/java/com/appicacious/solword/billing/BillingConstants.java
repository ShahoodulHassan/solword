package com.appicacious.solword.billing;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient.BillingResponseCode;

import java.util.HashMap;
import java.util.Map;

public class BillingConstants {




    /* BASE_64_ENCODED_PUBLIC_KEY should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program, construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    public static final String BASE_64_ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmVMnedZXccOeYdxJ1YtxKTWqc/CJWoBNBjFg6bwqs8hDVMe/gIRKCmOw3NJ8ywKgW7qSEcZJezwd9YVRU6R3MYEvxU5I3sD16ufLR7ESa6OA356hstJPLZOADuHN4xNB/bdD5iwELYb8Rc8c/GzgGEOn/cia67qUUNeQawUQnIb5Km25bzp6wf38dXA3b7CWs6O6nmtS76i15DMaqGeuMcTyCInBpCcuPU7QcbWHttG12U3gTUKpufOIUAkXmcuIl5psAS3Qh6m9ziqBFZJ8j8kddMD/QS3ncKyKhqYdwP3Cja8P8omuhX+vdwfM49wldl7w6UTUMrgu/SK9qZrLVwIDAQAB";


    public static final String PID_PREMIUM = "1.premium";

    private static final HashMap<String, Integer> RESPONSE_CODES;

    static {
        RESPONSE_CODES = new HashMap<>();
        RESPONSE_CODES.put("SERVICE_TIMEOUT", BillingResponseCode.SERVICE_TIMEOUT);
        RESPONSE_CODES.put("FEATURE_NOT_SUPPORTED", BillingResponseCode.FEATURE_NOT_SUPPORTED);
        RESPONSE_CODES.put("SERVICE_DISCONNECTED", BillingResponseCode.SERVICE_DISCONNECTED);
        RESPONSE_CODES.put("OK", BillingResponseCode.OK);
        RESPONSE_CODES.put("USER_CANCELED", BillingResponseCode.USER_CANCELED);
        RESPONSE_CODES.put("SERVICE_UNAVAILABLE", BillingResponseCode.SERVICE_UNAVAILABLE);
        RESPONSE_CODES.put("BILLING_UNAVAILABLE", BillingResponseCode.BILLING_UNAVAILABLE);
        RESPONSE_CODES.put("ITEM_UNAVAILABLE", BillingResponseCode.ITEM_UNAVAILABLE);
        RESPONSE_CODES.put("DEVELOPER_ERROR", BillingResponseCode.DEVELOPER_ERROR);
        RESPONSE_CODES.put("ERROR", BillingResponseCode.ERROR);
        RESPONSE_CODES.put("ITEM_ALREADY_OWNED", BillingResponseCode.ITEM_ALREADY_OWNED);
        RESPONSE_CODES.put("ITEM_NOT_OWNED", BillingResponseCode.ITEM_NOT_OWNED);
    }

    @Nullable
    public static String getResponseCodeStrByCode(int code) {
        for (Map.Entry<String, Integer> entry : RESPONSE_CODES.entrySet()) {
            if (code == entry.getValue()) return entry.getKey();
        }
        // If we don't find any value in map, we return the code itself.
        return "" + code;
    }

}
