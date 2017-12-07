package com.yangtao.engine;

/**
 * 点
 */
public class Point {
    public float x = 0; //3d坐标系指向东方、2d坐标系指向右端
    public float y = 0; //3d坐标系指向南方、2d坐标系指向底部
    public float z = 0; //3d坐标系指向天空

    public Point() {
    }

    /**
     * 直接设值
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Point set(float x, float y, float z) {
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
    public Point set(Point copy) {
        this.x = copy.x;
        this.y = copy.y;
        this.z = copy.z;
        return this;
    }

    /**
     * 计算点间距
     *
     * @param p
     * @return
     */
    public float distanceTo(Point p) {
        return (float) Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y) + (z - p.z) * (z - p.z));
    }
}
