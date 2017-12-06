package com.yangtao.threed;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.yangtao.engine.Camera;
import com.yangtao.threed.element.Rect;
import com.yangtao.threed.element.Rects;
import com.yangtao.threed.engine.Core;
import com.yangtao.threed.engine.ThreadView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new ThreadView(this, new MyCore(), null));
    }

    private static class MyCore extends Core {
        private Rects mRects; //长方体

        public MyCore() {
            addFloor(15, 1f, Color.TRANSPARENT, Color.WHITE);
            mRects = new Rects(1.5f, 1f, 0.5f);
            mRects.setColor(Color.WHITE, Color.RED).setTranslate(5f, 0, 0.25f);
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
        protected void doCompute(long ms, Camera.Lens lens, Object param) {
            mRects.setRotate(mRects.mRotateX, mRects.mRotateY, mRects.mRotateZ + 360 * ms / 6666f);
        }
    }
}
