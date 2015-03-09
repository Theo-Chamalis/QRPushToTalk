package com.terracom.qrpttbeta.service;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.terracom.qrpttbeta.R;

public class QRPushToTalkHotCorner implements View.OnTouchListener {
    private WindowManager mWindowManager;
    private Context mContext;
    private View mView;
    private boolean mShown;
    private int mHighlightColour;
    private QRPushToTalkHotCornerListener mListener;
    private WindowManager.LayoutParams mParams;

    public QRPushToTalkHotCorner(Context context, int gravity, QRPushToTalkHotCornerListener listener) {
        if (listener == null) {
            throw new NullPointerException("A QRPushToTalkHotCornerListener must be assigned.");
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mContext = context;
        mView = inflater.inflate(R.layout.ptt_corner, null, false);
        mView.setOnTouchListener(this);
        mListener = listener;
        mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = gravity;
        mHighlightColour = mContext.getResources().getColor(R.color.holo_blue_bright);
    }

    private void updateLayout() {
        if (!isShown()) return;
        mWindowManager.updateViewLayout(mView, mParams);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mView.setBackgroundColor(mHighlightColour);
                mListener.onHotCornerDown();
                return true;
            case MotionEvent.ACTION_UP:
                mView.setBackgroundColor(0);
                mListener.onHotCornerUp();
                return true;
            default:
                return false;
        }
    }

    public void setShown(boolean shown) {
        if (shown == mShown) return;
        if (shown) {
            mWindowManager.addView(mView, mParams);
        } else {
            mWindowManager.removeView(mView);
        }
        mShown = shown;
    }

    public boolean isShown() {
        return mShown;
    }

    public void setGravity(int gravity) {
        mParams.gravity = gravity;
        updateLayout();
    }

    public int getGravity() {
        return mParams.gravity;
    }

    public static interface QRPushToTalkHotCornerListener {
        public void onHotCornerDown();

        public void onHotCornerUp();
    }
}
