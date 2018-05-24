package com.zb.ioc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 有向图,用来
 * 1,确定拓扑顺序，便于依次创建Bean
 * 2,检测是否有环，也就是是否有循环依赖
 * @param <T>
 */
public class Digraph<T> {
    private Map< T, List<T> > map;

    public void addEdge(T v, T w){
        map.putIfAbsent(v, new ArrayList<>());
        map.get(v).add(w);
    }
}
