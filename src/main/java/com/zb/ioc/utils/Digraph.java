package com.zb.ioc.utils;

import com.zb.ioc.validation.Errors;

import java.util.*;

/**
 * 有向图,用来
 * 1,确定拓扑顺序，便于依次创建Bean
 * 2,检测是否有环，也就是是否有循环依赖
 * @param <T>
 */
public class Digraph<T> {
    private Map< T, Set<T> > map = new HashMap<>();
    private Map< T, Set<T> > reverseMap = new HashMap<>();

    public void addEdge(T v, T w){
        //邻接表保存图
        map.putIfAbsent(v, new HashSet<>());
        map.get(v).add(w);
        //邻接表保存图的逆
        reverseMap.putIfAbsent(w, new HashSet<>());
        reverseMap.get(w).add(v);
    }

    public void addEdge(List<T> v, T w){
        v.forEach(it -> addEdge(it, w));
    }

    public Set<T> getAllStartpoints(T w){
        return reverseMap.getOrDefault(w, new HashSet<>());
    }

    private LinkedList<T> topologicalList = new LinkedList<>();

    public List<T> getTopologicalList() {
        dfsInitializer();
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
        Map<T, Boolean> isVisited = new HashMap<>();
        Map<T, Boolean> isOnStack = new HashMap<>();
        map.forEach((k, v) -> {
            if(!isVisited.getOrDefault(k, false)){
                dfs(k, isVisited, isOnStack);
            }
        });
    }

    private void dfs(T t, Map<T, Boolean> isVisited, Map<T, Boolean> isOnStack){
        isVisited.put(t, true);
        isOnStack.put(t, true);
        if(map.get(t) != null){
            for(T item : map.get(t)){
                if(!isVisited.getOrDefault(item, false)){
                    dfs(item, isVisited, isOnStack);
                }else if(isOnStack.getOrDefault(item, false)){
                    errors.setError(String.format("检测到%s和%s存在循环依赖关系。", t, item));
                    return;
                }
            }
        }
        topologicalList.push(t);
        isOnStack.put(t, false);
    }


}
