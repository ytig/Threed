package com.yangtao.threed.engine;

import android.content.Context;
import android.view.animation.AnimationUtils;

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
    private long mTime; //计时

    protected TCamera(Context context, TMutex mutex, TCore core) {
        super(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels, TBuilder.EYE_SHOT);
        mLens.jumpTo(TBuilder.EYE_HIGH);
        mMutex = mutex;
        mCore = core;
        mTime = Long.MAX_VALUE;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            mCore.doMotion(time > mTime ? (time - mTime) : 0);
            mTime = time;
            clear();
            for (Surfaces surfaces : mCore.mScene) draw(surfaces);
            while (true) {
                if (mMutex.apply("bitmap")) {
                    mMutex.mBitmap.setPixels(mCanvas, 0, mWidth, 0, 0, mWidth, mHeight);
                    mMutex.release("bitmap");
                    break;
                }
            }
            long delay = 25L + time - AnimationUtils.currentAnimationTimeMillis();
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
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
            if (surfaces == null) return;
            if (mScene.contains(surfaces)) return;
            mScene.add(surfaces);
        }

        protected void addSurfaces(List objects) {
            if (objects == null) return;
            for (Object object : objects) {
                if (object instanceof Surfaces) addSurfaces((Surfaces) object);
            }
        }

        protected void addSurfaces() {
            //todo
        }

        protected abstract void doMotion(long ms); //间隔执行
    }
}
