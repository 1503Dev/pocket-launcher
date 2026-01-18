package dev1503.pocketlauncher;

import java.util.Map;

public class XboxAPI {
    public static final String TAG = "XboxAPI";

    public static void getSimpleProfileByName(String username, HttpUtils.HttpCallback callback) {
        HttpUtils.get("https://peoplehub-public.xboxlive.com/people/gt(" + username + ")", callback, Map.of(
                "accept-language", "*",
                "x-xbl-contract-version", "3"
        ));
    }
}
