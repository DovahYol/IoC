package com.zb.ioc;

import com.zb.ioc.annotation.Component;
import org.apache.commons.lang3.ClassUtils;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Bootstrap {
    public Map<Class, Object> createBeanMap(String packageName){
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Component.class);
        Map<Class, Object> map = new HashMap<>();
        annotated.forEach(t -> {
                    List<Class<?>> classes = ClassUtils.getAllInterfaces(t);
                    classes.forEach(c -> {
                        try {
                            map.put(c, t.getConstructor().newInstance());
                        } catch (InstantiationException | IllegalAccessException
                                | InvocationTargetException | NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    });
                });
        return map;

    }
}
