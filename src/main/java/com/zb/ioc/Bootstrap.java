package com.zb.ioc;

import com.zb.ioc.annotation.Antowired;
import com.zb.ioc.annotation.Component;
import com.zb.ioc.utils.Digraph;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;

public class Bootstrap {
    public Map<Class, Object> createBeanMap(String packageName) throws Exception {
        Reflections reflections = new Reflections(packageName);
        //支持类注解提供依赖
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Component.class);
        Digraph<Class> digraph = new Digraph<>();
        //构造依赖字典
        Map< Class, Map<Field, Class> > dependencyMap = new HashMap<>();
        for (Class<?> t:
                annotated) {
            Map<Field, Class> fieldMap = new HashMap<>();
            dependencyMap.put(t, fieldMap);
            Field[] fields = t.getDeclaredFields();
            for (Field f:
                 fields) {
                if(f.isAnnotationPresent(Antowired.class)){
                    Class concreteClass = scanImpl(annotated.iterator(), f.getType());
                    f.setAccessible(true);//private的field也可以注入
                    fieldMap.put(f, concreteClass);
                    digraph.addEdge(concreteClass, t);
                }
            }
        }
        List<Class> list = digraph.getTopologicalList();
        if(digraph.hasErrors()){
            throw new Exception(digraph.getAllErrors().get(0));
        }

        Map<Class, Object> result = new HashMap<>();

        for (Class t :
                list) {
            if (digraph.getAllStartpoints(t).size() == 0) {
                /**
                 * TODO
                 * 以后要做检查，看看是否有无参数构造函数。
                 * 现在先用Class来做识别，以后有来自运行时jar包的注入时，会改动
                 */
                result.put(t, t.getConstructor().newInstance());
            }else{
                Object object = t.getConstructor().newInstance();
                for (Map.Entry<Field, Class> e:
                        dependencyMap.get(t).entrySet()) {
                    if(result.get(e.getValue()) == null){
                        throw new NullPointerException();
                    }
                    e.getKey().set(object, result.get(e.getValue()));
                }
                result.put(t, object);
            }
        }
        return result;
    }

    //寻找field对应的实体类
    private Class scanImpl(Iterator<Class<?>> concreteClasses, Class declaringClass) throws Exception {
        List<Class> results = new ArrayList<>();
        while(concreteClasses.hasNext()){
            Class<?> c = concreteClasses.next();
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
