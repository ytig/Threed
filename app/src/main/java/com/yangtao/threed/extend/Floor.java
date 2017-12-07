package com.yangtao.threed.extend;

import com.yangtao.threed.element.Body;
import com.yangtao.threed.element.Rect;

/**
 * 地板
 */
public class Floor extends Body {
    private int mSize;
    private Integer[] mColors;
    private Rect mRect;

    public Floor(int size, float length, Integer... colors) {
        mSize = size;
        mColors = colors;
        mRect = new Rect(length, length);
    }

    @Override
    public Object getChildAt(int index) {
        if (mColors.length > 0 && index >= 0 && index < mSize * mSize) {
            int i = index / mSize;
            int j = index % mSize;
            mRect.setColor(mColors[(2 * (i * mSize + j)) % mColors.length], mColors[(2 * (i * mSize + j) + 1) % mColors.length]);
            mRect.setTranslate((2 * i + 1 - mSize) * mRect.mLengthX / 2, (2 * j + 1 - mSize) * mRect.mLengthY / 2, 0);
            return mRect;
        }
        return null;
    }
}
