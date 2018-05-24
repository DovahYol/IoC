package com.zb.ioc.utils;

import com.zb.ioc.validation.Errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 有向图,用来
 * 1,确定拓扑顺序，便于依次创建Bean
 * 2,检测是否有环，也就是是否有循环依赖
 * @param <T>
 */
public class Digraph<T> {
    private Map< T, List<T> > map = new HashMap<>();

    public void addEdge(T v, T w){
        map.putIfAbsent(v, new ArrayList<>());
        map.get(v).add(w);
    }

    private List<T> topologicalList = new ArrayList<>();

    public List<T> getTopologicalList() {
        return topologicalList;
    }

    private Errors errors = new Errors();

    public boolean hasErrors(){
        return errors.hasErrors();
    }

    public List<String> getAllErrors(){
        return errors.getAllErrors();
    }

    private void dfsInitializer(){
        if(map.size() == 0) return;
        boolean[] isVisited = new boolean[map.size()];
        boolean[] isOnStack = new boolean[map.size()];
        map.forEach((k, v) -> {

        });
    }

    private void dfs(){

    }


}
