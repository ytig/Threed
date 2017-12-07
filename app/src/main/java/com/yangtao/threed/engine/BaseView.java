package com.yangtao.threed.engine;

import android.content.Context;
import android.view.View;

/**
 * 基础视图
 */
public abstract class BaseView<Param> extends View {
    public static final int STATE_TODO = 0; //待开始
    public static final int STATE_DOING = 1; //在运行
    public static final int STATE_UNDO = 2; //被挂起
    public static final int STATE_DONE = 3; //已结束

    public BaseView(Context context) {
        super(context);
    }

    public abstract int getState(); //获取状态

    public abstract void doCreate(); //创建

    public abstract void doResume(); //恢复

    public abstract void doPause(); //暂停

    public abstract void doDestroy(); //销毁

    public abstract void setParam(Mutex.DataHandler<Param> handler); //设置参数
}
