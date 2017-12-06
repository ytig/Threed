package com.yangtao.threed.element;

import com.yangtao.engine.Point;
import com.yangtao.engine.Surfaces;

import java.util.List;

/**
 * 物体
 */
public abstract class Body implements Surfaces {
    protected Body mParent; //父容器
    public float mTranslateX = 0; //x轴位移(-∞,+∞)
    public float mTranslateY = 0; //y轴位移(-∞,+∞)
    public float mTranslateZ = 0; //z轴位移(-∞,+∞)
    public float mScaleX = 1; //x轴缩放[0,+∞)
    public float mScaleY = 1; //y轴缩放[0,+∞)
    public float mScaleZ = 1; //z轴缩放[0,+∞)
    public float mRotateX = 0; //x轴角度[0,360)
    public float mRotateY = 0; //y轴角度[0,360)
    public float mRotateZ = 0; //z轴角度[0,360)

    /**
     * 位移
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Body setTranslate(float x, float y, float z) {
        mTranslateX = x;
        mTranslateY = y;
        mTranslateZ = z;
        return this;
    }

    /**
     * 缩放
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Body setScale(float x, float y, float z) {
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        z = Math.max(z, 0);
        mScaleX = x;
        mScaleY = y;
        mScaleZ = z;
        return this;
    }

    /**
     * 旋转
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Body setRotate(float x, float y, float z) {
        while (x < 0) x += 360;
        while (x >= 360) x -= 360;
        while (y < 0) y += 360;
        while (y >= 360) y -= 360;
        while (z < 0) z += 360;
        while (z >= 360) z -= 360;
        mRotateX = x;
        mRotateY = y;
        mRotateZ = z;
        return this;
    }

    protected void setParent(Body parent) { //关联
        mParent = parent;
    }

    protected void setChildren(List<? extends Body> children) { //关联
        for (Body child : children) {
            if (child != null) child.setParent(this);
        }
    }

    protected void doTransform(Point point) { //变换
        point.x *= mScaleX;
        point.y *= mScaleY;
        point.z *= mScaleZ;
        float a, b;
        a = (float) (point.y * Math.cos(mRotateX * Math.PI / 180) - point.z * Math.sin(mRotateX * Math.PI / 180));
        b = (float) (point.y * Math.sin(mRotateX * Math.PI / 180) + point.z * Math.cos(mRotateX * Math.PI / 180));
        point.y = a;
        point.z = b;
        a = (float) (point.z * Math.cos(mRotateY * Math.PI / 180) - point.x * Math.sin(mRotateY * Math.PI / 180));
        b = (float) (point.z * Math.sin(mRotateY * Math.PI / 180) + point.x * Math.cos(mRotateY * Math.PI / 180));
        point.z = a;
        point.x = b;
        a = (float) (point.x * Math.cos(mRotateZ * Math.PI / 180) - point.y * Math.sin(mRotateZ * Math.PI / 180));
        b = (float) (point.x * Math.sin(mRotateZ * Math.PI / 180) + point.y * Math.cos(mRotateZ * Math.PI / 180));
        point.x = a;
        point.y = b;
        point.x += mTranslateX;
        point.y += mTranslateY;
        point.z += mTranslateZ;
        if (mParent != null) mParent.doTransform(point);
    }
}
