package dev1503.pocketlauncher.launcher;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.List;
import java.util.Objects;

import dev1503.Log;
import dev1503.pocketlauncher.HttpUtils;
import dev1503.pocketlauncher.R;
import dev1503.pocketlauncher.Utils;
import dev1503.pocketlauncher.XboxAPI;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    MainActivity self = this;

    ViewGroup contentView;
    ViewGroup layoutContainer;
    ViewGroup layoutMain;

    ImageView titleBarIcon;
    ImageView titleBarBack;
    ImageView titleBarExit;
    ImageView titleBarMinimize;
    TextView titleBarTitle;

    TextView mainAccountName;
    TextView mainLoginMethod;
    ImageView mainAccountIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentView = (ViewGroup) View.inflate(self, R.layout.activity_launcher, null);
        setContentView(contentView);
        layoutContainer = (ViewGroup) findViewWithTag("container");
        initLayouts();
        initMsAccount();
    }

    void initLayouts() {
        initTitleBar();
        layoutMain = (ViewGroup) View.inflate(self, R.layout.layout_launcher_main, layoutContainer);
        mainAccountName = (TextView) findViewWithTag("main_account_name");
        mainLoginMethod = (TextView) findViewWithTag("main_login_method");
        mainAccountIcon = (ImageView) findViewWithTag("main_account_icon");

        Utils.setAllTextColor(contentView, Utils.getColorFromAttr(self, com.google.android.material.R.attr.colorOnPrimary));
    }

    @SuppressLint("SetTextI18n")
    void initTitleBar() {
        titleBarIcon = (ImageView) findViewWithTag("title_bar_icon");
        titleBarBack = (ImageView) findViewWithTag("title_bar_back");
        titleBarExit = (ImageView) findViewWithTag("title_bar_exit");
        titleBarMinimize = (ImageView) findViewWithTag("title_bar_minimize");
        titleBarTitle = (TextView) findViewWithTag("title_bar_title");

        titleBarTitle.setText(getString(R.string.app_name) + " v" + Utils.getAppVersionName(self));

        titleBarMinimize.setOnClickListener((v) -> {
            moveTaskToBack(true);
        });
        titleBarExit.setOnClickListener((v) -> {
            new MaterialAlertDialogBuilder(self)
                    .setTitle(R.string.are_you_sure_exit)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        System.exit(0);
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });
    }

    @SuppressLint("CheckResult")
    void initMsAccount () {
        mainAccountIcon.setImageResource(R.drawable.person_24px);
        mainAccountName.setText(R.string.accounts);
        mainLoginMethod.setText(R.string.not_logged_in);
        Single single = Utils.getCurrentXalIdRx(self);
        if (single != null) {
            single.subscribe((str) -> {
                if (str != null) {
                    try {
                        JsonObject jsonObject = new Gson().fromJson((String) str, JsonObject.class);
                        String id = jsonObject.get("default").getAsString();
                        Utils.searchFilesWithContent(Utils.getXalDirPath(self), id, new Utils.FilesSearchWithContentListener() {
                            @Override
                            public void onSearchComplete(List<File> files, List<String> fileContents) {
                                new Thread(() -> {
                                    for (int i = 0; i < files.size(); i++) {
                                        try {
                                            String content = fileContents.get(i);
                                            JsonObject jsonObject = new Gson().fromJson(content, JsonObject.class);
                                            JsonArray tokens = jsonObject.get("tokens").getAsJsonArray();
                                            for (int j = 0; j < tokens.size(); j++) {
                                                Log.d(TAG, "token: " + tokens.get(j).getAsJsonObject());
                                                try {
                                                    JsonObject token = tokens.get(j).getAsJsonObject();
                                                    JsonObject tokenData = token.get("TokenData").getAsJsonObject();
                                                    JsonObject displayClaims = tokenData.get("DisplayClaims").getAsJsonObject();
                                                    JsonArray xui = displayClaims.get("xui").getAsJsonArray();
                                                    for (int k = 0; k < xui.size(); k++) {
                                                        try {
                                                            JsonObject xuiObj = xui.get(k).getAsJsonObject();
                                                            String gtg = xuiObj.get("gtg").getAsString();
                                                            Log.d(TAG, "gtg: " + gtg);
                                                            if (!gtg.isEmpty()) {
                                                                runOnUiThread(() -> {
                                                                    mainAccountName.setText(gtg);
                                                                    mainLoginMethod.setText(R.string.microsoft_account);
                                                                });
                                                                XboxAPI.getSimpleProfileByName(gtg, new HttpUtils.HttpCallback() {
                                                                    @Override
                                                                    public void onSuccess(int code, String body) {
                                                                        try {
                                                                            JsonObject jsonObject = new Gson().fromJson(body, JsonObject.class).get("people").getAsJsonArray().get(0).getAsJsonObject();
                                                                            String avatar = jsonObject.get("displayPicRaw").getAsString();
                                                                            runOnUiThread(() -> {
                                                                                Glide.with(self).load(avatar).into(mainAccountIcon);
                                                                            });
                                                                        } catch (Exception e) {
                                                                            Log.w(TAG, e);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onError(String error) {
                                                                        Log.w(TAG, error);
                                                                    }
                                                                });
                                                                return;
                                                            }
                                                        } catch (Exception e) {
                                                            Log.w(TAG, e);
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    Log.w(TAG, e);
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.w(TAG, e);
                                        }
                                    }
                                }).start();
                            }

                            @Override
                            public void onSearchError(Throwable error) {

                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                }
            });
        }
    }

    View findViewWithTag(String tag) {
        return getWindow().getDecorView().findViewWithTag(tag);
    }
}
