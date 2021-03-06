package com.yangtao.threed;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.yangtao.engine.Camera;
import com.yangtao.threed.element.Rects;
import com.yangtao.threed.engine.BaseView;
import com.yangtao.threed.engine.Core;
import com.yangtao.threed.engine.ThreadView;
import com.yangtao.threed.extend.ControlView;
import com.yangtao.threed.extend.Floor;
import com.yangtao.threed.extend.Param;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private static List initScene() { //构建场景
        return Arrays.asList(
                new Floor(15, 1f, Color.TRANSPARENT, Color.WHITE, Color.TRANSPARENT, Color.BLACK),
                new Rects(1f, 1f, 1f).setColor(Color.BLACK, Color.WHITE).setTranslate(0, 0, 0.5f).setRotate(0, 0, 45)
        );
    }

    private BaseView<Param> mView; //渲染视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new ThreadView<>(this, new Core<Param>() {
            @Override
            protected void doCompute(long ms, Camera.Lens lens, Param param) {
                param.doLens(ms, lens);
            }
        }.addSurfaces(initScene()), new Param(this));
        FrameLayout content = new FrameLayout(this);
        content.addView(mView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        content.addView(new ControlView(mView), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        setContentView(content);
        mView.doCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.doResume();
    }

    @Override
    protected void onPause() {
        mView.doPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mView.doDestroy();
        super.onDestroy();
    }
}
