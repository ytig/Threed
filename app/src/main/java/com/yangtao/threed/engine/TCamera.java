package com.yangtao.threed.engine;

import android.content.Context;

import com.yangtao.engine.Camera;
import com.yangtao.engine.Surfaces;

import java.util.ArrayList;
import java.util.List;

/**
 * 渲染器
 */
public class TCamera extends Camera implements Runnable {
    private TMutex mMutex; //数据
    private TCore mCore; //核心

    protected TCamera(Context context, TMutex mutex, TCore core) {
        super(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels, TBuilder.EYE_SHOT);
        mMutex = mutex;
        mCore = core;
        mLens.jumpTo(TBuilder.EYE_HIGH);
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            clear();
            for (Surfaces surfaces : mCore.mScene) draw(surfaces);
            while (true) {
                if (mMutex.apply("bitmap")) {
                    mMutex.mBitmap.setPixels(mCanvas, 0, mWidth, 0, 0, mWidth, mHeight);
                    mMutex.release("bitmap");
                    break;
                }
            }
        }
    }

    /**
     * 处理核
     */
    public abstract static class TCore {
        private List<Surfaces> mScene; //场景

        public TCore() {
            mScene = new ArrayList<>();
        }

        protected void addSurfaces(Surfaces surfaces) {
            mScene.add(surfaces);
        }
    }
}
