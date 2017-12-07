package com.yangtao.engine;

/**
 * 线
 */
public class Vector {
    public float x = 0; //3d坐标系指向东方、2d坐标系指向右端
    public float y = 0; //3d坐标系指向南方、2d坐标系指向底部
    public float z = 0; //3d坐标系指向天空

    public Vector() {
    }

    /**
     * 直接设值
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Vector set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * 复制设值
     *
     * @param copy
     * @return
     */
    public Vector set(Vector copy) {
        this.x = copy.x;
        this.y = copy.y;
        this.z = copy.z;
        return this;
    }

    /**
     * 方向向量
     *
     * @param start
     * @param end
     * @return
     */
    public Vector set(Point start, Point end) {
        this.x = end.x - start.x;
        this.y = end.y - start.y;
        this.z = end.z - start.z;
        return this;
    }

    /**
     * 法向量（右手螺旋）
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public Vector set(Point a, Point b, Point c) {
        this.x = (b.y - a.y) * (a.z - c.z) - (b.z - a.z) * (a.y - c.y);
        this.y = (b.z - a.z) * (a.x - c.x) - (b.x - a.x) * (a.z - c.z);
        this.z = (b.x - a.x) * (a.y - c.y) - (b.y - a.y) * (a.x - c.x);
        setLength(1);
        return this;
    }

    /**
     * 计算向量长度
     *
     * @return
     */
    public float getLength() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * 伸缩向量至指定长度，指定负值即反向
     *
     * @param length
     * @return
     */
    public boolean setLength(float length) {
        float mLength = getLength();
        if (mLength == 0) {
            if (length == 0) return true;
            else return false;
        }
        float k = length / mLength;
        x *= k;
        y *= k;
        z *= k;
        return true;
    }

    /**
     * 计算向量积
     *
     * @param v
     * @return
     */
    public float multiplyBy(Vector v) {
        return x * v.x + y * v.y + z * v.z;
    }
}
