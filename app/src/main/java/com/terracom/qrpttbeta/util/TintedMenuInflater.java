package com.terracom.qrpttbeta.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.terracom.qrpttbeta.R;

public class TintedMenuInflater {
    private MenuInflater mInflater;
    private int mTintColour;

    public TintedMenuInflater(Context context, MenuInflater inflater) {
        mInflater = inflater;
        TypedArray actionBarThemeArray =
                context.obtainStyledAttributes(new int[]{R.attr.actionBarStyle});
        int actionBarTheme = actionBarThemeArray.getResourceId(0, 0);
        actionBarThemeArray.recycle();

        TypedArray titleTextStyleArray =
                context.obtainStyledAttributes(actionBarTheme, new int[]{R.attr.titleTextStyle});
        int titleTextStyle = titleTextStyleArray.getResourceId(0, 0);
        titleTextStyleArray.recycle();

        TypedArray textColorArray =
                context.obtainStyledAttributes(titleTextStyle, new int[]{android.R.attr.textColor});
        mTintColour = textColorArray.getColor(0, 0);
        textColorArray.recycle();
    }

    public void inflate(int menuRes, Menu menu) {
        mInflater.inflate(menuRes, menu);
        for (int x = 0; x < menu.size(); x++) {
            MenuItem item = menu.getItem(x);
            tintItem(item);
        }
    }

    public void tintItem(MenuItem item) {
        if (item.getIcon() != null) {
            Drawable icon = item.getIcon().mutate();
            icon.setColorFilter(mTintColour, PorterDuff.Mode.MULTIPLY);
        }
    }
}
