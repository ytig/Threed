package com.yangtao.engine;

/**
 * 面
 */
public abstract class Surface {
    public static final int TRANSPARENT = 0; //透明色
    public static final int TEXTURE_TYPE_NULL = 0; //无纹理
    public static final int TEXTURE_TYPE_UNIQUE = 1; //纯色纹理
    public static final int TEXTURE_TYPE_BLUR = 2; //模糊纹理
    public static final int TEXTURE_TYPE_EXACT = 3; //精确纹理

    public Point a; //顶点
    public Point b; //顶点
    public Point c; //顶点
    public boolean doubleFace = false; //是否双面（默认单面）
    public boolean exactDistance = false; //精确测距（默认近似）
    public int textureType = TEXTURE_TYPE_NULL; //纹理类型（默认无色）
    public int lineType = toLineType(false, false, false); //边线类型（默认无线）

    public Surface(Point a, Point b, Point c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * 根据面内坐标映射纹理色彩值
     * 模糊纹理以映射ab为x轴，以映射ac为y轴，参数表示坐标与对应轴长比值
     * 精确纹理以真实ab为x轴，以x轴垂向为y轴，参数表示坐标与横轴长比值（钝角置于c点可简化运算）
     *
     * @param x
     * @param y
     * @return
     */
    public abstract int getTextureColor(float x, float y);

    /**
     * 获取边线色彩值
     *
     * @return
     */
    public abstract int getLineColor();

    /**
     * 边线编码
     *
     * @param ab
     * @param bc
     * @param ca
     * @return
     */
    public static int toLineType(boolean ab, boolean bc, boolean ca) {
        if (ab) {
            if (bc) {
                if (ca) return 7;
                else return 6;
            } else {
                if (ca) return 5;
                else return 4;
            }
        } else {
            if (bc) {
                if (ca) return 3;
                else return 2;
            } else {
                if (ca) return 1;
                else return 0;
            }
        }
    }
}
