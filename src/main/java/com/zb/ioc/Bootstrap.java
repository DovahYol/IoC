package com.zb.ioc;

import com.zb.ioc.annotation.Component;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Bootstrap {
    public Map<Class, Object> createBeanMap(String packageName){
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Component.class);
        Map<Class, Object> map = new HashMap<>();
        annotated.forEach(t -> {
            try {
                map.put(t, t.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
        return map;
    }

    public void interceptAllMethods(String packageName){
//        Proxy.newProxyInstance(null,);
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(new SubTypesScanner(false)));
        Set<String> usages = reflections.getAllTypes();
        usages.forEach(u -> {
            Class<?> c = null;
            try {
                c = Class.forName(u);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println(c);
        });
    }
}
