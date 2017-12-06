package com.yangtao.engine;

import java.util.List;

/**
 * 物
 */
public interface Surfaces {
    /**
     * 根据目录获取子项，不存在返回空
     *
     * @param index
     * @return
     */
    Object getChildAt(int index);

    class Tools {
        public static Object getChildAt(List list, int index) {
            return (list != null && index < list.size()) ? list.get(index) : null;
        }
    }
}
