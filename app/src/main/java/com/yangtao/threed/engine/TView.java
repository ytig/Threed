package com.yangtao.threed.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * 控制视图
 */
public class TView extends View {
    private TMutex mMutex; //数据

    protected TView(Context context, TMutex mutex) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null); //关闭硬件加速
        mMutex = mutex;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        while (true) {
            if (mMutex.apply("bitmap")) {
                canvas.drawBitmap(mMutex.mBitmap, 0, 0, null);
                mMutex.release("bitmap");
                break;
            }
        }
        postInvalidate();
    }
}
