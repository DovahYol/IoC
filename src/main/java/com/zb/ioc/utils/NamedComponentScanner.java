package com.zb.ioc.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NamedComponentScanner implements ComponentScanner{
    private static class NamedComponent{
        private String name;
        private Class component;
    }

    private List<NamedComponent> namedComponents;

    public NamedComponentScanner(Iterator<Class<?>> iterator){
        namedComponents = newList(iterator);
    }

    private static List<NamedComponent> newList(Iterator<Class<?>> iterator){
        List<NamedComponent> namedComponents = new ArrayList<>();
        iterator.forEachRemaining(it -> {
            NamedComponent namedComponent = new NamedComponent();
            namedComponent.component = it;
            try {
                namedComponent.name = AnnotationPropertyResolver.getComponentValue(it);
            } catch (Exception e) {
                e.printStackTrace();
            }
            namedComponents.add(namedComponent);
        });
        return namedComponents;
    }

    public Class scanImpl(String name, Class component) throws Exception {
        Class[] results = namedComponents.stream()
                .filter(it -> component.isAssignableFrom(it.component) && name.equals(it.name))
                .map(it -> it.component)
                .toArray(Class[]::new);
        if(results.length == 0){
            return null;
        }else if(results.length > 1){
            throw new Exception(String.format("存在多个名字为%s的%s的实现类", name, component));
        }

        return results[0];
    }
}