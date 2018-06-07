package com.zb.ioc.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class TypedComponentScanner implements ComponentScanner {

    private List<Class<?>> components = new ArrayList<>();

    public TypedComponentScanner(Iterator<Class<?>> iterator){
        iterator.forEachRemaining(components::add);
    }

    //寻找field对应的实体类
    public Class scanImpl(Class declaringClass) throws Exception {
        Class[] results = components.stream()
                .filter(declaringClass::isAssignableFrom)
                .toArray(Class[]::new);
        if(results.length == 0){
            return null;
        }else if(results.length > 1){
            Class[] filteredResults = Stream.of(results)
                    .filter(it -> {
                        String name = "";
                        try {
                            name = AnnotationPropertyResolver.getComponentValue(it);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return "".equals(name);
                    })
                    .toArray(Class[]::new);
            if(filteredResults.length == 0){
                return null;
            }else if(filteredResults.length > 1){
                throw new Exception(String.format("存在多个%s的实现类", declaringClass));
            }else{
                return filteredResults[0];
            }
        }

        return results[0];
    }
}
