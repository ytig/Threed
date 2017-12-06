package com.yangtao.threed.engine;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * 互斥数据
 */
public class TMutex {
    private HashMap<String, Boolean> mMap; //互斥变量
    public Bitmap mBitmap; //位图

    protected TMutex(Context context) {
        mMap = new HashMap<>();
        mBitmap = Bitmap.createBitmap(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels, Bitmap.Config.RGB_565);
    }

    public synchronized boolean apply(String key) {
        Boolean mutex = mMap.get(key);
        if (Boolean.TRUE.equals(mutex)) return false;
        mMap.put(key, Boolean.TRUE);
        return true;
    }

    public synchronized boolean release(String key) {
        Boolean mutex = mMap.get(key);
        if (!Boolean.TRUE.equals(mutex)) return false;
        mMap.put(key, Boolean.FALSE);
        return true;
    }
}
