package com.yangtao.threed.extend;

import com.yangtao.threed.element.Body;
import com.yangtao.threed.element.Rect;

/**
 * 地板
 */
public class Floor extends Body {
    private int xSize; //列数
    private int ySize; //行数
    private int[] mColors; //颜色集
    private Rect mRect; //地板块

    public Floor(int size, float length, int... colors) {
        this(size, size, length, length, colors);
    }

    public Floor(int xSize, int ySize, float xLength, float yLength, int... colors) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.mColors = colors;
        this.mRect = new Rect(xLength, yLength);
    }

    @Override
    public Object getChildAt(int index) {
        if (mColors.length > 0 && index >= 0 && index < xSize * ySize) {
            mRect.setColor(mColors[(2 * index) % mColors.length], mColors[(2 * index + 1) % mColors.length]);
            mRect.setTranslate((2 * (index % xSize) + 1 - xSize) * mRect.mLengthX / 2, (2 * (index / xSize) + 1 - ySize) * mRect.mLengthY / 2, 0);
            return mRect;
        }
        return null;
    }
}
