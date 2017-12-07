package com.yangtao.threed;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.yangtao.threed.engine.BaseView;
import com.yangtao.threed.engine.ThreadView;
import com.yangtao.threed.extend.ControlView;
import com.yangtao.threed.extend.Home;
import com.yangtao.threed.extend.Param;

public class MainActivity extends Activity {
    private BaseView<Param> mView; //渲染视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new ThreadView<>(this, new Home(), new Param());
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
}
