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
        mRotateV += vertical;
        if (mRotateV <= -90) mRotateV = -90;
        if (mRotateV >= 90) mRotateV = 90;
        return this;
    }

    protected void transform(Point point) {
        //todo
        point.x += mTranslateX;
        point.y += mTranslateY;
        point.z += mTranslateZ;
    }
}
