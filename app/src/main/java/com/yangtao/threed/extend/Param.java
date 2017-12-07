package com.yangtao.threed.extend;

import com.yangtao.engine.Camera;

/**
 * 控制参数
 */
public class Param {
    private static final float HORIZONTAL_POWER = 66 / 1000f;
    private static final float VERTICAL_POWER = 66 / 1000f;

    public float movePower = 0; //移动速度
    public float moveAngle = 0; //移动方向
    public float horizontalAngle = 0; //水平视角（增量）
    public float verticalAngle = 0; //垂直视角（增量）

    public void doCompute(long ms, Camera.Lens lens) {
        lens.moveBy(movePower * ms, moveAngle);
        float h = HORIZONTAL_POWER * ms;
        float tmp = Math.abs(horizontalAngle) - h;
        if (tmp <= 0) {
            h = horizontalAngle;
            horizontalAngle = 0;
        } else {
            h *= (horizontalAngle < 0 ? -1 : 1);
            horizontalAngle = (horizontalAngle < 0 ? -1 : 1) * tmp;
        }
        float v = VERTICAL_POWER * ms;
        tmp = Math.abs(verticalAngle) - v;
        if (tmp <= 0) {
            v = verticalAngle;
            verticalAngle = 0;
        } else {
            v *= (verticalAngle < 0 ? -1 : 1);
            verticalAngle = (verticalAngle < 0 ? -1 : 1) * tmp;
        }
        lens.rotateBy(h, v);
    }
}
