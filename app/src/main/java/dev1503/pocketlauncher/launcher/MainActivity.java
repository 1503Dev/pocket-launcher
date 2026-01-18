package dev1503.pocketlauncher.launcher;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev1503.pocketlauncher.R;
import dev1503.pocketlauncher.Utils;

public class MainActivity extends AppCompatActivity {
    MainActivity self = this;

    ViewGroup contentView;
    ViewGroup layoutContainer;
    ViewGroup layoutMain;

    ImageView titleBarIcon;
    ImageView titleBarBack;
    ImageView titleBarExit;
    ImageView titleBarMinimize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentView = (ViewGroup) View.inflate(self, R.layout.activity_launcher, null);
        setContentView(contentView);
        layoutContainer = (ViewGroup) findViewWithTag("container");
        initLayouts();
    }

    void initLayouts() {
        initTitleBar();
        layoutMain = (ViewGroup) View.inflate(self, R.layout.layout_launcher_main, layoutContainer);

        Utils.setAllTextColor(contentView, Utils.getColorFromAttr(self, com.google.android.material.R.attr.colorOnPrimary));
    }

    void initTitleBar() {
        titleBarIcon = (ImageView) findViewWithTag("title_bar_icon");
        titleBarBack = (ImageView) findViewWithTag("title_bar_back");
        titleBarExit = (ImageView) findViewWithTag("title_bar_exit");
        titleBarMinimize = (ImageView) findViewWithTag("title_bar_minimize");

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

    View findViewWithTag(String tag) {
        return getWindow().getDecorView().findViewWithTag(tag);
    }
}
