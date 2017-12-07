package com.yangtao.threed.extend;

import android.content.Context;

import com.yangtao.engine.Camera;
import com.yangtao.threed.engine.Mutex;

/**
 * 控制参数
 */
public class Param implements Mutex.DataHandler<Param> {
    private float movePower = 0; //移动速度
    private float moveAngle = 0; //移动方向
    private float horizontalAngle = 0; //水平存量
    private float verticalAngle = 0; //垂直存量

    public Param() {
    }

    /**
     * 移动镜头
     *
     * @param context
     * @param dx
     * @param dy
     * @return
     */
    public Param doMove(Context context, float dx, float dy) {
        float MAX_POWER = 3.3f / 1000; //最大移速
        if (context != null) {
            movePower = (float) (MAX_POWER * Math.min(Math.sqrt(dx * dx + dy * dy) / (50 * context.getResources().getDisplayMetrics().density), 1));
            moveAngle = (float) Math.toDegrees(angle(-dy, dx));
        }
        return this;
    }

    /**
     * 旋转镜头
     *
     * @param context
     * @param vx
     * @param vy
     * @return
     */
    public Param doRotate(Context context, float vx, float vy) {
        float MAX_ANGLE = 16f; //最大转角
        if (context != null) {
            boolean x = Math.abs(vx) > Math.abs(vy);
            float v = x ? vx : vy;
            float a = (v < 0 ? -1 : 1) * MAX_ANGLE * Math.max(Math.min(Math.abs(v) / (1f * context.getResources().getDisplayMetrics().density), 1), 0);
            if (x) horizontalAngle += a;
            else verticalAngle -= a;
        }
        return this;
    }

    /**
     * 镜头变换
     *
     * @param ms
     * @param lens
     * @return
     */
    public Param doLens(long ms, Camera.Lens lens) {
        float ROTATE_POWER = 66f / 1000; //视角转速
        lens.moveBy(movePower * ms, moveAngle);
        float h = ROTATE_POWER * ms;
        float tmp = Math.abs(horizontalAngle) - h;
        if (tmp <= 0) {
            h = horizontalAngle;
            horizontalAngle = 0;
        } else {
            h *= (horizontalAngle < 0 ? -1 : 1);
            horizontalAngle = (horizontalAngle < 0 ? -1 : 1) * tmp;
        }
        float v = ROTATE_POWER * ms;
        tmp = Math.abs(verticalAngle) - v;
        if (tmp <= 0) {
            v = verticalAngle;
            verticalAngle = 0;
        } else {
            v *= (verticalAngle < 0 ? -1 : 1);
            verticalAngle = (verticalAngle < 0 ? -1 : 1) * tmp;
        }
        lens.rotateBy(h, v);
        return this;
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

    private static double angle(double x, double y) {
        if (x == 0) {
            if (y == 0) return 0;
            return (y > 0 ? 1 : -1) * Math.PI / 2;
        } else {
            double a = Math.atan(y / x);
            if (x < 0) a += (a > 0 ? -1 : 1) * Math.PI;
            return a;
        }
    }
}
