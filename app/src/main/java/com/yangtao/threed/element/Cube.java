package com.yangtao.threed.element;

import android.graphics.Color;

import com.yangtao.engine.Point;
import com.yangtao.engine.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * 立方体
 */
@Deprecated
public class Cube extends Body {
    public static final float UNIT = 1f;

    private int mLineColor = Surface.TRANSPARENT; //线色
    private int mTextureColor = Surface.TRANSPARENT; //面色
    private List<Point> mPoints = new ArrayList<>(); //点集
    private List<Surface> mSurfaces = new ArrayList<>(); //面集

    public Cube() {
        mPoints.add(new Point());
        mPoints.add(new Point());
        mPoints.add(new Point());
        mPoints.add(new Point());
        mPoints.add(new Point());
        mPoints.add(new Point());
        mPoints.add(new Point());
        mPoints.add(new Point());
        mSurfaces.add(new MySurface(mPoints.get(2), mPoints.get(0), mPoints.get(1)));
        mSurfaces.add(new MySurface(mPoints.get(0), mPoints.get(2), mPoints.get(3)));
        mSurfaces.add(new MySurface(mPoints.get(3), mPoints.get(4), mPoints.get(0)));
        mSurfaces.add(new MySurface(mPoints.get(4), mPoints.get(3), mPoints.get(7)));
        mSurfaces.add(new MySurface(mPoints.get(2), mPoints.get(7), mPoints.get(3)));
        mSurfaces.add(new MySurface(mPoints.get(7), mPoints.get(2), mPoints.get(6)));
        mSurfaces.add(new MySurface(mPoints.get(1), mPoints.get(6), mPoints.get(2)));
        mSurfaces.add(new MySurface(mPoints.get(6), mPoints.get(1), mPoints.get(5)));
        mSurfaces.add(new MySurface(mPoints.get(0), mPoints.get(5), mPoints.get(1)));
        mSurfaces.add(new MySurface(mPoints.get(5), mPoints.get(0), mPoints.get(4)));
        mSurfaces.add(new MySurface(mPoints.get(7), mPoints.get(5), mPoints.get(4)));
        mSurfaces.add(new MySurface(mPoints.get(5), mPoints.get(7), mPoints.get(6)));
    }

    /**
     * 设置颜色
     *
     * @param textureColor
     * @param lineColor
     * @return
     */
    public Cube setColor(int textureColor, int lineColor) {
        mTextureColor = textureColor;
        mLineColor = lineColor;
        for (Surface surface : mSurfaces) {
            surface.textureType = (mTextureColor == Color.TRANSPARENT) ? Surface.TEXTURE_TYPE_NULL : Surface.TEXTURE_TYPE_UNIQUE;
            surface.lineType = (mLineColor == Color.TRANSPARENT) ? Surface.toLineType(false, false, false) : Surface.toLineType(false, true, true);
        }
        return this;
    }

    @Override
    public Object getChildAt(int index) {
        if (index == 0) {
            mPoints.get(0).set(-UNIT / 2, UNIT / 2, UNIT / 2);
            mPoints.get(1).set(-UNIT / 2, -UNIT / 2, UNIT / 2);
            mPoints.get(2).set(UNIT / 2, -UNIT / 2, UNIT / 2);
            mPoints.get(3).set(UNIT / 2, UNIT / 2, UNIT / 2);
            mPoints.get(4).set(-UNIT / 2, UNIT / 2, -UNIT / 2);
            mPoints.get(5).set(-UNIT / 2, -UNIT / 2, -UNIT / 2);
            mPoints.get(6).set(UNIT / 2, -UNIT / 2, -UNIT / 2);
            mPoints.get(7).set(UNIT / 2, UNIT / 2, -UNIT / 2);
            for (Point point : mPoints) doTransform(point);
        }
        return Tools.getChildAt(mSurfaces, index);
    }

    private class MySurface extends Surface {
        public MySurface(Point a, Point b, Point c) {
            super(a, b, c);
        }

        @Override
        public int getTextureColor(float x, float y) {
            return mTextureColor;
        }

        @Override
        public int getLineColor() {
            return mLineColor;
        }
    }
}
