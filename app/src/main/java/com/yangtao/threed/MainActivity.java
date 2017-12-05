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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainView content = new MainView(this);
        content.scene.add(new Cube()
                .setColor(Color.TRANSPARENT, Color.RED)
                .setTranslate(0, -100, 0));
        setContentView(content);
    }
}

class MainView extends View {
    private MainCamera camera; //相机
    public List<Surfaces> scene; //场景

    public MainView(Context context) {
        super(context);
        camera = new MainCamera(getContext().getResources().getDisplayMetrics().widthPixels, getContext().getResources().getDisplayMetrics().heightPixels, 90);
        camera.mLens.jumpTo(1.8f);
        scene = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        camera.draw(scene);
        canvas.drawBitmap(camera.mBitmap, 0, 0, null);
        postInvalidate();
    }
}

class MainCamera extends Camera {
    public Bitmap mBitmap; //位图

    public MainCamera(int width, int height, float verticalAngle) {
        super(width, height, verticalAngle);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void draw(List<Surfaces> scene) {
        super.draw(scene);
        mBitmap.setPixels(mCanvas, 0, mWidth, 0, 0, mWidth, mHeight);
    }
}
