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
public class ThreadView<Param> extends BaseView<Param> {
    private static final float EYE_SHOT = 66; //视野
    private static final float EYE_HIGH = 1.8f; //视高

    private Mutex<MyState> mState; //互斥状态
    private Mutex<Bitmap> mBitmap; //互斥位图
    private Mutex<Param> mParam; //互斥参数
    private MyCamera mCamera; //相机
    private BitmapHandler mHandler; //位图处理（主线程）

    public ThreadView(Context context, Core<Param> core, Param param) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null); //关闭硬件加速
        mState = new Mutex<>(new MyState());
        mBitmap = new Mutex<>(Bitmap.createBitmap(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels, Bitmap.Config.RGB_565));
        mParam = new Mutex<>(param);
        mCamera = new MyCamera(core);
        mHandler = new BitmapHandler();
    }

    @Override
    public int getState() {
        final MyState state = new MyState();
        mState.block(new Mutex.DataHandler<MyState>() {
            @Override
            public void handleData(MyState myState) {
                if (myState == null) return;
                state.mState = myState.mState;
            }
        });
        return state.mState;
    }

    @Override
    public void doCreate() {
        mState.block(new Mutex.DataHandler<MyState>() {
            @Override
            public void handleData(MyState myState) {
                if (myState == null) return;
                if (myState.mState != STATE_TODO) return;
                myState.mState = STATE_DOING;
                new Thread(mCamera).start();
            }
        });
    }

    @Override
    public void doResume() {
        mState.block(new Mutex.DataHandler<MyState>() {
            @Override
            public void handleData(MyState myState) {
                if (myState == null) return;
                if (myState.mState != STATE_UNDO) return;
                myState.mState = STATE_DOING;
            }
        });
    }

    @Override
    public void doPause() {
        mState.block(new Mutex.DataHandler<MyState>() {
            @Override
            public void handleData(MyState myState) {
                if (myState == null) return;
                if (myState.mState != STATE_DOING) return;
                myState.mState = STATE_UNDO;
            }
        });
    }

    @Override
    public void doDestroy() {
        mState.block(new Mutex.DataHandler<MyState>() {
            @Override
            public void handleData(MyState myState) {
                if (myState == null) return;
                if (myState.mState != STATE_DOING && myState.mState != STATE_UNDO) return;
                myState.mState = STATE_DONE;
            }
        });
    }

    @Override
    public void setParam(Mutex.DataHandler<Param> handler) {
        mParam.block(handler); //注参
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mHandler.handle(canvas, mBitmap); //绘制
        postInvalidate();
    }

    private class MyCamera extends Camera implements Runnable {
        private int mChoice; //处理类型
        private Core<Param> mCore; //处理内核
        private Mutex.DataHandler<MyState> sHandler; //状态处理（子线程）
        private Mutex.DataHandler<Bitmap> bHandler; //位图处理（子线程）
        private Mutex.DataHandler<Param> pHandler; //参数处理（子线程）

        public MyCamera(Core<Param> core) {
            super(getContext().getResources().getDisplayMetrics().widthPixels, getContext().getResources().getDisplayMetrics().heightPixels, EYE_SHOT);
            mLens.jumpTo(EYE_HIGH);
            mCore = core;
            sHandler = new Mutex.DataHandler<MyState>() {
                @Override
                public void handleData(MyState myState) {
                    if (myState != null) {
                        switch (myState.mState) {
                            case STATE_DOING:
                                mChoice = 1;
                                break;
                            case STATE_UNDO:
                                mChoice = 0;
                                break;
                            case STATE_DONE:
                                mChoice = -1;
                                break;
                        }
                    }
                }
            };
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
        }

        @Override
        public void run() {
            while (true) {
                long millis = AnimationUtils.currentAnimationTimeMillis();
                mState.block(sHandler); //分岔
                if (mChoice < 0) break;
                if (mChoice > 0) {
                    mParam.block(pHandler); //运算
                    clear();
                    for (Surfaces surfaces : mCore.mScene) draw(surfaces); //渲染
                    mBitmap.block(bHandler); //上色
                }
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

    private static class MyState {
        public int mState = STATE_TODO; //状态标记
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
}
