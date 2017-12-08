package com.yangtao.threed.extend;

import android.content.Context;

import com.yangtao.engine.Camera;
import com.yangtao.threed.engine.Mutex;

/**
 * 控制参数
 */
public class Param implements Mutex.DataHandler<Param> {
    private static final float MOVE_POWER = 3.3f / 1000; //最大移速
    private static final float MOVE_RANGE = 50f; //响应半径
    private static final float ROTATE_ANGLE = 60f; //最大转角
    private static final float ROTATE_RANGE = 2f; //有效速度
    private static final float ROTATE_POWER = 50f / 1000; //视角转速

    private float mDensity; //屏幕密度
    private boolean jumpOrder = false; //跳跃指令
    private float jumpValue = 1; //跳跃进度
    private float movePower = 0; //移动速度
    private float moveAngle = 0; //移动方向
    private float horizontalAngle = 0; //水平存量
    private float verticalAngle = 0; //垂直存量

    public Param(Context context) {
        mDensity = context.getResources().getDisplayMetrics().density;
    }

    /**
     * 跳跃镜头
     *
     * @return
     */
    public Param doJump() {
        jumpOrder = true;
        return this;
    }

    /**
     * 移动镜头
     *
     * @param dx
     * @param dy
     * @return
     */
    public Param doMove(float dx, float dy) {
        movePower = (float) (MOVE_POWER * Math.min(Math.sqrt(dx * dx + dy * dy) / (MOVE_RANGE * mDensity), 1));
        moveAngle = (float) Math.toDegrees(angle(dx, dy) + Math.PI / 2);
        return this;
    }

    /**
     * 旋转镜头
     *
     * @param vx
     * @param vy
     * @return
     */
    public Param doRotate(float vx, float vy) {
        boolean x = Math.abs(vx) > Math.abs(vy);
        float v = x ? vx : vy;
        float a = (v < 0 ? -1 : 1) * ROTATE_ANGLE * Math.min(Math.abs(v) / (ROTATE_RANGE * mDensity), 1);
        if (x) horizontalAngle += a;
        else verticalAngle -= a * 0.8f;
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
        jumpValue = Math.min(jumpValue + ms / 600f, 1);
        float i = 1 - Math.abs(1 - 2 * jumpValue);
        lens.jumpTo(1.8f + (1 - (1 - i) * (1 - i)) * 1f);
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
        if (jumpOrder) {
            if (param.jumpValue == 1) param.jumpValue = 0;
            jumpOrder = false;
        }
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
