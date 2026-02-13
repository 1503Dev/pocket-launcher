package com.microsoft.xal.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.microsoft.xal.browser.BrowserLaunchActivity;
import com.microsoft.xal.logging.XalLogger;
import java.util.HashMap;

import dev1503.pocketlauncher.Log;

/* loaded from: classes.dex */
public class WebKitWebViewController extends Activity {
    public static final String END_URL = "END_URL";
    public static final String REQUEST_HEADER_KEYS = "REQUEST_HEADER_KEYS";
    public static final String REQUEST_HEADER_VALUES = "REQUEST_HEADER_VALUES";
    public static final String RESPONSE_KEY = "RESPONSE";
    public static final int RESULT_FAILED = 8054;
    public static final String SHOW_TYPE = "SHOW_TYPE";
    public static final String START_URL = "START_URL";
    private final XalLogger xalLogger = new XalLogger("WebKitWebViewController");
    private WebView webView;
    private final String TAG = "WebKitWebViewController";

    @SuppressLint("SetJavaScriptEnabled")
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            this.xalLogger.Error("onCreate() Called with no extras.");
            this.xalLogger.Flush();
            setResult(RESULT_FAILED);
            finish();
            return;
        }
        String START_URL = extras.getString("START_URL", "");
        final String END_URL = extras.getString("END_URL", "");
        if (START_URL.isEmpty() || END_URL.isEmpty()) {
            this.xalLogger.Error("onCreate() Received invalid start or end URL.");
            this.xalLogger.Flush();
            setResult(RESULT_FAILED);
            finish();
            return;
        }
        Log.d(TAG, "onCreate() START_URL: " + START_URL  + "\nEND_URL: " + END_URL);
        String[] stringArray = extras.getStringArray("REQUEST_HEADER_KEYS");
        String[] stringArray2 = extras.getStringArray("REQUEST_HEADER_VALUES");
        if (stringArray.length != stringArray2.length) {
            this.xalLogger.Error("onCreate() Received request header and key arrays of different lengths.");
            this.xalLogger.Flush();
            setResult(RESULT_FAILED);
            finish();
            return;
        }
        BrowserLaunchActivity.ShowUrlType showUrlType = (BrowserLaunchActivity.ShowUrlType) extras.get("SHOW_TYPE");
        if (showUrlType == BrowserLaunchActivity.ShowUrlType.CookieRemoval || showUrlType == BrowserLaunchActivity.ShowUrlType.CookieRemovalSkipIfSharedCredentials) {
            this.xalLogger.Important("onCreate() WebView invoked for cookie removal. Deleting cookies and finishing.");
            if (stringArray.length > 0) {
                this.xalLogger.Warning("onCreate() WebView invoked for cookie removal with requestHeaders.");
            }
            deleteCookies("login.live.com", true);
            deleteCookies("account.live.com", true);
            deleteCookies("live.com", true);
            deleteCookies("xboxlive.com", true);
            deleteCookies("sisu.xboxlive.com", true);
            this.xalLogger.Flush();
            Intent intent = new Intent();
            intent.putExtra(RESPONSE_KEY, END_URL);
            setResult(-1, intent);
            finish();
            return;
        }
        HashMap<String, String> map = new HashMap<>(stringArray.length);
        for (int i = 0; i < stringArray.length; i++) {
            if (stringArray[i] == null || stringArray[i].isEmpty() || stringArray2[i] == null || stringArray2[i].isEmpty()) {
                this.xalLogger.Error("onCreate() Received null or empty request field.");
                this.xalLogger.Flush();
                setResult(RESULT_FAILED);
                finish();
                return;
            }
            map.put(stringArray[i], stringArray2[i]);
        }
        WebView webView = new WebView(this);
        this.webView = webView;
        setContentView(webView);
        this.webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            this.webView.getSettings().setMixedContentMode(2);
        }
        this.webView.setWebChromeClient(new WebChromeClient() { // from class: com.microsoft.xal.browser.WebKitWebViewController.1
            @Override // android.webkit.WebChromeClient
            public void onProgressChanged(WebView webView2, int i2) {
                WebKitWebViewController.this.setProgress(i2 * 100);
            }
        });
        this.webView.setWebViewClient(new WebViewClient() { // from class: com.microsoft.xal.browser.WebKitWebViewController.2
            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView2, String str) {
                super.onPageFinished(webView2, str);
                webView2.requestFocus(130);
                webView2.sendAccessibilityEvent(8);
                webView2.evaluateJavascript("if (typeof window.__xal__performAccessibilityFocus === \"function\") { window.__xal__performAccessibilityFocus(); }", null);
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, String str) {
                Log.d(TAG, "shouldOverrideUrlLoading(" + str + ")");
                if (!str.startsWith(END_URL)) {
                    return false;
                }
                WebKitWebViewController.this.xalLogger.Important("WebKitWebViewController found end URL. Ending UI flow.");
                WebKitWebViewController.this.xalLogger.Flush();
                Intent intent2 = new Intent();
                intent2.putExtra(WebKitWebViewController.RESPONSE_KEY, str);
                WebKitWebViewController.this.setResult(-1, intent2);
                Log.i(TAG, "Login result: " + str);
                WebKitWebViewController.this.finish();
                return true;
            }
        });
        this.webView.loadUrl(START_URL, map);
    }

    private void deleteCookies(String str, boolean z) {
        CookieManager cookieManager = CookieManager.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append(z ? "https://" : "http://");
        sb.append(str);
        String string = sb.toString();
        String cookie = cookieManager.getCookie(string);
        boolean z2 = false;
        if (cookie != null) {
            String[] strArrSplit = cookie.split(";");
            for (String str2 : strArrSplit) {
                String strTrim = str2.split("=")[0].trim();
                String str3 = strTrim + "=;";
                if (strTrim.startsWith("__Secure-")) {
                    str3 = str3 + "Secure;Domain=" + str + ";Path=/";
                }
                cookieManager.setCookie(string, strTrim.startsWith("__Host-") ? str3 + "Secure;Path=/" : str3 + "Domain=" + str + ";Path=/");
            }
            if (strArrSplit.length > 0) {
                z2 = true;
            }
        }
        if (z2) {
            this.xalLogger.Information("deleteCookies() Deleted cookies for " + str);
        } else {
            this.xalLogger.Information("deleteCookies() Found no cookies for " + str);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            cookieManager.flush();
        }
    }
}