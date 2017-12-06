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
        content.scene.add(new Cube()
                .setColor(Color.TRANSPARENT, Color.RED)
                .setTranslate(5.5f, 0, Cube.UNIT / 2));
        setContentView(content);
    }
}

class MainView extends View {
    private static final float SHRINK = 1; //缩小常数

    private MainCamera camera; //相机
    public List<Surfaces> scene; //场景

    public MainView(Context context) {
        super(context);
        camera = new MainCamera((int) (getContext().getResources().getDisplayMetrics().widthPixels / SHRINK), (int) (getContext().getResources().getDisplayMetrics().heightPixels / SHRINK), 90);
        camera.mLens.jumpTo(1.8f);
        scene = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        camera.draw(scene);
        canvas.scale(SHRINK, SHRINK);
        canvas.drawBitmap(camera.mBitmap, 0, 0, null);
        postInvalidate();
    }
}

class MainCamera extends Camera {
    public Bitmap mBitmap; //位图

    public MainCamera(int width, int height, float verticalAngle) {
        super(width, height, verticalAngle);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    }

    @Override
    public void draw(List<Surfaces> scene) {
        super.draw(scene);
        mBitmap.setPixels(mCanvas, 0, mWidth, 0, 0, mWidth, mHeight);
    }
}
