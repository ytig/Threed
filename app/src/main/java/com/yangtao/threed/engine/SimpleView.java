package com.yangtao.threed.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.yangtao.engine.Camera;
import com.yangtao.engine.Surfaces;

/**
 * 简单视图
 *
 * @param <Param>
 */
public class SimpleView<Param> extends BaseView<Param> {
    private static final float EYE_SHOT = 66; //视野
    private static final float EYE_HIGH = 1.8f; //视高

    private int mState; //状态
    private Camera mCamera; //相机
    private Bitmap mBitmap; //位图
    private Core<Param> mCore; //内核
    private Param mParam; //参数

    public SimpleView(Context context, Core<Param> core, Param param) {
        super(context);
        mState = STATE_TODO;
        mCamera = new Camera(getContext().getResources().getDisplayMetrics().widthPixels, getContext().getResources().getDisplayMetrics().heightPixels, EYE_SHOT);
        mCamera.mLens.jumpTo(EYE_HIGH);
        mBitmap = Bitmap.createBitmap(mCamera.mWidth, mCamera.mHeight, Bitmap.Config.RGB_565);
        mCore = core;
        mParam = param;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void doCreate() {
        if (mState != STATE_TODO) return;
        mState = STATE_DOING;
        invalidate();
    }

    @Override
    public void doResume() {
        if (mState != STATE_UNDO) return;
        mState = STATE_DOING;
        invalidate();
    }

    @Override
    public void doPause() {
        if (mState != STATE_DOING) return;
        mState = STATE_UNDO;
    }

    @Override
    public void doDestroy() {
        if (mState != STATE_DOING && mState != STATE_UNDO) return;
        mState = STATE_DONE;
    }

    @Override
    public void setParam(Mutex.DataHandler<Param> handler) {
        if (handler != null) handler.handleData(mParam);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mState == STATE_DOING) {
            mCore.doCompute(mCamera.mLens, mParam);
            mCamera.clear();
            for (Surfaces surfaces : mCore.mScene) mCamera.draw(surfaces);
            mBitmap.setPixels(mCamera.mCanvas, 0, mCamera.mWidth, 0, 0, mCamera.mWidth, mCamera.mHeight);
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }
}
