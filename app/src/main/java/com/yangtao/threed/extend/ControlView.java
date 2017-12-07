package com.yangtao.threed.extend;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.yangtao.threed.engine.BaseView;

/**
 * 控制视图
 */
public class ControlView extends View {
    private BaseView<Param> mView; //渲染视图
    private Param mParam = new Param(); //控制参数
    private Pointer mLeft = new Pointer(); //左屏触控
    private Pointer mRight = new Pointer(); //右屏触控

    public ControlView(BaseView<Param> view) {
        super(view.getContext());
        mView = view;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int masked = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || masked == MotionEvent.ACTION_POINTER_DOWN) {
            int id = event.getPointerId(event.getActionIndex());
            float x = getRawX(event, event.getActionIndex());
            float y = getRawY(event, event.getActionIndex());
            Pointer pointer = (x <= getContext().getResources().getDisplayMetrics().widthPixels / 2) ? mLeft : mRight;
            if (pointer.id == -1) {
                pointer.id = id;
                pointer.x = x;
                pointer.y = y;
                pointer.t = AnimationUtils.currentAnimationTimeMillis();
            }
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (mLeft.id != -1) {
                int index = event.findPointerIndex(mLeft.id);
                float x = getRawX(event, index);
                float y = getRawY(event, index);
                mView.setParam(mParam.doMove(getContext(), x - mLeft.x, y - mLeft.y));
            }
        }
        if (action == MotionEvent.ACTION_UP || masked == MotionEvent.ACTION_POINTER_UP) {
            int id = event.getPointerId(event.getActionIndex());
            float x = getRawX(event, event.getActionIndex());
            float y = getRawY(event, event.getActionIndex());
            if (mLeft.id == id) {
                mLeft.id = -1;
                mView.setParam(mParam.doMove(getContext(), 0, 0));
            }
            if (mRight.id == id) {
                mRight.id = -1;
                long dt = AnimationUtils.currentAnimationTimeMillis() - mRight.t;
                if (dt > 0 && dt < 500)
                    mView.setParam(mParam.doRotate(getContext(), (x - mRight.x) / dt, (y - mRight.y) / dt));
            }
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            mLeft.id = -1;
            mView.setParam(mParam.doMove(getContext(), 0, 0));
            mRight.id = -1;
        }
        return true;
    }

    private static float getRawX(MotionEvent event, int index) {
        return event.getRawX() - event.getX() + event.getX(index);
    }

    private static float getRawY(MotionEvent event, int index) {
        return event.getRawY() - event.getY() + event.getY(index);
    }

    private static class Pointer {
        public int id = -1; //触控点索引
        public float x = 0; //落点横坐标
        public float y = 0; //落点纵坐标
        public long t = 0; //落点时间戳
    }
}
