package com.yangtao.threed;

import com.yangtao.engine.Point;
import com.yangtao.engine.Surfaces;

/**
 * 物体
 */
public abstract class Body implements Surfaces {
    protected float mTranslateX = 0; //x轴位移(-∞,+∞)
    protected float mTranslateY = 0; //y轴位移(-∞,+∞)
    protected float mTranslateZ = 0; //z轴位移(-∞,+∞)
    protected float mScale = 1; //缩放比例[0,+∞)
    protected float mRotateH = 0; //水平角度[0,360)
    protected float mRotateV = 0; //垂直角度[-90,90]

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
     * @param scale
     * @return
     */
    public Body setScale(float scale) {
        mScale = scale;
        return this;
    }

    /**
     * 旋转
     *
     * @param horizontal
     * @param vertical
     * @return
     */
    public Body setRotate(float horizontal, float vertical) {
        mRotateH = horizontal;
        while (mRotateH < 0) mRotateH += 360;
        while (mRotateH >= 360) mRotateH -= 360;
        mRotateV = vertical;
        while (mRotateV < 0) mRotateV += 360;
        while (mRotateV >= 360) mRotateV -= 360;
        return this;
    }

    protected void transform(Point point) {
        point.x *= mScale;
        point.y *= mScale;
        point.z *= mScale;
        float x, y, z;
        y = (float) (point.y * Math.cos(mRotateV * Math.PI / 180) - point.z * Math.sin(mRotateV * Math.PI / 180));
        z = (float) (point.y * Math.sin(mRotateV * Math.PI / 180) + point.z * Math.cos(mRotateV * Math.PI / 180));
        point.y = y;
        point.z = z;
        x = (float) (point.x * Math.cos(mRotateH * Math.PI / 180) - point.y * Math.sin(mRotateH * Math.PI / 180));
        y = (float) (point.x * Math.sin(mRotateH * Math.PI / 180) + point.y * Math.cos(mRotateH * Math.PI / 180));
        point.x = x;
        point.y = y;
        point.x += mTranslateX;
        point.y += mTranslateY;
        point.z += mTranslateZ;
    }
}
