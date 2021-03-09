package com.teamayka.vansaleandmgmt.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.teamayka.vansaleandmgmt.R;


public class SignatureView extends View {
    private Path mPath;
    private Paint mPaint;

    private Bitmap mBitmap;
    private Canvas mCanvas;

    private float curX, curY;

    private static final int TOUCH_TOLERANCE = 2;
    private static final int MINIMUM_SIGNATURE_LENGTH = 20;
    private static final int STROKE_WIDTH = 6;

    private boolean isSignatureValid = false;
    private Context context;


    public SignatureView(Context context) {
        super(context);
        init(context);
    }

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SignatureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setFocusable(true);
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);
    }

    public boolean clearSignature() {
        if (mBitmap != null)
            createFakeMotionEvents();
        if (mCanvas != null) {

//            mCanvas.drawColor(0xfff9f9f9);
            Drawable drawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = getResources().getDrawable(R.drawable.shape_bg_sv, context.getTheme());
            } else
                drawable = getResources().getDrawable(R.drawable.shape_bg_sv);
            mCanvas.drawBitmap(drawableToBitmap(drawable), 0, 0, new Paint());

            mPath.reset();
            invalidate();
            this.isSignatureValid = false;
        } else {
            return false;
        }
        return true;
    }

    public boolean isSignatureValid() {
        return this.isSignatureValid;
    }

    public Bitmap getImage() {
        return this.mBitmap;
    }

    public void setImage(Bitmap bitmap) {
        this.mBitmap = bitmap;
        this.invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        int bitmapWidth = mBitmap != null ? mBitmap.getWidth() : 0;
        int bitmapHeight = mBitmap != null ? mBitmap.getWidth() : 0;
        if (bitmapWidth >= width && bitmapHeight >= height)
            return;
        if (bitmapWidth < width)
            bitmapWidth = width;
        if (bitmapHeight < height)
            bitmapHeight = height;

//        Bitmap newBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = getResources().getDrawable(R.drawable.shape_bg_sv, context.getTheme());
        } else
            drawable = getResources().getDrawable(R.drawable.shape_bg_sv);

        Bitmap newBitmap = drawableToBitmap(drawable);
        Canvas newCanvas = new Canvas();
        newCanvas.setBitmap(newBitmap);

        if (mBitmap != null)
            newCanvas.drawBitmap(mBitmap, 0, 0, null);
        mBitmap = newBitmap;
        mCanvas = newCanvas;
    }

    private void createFakeMotionEvents() {
        MotionEvent downEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, 1f, 1f, 0);
        MotionEvent upEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_UP, 1f, 1f, 0);
        onTouchEvent(downEvent);
        onTouchEvent(upEvent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
//          used to remove scrolling scroll view on draw signature
            getParent().requestDisallowInterceptTouchEvent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        canvas.drawColor(0xfff9f9f9);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }
        invalidate();
        return true;
    }

    private void touchDown(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        curX = x;
        curY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - curX);
        float dy = Math.abs(y - curY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(curX, curY, (x + curX) / 2, (y + curY) / 2);
            curX = x;
            curY = y;
//          if signature length is less then not valid
            if (dx >= MINIMUM_SIGNATURE_LENGTH || dy >= MINIMUM_SIGNATURE_LENGTH)
                this.isSignatureValid = true;
        }
    }

    private void touchUp() {
        mPath.lineTo(curX, curY);
        if (mCanvas == null) {
            mCanvas = new Canvas();
            mCanvas.setBitmap(mBitmap);
        }
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}