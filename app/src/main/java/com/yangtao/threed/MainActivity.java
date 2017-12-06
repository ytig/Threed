package com.yangtao.threed;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.yangtao.threed.element.Rect;
import com.yangtao.threed.element.Rects;
import com.yangtao.threed.engine.TBuilder;
import com.yangtao.threed.engine.TCamera;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(TBuilder.build(this, new MyCore()));
    }

    private static class MyCore extends TCamera.TCore {
        public MyCore() {
            addSurfaces(new Rects(1.5f, 1f, 0.5f)
                    .setColor(Color.GRAY, Color.RED)
                    .setTranslate(5f, 0, 0.25f));
            float l = 1f;
            int s = 15;
            for (int i = -s; i <= s; i++) {
                for (int j = -s; j <= s; j++) {
                    addSurfaces(new Rect(l, l)
                            .setColor(Color.TRANSPARENT, Color.WHITE)
                            .setTranslate(i * l, j * l, 0));
                }
            }
        }
    }
}
