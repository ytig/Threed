package com.yangtao.threed.engine;

import android.content.Context;
import android.view.View;

/**
 * 构造方法
 */
public class TBuilder {
    public static final float EYE_SHOT = 66; //视野
    public static final float EYE_HIGH = 1.8f; //视高

    public static View build(Context context, TCamera.TCore core) {
        TMutex mutex = new TMutex(context);
        TView view = new TView(context, mutex);
        TCamera camera = new TCamera(context, mutex, core);
        return view;
    }
}
