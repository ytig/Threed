package com.yangtao.threed;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.yangtao.engine.Camera;
import com.yangtao.threed.element.Rect;
import com.yangtao.threed.element.Rects;
import com.yangtao.threed.engine.BaseView;
import com.yangtao.threed.engine.Core;
import com.yangtao.threed.engine.ThreadView;
import com.yangtao.threed.extend.ControlView;
import com.yangtao.threed.extend.Param;

public class MainActivity extends Activity {
    private BaseView<Param> mView; //渲染视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new ThreadView<>(this, new MyCore(), new Param());
        mView.doCreate();
        mView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mView.doResume();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                mView.doPause();
            }
        });
        FrameLayout content = new FrameLayout(this);
        content.addView(mView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        content.addView(new ControlView(mView), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        setContentView(content);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mView.doDestroy();
    }

    private static class MyCore extends Core<Param> {
        private Rects mRects; //长方体

        public MyCore() {
            addFloor(15, 1f, Color.TRANSPARENT, Color.WHITE);
            mRects = new Rects(1.5f, 1f, 0.5f);
            mRects.setColor(Color.BLACK, Color.RED).setTranslate(5f, 0, 0.25f);
            addSurfaces(); //反射
        }

        protected void addFloor(int size, float length, Integer... colors) { //添加地板
            if (colors.length <= 0) return;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    Rect rect = new Rect(length, length);
                    rect.setColor(colors[(2 * (i * size + j)) % colors.length], colors[(2 * (i * size + j) + 1) % colors.length]);
                    rect.setTranslate((2 * i + 1 - size) * length / 2, (2 * j + 1 - size) * length / 2, 0);
                    addSurfaces(rect);
                }
            }
        }

        @Override
        protected void doCompute(long ms, Camera.Lens lens, Param param) {
            param.doLens(ms, lens);
            mRects.setRotate(mRects.mRotateX, mRects.mRotateY, mRects.mRotateZ + 360 * ms / 6666f);
        }
    }
}
