package com.yangtao.threed.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.yangtao.engine.Camera;
import com.yangtao.engine.Surfaces;

/**
 * 线程视图
 *
 * @param <Param>
 */
public class ThreadView<Param> extends View {
    public static final float EYE_SHOT = 66; //视野
    public static final float EYE_HIGH = 1.8f; //视高

    private Mutex<Bitmap> mBitmap; //互斥位图
    private Mutex<Param> mParam; //互斥参数
    private BitmapHandler mHandler; //位图处理（主线程）

    public ThreadView(Context context, Core<Param> core, Param param) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null); //关闭硬件加速
        mBitmap = new Mutex<>(Bitmap.createBitmap(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels, Bitmap.Config.RGB_565));
        mParam = new Mutex<>(param);
        mHandler = new BitmapHandler();
        new MyCamera(core);
    }

    /**
     * 阻塞式设置参数
     *
     * @param handler
     */
    public void setParam(Mutex.DataHandler<Param> handler) {
        mParam.block(handler); //注参
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mHandler.handle(canvas, mBitmap); //绘制
        postInvalidate();
    }

    private static class BitmapHandler implements Mutex.DataHandler<Bitmap> {
        private Canvas mCanvas; //画布

        public void handle(Canvas canvas, Mutex<Bitmap> bitmap) {
            if (canvas != null && bitmap != null) {
                mCanvas = canvas;
                bitmap.block(this);
            }
        }

        @Override
        public void handleData(Bitmap data) {
            if (mCanvas != null) {
                if (data != null) mCanvas.drawBitmap(data, 0, 0, null);
                mCanvas = null;
            }
        }
    }

    private class MyCamera extends Camera implements Runnable {
        private Core<Param> mCore; //处理内核
        private Mutex.DataHandler<Bitmap> bHandler; //位图处理（子线程）
        private Mutex.DataHandler<Param> pHandler; //参数处理（子线程）

        public MyCamera(Core<Param> core) {
            super(getContext().getResources().getDisplayMetrics().widthPixels, getContext().getResources().getDisplayMetrics().heightPixels, EYE_SHOT);
            mLens.jumpTo(EYE_HIGH);
            mCore = core;
            bHandler = new Mutex.DataHandler<Bitmap>() {
                @Override
                public void handleData(Bitmap data) {
                    if (data != null) data.setPixels(mCanvas, 0, mWidth, 0, 0, mWidth, mHeight);
                }
            };
            pHandler = new Mutex.DataHandler<Param>() {
                @Override
                public void handleData(Param data) {
                    if (mCore != null) mCore.doCompute(mLens, data);
                }
            };
            new Thread(this).start();
        }

        @Override
        public void run() {
            while (true) {
                long millis = AnimationUtils.currentAnimationTimeMillis();
                mParam.block(pHandler); //运算
                clear();
                for (Surfaces surfaces : mCore.mScene) draw(surfaces); //渲染
                mBitmap.block(bHandler); //上色
                millis = 25L + millis - AnimationUtils.currentAnimationTimeMillis();
                if (millis > 0) {
                    try {
                        Thread.sleep(millis);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}

/**
 * 互斥数据
 *
 * @param <Data>
 */
class Mutex<Data> {
    private boolean mMutex; //互斥
    private Data mData; //数据

    public Mutex(Data data) {
        mMutex = false;
        mData = data;
    }

    /**
     * 非阻塞式执行
     *
     * @param handler
     * @return
     */
    public boolean unblock(DataHandler<Data> handler) {
        if (apply()) {
            if (handler != null) handler.handleData(mData);
            release();
            return true;
        }
        return false;
    }

    /**
     * 阻塞式执行
     *
     * @param handler
     */
    public void block(DataHandler<Data> handler) {
        while (!unblock(handler)) {
        }
    }

    private synchronized boolean apply() { //申请
        if (mMutex) return false;
        mMutex = true;
        return true;
    }

    private synchronized boolean release() { //释放
        if (!mMutex) return false;
        mMutex = false;
        return true;
    }

    /**
     * 数据处理
     *
     * @param <Data>
     */
    public interface DataHandler<Data> {
        void handleData(Data data);
    }
}
