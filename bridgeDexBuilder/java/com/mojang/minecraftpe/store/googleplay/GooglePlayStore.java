package com.mojang.minecraftpe.store.googleplay;

import com.mojang.minecraftpe.MainActivity;
import com.mojang.minecraftpe.store.ExtraLicenseResponseData;
import com.mojang.minecraftpe.store.Store;
import com.mojang.minecraftpe.store.StoreListener;

public class GooglePlayStore implements Store {
    MainActivity mActivity;
    StoreListener mListener;

    public void acknowledgePurchase(String str, String str2) {
    }

    public void destructor() {
    }

    public String getProductSkuPrefix() {
        return "";
    }

    public String getRealmsSkuPrefix() {
        return "";
    }

    public String getStoreId() {
        return "android.googleplay";
    }

    public boolean hasVerifiedLicense() {
        return true;
    }

    public void purchase(String str, boolean z, String str2) {
    }

    public void purchaseGame() {
    }

    public void queryProducts(String[] strArr) {
    }

    public void queryPurchases() {
    }

    public boolean receivedLicenseResponse() {
        return true;
    }

    public GooglePlayStore(MainActivity mainActivity, String str, StoreListener storeListener) {
        this.mActivity = mainActivity;
        this.mListener = storeListener;
        storeListener.onStoreInitialized(true);
    }

    public ExtraLicenseResponseData getExtraLicenseData() {
        return new ExtraLicenseResponseData(0L, 0L, 0L);
    }
}