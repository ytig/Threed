package com.yangtao.threed.engine;

import android.view.animation.AnimationUtils;

import com.yangtao.engine.Camera;
import com.yangtao.engine.Surfaces;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理内核
 *
 * @param <Param>
 */
public abstract class Core<Param> {
    private long mTime; //计时
    List<Surfaces> mScene; //场景

    public Core() {
        mTime = Long.MAX_VALUE;
        mScene = new ArrayList<>();
    }

    protected void addSurfaces(Surfaces surfaces) { //添加物体
        if (surfaces == null) return;
        if (mScene.contains(surfaces)) return;
        mScene.add(surfaces);
    }

    protected void addSurfaces(List objects) { //添加列表
        if (objects == null) return;
        for (Object object : objects) {
            if (object instanceof Surfaces) addSurfaces((Surfaces) object);
        }
    }

    protected void addSurfaces() { //添加成员变量
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    if (field != null) {
                        field.setAccessible(true);
                        Object object = field.get(this);
                        if (object instanceof Surfaces) addSurfaces((Surfaces) object);
                        else if (object instanceof List) addSurfaces((List) object);
                    }
                }
            }
            fields = this.getClass().getFields();
            if (fields != null) {
                for (Field field : fields) {
                    if (field != null) {
                        Object object = field.get(this);
                        if (object instanceof Surfaces) addSurfaces((Surfaces) object);
                        else if (object instanceof List) addSurfaces((List) object);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    void doCompute(Camera.Lens lens, Param param) { //发起计算
        long time = AnimationUtils.currentAnimationTimeMillis();
        doCompute(time > mTime ? (time - mTime) : 0, lens, param);
        mTime = time;
    }

    protected abstract void doCompute(long ms, Camera.Lens lens, Param param); //处理计算
}
