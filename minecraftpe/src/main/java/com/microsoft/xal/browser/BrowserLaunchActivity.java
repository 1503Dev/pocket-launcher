package com.microsoft.xal.browser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.browser.customtabs.CustomTabsIntent;
import com.microsoft.xal.logging.XalLogger;
import com.microsoft.xbox.telemetry.helpers.UTCTelemetry;

import java.security.NoSuchAlgorithmException;

public class BrowserLaunchActivity extends Activity {
    private static final String BROWSER_INFO_STATE_KEY = "BROWSER_INFO_STATE";
    private static final String CUSTOM_TABS_IN_PROGRESS_STATE_KEY = "CUSTOM_TABS_IN_PROGRESS_STATE";
    public static final String END_URL = "END_URL";
    public static final String IN_PROC_BROWSER = "IN_PROC_BROWSER";
    public static final String OPERATION_ID = "OPERATION_ID";
    private static final String OPERATION_ID_STATE_KEY = "OPERATION_ID_STATE";
    public static final String REQUEST_HEADER_KEYS = "REQUEST_HEADER_KEYS";
    public static final String REQUEST_HEADER_VALUES = "REQUEST_HEADER_VALUES";
    public static final int RESULT_FAILED = 8052;
    private static final String SHARED_BROWSER_USED_STATE_KEY = "SHARED_BROWSER_USED_STATE";
    public static final String SHOW_TYPE = "SHOW_TYPE";
    public static final String START_URL = "START_URL";
    public static final int WEB_KIT_WEB_VIEW_REQUEST = 8053;

    private final XalLogger xalLogger = new XalLogger("BrowserLaunchActivity");
    private final String TAG = "BrowserLaunchActivity";
    private BrowserLaunchParameters launchParameters = null;
    private long operationId = 0;
    private boolean customTabsInProgress = false;
    private boolean sharedBrowserUsed = false;
    private String browserInfo = null;

    private enum WebResult {
        SUCCESS,
        FAIL,
        CANCEL
    }

    private static native void checkIsLoaded();

    private static native void urlOperationCanceled(long j, boolean z, String str);

    private static native void urlOperationFailed(long j, boolean z, String str);

    private static native void urlOperationSucceeded(long j, String str, boolean z, String str2);

    public enum ShowUrlType {
        Normal,
        CookieRemoval,
        CookieRemovalSkipIfSharedCredentials,
        NonAuthFlow;

        public static ShowUrlType fromInt(int i) {
            if (i == 0) {
                return Normal;
            }
            if (i == 1) {
                return CookieRemoval;
            }
            if (i == 2) {
                return CookieRemovalSkipIfSharedCredentials;
            }
            if (i != 3) {
                return null;
            }
            return NonAuthFlow;
        }

        @Override
        public String toString() {
            int i = BrowserLaunchActivity.Switches.showUrlTypeOrdinalToIndexMap[this.ordinal()];
            return i != 1 ? i != 2 ? i != 3 ? i != 4 ? UTCTelemetry.UNKNOWNPAGE : "NonAuthFlow" : "CookieRemovalSkipIfSharedCredentials" : "CookieRemoval" : "Normal";
        }
    }

    private static class BrowserLaunchParameters {
        public final String endUrl;
        public final String[] requestHeaderKeys;
        public final String[] requestHeaderValues;
        public final ShowUrlType showType;
        public final String startUrl;
        public final boolean useInProcBrowser;

        public static BrowserLaunchParameters fromArgs(Bundle bundle) {
            String startUrl = bundle.getString(START_URL);
            String endUrl = bundle.getString(END_URL);
            String[] requestHeaderKeys = bundle.getStringArray(REQUEST_HEADER_KEYS);
            String[] requestHeaderValues = bundle.getStringArray(REQUEST_HEADER_VALUES);
            ShowUrlType showUrlType = (ShowUrlType) bundle.get(SHOW_TYPE);
            boolean useInProcBrowser = bundle.getBoolean(IN_PROC_BROWSER);
            if (startUrl == null || endUrl == null || requestHeaderKeys == null || requestHeaderValues == null || requestHeaderKeys.length != requestHeaderValues.length) {
                return null;
            }
            return new BrowserLaunchParameters(startUrl, endUrl, requestHeaderKeys, requestHeaderValues, showUrlType, useInProcBrowser);
        }

        private BrowserLaunchParameters(String startUrl, String endUrl, String[] requestHeaderKeys, String[] requestHeaderValues, ShowUrlType showType, boolean useInProcBrowser) {
            XalLogger paramLogger = new XalLogger("BrowserLaunchActivity.BrowserLaunchParameters");
            try {
                this.startUrl = startUrl;
                this.endUrl = endUrl;
                this.requestHeaderKeys = requestHeaderKeys;
                this.requestHeaderValues = requestHeaderValues;
                this.showType = showType;
                if (showType == ShowUrlType.NonAuthFlow) {
                    paramLogger.Important("BrowserLaunchParameters() Forcing inProc browser because flow is marked non-auth.");
                    this.useInProcBrowser = true;
                } else {
                    if (requestHeaderKeys.length > 0) {
                        paramLogger.Important("BrowserLaunchParameters() Forcing inProc browser because request headers were found.");
                    }
                    this.useInProcBrowser = useInProcBrowser;
                }
                paramLogger.close();
            } catch (Throwable th) {
                try {
                    throw th;
                } catch (Throwable th2) {
                    if (th != null) {
                        try {
                            paramLogger.close();
                        } catch (Throwable th3) {
                            th.addSuppressed(th3);
                        }
                    } else {
                        paramLogger.close();
                    }
                    throw th2;
                }
            }
        }
    }

    public static void showUrl(long operationId, Context context, String startUrl, String endUrl, int showType, String[] requestHeaderKeys, String[] requestHeaderValues, boolean useInProcBrowser) {
        XalLogger showUrlLogger = new XalLogger("BrowserLaunchActivity.showUrl()");
        try {
            showUrlLogger.Important("JNI call received.");
            if (!startUrl.isEmpty() && !endUrl.isEmpty()) {
                ShowUrlType showUrlTypeFromInt = ShowUrlType.fromInt(showType);
                if (showUrlTypeFromInt == null) {
                    showUrlLogger.Error("Unrecognized show type received: " + showType);
                    urlOperationFailed(operationId, false, null);
                    showUrlLogger.close();
                    return;
                }
                if (requestHeaderKeys.length != requestHeaderValues.length) {
                    showUrlLogger.Error("requestHeaderKeys different length than requestHeaderValues.");
                    urlOperationFailed(operationId, false, null);
                    showUrlLogger.close();
                    return;
                }
                Intent intent = new Intent(context, BrowserLaunchActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong(OPERATION_ID, operationId);
                bundle.putString(START_URL, startUrl);
                bundle.putString(END_URL, endUrl);
                bundle.putSerializable(SHOW_TYPE, showUrlTypeFromInt);
                bundle.putStringArray(REQUEST_HEADER_KEYS, requestHeaderKeys);
                bundle.putStringArray(REQUEST_HEADER_VALUES, requestHeaderValues);
                bundle.putBoolean(IN_PROC_BROWSER, useInProcBrowser);
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                showUrlLogger.close();
                return;
            }
            showUrlLogger.Error("Received invalid start or end URL.");
            urlOperationFailed(operationId, false, null);
            showUrlLogger.close();
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (th != null) {
                    try {
                        showUrlLogger.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                } else {
                    showUrlLogger.close();
                }
                throw th2;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        xalLogger.Important("onCreate()");
        Bundle extras = getIntent().getExtras();
        if (!checkNativeCodeLoaded()) {
            xalLogger.Warning("onCreate() Called while XAL not loaded. Dropping flow and starting app's main activity.");
            xalLogger.Flush();
            startActivity(getApplicationContext().getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName()));
            finish();
            return;
        }
        if (savedInstanceState != null) {
            xalLogger.Important("onCreate() Recreating with saved state.");
            operationId = savedInstanceState.getLong(OPERATION_ID_STATE_KEY);
            customTabsInProgress = savedInstanceState.getBoolean(CUSTOM_TABS_IN_PROGRESS_STATE_KEY);
            sharedBrowserUsed = savedInstanceState.getBoolean(SHARED_BROWSER_USED_STATE_KEY);
            browserInfo = savedInstanceState.getString(BROWSER_INFO_STATE_KEY);
            return;
        }
        if (extras != null) {
            xalLogger.Important("onCreate() Created with intent args. Starting auth session.");
            operationId = extras.getLong(OPERATION_ID, 0L);
            BrowserLaunchParameters browserLaunchParametersFromArgs = BrowserLaunchParameters.fromArgs(extras);
            launchParameters = browserLaunchParametersFromArgs;
            if (browserLaunchParametersFromArgs == null || operationId == 0) {
                xalLogger.Error("onCreate() Found invalid args, failing operation.");
                finishOperation(WebResult.FAIL, null);
                return;
            }
            return;
        }
        if (getIntent().getData() != null) {
            xalLogger.Error("onCreate() Unexpectedly created with intent data. Finishing with failure.");
            setResult(RESULT_FAILED);
            finishOperation(WebResult.FAIL, null);
        } else {
            xalLogger.Error("onCreate() Unexpectedly created, reason unknown. Finishing with failure.");
            setResult(RESULT_FAILED);
            finishOperation(WebResult.FAIL, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        xalLogger.Important("onResume()");
        boolean customTabsInProgressLocal = customTabsInProgress;
        if (!customTabsInProgressLocal && launchParameters != null) {
            xalLogger.Important("onResume() Resumed with launch parameters. Starting auth session.");
            BrowserLaunchParameters browserLaunchParameters = launchParameters;
            launchParameters = null;
            try {
                startAuthSession(browserLaunchParameters);
            } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if (customTabsInProgressLocal) {
            customTabsInProgress = false;
            Uri data = getIntent().getData();
            if (data != null) {
                xalLogger.Important("onResume() Resumed with intent data. Finishing operation successfully.");
                finishOperation(WebResult.SUCCESS, data.toString());
                return;
            } else {
                xalLogger.Warning("onResume() Resumed with no intent data. Canceling operation.");
                finishOperation(WebResult.CANCEL, null);
                return;
            }
        }
        xalLogger.Warning("onResume() No action to take. This shouldn't happen.");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        xalLogger.Important("onSaveInstanceState() Preserving state.");
        outState.putLong(OPERATION_ID_STATE_KEY, operationId);
        outState.putBoolean(CUSTOM_TABS_IN_PROGRESS_STATE_KEY, customTabsInProgress);
        outState.putBoolean(SHARED_BROWSER_USED_STATE_KEY, sharedBrowserUsed);
        outState.putString(BROWSER_INFO_STATE_KEY, browserInfo);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        xalLogger.Important("onNewIntent() Received intent.");
        setIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        xalLogger.Important("onActivityResult() Result received.");
        if (requestCode == WEB_KIT_WEB_VIEW_REQUEST) {
            if (resultCode == -1) {
                String finalUrl = data.getExtras().getString(WebKitWebViewController.RESPONSE_KEY, "");
                if (finalUrl.isEmpty()) {
                    xalLogger.Error("onActivityResult() Invalid final URL received from web view.");
                } else {
                    finishOperation(WebResult.SUCCESS, finalUrl);
                    return;
                }
            } else if (resultCode == 0) {
                finishOperation(WebResult.CANCEL, null);
                return;
            } else if (resultCode != 8054) {
                xalLogger.Warning("onActivityResult() Unrecognized result code received from web view:" + resultCode);
            }
            finishOperation(WebResult.FAIL, null);
            return;
        }
        xalLogger.Warning("onActivityResult() Result received from unrecognized request.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        xalLogger.Important("onDestroy()");
        if (!isFinishing() || operationId == 0) {
            return;
        }
        xalLogger.Warning("onDestroy() Activity is finishing with operation in progress, canceling.");
        finishOperation(WebResult.CANCEL, null);
    }

    private void startAuthSession(BrowserLaunchParameters browserLaunchParameters) throws PackageManager.NameNotFoundException, NoSuchAlgorithmException {
        BrowserSelectionResult browserSelectionResult = BrowserSelector.selectBrowser(getApplicationContext(), browserLaunchParameters.useInProcBrowser);
        browserInfo = browserSelectionResult.toString();
        xalLogger.Important("startAuthSession() Set browser info: " + browserInfo);
        xalLogger.Important("startAuthSession() Starting auth session for ShowUrlType: " + browserLaunchParameters.showType.toString());
        String packageName = browserSelectionResult.packageName();
        if (packageName == null) {
            xalLogger.Important("startAuthSession() BrowserSelector returned null package name. Choosing WebKit strategy.");
            startWebView(browserLaunchParameters.startUrl, browserLaunchParameters.endUrl, browserLaunchParameters.showType, browserLaunchParameters.requestHeaderKeys, browserLaunchParameters.requestHeaderValues);
        } else {
            xalLogger.Important("startAuthSession() BrowserSelector returned non-null package name. Choosing CustomTabs strategy.");
            startCustomTabsInBrowser(packageName, browserLaunchParameters.startUrl, browserLaunchParameters.endUrl, browserLaunchParameters.showType);
        }
    }

    private void startCustomTabsInBrowser(String packageName, String startUrl, String endUrl, ShowUrlType showUrlType) {
        if (showUrlType == ShowUrlType.CookieRemovalSkipIfSharedCredentials) {
            finishOperation(WebResult.SUCCESS, endUrl);
            return;
        }
        customTabsInProgress = true;
        sharedBrowserUsed = true;
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setData(Uri.parse(startUrl));
        customTabsIntent.intent.setPackage(packageName);
        startActivity(customTabsIntent.intent);
    }

    private void startWebView(String startUrl, String endUrl, ShowUrlType showUrlType, String[] requestHeaderKeys, String[] requestHeaderValues) {
        sharedBrowserUsed = false;
        Intent intent = new Intent(getApplicationContext(), WebKitWebViewController.class);
        Bundle bundle = new Bundle();
        bundle.putString(START_URL, startUrl);
        bundle.putString(END_URL, endUrl);
        bundle.putSerializable(SHOW_TYPE, showUrlType);
        bundle.putStringArray(REQUEST_HEADER_KEYS, requestHeaderKeys);
        bundle.putStringArray(REQUEST_HEADER_VALUES, requestHeaderValues);
        intent.putExtras(bundle);
        startActivityForResult(intent, WEB_KIT_WEB_VIEW_REQUEST);
    }

    private void finishOperation(WebResult webResult, String responseUrl) {
        long currentOperationId = operationId;
        operationId = 0L;
        finish();
        if (currentOperationId == 0) {
            xalLogger.Error("finishOperation() No operation ID to complete.");
            xalLogger.Flush();
            return;
        }
        xalLogger.Flush();
        int switchValue = BrowserLaunchActivity.Switches.webResultOrdinalToIndexMap[webResult.ordinal()];
        if (switchValue == 1) {
            urlOperationSucceeded(currentOperationId, responseUrl, sharedBrowserUsed, browserInfo);
        } else if (switchValue == 2) {
            urlOperationCanceled(currentOperationId, sharedBrowserUsed, browserInfo);
        } else {
            if (switchValue != 3) {
                return;
            }
            urlOperationFailed(currentOperationId, sharedBrowserUsed, browserInfo);
        }
    }

    static class Switches {
        static final int[] showUrlTypeOrdinalToIndexMap;
        static final int[] webResultOrdinalToIndexMap;

        static {
            int[] webResultSwitch = new int[WebResult.values().length];
            webResultOrdinalToIndexMap = webResultSwitch;
            try {
                webResultSwitch[WebResult.SUCCESS.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                webResultOrdinalToIndexMap[WebResult.CANCEL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                webResultOrdinalToIndexMap[WebResult.FAIL.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            int[] showTypeSwitch = new int[ShowUrlType.values().length];
            showUrlTypeOrdinalToIndexMap = showTypeSwitch;
            try {
                showTypeSwitch[ShowUrlType.Normal.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                showUrlTypeOrdinalToIndexMap[ShowUrlType.CookieRemoval.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                showUrlTypeOrdinalToIndexMap[ShowUrlType.CookieRemovalSkipIfSharedCredentials.ordinal()] = 3;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                showUrlTypeOrdinalToIndexMap[ShowUrlType.NonAuthFlow.ordinal()] = 4;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }

    private boolean checkNativeCodeLoaded() {
        try {
            checkIsLoaded();
            return true;
        } catch (UnsatisfiedLinkError unused) {
            xalLogger.Error("checkNativeCodeLoaded() Caught UnsatisfiedLinkError, native code not loaded");
            return false;
        }
    }
}