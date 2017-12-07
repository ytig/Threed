package com.yangtao.threed.engine;

/**
 * 互斥数据
 *
 * @param <Data>
 */
public class Mutex<Data> {
    private boolean mMutex; //互斥
    private Data mData; //数据

    public Mutex(Data data) {
        mMutex = false;
        mData = data;
    }

    /**
     * 非阻塞式执行
     *
     * @param handler
     * @return
     */
    public boolean unblock(DataHandler<Data> handler) {
        if (apply()) {
            if (handler != null) handler.handleData(mData);
            release();
            return true;
        }
        return false;
    }

    /**
     * 阻塞式执行
     *
     * @param handler
     */
    public void block(DataHandler<Data> handler) {
        while (!unblock(handler)) {
        }
    }

    /**
     * 非互斥执行
     *
     * @param handler
     */
    public void direct(DataHandler<Data> handler) {
        if (handler != null) handler.handleData(mData);
    }

    private synchronized boolean apply() { //申请
        if (mMutex) return false;
        mMutex = true;
        return true;
    }

    private synchronized boolean release() { //释放
        if (!mMutex) return false;
        mMutex = false;
        return true;
    }

    /**
     * 数据处理
     *
     * @param <Data>
     */
    public interface DataHandler<Data> {
        void handleData(Data data);
    }
}
