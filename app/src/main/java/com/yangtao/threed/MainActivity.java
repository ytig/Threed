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
import com.yangtao.threed.element.Rect;
import com.yangtao.threed.element.Rects;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainView content = new MainView(this);
        content.mCamera.mScene.add(new Rects(1, 1, 0.5f)
                .setColor(Color.GRAY, Color.RED)
                .setTranslate(5.5f, 0, 0.25f));
        float l = 1f;
        int s = 15;
        for (int i = -s; i <= s; i++) {
            for (int j = -s; j <= s; j++) {
                content.mCamera.mScene.add(new Rect(l, l)
                        .setColor(Color.TRANSPARENT, Color.WHITE)
                        .setTranslate(i * l, j * l, 0));
            }
        }
        setContentView(content);
    }
}

class MainView extends View implements Runnable {
    public MainCamera mCamera; //相机

    public MainView(Context context) {
        super(context);
        mCamera = new MainCamera(getContext().getResources().getDisplayMetrics().widthPixels, getContext().getResources().getDisplayMetrics().heightPixels, 66);
        post(this);
    }

    @Override
    public void run() {
        for (Surfaces surfaces : mCamera.mScene) {
            if (surfaces instanceof Rects) {
                Body body = (Body) surfaces;
//                body.setTranslate(body.mTranslateX + 0.05f, body.mTranslateY, body.mTranslateZ);
//                body.setScale(body.mScale * 1.01f);
                body.setRotate(body.mRotateX + 1, body.mRotateY + 1, body.mRotateZ + 1);
            }
        }
//        double r = 5.5f;
//        double a = 1;
//        mCamera.mLens.moveBy((float) (2 * r * Math.sin(Math.toRadians(a / 2))), (float) ((a - 180) / 2));
//        mCamera.mLens.rotateBy((float) a, 0);
        postDelayed(this, 25);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mCamera.draw(), 0, 0, null);
        postInvalidate();
    }
}

class MainCamera extends Camera {
    private Bitmap mBitmap; //位图
    public List<Surfaces> mScene; //场景

    public MainCamera(int width, int height, float verticalAngle) {
        super(width, height, verticalAngle);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mScene = new ArrayList<>();
        mLens.jumpTo(1.8f);
    }

    public Bitmap draw() {
        clear();
        for (Surfaces surfaces : mScene) draw(surfaces);
        mBitmap.setPixels(mCanvas, 0, mWidth, 0, 0, mWidth, mHeight);
        return mBitmap;
    }
}
