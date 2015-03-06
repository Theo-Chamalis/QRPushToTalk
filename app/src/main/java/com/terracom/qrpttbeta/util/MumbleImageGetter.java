package com.terracom.qrpttbeta.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.text.Html;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.terracom.qrpttbeta.Settings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class MumbleImageGetter implements Html.ImageGetter {


    private static final int MAX_LENGTH = 64000;

    private Context mContext;
    private Settings mSettings;
    private Map<String, Drawable> mBitmapCache;

    public MumbleImageGetter(Context context) {
        mContext = context;
        mSettings = Settings.getInstance(context);
        mBitmapCache = new HashMap<String, Drawable>();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public Drawable getDrawable(String source) {
        Drawable cachedDrawable = mBitmapCache.get(source);
        if(cachedDrawable != null) return cachedDrawable;

        String decodedSource;
        try {
            decodedSource = URLDecoder.decode(source, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        Bitmap bitmap = null;
        try {
            if(decodedSource.startsWith("data:image")) {
                bitmap = getBase64Image(decodedSource.split(",")[1]);
            } else if(mSettings.shouldLoadExternalImages()) {
                bitmap = getURLImage(decodedSource);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        if(bitmap == null) return null;

        BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics(); // Use display metrics to scale image to mdpi
        drawable.setBounds(0, 0, (int)((float)drawable.getIntrinsicWidth()*metrics.density), (int)((float)drawable.getIntrinsicHeight()*metrics.density));
        mBitmapCache.put(source, drawable);
        return drawable;
    }

    private Bitmap getBase64Image(String base64) throws IllegalArgumentException {
        byte[] src = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(src, 0, src.length);
    }

    private Bitmap getURLImage(String source) {
        try {
            URL url = new URL(source);
            URLConnection conn = url.openConnection();
            if(conn.getContentLength() > MAX_LENGTH) return null;
            return BitmapFactory.decodeStream(conn.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
