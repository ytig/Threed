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
import com.yangtao.threed.element.Rects;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainView content = new MainView(this);
        content.mCamera.mScene.add(new Rects(1, 1, 0.5f)
                .setColor(Color.TRANSPARENT, Color.RED)
                .setTranslate(5.5f, 0, 0.25f));
        setContentView(content);
    }
}

class MainView extends View implements Runnable {
    private static final float SHRINK = 1; //缩小常数

    public MainCamera mCamera; //相机

    public MainView(Context context) {
        super(context);
        mCamera = new MainCamera((int) (getContext().getResources().getDisplayMetrics().widthPixels / SHRINK), (int) (getContext().getResources().getDisplayMetrics().heightPixels / SHRINK), 90);
        post(this);
    }

    @Override
    public void run() {
//        for (Surfaces surfaces : mCamera.mScene) {
//            if (surfaces instanceof Body) {
//                Body body = (Body) surfaces;
//                body.setRotate(body.mRotateH + 1, body.mRotateV + 1);
//            }
//        }
        postDelayed(this, 25);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.scale(SHRINK, SHRINK);
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
