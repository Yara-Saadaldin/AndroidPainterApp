package com.example.imd4008a__assignment4_onyinyeagetu_yarasaadaldin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.Stack;

public class PaintView extends View {
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private Stack<Image> mImages = new Stack<>();
    private Stack<Image> mUndoneImages = new Stack<>();
    private int mBrushColor;
    private float mStrokeWidth;
    private Bitmap mBitmap;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Context mContext;
    private Canvas mCanvas;
    private boolean mClear = false;
    private int mBackgroundColor;
    private boolean mCircleMode = false, mLineMode = false, mColorFillMode = false, mRectangleMode = false;
    private int mWidth, mHeight;

    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPath = new Path();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.black));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(8);
        mContext = context;
        mBackgroundColor = getResources().getColor(R.color.white);
    }

    public void init(int height, int width) {
        mHeight = height;
        mWidth = width;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mBackgroundColor);
        mBrushColor = getResources().getColor(R.color.black);
        mImages.push(new Image(mBitmap));
    }

    public void setColor(int color) {
        mBrushColor = color;
    }

    public void setMostRecentBitmap() {
        if (!mImages.isEmpty()) {
            Bitmap lastBitmap = mImages.peek().bitmap;
            setBitmap(lastBitmap);
        }
    }

    public int getColor() {
        return mBrushColor;
    }

    public void setStrokeWidth(float width) {
        mStrokeWidth = width;
    }

    public void setCircleMode(boolean mode) {
        mCircleMode = mode;
    }

    public void setBitmap(Bitmap bitmap) {
        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mBitmap = mutableBitmap;
        mCanvas = new Canvas(mutableBitmap);
        mPath.reset();
        mImages.push(new Image(mBitmap));
        invalidate();
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        invalidate();
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void drawCurrentPathOnTheMostRecentBitmap() {
        if (!mImages.isEmpty()) {
            mCanvas.drawBitmap(mImages.peek().bitmap, 0, 0, mBitmapPaint);
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        }
        else
            mCanvas.drawColor(mBackgroundColor);
        invalidate();
    }

    public void undo() {
        if (!mImages.isEmpty()) {
            mPath.reset();
            if (mImages.size() > 1)
                mUndoneImages.push(mImages.pop());

            Bitmap lastBitmap = mImages.peek().bitmap;
            mCanvas.drawBitmap(lastBitmap, 0, 0, mBitmapPaint);
            invalidate();
        }
    }

    public void redo() {
        if (!mUndoneImages.isEmpty()) {
            Bitmap lastBitmap = mUndoneImages.peek().bitmap;
            mImages.push(mUndoneImages.pop());
            mPath.reset();
            mCanvas.drawBitmap(lastBitmap, 0, 0, mBitmapPaint);
            invalidate();
        }
    }

    public Bitmap save() {
        return mBitmap;
    }

    public void clearCanvas() {
        mClear = true;
        invalidate();
    }

    public void setLineMode(boolean lineMode) {
        mLineMode = lineMode;
    }

    public void setRectangleMode(boolean rectangleMode) {
        mRectangleMode = rectangleMode;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mClear) {
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.drawColor(mBackgroundColor);
            mImages.clear();
            mUndoneImages.clear();
            mImages.push(new Image(mBitmap));
            mPath.reset();
            mClear = false;
            return;
        }
        mPaint.setColor(mBrushColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        mCanvas.drawPath(mPath, mPaint);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint); // draw the main bitmap on canvas
    }

    private void touchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (mCircleMode) {
            float radius = (float) Math.sqrt(dx * dx + dy * dy); // calculate radius of the circle
            drawCurrentPathOnTheMostRecentBitmap();
            mPath.addCircle(mX, mY, radius, Path.Direction.CW);
        }

        else if (mLineMode) {
            drawCurrentPathOnTheMostRecentBitmap();
            mPath.moveTo(mX, mY);
            mPath.lineTo(x, y);
        }

        else if (mRectangleMode) {
            drawCurrentPathOnTheMostRecentBitmap();
            // draws a rectangle
            mPath.moveTo(mX, mY);
            mPath.lineTo(x, mY);
            mPath.moveTo(x, mY);
            mPath.lineTo(x, y);
            mPath.moveTo(mX, mY);
            mPath.lineTo(mX, y);
            mPath.moveTo(mX, y);
            mPath.lineTo(x, y);
            mPath.moveTo(x, y);
        }

        else {
            // draws path, which is not a circle, line or a rectangle
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }
    }

    private void touchUp(float x, float y) {
        mImages.push(new Image(mBitmap));
        mPath = new Path();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                invalidate();
                break;
        }
        return true;
    }
}

