package com.yangtao.threed.element;

import java.util.ArrayList;
import java.util.List;

/**
 * 长方体
 */
public class Rects extends Body {
    public float mLengthX; //x轴边长
    public float mLengthY; //y轴边长
    public float mLengthZ; //z轴边长
    private List<Rect> mRects = new ArrayList<>(); //物集

    public Rects(float x, float y, float z) {
        mLengthX = Math.max(x, 0);
        mLengthY = Math.max(y, 0);
        mLengthZ = Math.max(z, 0);
        mRects.add(new Rect(mLengthX, mLengthY));
        mRects.add(new Rect(mLengthX, mLengthY));
        mRects.add(new Rect(mLengthX, mLengthZ));
        mRects.add(new Rect(mLengthX, mLengthZ));
        mRects.add(new Rect(mLengthY, mLengthZ));
        mRects.add(new Rect(mLengthY, mLengthZ));
        setChildren(mRects);
    }

    /**
     * 设置颜色
     *
     * @param t
     * @param l
     * @return
     */
    public Rects setColor(int t, int l) {
        for (Rect rect : mRects) rect.setColor(t, l);
        return this;
    }

    @Override
    public Object getChildAt(int index) {
        if (index < 0) {
            mRects.get(0).setTranslate(0, 0, mLengthZ / 2).setScale(1, 1, 1).setRotate(0, 0, 0);
            mRects.get(1).setTranslate(0, 0, -mLengthZ / 2).setScale(1, 1, 1).setRotate(180, 0, 0);
            mRects.get(2).setTranslate(0, mLengthY / 2, 0).setScale(1, 1, 1).setRotate(270, 0, 0);
            mRects.get(3).setTranslate(0, -mLengthY / 2, 0).setScale(1, 1, 1).setRotate(90, 0, 0);
            mRects.get(4).setTranslate(-mLengthX / 2, 0, 0).setScale(1, 1, 1).setRotate(270, 0, 90);
            mRects.get(5).setTranslate(mLengthX / 2, 0, 0).setScale(1, 1, 1).setRotate(270, 0, 270);
        }
        return Tools.getChildAt(mRects, index);
    }
}
