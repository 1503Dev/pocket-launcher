package dev1503.pocketlauncher;

import android.os.Handler;
import android.os.Looper;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static OkHttpClient client;

    /**
     * HTTP 请求回调接口
     */
    public interface HttpCallback {
        /**
         * 请求成功回调
         * @param responseCode 响应码
         * @param responseBody 响应体
         */
        void onSuccess(int responseCode, String responseBody);

        /**
         * 请求失败回调
         * @param error 错误信息
         */
        void onError(String error);
    }

    /**
     * 获取 OkHttpClient 实例
     */
    private static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    /**
     * 异步 GET 请求
     * @param url 请求URL
     * @param callback 回调接口
     * @param headers 请求头（可以为null）
     */
    public static void get(String url, HttpCallback callback, Map<String, String> headers) {
        if (url == null || url.isEmpty()) {
            callback.onError("URL不能为空");
            return;
        }

        // 构建请求
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();

        // 发起异步请求
        getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    String body = response.body() != null ? response.body().string() : "";
                    int code = response.code();

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            callback.onSuccess(code, body);
                        } else {
                            callback.onError("HTTP " + code + ": " + body);
                        }
                    });

                } catch (IOException e) {
                    runOnUiThread(() -> callback.onError(e.getMessage()));
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * 在 UI 线程执行
     */
    private static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * 简化版本 - 不带请求头
     */
    public static void get(String url, HttpCallback callback) {
        get(url, callback, null);
    }

    /**
     * 异步 GET 请求（带超时设置）
     */
    public static void get(String url, HttpCallback callback, Map<String, String> headers,
                           int connectTimeout, int readTimeout) {
        OkHttpClient customClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(connectTimeout, TimeUnit.SECONDS)
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();

        customClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    String body = response.body() != null ? response.body().string() : "";
                    int code = response.code();

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            callback.onSuccess(code, body);
                        } else {
                            callback.onError("HTTP " + code + ": " + body);
                        }
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> callback.onError(e.getMessage()));
                } finally {
                    response.close();
                }
            }
        });
    }
}