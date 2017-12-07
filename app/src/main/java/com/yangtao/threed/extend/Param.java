package com.yangtao.threed.extend;

import com.yangtao.engine.Camera;
import com.yangtao.threed.engine.Mutex;

/**
 * 控制参数
 */
public class Param implements Mutex.DataHandler<Param> {
    private static final float HORIZONTAL_POWER = 66f / 1000; //水平转速
    private static final float VERTICAL_POWER = 66f / 1000; //垂直转速

    public float movePower = 0; //移动速度
    public float moveAngle = 0; //移动方向
    public float horizontalAngle = 0; //水平存量
    public float verticalAngle = 0; //垂直存量

    public Param() {
    }

    /**
     * 镜头变换
     *
     * @param ms
     * @param lens
     */
    public void doLens(long ms, Camera.Lens lens) {
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

    @Override
    public void handleData(Param param) {
        param.movePower = movePower;
        param.moveAngle = moveAngle;
        param.horizontalAngle += horizontalAngle;
        param.verticalAngle += verticalAngle;
        horizontalAngle = 0;
        verticalAngle = 0;
    }
}
