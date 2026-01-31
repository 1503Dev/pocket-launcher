package com.mojang.minecraftpe.store.amazonappstore;

import android.content.Context;
import com.mojang.minecraftpe.store.ExtraLicenseResponseData;
import com.mojang.minecraftpe.store.Store;
import com.mojang.minecraftpe.store.StoreListener;

/* loaded from: C:\Users\TheChuan1503\Downloads\launcher.dex */
public class AmazonAppStore implements Store {
    private boolean mForFireTV;
    StoreListener mListener;

    public void acknowledgePurchase(String str, String str2) {
    }

    public void destructor() {
    }

    public String getStoreId() {
        return "android.amazonappstore";
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

    public AmazonAppStore(Context context, StoreListener storeListener) {
        this.mListener = storeListener;
    }

    public AmazonAppStore(Context context, StoreListener storeListener, boolean z) {
        this.mListener = storeListener;
        this.mForFireTV = z;
    }

    public String getProductSkuPrefix() {
        return this.mForFireTV ? "firetv." : "";
    }

    public String getRealmsSkuPrefix() {
        return this.mForFireTV ? "firetv." : "";
    }

    public ExtraLicenseResponseData getExtraLicenseData() {
        return new ExtraLicenseResponseData(0L, 0L, 0L);
    }
}