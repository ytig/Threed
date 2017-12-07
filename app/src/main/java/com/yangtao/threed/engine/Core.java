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

    /**
     * 添加物体
     *
     * @param surfaces
     * @return
     */
    public Core<Param> addSurfaces(Surfaces surfaces) {
        if (surfaces != null) {
            if (!mScene.contains(surfaces)) mScene.add(surfaces);
        }
        return this;
    }

    /**
     * 添加列表
     *
     * @param objects
     * @return
     */
    public Core<Param> addSurfaces(List objects) {
        if (objects != null) {
            for (Object object : objects) {
                if (object instanceof Surfaces) addSurfaces((Surfaces) object);
            }
        }
        return this;
    }

    /**
     * 添加成员变量
     *
     * @return
     */
    public Core<Param> addSurfaces() {
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
        return this;
    }

    void doCompute(Camera.Lens lens, Param param) { //发起计算
        long time = AnimationUtils.currentAnimationTimeMillis();
        doCompute(time > mTime ? (time - mTime) : 0, lens, param);
        mTime = time;
    }

    protected abstract void doCompute(long ms, Camera.Lens lens, Param param); //处理计算
}
