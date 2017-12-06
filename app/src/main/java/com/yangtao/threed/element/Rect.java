package com.yangtao.threed.element;

import android.graphics.Color;

import com.yangtao.engine.Point;
import com.yangtao.engine.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * 长方形
 */
public class Rect extends Body {
    public float mLengthX; //x轴边长
    public float mLengthY; //y轴边长
    private List<Point> mPoints = new ArrayList<>(); //点集
    private List<MySurface> mSurfaces = new ArrayList<>(); //面集

    public Rect(float x, float y) {
        mLengthX = Math.max(x, 0);
        mLengthY = Math.max(y, 0);
        mPoints.add(new Point());
        mPoints.add(new Point());
        mPoints.add(new Point());
        mPoints.add(new Point());
        mSurfaces.add(new MySurface(mPoints.get(2), mPoints.get(0), mPoints.get(1)));
        mSurfaces.add(new MySurface(mPoints.get(0), mPoints.get(2), mPoints.get(3)));
    }

    /**
     * 设置颜色
     *
     * @param t
     * @param l
     * @return
     */
    public Rect setColor(int t, int l) {
        for (MySurface surface : mSurfaces) surface.setColor(t, l);
        return this;
    }

    @Override
    public Object getChildAt(int index) {
        if (index < 0) {
            mPoints.get(0).set(-mLengthX / 2, mLengthY / 2, 0);
            mPoints.get(1).set(-mLengthX / 2, -mLengthY / 2, 0);
            mPoints.get(2).set(mLengthX / 2, -mLengthY / 2, 0);
            mPoints.get(3).set(mLengthX / 2, mLengthY / 2, 0);
            for (Point point : mPoints) doTransform(point);
        }
        return Tools.getChildAt(mSurfaces, index);
    }

    private static class MySurface extends Surface {
        private int textureColor = Surface.TRANSPARENT; //面色
        private int lineColor = Surface.TRANSPARENT; //线色

        public MySurface(Point a, Point b, Point c) {
            super(a, b, c);
            exactDistance = true;
        }

        public void setColor(int t, int l) {
            textureColor = t;
            textureType = (textureColor == Color.TRANSPARENT) ? Surface.TEXTURE_TYPE_NULL : Surface.TEXTURE_TYPE_UNIQUE;
            lineColor = l;
            lineType = (lineColor == Color.TRANSPARENT) ? Surface.toLineType(false, false, false) : Surface.toLineType(false, true, true);
        }

        @Override
        public int getTextureColor(float x, float y) {
            return textureColor;
        }

        @Override
        public int getLineColor() {
            return lineColor;
        }
    }
}
