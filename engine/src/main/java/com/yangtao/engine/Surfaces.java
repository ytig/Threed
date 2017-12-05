package com.yangtao.engine;

/**
 * 物
 */
public interface Surfaces {
    /**
     * 根据目录获取面，不存在返回空
     *
     * @param index
     * @return
     */
    Surface getSurface(int index);
}
