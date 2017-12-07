package com.yangtao.threed.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import com.yangtao.engine.Camera;
import com.yangtao.engine.Surfaces;

/**
 * 简单视图
 */
public class SimpleView<Param> extends View implements Mutex.DataMessenger<Param> {
    private static final float EYE_SHOT = 66; //视野
    private static final float EYE_HIGH = 1.8f; //视高

    private MyCamera mCamera; //相机
    private Param mParam; //参数

    public SimpleView(Context context, Core<Param> core, Param param) {
        super(context);
        mCamera = new MyCamera(core);
        mParam = param;
    }

    @Override
    public void setData(Mutex.DataHandler<Param> handler) {
        if (handler != null) handler.handleData(mParam);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCamera.draw(canvas);
        postInvalidate();
    }

    private class MyCamera extends Camera {
        private Core<Param> mCore; //内核
        private Bitmap mBitmap; //位图

        public MyCamera(Core<Param> core) {
            super(getContext().getResources().getDisplayMetrics().widthPixels, getContext().getResources().getDisplayMetrics().heightPixels, EYE_SHOT);
            mLens.jumpTo(EYE_HIGH);
            mCore = core;
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        }

        public void draw(Canvas canvas) {
            mCore.doCompute(mLens, mParam);
            clear();
            for (Surfaces surfaces : mCore.mScene) draw(surfaces);
            mBitmap.setPixels(mCanvas, 0, mWidth, 0, 0, mWidth, mHeight);
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }
}
