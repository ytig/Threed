package com.yangtao.threed;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.yangtao.engine.Camera;
import com.yangtao.engine.Surfaces;
import com.yangtao.threed.element.Body;
import com.yangtao.threed.element.Cube;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainView content = new MainView(this);
        content.camera.scene.add(new Cube()
                .setColor(Color.TRANSPARENT, Color.RED)
                .setTranslate(5.5f, 0, Cube.UNIT / 2));
        setContentView(content);
    }
}

class MainView extends View implements Runnable {
    private static final float SHRINK = 1; //缩小常数

    public MainCamera camera; //相机

    public MainView(Context context) {
        super(context);
        camera = new MainCamera((int) (getContext().getResources().getDisplayMetrics().widthPixels / SHRINK), (int) (getContext().getResources().getDisplayMetrics().heightPixels / SHRINK), 90);
        post(this);
    }

    @Override
    public void run() {
        for (Surfaces surfaces : camera.scene) {
            if (surfaces instanceof Body) {
                Body body = (Body) surfaces;
                body.setRotate(body.mRotateH + 1, body.mRotateV + 1);
            }
        }
        postDelayed(this, 25);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.scale(SHRINK, SHRINK);
        canvas.drawBitmap(camera.draw(), 0, 0, null);
        postInvalidate();
    }
}

class MainCamera extends Camera {
    private Bitmap bitmap; //位图
    public List<Surfaces> scene; //场景

    public MainCamera(int width, int height, float verticalAngle) {
        super(width, height, verticalAngle);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        scene = new ArrayList<>();
        mLens.jumpTo(1.8f);
    }

    public Bitmap draw() {
        clear();
        for (Surfaces surfaces : scene) draw(surfaces);
        bitmap.setPixels(mCanvas, 0, mWidth, 0, 0, mWidth, mHeight);
        return bitmap;
    }
}
