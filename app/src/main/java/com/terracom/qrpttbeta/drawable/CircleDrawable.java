package com.terracom.qrpttbeta.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.terracom.qrpttbeta.R;

public class CircleDrawable extends Drawable {
    public static final int STROKE_WIDTH_DP = 1;
    private Resources mResources;
    private Bitmap mBitmap;
    private Paint mPaint;
    private Paint mStrokePaint;
    private ConstantState mConstantState;

    public CircleDrawable(Resources resources, Bitmap bitmap) {
        mResources = resources;
        mBitmap = bitmap;

        mPaint = new Paint();
        mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        mStrokePaint = new Paint();
        mStrokePaint.setDither(true);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setColor(resources.getColor(R.color.ripple_talk_state_disabled));
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                STROKE_WIDTH_DP, resources.getDisplayMetrics());
        mStrokePaint.setStrokeWidth(strokeWidth);
        mStrokePaint.setStyle(Paint.Style.STROKE);

        mConstantState = new ConstantState() {
            @Override
            public Drawable newDrawable() {
                return new CircleDrawable(mResources, mBitmap);
            }

            @Override
            public int getChangingConfigurations() {
                return 0;
            }
        };
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        RectF bitmapRect = new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        Matrix matrix = new Matrix();
        matrix.setRectToRect(bitmapRect, new RectF(bounds), Matrix.ScaleToFit.CENTER);
        mPaint.getShader().setLocalMatrix(matrix);
    }

    @Override
    public void draw(Canvas canvas) {
        RectF imageRect = new RectF(getBounds());
        RectF strokeRect = new RectF(getBounds());
<<<<<<< HEAD
        strokeRect.inset(mStrokePaint.getStrokeWidth() / 2,
                mStrokePaint.getStrokeWidth() / 2);
=======
        strokeRect.inset(mStrokePaint.getStrokeWidth()/2,
                         mStrokePaint.getStrokeWidth()/2);
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0

        canvas.drawOval(imageRect, mPaint);
        canvas.drawOval(strokeRect, mStrokePaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public ConstantState getConstantState() {
        return mConstantState;
    }
}
