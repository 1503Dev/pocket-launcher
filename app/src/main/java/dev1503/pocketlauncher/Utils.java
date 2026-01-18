package dev1503.pocketlauncher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev1503.Log;

public class Utils {
    public static final String TAG = "Utils";

    public static @ColorInt int getColorFromAttr(Context context, int attrResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return Color.BLACK;
    }
    public static void setAllTextColor(ViewGroup viewGroup, @ColorInt int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup) {
                setAllTextColor((ViewGroup) view, color);
            } else if (view instanceof TextView) {
                if (view.getTag() != null && view.getTag().equals("main_login_method")) {
                    continue;
                }
                ((TextView) view).setTextColor(color);
            }
        }
    }
}
