package com.yangtao.engine;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 相机
 */
public class Camera {
    public Lens mLens; //镜头
    public int mWidth; //画布宽度
    public int mHeight; //画布高度
    public int[] mCanvas; //画布色彩值
    private float[] mDistance; //画布点距
    private Recycler<Point> POINTS = new Recycler<Point>() {
        @Override
        protected Point newModel() {
            return new Point();
        }
    }; //点缓存池
    private Recycler<Vector> VECTORS = new Recycler<Vector>() {
        @Override
        protected Vector newModel() {
            return new Vector();
        }
    }; //线缓存池

    public Camera(int width, int height, float verticalAngle) {
        mLens = new Lens(width, height, verticalAngle);
        mWidth = width;
        mHeight = height;
        mCanvas = new int[mWidth * mHeight];
        mDistance = new float[mWidth * mHeight];
    }

    /**
     * 清空
     *
     * @return
     */
    public Camera clear() {
        Arrays.fill(mCanvas, Surface.TRANSPARENT); //无色
        Arrays.fill(mDistance, Float.MAX_VALUE); //最远距离
        return this;
    }

    /**
     * 绘制
     *
     * @param surfaces
     * @return
     */
    public Camera draw(Surfaces surfaces) {
        surfaces.getChildAt(-1);
        if (surfaces instanceof Surface) draw((Surface) surfaces);
        int index = 0;
        while (true) { //遍历绘制
            Object child = surfaces.getChildAt(index);
            if (child == null) break;
            if (child instanceof Surfaces) draw((Surfaces) child);
            else if (child instanceof Surface) draw((Surface) child);
            index++;
        }
        return this;
    }

    private void draw(Surface surface) { //绘制面
        if (!mLens.map(surface)) return;
        if (!surface.doubleFace) {
            if (mLens.mapDirection < 0) { //顺时针成像
                drawTexture(surface);
                drawLine(surface);
            }
        } else {
            if (mLens.mapDirection != 0) drawTexture(surface); //不重合、不共线
            drawLine(surface);
        }
    }

    private void drawTexture(Surface surface) { //绘制纹理
        Point _a = mLens.mapA, _b = mLens.mapB, _c = mLens.mapC, p; //先按y值正序冒泡
        if (_a.y > _b.y) {
            p = _a;
            _a = _b;
            _b = p;
        }
        if (_b.y > _c.y) {
            p = _b;
            _b = _c;
            _c = p;
            if (_a.y > _b.y) {
                p = _a;
                _a = _b;
                _b = p;
            }
        }
        if (_c.y - _a.y < 1) drawTexture(surface, _a, _b, _c); //暴力遍历，防止浮点数溢出
        else {
            if (_a.y == _b.y) { //平顶三角形
                if (_a.x < _b.x) drawTexture(surface, _a, _b, _c, true);
                else drawTexture(surface, _b, _a, _c, true);
            } else {
                if (_b.y == _c.y) { //平底三角形
                    if (_b.x < _c.x) drawTexture(surface, _a, _b, _c, false);
                    else drawTexture(surface, _a, _c, _b, false);
                } else { //非平三角形
                    p = POINTS.obtain().set((_a.x * _b.y + _c.x * _a.y - _c.x * _b.y - _a.x * _c.y) / (_a.y - _c.y), _b.y, 0);
                    if (_b.x < p.x) {
                        if (_b.y - _a.y < 1) drawTexture(surface, _a, _b, p); //暴力遍历，防止浮点数溢出
                        else drawTexture(surface, _a, _b, p, false);
                        if (_c.y - _b.y < 1) drawTexture(surface, _b, p, _c); //暴力遍历，防止浮点数溢出
                        else drawTexture(surface, _b, p, _c, true);
                    } else {
                        if (_b.y - _a.y < 1) drawTexture(surface, _a, p, _b); //暴力遍历，防止浮点数溢出
                        else drawTexture(surface, _a, p, _b, false);
                        if (_c.y - _b.y < 1) drawTexture(surface, p, _b, _c); //暴力遍历，防止浮点数溢出
                        else drawTexture(surface, p, _b, _c, true);
                    }
                    POINTS.recycle(p);
                }
            }
        }
    }

    private void drawTexture(Surface surface, Point _a, Point _b, Point _c) {
        int x, y, startX, endX, startY, endY;
        float minX, maxX, minY, maxY, direction;
        minX = _a.x;
        if (minX > _b.x) minX = _b.x;
        if (minX > _c.x) minX = _c.x;
        maxX = _a.x;
        if (maxX < _b.x) maxX = _b.x;
        if (maxX < _c.x) maxX = _c.x;
        minY = _a.y;
        if (minY > _b.y) minY = _b.y;
        if (minY > _c.y) minY = _c.y;
        maxY = _a.y;
        if (maxY < _b.y) maxY = _b.y;
        if (maxY < _c.y) maxY = _c.y;
        if (minX < 0) startX = 0;
        else {
            startX = (int) minX;
            if (startX < minX) startX++;
        }
        if (maxX < 0) return;
        else {
            endX = (int) maxX;
            if (endX > mWidth - 1) endX = mWidth - 1;
        }
        if (minY < 0) startY = 0;
        else {
            startY = (int) minY;
            if (startY < minY) startY++;
        }
        if (maxY < 0) return;
        else {
            endY = (int) maxY;
            if (endY > mHeight - 1) endY = mHeight - 1;
        }
        if (!surface.exactDistance) { //模糊测距
            float averageDistance = (surface.a.distanceTo(mLens.mPoint) + surface.b.distanceTo(mLens.mPoint) + surface.c.distanceTo(mLens.mPoint)) / 3;
            if (surface.textureType == Surface.TEXTURE_TYPE_UNIQUE) { //绘制纯色
                for (y = startY; y <= endY; y++) {
                    for (x = startX; x <= endX; x++) {
                        direction = (mLens.mapB.x - x) * (y - mLens.mapC.y) - (mLens.mapB.y - y) * (x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (x - mLens.mapA.x) * (mLens.mapA.y - mLens.mapC.y) - (y - mLens.mapA.y) * (mLens.mapA.x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapA.y - y) - (mLens.mapB.y - mLens.mapA.y) * (mLens.mapA.x - x);
                        if (direction * mLens.mapDirection < 0) continue;
                        if (mDistance[x + y * mWidth] >= averageDistance) {
                            mDistance[x + y * mWidth] = averageDistance;
                            mCanvas[x + y * mWidth] = surface.getTextureColor(0, 0);
                        }
                    }
                }
                return;
            }
            if (surface.textureType == Surface.TEXTURE_TYPE_BLUR) { //绘制模糊纹理
                Vector ap = VECTORS.obtain();
                float c1, c2, c3, c4, c5, c6;
                c1 = mLens.mapC.y - mLens.mapA.y;
                c2 = mLens.mapC.x - mLens.mapA.x;
                c3 = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapC.y - mLens.mapA.y) - (mLens.mapC.x - mLens.mapA.x) * (mLens.mapB.y - mLens.mapA.y);
                c4 = mLens.mapB.y - mLens.mapA.y;
                c5 = mLens.mapB.x - mLens.mapA.x;
                c6 = (mLens.mapC.x - mLens.mapA.x) * (mLens.mapB.y - mLens.mapA.y) - (mLens.mapB.x - mLens.mapA.x) * (mLens.mapC.y - mLens.mapA.y);
                for (y = startY; y <= endY; y++) {
                    for (x = startX; x <= endX; x++) {
                        direction = (mLens.mapB.x - x) * (y - mLens.mapC.y) - (mLens.mapB.y - y) * (x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (x - mLens.mapA.x) * (mLens.mapA.y - mLens.mapC.y) - (y - mLens.mapA.y) * (mLens.mapA.x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapA.y - y) - (mLens.mapB.y - mLens.mapA.y) * (mLens.mapA.x - x);
                        if (direction * mLens.mapDirection < 0) continue;
                        if (mDistance[x + y * mWidth] >= averageDistance) {
                            mDistance[x + y * mWidth] = averageDistance;
                            ap.set(x - mLens.mapA.x, y - mLens.mapA.y, 0);
                            mCanvas[x + y * mWidth] = surface.getTextureColor((ap.x * c1 - ap.y * c2) / c3, (ap.x * c4 - ap.y * c5) / c6);
                        }
                    }
                }
                VECTORS.recycle(ap);
                return;
            }
            if (surface.textureType == Surface.TEXTURE_TYPE_EXACT) { //绘制精确纹理
                Vector ab = VECTORS.obtain().set(surface.a, surface.b);
                Vector ap = VECTORS.obtain();
                float c1, c2, c3, c4, c5, c6;
                c1 = mLens.mPoint.x - surface.a.x;
                c2 = mLens.mPoint.y - surface.a.y;
                c3 = mLens.mPoint.z - surface.a.z;
                c4 = ab.getLength();
                for (y = startY; y <= endY; y++) {
                    for (x = startX; x <= endX; x++) {
                        direction = (mLens.mapB.x - x) * (y - mLens.mapC.y) - (mLens.mapB.y - y) * (x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (x - mLens.mapA.x) * (mLens.mapA.y - mLens.mapC.y) - (y - mLens.mapA.y) * (mLens.mapA.x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapA.y - y) - (mLens.mapB.y - mLens.mapA.y) * (mLens.mapA.x - x);
                        if (direction * mLens.mapDirection < 0) continue;
                        if (!mLens.remap(x, y)) continue;
                        if (mDistance[x + y * mWidth] >= averageDistance) {
                            mDistance[x + y * mWidth] = averageDistance;
                            mLens.remapVector.setLength(mLens.remapDistance); //同向伸缩（不考虑反向排异）
                            ap.set(mLens.remapVector.x + c1, mLens.remapVector.y + c2, mLens.remapVector.z + c3);
                            c5 = ap.getLength();
                            c6 = ab.multiplyBy(ap);
                            mCanvas[x + y * mWidth] = surface.getTextureColor(c6 / (c4 * c4), (float) (Math.sqrt(c5 * c5 - c6 * c6 / (c4 * c4)) / c4));
                        }
                    }
                }
                VECTORS.recycle(ab);
                VECTORS.recycle(ap);
                return;
            }
        } else { //精确测距
            if (surface.textureType == Surface.TEXTURE_TYPE_UNIQUE) { //绘制纯色
                for (y = startY; y <= endY; y++) {
                    for (x = startX; x <= endX; x++) {
                        direction = (mLens.mapB.x - x) * (y - mLens.mapC.y) - (mLens.mapB.y - y) * (x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (x - mLens.mapA.x) * (mLens.mapA.y - mLens.mapC.y) - (y - mLens.mapA.y) * (mLens.mapA.x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapA.y - y) - (mLens.mapB.y - mLens.mapA.y) * (mLens.mapA.x - x);
                        if (direction * mLens.mapDirection < 0) continue;
                        if (!mLens.remap(x, y)) continue;
                        if (mDistance[x + y * mWidth] >= mLens.remapDistance) {
                            mDistance[x + y * mWidth] = mLens.remapDistance;
                            mCanvas[x + y * mWidth] = surface.getTextureColor(0, 0);
                        }
                    }
                }
                return;
            }
            if (surface.textureType == Surface.TEXTURE_TYPE_BLUR) { //绘制模糊纹理
                Vector ap = VECTORS.obtain();
                float c1, c2, c3, c4, c5, c6;
                c1 = mLens.mapC.y - mLens.mapA.y;
                c2 = mLens.mapC.x - mLens.mapA.x;
                c3 = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapC.y - mLens.mapA.y) - (mLens.mapC.x - mLens.mapA.x) * (mLens.mapB.y - mLens.mapA.y);
                c4 = mLens.mapB.y - mLens.mapA.y;
                c5 = mLens.mapB.x - mLens.mapA.x;
                c6 = (mLens.mapC.x - mLens.mapA.x) * (mLens.mapB.y - mLens.mapA.y) - (mLens.mapB.x - mLens.mapA.x) * (mLens.mapC.y - mLens.mapA.y);
                for (y = startY; y <= endY; y++) {
                    for (x = startX; x <= endX; x++) {
                        direction = (mLens.mapB.x - x) * (y - mLens.mapC.y) - (mLens.mapB.y - y) * (x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (x - mLens.mapA.x) * (mLens.mapA.y - mLens.mapC.y) - (y - mLens.mapA.y) * (mLens.mapA.x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapA.y - y) - (mLens.mapB.y - mLens.mapA.y) * (mLens.mapA.x - x);
                        if (direction * mLens.mapDirection < 0) continue;
                        if (!mLens.remap(x, y)) continue;
                        if (mDistance[x + y * mWidth] >= mLens.remapDistance) {
                            mDistance[x + y * mWidth] = mLens.remapDistance;
                            ap.set(x - mLens.mapA.x, y - mLens.mapA.y, 0);
                            mCanvas[x + y * mWidth] = surface.getTextureColor((ap.x * c1 - ap.y * c2) / c3, (ap.x * c4 - ap.y * c5) / c6);
                        }
                    }
                }
                VECTORS.recycle(ap);
                return;
            }
            if (surface.textureType == Surface.TEXTURE_TYPE_EXACT) { //绘制精确纹理
                Vector ab = VECTORS.obtain().set(surface.a, surface.b);
                Vector ap = VECTORS.obtain();
                float c1, c2, c3, c4, c5, c6;
                c1 = mLens.mPoint.x - surface.a.x;
                c2 = mLens.mPoint.y - surface.a.y;
                c3 = mLens.mPoint.z - surface.a.z;
                c4 = ab.getLength();
                for (y = startY; y <= endY; y++) {
                    for (x = startX; x <= endX; x++) {
                        direction = (mLens.mapB.x - x) * (y - mLens.mapC.y) - (mLens.mapB.y - y) * (x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (x - mLens.mapA.x) * (mLens.mapA.y - mLens.mapC.y) - (y - mLens.mapA.y) * (mLens.mapA.x - mLens.mapC.x);
                        if (direction * mLens.mapDirection < 0) continue;
                        direction = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapA.y - y) - (mLens.mapB.y - mLens.mapA.y) * (mLens.mapA.x - x);
                        if (direction * mLens.mapDirection < 0) continue;
                        if (!mLens.remap(x, y)) continue;
                        if (mDistance[x + y * mWidth] >= mLens.remapDistance) {
                            mDistance[x + y * mWidth] = mLens.remapDistance;
                            mLens.remapVector.setLength(mLens.remapDistance); //同向伸缩（不考虑反向排异）
                            ap.set(mLens.remapVector.x + c1, mLens.remapVector.y + c2, mLens.remapVector.z + c3);
                            c5 = ap.getLength();
                            c6 = ab.multiplyBy(ap);
                            mCanvas[x + y * mWidth] = surface.getTextureColor(c6 / (c4 * c4), (float) (Math.sqrt(c5 * c5 - c6 * c6 / (c4 * c4)) / c4));
                        }
                    }
                }
                VECTORS.recycle(ab);
                VECTORS.recycle(ap);
                return;
            }
        }
    }

    private void drawTexture(Surface surface, Point _a, Point _b, Point _c, boolean desc) {
        int x, y;
        float k1, b1, k2, b2, startX, endX, startY, endY;
        if (!surface.exactDistance) { //模糊测距
            float averageDistance = (surface.a.distanceTo(mLens.mPoint) + surface.b.distanceTo(mLens.mPoint) + surface.c.distanceTo(mLens.mPoint)) / 3;
            if (surface.textureType == Surface.TEXTURE_TYPE_UNIQUE) { //绘制纯色
                if (!desc) {
                    k1 = (_a.x - _b.x) / (_a.y - _b.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_a.x - _c.x) / (_a.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                } else {
                    k1 = (_a.x - _c.x) / (_a.y - _c.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_b.x - _c.x) / (_b.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                }
                startY = _a.y;
                endY = _c.y;
                if (startY < 0) startY = 0;
                if (endY > mHeight - 1) endY = mHeight - 1;
                for (y = (int) endY; y >= startY; y--) {
                    startX = k1 * y + b1;
                    endX = k2 * y + b2;
                    if (startX < 0) startX = 0;
                    if (endX > mWidth - 1) endX = mWidth - 1;
                    for (x = (int) endX; x >= startX; x--) {
                        if (mDistance[x + y * mWidth] >= averageDistance) {
                            mDistance[x + y * mWidth] = averageDistance;
                            mCanvas[x + y * mWidth] = surface.getTextureColor(0, 0);
                        }
                    }
                }
                return;
            }
            if (surface.textureType == Surface.TEXTURE_TYPE_BLUR) { //绘制模糊纹理
                Vector ap = VECTORS.obtain();
                float c1, c2, c3, c4, c5, c6;
                c1 = mLens.mapC.y - mLens.mapA.y;
                c2 = mLens.mapC.x - mLens.mapA.x;
                c3 = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapC.y - mLens.mapA.y) - (mLens.mapC.x - mLens.mapA.x) * (mLens.mapB.y - mLens.mapA.y);
                c4 = mLens.mapB.y - mLens.mapA.y;
                c5 = mLens.mapB.x - mLens.mapA.x;
                c6 = (mLens.mapC.x - mLens.mapA.x) * (mLens.mapB.y - mLens.mapA.y) - (mLens.mapB.x - mLens.mapA.x) * (mLens.mapC.y - mLens.mapA.y);
                if (!desc) {
                    k1 = (_a.x - _b.x) / (_a.y - _b.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_a.x - _c.x) / (_a.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                } else {
                    k1 = (_a.x - _c.x) / (_a.y - _c.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_b.x - _c.x) / (_b.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                }
                startY = _a.y;
                endY = _c.y;
                if (startY < 0) startY = 0;
                if (endY > mHeight - 1) endY = mHeight - 1;
                for (y = (int) endY; y >= startY; y--) {
                    startX = k1 * y + b1;
                    endX = k2 * y + b2;
                    if (startX < 0) startX = 0;
                    if (endX > mWidth - 1) endX = mWidth - 1;
                    for (x = (int) endX; x >= startX; x--) {
                        if (mDistance[x + y * mWidth] >= averageDistance) {
                            mDistance[x + y * mWidth] = averageDistance;
                            ap.set(x - mLens.mapA.x, y - mLens.mapA.y, 0);
                            mCanvas[x + y * mWidth] = surface.getTextureColor((ap.x * c1 - ap.y * c2) / c3, (ap.x * c4 - ap.y * c5) / c6);
                        }
                    }
                }
                VECTORS.recycle(ap);
                return;
            }
            if (surface.textureType == Surface.TEXTURE_TYPE_EXACT) { //绘制精确纹理
                Vector ab = VECTORS.obtain().set(surface.a, surface.b);
                Vector ap = VECTORS.obtain();
                float c1, c2, c3, c4, c5, c6;
                c1 = mLens.mPoint.x - surface.a.x;
                c2 = mLens.mPoint.y - surface.a.y;
                c3 = mLens.mPoint.z - surface.a.z;
                c4 = ab.getLength();
                if (!desc) {
                    k1 = (_a.x - _b.x) / (_a.y - _b.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_a.x - _c.x) / (_a.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                } else {
                    k1 = (_a.x - _c.x) / (_a.y - _c.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_b.x - _c.x) / (_b.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                }
                startY = _a.y;
                endY = _c.y;
                if (startY < 0) startY = 0;
                if (endY > mHeight - 1) endY = mHeight - 1;
                for (y = (int) endY; y >= startY; y--) {
                    startX = k1 * y + b1;
                    endX = k2 * y + b2;
                    if (startX < 0) startX = 0;
                    if (endX > mWidth - 1) endX = mWidth - 1;
                    for (x = (int) endX; x >= startX; x--) {
                        if (!mLens.remap(x, y)) continue;
                        if (mDistance[x + y * mWidth] >= averageDistance) {
                            mDistance[x + y * mWidth] = averageDistance;
                            mLens.remapVector.setLength(mLens.remapDistance); //同向伸缩（不考虑反向排异）
                            ap.set(mLens.remapVector.x + c1, mLens.remapVector.y + c2, mLens.remapVector.z + c3);
                            c5 = ap.getLength();
                            c6 = ab.multiplyBy(ap);
                            mCanvas[x + y * mWidth] = surface.getTextureColor(c6 / (c4 * c4), (float) (Math.sqrt(c5 * c5 - c6 * c6 / (c4 * c4)) / c4));
                        }
                    }
                }
                VECTORS.recycle(ab);
                VECTORS.recycle(ap);
                return;
            }
        } else { //精确测距
            if (surface.textureType == Surface.TEXTURE_TYPE_UNIQUE) { //绘制纯色
                if (!desc) {
                    k1 = (_a.x - _b.x) / (_a.y - _b.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_a.x - _c.x) / (_a.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                } else {
                    k1 = (_a.x - _c.x) / (_a.y - _c.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_b.x - _c.x) / (_b.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                }
                startY = _a.y;
                endY = _c.y;
                if (startY < 0) startY = 0;
                if (endY > mHeight - 1) endY = mHeight - 1;
                for (y = (int) endY; y >= startY; y--) {
                    startX = k1 * y + b1;
                    endX = k2 * y + b2;
                    if (startX < 0) startX = 0;
                    if (endX > mWidth - 1) endX = mWidth - 1;
                    for (x = (int) endX; x >= startX; x--) {
                        if (!mLens.remap(x, y)) continue;
                        if (mDistance[x + y * mWidth] >= mLens.remapDistance) {
                            mDistance[x + y * mWidth] = mLens.remapDistance;
                            mCanvas[x + y * mWidth] = surface.getTextureColor(0, 0);
                        }
                    }
                }
                return;
            }
            if (surface.textureType == Surface.TEXTURE_TYPE_BLUR) { //绘制模糊纹理
                Vector ap = VECTORS.obtain();
                float c1, c2, c3, c4, c5, c6;
                c1 = mLens.mapC.y - mLens.mapA.y;
                c2 = mLens.mapC.x - mLens.mapA.x;
                c3 = (mLens.mapB.x - mLens.mapA.x) * (mLens.mapC.y - mLens.mapA.y) - (mLens.mapC.x - mLens.mapA.x) * (mLens.mapB.y - mLens.mapA.y);
                c4 = mLens.mapB.y - mLens.mapA.y;
                c5 = mLens.mapB.x - mLens.mapA.x;
                c6 = (mLens.mapC.x - mLens.mapA.x) * (mLens.mapB.y - mLens.mapA.y) - (mLens.mapB.x - mLens.mapA.x) * (mLens.mapC.y - mLens.mapA.y);
                if (!desc) {
                    k1 = (_a.x - _b.x) / (_a.y - _b.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_a.x - _c.x) / (_a.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                } else {
                    k1 = (_a.x - _c.x) / (_a.y - _c.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_b.x - _c.x) / (_b.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                }
                startY = _a.y;
                endY = _c.y;
                if (startY < 0) startY = 0;
                if (endY > mHeight - 1) endY = mHeight - 1;
                for (y = (int) endY; y >= startY; y--) {
                    startX = k1 * y + b1;
                    endX = k2 * y + b2;
                    if (startX < 0) startX = 0;
                    if (endX > mWidth - 1) endX = mWidth - 1;
                    for (x = (int) endX; x >= startX; x--) {
                        if (!mLens.remap(x, y)) continue;
                        if (mDistance[x + y * mWidth] >= mLens.remapDistance) {
                            mDistance[x + y * mWidth] = mLens.remapDistance;
                            ap.set(x - mLens.mapA.x, y - mLens.mapA.y, 0);
                            mCanvas[x + y * mWidth] = surface.getTextureColor((ap.x * c1 - ap.y * c2) / c3, (ap.x * c4 - ap.y * c5) / c6);
                        }
                    }
                }
                VECTORS.recycle(ap);
                return;
            }
            if (surface.textureType == Surface.TEXTURE_TYPE_EXACT) { //绘制精确纹理
                Vector ab = VECTORS.obtain().set(surface.a, surface.b);
                Vector ap = VECTORS.obtain();
                float c1, c2, c3, c4, c5, c6;
                c1 = mLens.mPoint.x - surface.a.x;
                c2 = mLens.mPoint.y - surface.a.y;
                c3 = mLens.mPoint.z - surface.a.z;
                c4 = ab.getLength();
                if (!desc) {
                    k1 = (_a.x - _b.x) / (_a.y - _b.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_a.x - _c.x) / (_a.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                } else {
                    k1 = (_a.x - _c.x) / (_a.y - _c.y);
                    b1 = _a.x - k1 * _a.y;
                    k2 = (_b.x - _c.x) / (_b.y - _c.y);
                    b2 = _c.x - k2 * _c.y;
                }
                startY = _a.y;
                endY = _c.y;
                if (startY < 0) startY = 0;
                if (endY > mHeight - 1) endY = mHeight - 1;
                for (y = (int) endY; y >= startY; y--) {
                    startX = k1 * y + b1;
                    endX = k2 * y + b2;
                    if (startX < 0) startX = 0;
                    if (endX > mWidth - 1) endX = mWidth - 1;
                    for (x = (int) endX; x >= startX; x--) {
                        if (!mLens.remap(x, y)) continue;
                        if (mDistance[x + y * mWidth] >= mLens.remapDistance) {
                            mDistance[x + y * mWidth] = mLens.remapDistance;
                            mLens.remapVector.setLength(mLens.remapDistance); //同向伸缩（不考虑反向排异）
                            ap.set(mLens.remapVector.x + c1, mLens.remapVector.y + c2, mLens.remapVector.z + c3);
                            c5 = ap.getLength();
                            c6 = ab.multiplyBy(ap);
                            mCanvas[x + y * mWidth] = surface.getTextureColor(c6 / (c4 * c4), (float) (Math.sqrt(c5 * c5 - c6 * c6 / (c4 * c4)) / c4));
                        }
                    }
                }
                VECTORS.recycle(ab);
                VECTORS.recycle(ap);
                return;
            }
        }
    }

    private void drawLine(Surface surface) { //绘制边线
        if (surface.lineType % 2 == 1)
            drawLine(surface, surface.c, surface.a, mLens.mapC, mLens.mapA); //绘制ca
        if ((surface.lineType / 2) % 2 == 1)
            drawLine(surface, surface.b, surface.c, mLens.mapB, mLens.mapC); //绘制bc
        if ((surface.lineType / 4) % 2 == 1)
            drawLine(surface, surface.a, surface.b, mLens.mapA, mLens.mapB); //绘制ab
    }

    private void drawLine(Surface surface, Point p1, Point p2, Point _p1, Point _p2) {
        float minX, maxX, minY, maxY;
        minX = _p1.x < _p2.x ? _p1.x : _p2.x;
        maxX = _p1.x > _p2.x ? _p1.x : _p2.x;
        minY = _p1.y < _p2.y ? _p1.y : _p2.y;
        maxY = _p1.y > _p2.y ? _p1.y : _p2.y;
        if (minX > mWidth - 1) return;
        if (maxX < 0) return;
        if (minY > mHeight - 1) return;
        if (maxY < 0) return;
        if (minX == maxX && minY == maxY) return; //映射点重合
        if (!surface.exactDistance) { //模糊测距
            float d = (surface.a.distanceTo(mLens.mPoint) + surface.b.distanceTo(mLens.mPoint) + surface.c.distanceTo(mLens.mPoint)) / 3;
            if (maxX - minX > maxY - minY) {
                int x, y, startX, endX, startY, endY;
                double k, b, y1, y2, y3;
                k = (_p2.y - _p1.y) / (_p2.x - _p1.x);
                b = (_p1.y - k * _p1.x + _p2.y - k * _p2.x) / 2;
                startX = Math.max((int) Math.floor(minX), 0);
                endX = Math.min((int) Math.ceil(maxX), mWidth - 1);
                for (x = startX; x <= endX; x++) { //过量遍历
                    if (k > 0) {
                        y1 = k * (x - 0.5) + b;
                        y2 = k * (x + 0.5) + b;
                        y3 = y2 + 0.5; //开区间排点
                    } else {
                        y1 = k * (x + 0.5) + b;
                        y2 = k * (x - 0.5) + b;
                        y3 = -1;
                    }
                    if (y1 <= maxY && y2 >= minY) {
                        startY = Math.max((int) Math.round(Math.max(y1, minY)), 0);
                        endY = Math.min((int) Math.round(Math.min(y2, maxY)), mHeight - 1);
                        for (y = startY; y <= endY; y++) {
                            if (y != y3) {
                                if (mDistance[x + y * mWidth] >= d) {
                                    mDistance[x + y * mWidth] = d;
                                    mCanvas[x + y * mWidth] = surface.getLineColor();
                                }
                            }
                        }
                    }
                }
            } else {
                int x, y, startX, endX, startY, endY;
                double k, b, x1, x2, x3;
                k = (_p2.x - _p1.x) / (_p2.y - _p1.y);
                b = (_p1.x - k * _p1.y + _p2.x - k * _p2.y) / 2;
                startY = Math.max((int) Math.floor(minY), 0);
                endY = Math.min((int) Math.ceil(maxY), mHeight - 1);
                for (y = startY; y <= endY; y++) { //过量遍历
                    if (k > 0) {
                        x1 = k * (y - 0.5) + b;
                        x2 = k * (y + 0.5) + b;
                        x3 = x2 + 0.5; //开区间排点
                    } else {
                        x1 = k * (y + 0.5) + b;
                        x2 = k * (y - 0.5) + b;
                        x3 = -1;
                    }
                    if (x1 <= maxX && x2 >= minX) {
                        startX = Math.max((int) Math.round(Math.max(x1, minX)), 0);
                        endX = Math.min((int) Math.round(Math.min(x2, maxX)), mWidth - 1);
                        for (x = startX; x <= endX; x++) {
                            if (x != x3) {
                                if (mDistance[x + y * mWidth] >= d) {
                                    mDistance[x + y * mWidth] = d;
                                    mCanvas[x + y * mWidth] = surface.getLineColor();
                                }
                            }
                        }
                    }
                }
            }
        } else { //精确测距
            Vector v1 = VECTORS.obtain().set(_p1, _p2);
            Vector v2 = VECTORS.obtain();
            float d1, d2, d3, d;
            d1 = p1.distanceTo(mLens.mPoint);
            d2 = p2.distanceTo(mLens.mPoint);
            d3 = v1.getLength();
            if (maxX - minX > maxY - minY) {
                int x, y, startX, endX, startY, endY;
                double k, b, y1, y2, y3;
                k = (_p2.y - _p1.y) / (_p2.x - _p1.x);
                b = (_p1.y - k * _p1.x + _p2.y - k * _p2.x) / 2;
                startX = Math.max((int) Math.floor(minX), 0);
                endX = Math.min((int) Math.ceil(maxX), mWidth - 1);
                for (x = startX; x <= endX; x++) { //过量遍历
                    if (k > 0) {
                        y1 = k * (x - 0.5) + b;
                        y2 = k * (x + 0.5) + b;
                        y3 = y2 + 0.5; //开区间排点
                    } else {
                        y1 = k * (x + 0.5) + b;
                        y2 = k * (x - 0.5) + b;
                        y3 = -1;
                    }
                    if (y1 <= maxY && y2 >= minY) {
                        startY = Math.max((int) Math.round(Math.max(y1, minY)), 0);
                        endY = Math.min((int) Math.round(Math.min(y2, maxY)), mHeight - 1);
                        for (y = startY; y <= endY; y++) {
                            if (y != y3) {
                                if (mLens.remap(x, y)) { //优先采用反映射测距
                                    if (mDistance[x + y * mWidth] >= mLens.remapDistance) {
                                        mDistance[x + y * mWidth] = mLens.remapDistance;
                                        mCanvas[x + y * mWidth] = surface.getLineColor();
                                    }
                                } else { //比例测距
                                    d = v1.multiplyBy(v2.set(x - _p1.x, y - _p1.y, 0)) / (d3 * d3);
                                    if (d < 0) d = 0;
                                    if (d > 1) d = 1;
                                    d = (1 - d) * d1 + d * d2;
                                    if (mDistance[x + y * mWidth] >= d) {
                                        mDistance[x + y * mWidth] = d;
                                        mCanvas[x + y * mWidth] = surface.getLineColor();
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                int x, y, startX, endX, startY, endY;
                double k, b, x1, x2, x3;
                k = (_p2.x - _p1.x) / (_p2.y - _p1.y);
                b = (_p1.x - k * _p1.y + _p2.x - k * _p2.y) / 2;
                startY = Math.max((int) Math.floor(minY), 0);
                endY = Math.min((int) Math.ceil(maxY), mHeight - 1);
                for (y = startY; y <= endY; y++) { //过量遍历
                    if (k > 0) {
                        x1 = k * (y - 0.5) + b;
                        x2 = k * (y + 0.5) + b;
                        x3 = x2 + 0.5; //开区间排点
                    } else {
                        x1 = k * (y + 0.5) + b;
                        x2 = k * (y - 0.5) + b;
                        x3 = -1;
                    }
                    if (x1 <= maxX && x2 >= minX) {
                        startX = Math.max((int) Math.round(Math.max(x1, minX)), 0);
                        endX = Math.min((int) Math.round(Math.min(x2, maxX)), mWidth - 1);
                        for (x = startX; x <= endX; x++) {
                            if (x != x3) {
                                if (mLens.remap(x, y)) { //优先采用反映射测距
                                    if (mDistance[x + y * mWidth] >= mLens.remapDistance) {
                                        mDistance[x + y * mWidth] = mLens.remapDistance;
                                        mCanvas[x + y * mWidth] = surface.getLineColor();
                                    }
                                } else { //比例测距
                                    d = v1.multiplyBy(v2.set(x - _p1.x, y - _p1.y, 0)) / (d3 * d3);
                                    if (d < 0) d = 0;
                                    if (d > 1) d = 1;
                                    d = (1 - d) * d1 + d * d2;
                                    if (mDistance[x + y * mWidth] >= d) {
                                        mDistance[x + y * mWidth] = d;
                                        mCanvas[x + y * mWidth] = surface.getLineColor();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            VECTORS.recycle(v1);
            VECTORS.recycle(v2);
        }
    }

    /**
     * 镜头
     */
    public class Lens {
        private static final float FOCUS_DISTANCE = 0.001f; //焦距，单位米

        private float mScale; //成像坐标系到真实坐标系转换比例，单位像素比米
        private float mOffsetX; //成像坐标系横轴偏移
        private float mOffsetY; //成像坐标系纵轴偏移
        private Point mPoint = new Point(); //焦点坐标
        private float mAngleX = 0; //水平角度[0,360)
        private float mAngleY = 0; //垂直角度(-90,90)
        private Vector mVector = new Vector(); //焦距向量
        private Vector mVectorX = new Vector(); //水平向量
        private Vector mVectorY = new Vector(); //垂直向量

        private Lens(int width, int height, float verticalAngle) {
            mScale = (float) (height / (2 * FOCUS_DISTANCE * Math.tan(verticalAngle * Math.PI / 360))); //根据屏高与垂直视角计算
            mOffsetX = (width - 1) / 2f;
            mOffsetY = (height - 1) / 2f;
            update();
        }

        /**
         * 调整镜头高度
         *
         * @param height
         */
        public void jumpTo(float height) {
            mPoint.set(mPoint.x, mPoint.y, height);
            update();
        }

        /**
         * 基于当前朝向水平移动
         *
         * @param distance
         * @param angle
         */
        public void moveBy(float distance, float angle) {
            angle += mAngleX;
            while (angle < 0) angle += 360;
            while (angle >= 360) angle -= 360;
            mPoint.set(mPoint.x + (float) (distance * Math.cos(angle * Math.PI / 180)), mPoint.y + (float) (distance * Math.sin(angle * Math.PI / 180)), mPoint.z);
            update();
        }

        /**
         * 基于当前朝向旋转镜头
         *
         * @param horizontal
         * @param vertical
         */
        public void rotateBy(float horizontal, float vertical) {
            mAngleX += horizontal;
            while (mAngleX < 0) mAngleX += 360;
            while (mAngleX >= 360) mAngleX -= 360;
            mAngleY += vertical;
            if (mAngleY <= -90) mAngleY = -89;
            if (mAngleY >= 90) mAngleY = 89;
            update();
        }

        private void update() { //更新冗余数据，镜头变化时调用
            mVector.set((float) Math.cos(mAngleX * Math.PI / 180), (float) Math.sin(mAngleX * Math.PI / 180), (float) Math.tan(mAngleY * Math.PI / 180));
            mVector.setLength(FOCUS_DISTANCE);
            mVectorX.set(-mVector.y, mVector.x, 0);
            mVectorX.setLength(1);
            mVectorY.set(mVector.x * mVector.z, mVector.y * mVector.z, -mVector.x * mVector.x - mVector.y * mVector.y);
            mVectorY.setLength(1);
        }

        private Point mapA = new Point(); //映射点a
        private Point mapB = new Point(); //映射点b
        private Point mapC = new Point(); //映射点c
        private float mapDirection = 0; //映射面方向
        private Vector normalVector = new Vector(); //法向量
        private float verticalDistance = 0; //焦点到成像面距离
        private Vector remapVector = new Vector(); //反映射向量
        private float remapDistance = 0; //反映射距离

        private boolean map(Surface surface) { //转换真实坐标为映射坐标
            Vector v = VECTORS.obtain();
            if (mVector.multiplyBy(v.set(surface.a.x - mPoint.x - mVector.x, surface.a.y - mPoint.y - mVector.y, surface.a.z - mPoint.z - mVector.z)) >= 0) {
                v.set(mPoint, surface.a);
                v.setLength(FOCUS_DISTANCE * v.getLength() * FOCUS_DISTANCE / v.multiplyBy(mVector));
                v.set(v.x - mVector.x, v.y - mVector.y, v.z - mVector.z);
                mapA.set(v.multiplyBy(mVectorX) * mScale + mOffsetX, v.multiplyBy(mVectorY) * mScale + mOffsetY, 0);
            } else { //a点处于屏幕后方
                VECTORS.recycle(v);
                return false;
            }
            if (mVector.multiplyBy(v.set(surface.b.x - mPoint.x - mVector.x, surface.b.y - mPoint.y - mVector.y, surface.b.z - mPoint.z - mVector.z)) >= 0) {
                v.set(mPoint, surface.b);
                v.setLength(FOCUS_DISTANCE * v.getLength() * FOCUS_DISTANCE / v.multiplyBy(mVector));
                v.set(v.x - mVector.x, v.y - mVector.y, v.z - mVector.z);
                mapB.set(v.multiplyBy(mVectorX) * mScale + mOffsetX, v.multiplyBy(mVectorY) * mScale + mOffsetY, 0);
            } else { //b点处于屏幕后方
                VECTORS.recycle(v);
                return false;
            }
            if (mVector.multiplyBy(v.set(surface.c.x - mPoint.x - mVector.x, surface.c.y - mPoint.y - mVector.y, surface.c.z - mPoint.z - mVector.z)) >= 0) {
                v.set(mPoint, surface.c);
                v.setLength(FOCUS_DISTANCE * v.getLength() * FOCUS_DISTANCE / v.multiplyBy(mVector));
                v.set(v.x - mVector.x, v.y - mVector.y, v.z - mVector.z);
                mapC.set(v.multiplyBy(mVectorX) * mScale + mOffsetX, v.multiplyBy(mVectorY) * mScale + mOffsetY, 0);
            } else { //c点处于屏幕后方
                VECTORS.recycle(v);
                return false;
            }
            mapDirection = (mapB.x - mapA.x) * (mapA.y - mapC.y) - (mapB.y - mapA.y) * (mapA.x - mapC.x); //顺时为负、逆时为正、重合共线为零
            normalVector.set(surface.a, surface.b, surface.c); //点重合时为零向量
            verticalDistance = Math.abs(normalVector.multiplyBy(v.set(mPoint, surface.a))); //点重合、焦点共面时为零
            VECTORS.recycle(v);
            return true;
        }

        private boolean remap(float x, float y) { //逆转换映射坐标为真实坐标
            if (verticalDistance == 0) return false; //点重合、焦点共面
            x -= mOffsetX;
            y -= mOffsetY;
            remapVector.set(mVector.x + (x * mVectorX.x + y * mVectorY.x) / mScale, mVector.y + (x * mVectorX.y + y * mVectorY.y) / mScale, mVector.z + y * mVectorY.z / mScale);
            float k = normalVector.multiplyBy(remapVector);
            if (k == 0) return false; //反映射线与成像面平行，不存在交点
            remapDistance = Math.abs(verticalDistance * remapVector.getLength() / k);
            return true;
        }
    }
}

/**
 * 对象循环机
 *
 * @param <Model>
 */
abstract class Recycler<Model> {
    private int mSize; //数量上限
    private Queue<Model> mCache; //对象缓存

    public Recycler() {
        this(Integer.MAX_VALUE);
    }

    public Recycler(int size) {
        mSize = size;
        mCache = new LinkedList<>();
    }

    /**
     * 获取对象
     *
     * @return
     */
    public Model obtain() {
        return mCache.size() > 0 ? mCache.remove() : newModel();
    }

    /**
     * 归还对象
     *
     * @param model
     * @return
     */
    public boolean recycle(Model model) {
        if (mCache.size() < mSize) {
            mCache.add(model);
            return true;
        }
        return false;
    }

    protected abstract Model newModel(); //新建对象
}
