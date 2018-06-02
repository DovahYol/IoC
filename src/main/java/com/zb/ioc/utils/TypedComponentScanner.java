package com.zb.ioc.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypedComponentScanner implements ComponentScanner {

    private Iterator<Class<?>> iterator;

    public TypedComponentScanner(Iterator<Class<?>> iterator){
        this.iterator = iterator;
    }

    //寻找field对应的实体类
    private Class scanImpl(Class declaringClass) throws Exception {
        List<Class> results = new ArrayList<>();
        while(iterator.hasNext()){
            Class<?> c = iterator.next();
            if(declaringClass.isAssignableFrom(c)){
                results.add(c);
            }
        }
        if(results.size() == 0){
            throw new Exception(String.format("并未找到%s的实现类", declaringClass));
        }else if(results.size() > 1){
            throw new Exception(String.format("存在多个%s的实现类", declaringClass));
        }

        return results.get(0);
    }
}
