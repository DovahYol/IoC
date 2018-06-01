package com.zb.ioc;

import com.zb.ioc.annotation.Autowired;
import com.zb.ioc.annotation.Component;
import com.zb.ioc.utils.Digraph;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class Bootstrap {
    public Map<Class, Object> createBeanMap(String packageName) throws Exception {
        Reflections reflections = new Reflections(packageName);
        //支持类注解提供依赖
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Component.class);
        //named components
        List<NamedComponent> namedComponents = NamedComponent.newList(annotated.iterator());
        Digraph<Class> digraph = new Digraph<>();
        //属性依赖字典
        Map< Class, Map< Field, Class > > fieldDependencyMap = new HashMap<>();
        //方法依赖字典
        Map< Class, Map< Method, List<Class> > > methodDependencyMap = new HashMap<>();
        //构造函数依赖字典
        Map< Class, Map< Constructor, List<Class> > > constructorDependencyMap = new HashMap<>();
        List<Class> parameterTypes;
        for (Class<?> t:
                annotated) {
            //构造属性依赖字典
            Map< Field, Class > fieldMap = new HashMap<>();
            fieldDependencyMap.put(t, fieldMap);
            Field[] fields = t.getDeclaredFields();
            for (Field f:
                 fields) {
                if(f.isAnnotationPresent(Autowired.class)){
                    f.setAccessible(true);//private的field也可以注入
                    Class concreteClass = scanImpl(annotated.iterator(), f.getType());
                    fieldMap.put(f, concreteClass);
                    digraph.addEdge(concreteClass, t);
                }
            }
            //构造方法依赖字典
            Map< Method, List<Class> > methodMap = new HashMap<>();
            methodDependencyMap.put(t, methodMap);
            Method[] methods = t.getDeclaredMethods();
            for (Method m:
                    methods) {
                if (m.isAnnotationPresent(Autowired.class)){
                    m.setAccessible(true);
                    methodMap.put(m, new ArrayList<>());
                    parameterTypes = methodMap.get(m);
                    Class[] classes = m.getParameterTypes();
                    for (Class parameterType :
                            classes) {
                        Class concreteClass = scanImpl(annotated.iterator(), parameterType);
                        parameterTypes.add(concreteClass);
                        digraph.addEdge(concreteClass, t);
                    }
                }
            }
            //构造构造函数依赖字典
            Map< Constructor, List<Class> > constructorMap = new HashMap<>();
            constructorDependencyMap.put(t, constructorMap);
            Optional<Constructor> c = scanConstructor(t);
            if (c.isPresent()){
                c.get().setAccessible(true);
                constructorMap.put(c.get(), new ArrayList<>());
                parameterTypes = constructorMap.get(c.get());
                Class[] classes = c.get().getParameterTypes();
                for (Class parameterType :
                        classes) {
                    Class concreteClass = scanImpl(annotated.iterator(), parameterType);
                    parameterTypes.add(concreteClass);
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
                //创建一个实例
                Object object = null;
                if(constructorDependencyMap.get(t).size() == 0){
                    object = t.getConstructor().newInstance();
                }
                for (Map.Entry< Constructor, List<Class> > e:
                        constructorDependencyMap.get(t).entrySet()) {
                    Object[] parameters = e.getValue().stream()
                            .map(result::get)
                            .toArray();
                    object = e.getKey().newInstance(parameters);
                }
                //设置property的值
                for (Map.Entry< Field, Class > e:
                        fieldDependencyMap.get(t).entrySet()) {
                    if(result.get(e.getValue()) == null){
                        throw new NullPointerException();
                    }
                    e.getKey().set(object, result.get(e.getValue()));
                }
                //调用被@Autowired注解的方法
                for (Map.Entry< Method, List<Class> > e:
                        methodDependencyMap.get(t).entrySet()) {
                    Object[] parameters = e.getValue().stream()
                            .map(result::get)
                            .toArray();
                    e.getKey().invoke(object, parameters);
                }

                result.put(t, object);
            }
        }
        return result;
    }

    private Optional<Constructor> scanConstructor(Class<?> t) throws Exception {
        Constructor[] constructors = t.getDeclaredConstructors();
        Constructor[] results = new Constructor[constructors.length];
        int count = 0;
        for (Constructor c :
                constructors) {
            if (c.isAnnotationPresent(Autowired.class)){
                results[count++] = c;
            }
        }
        if(count > 1){
            throw new Exception(String.format("%s有多个被@Autowired注解的构造函数", t));
        }

        if (count == 0) return Optional.empty();
        else return Optional.of(results[0]);
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

    private static class NamedComponent {
        private String name;
        private Class component;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Class getComponent() {
            return component;
        }

        public void setComponent(Class component) {
            this.component = component;
        }

        public NamedComponent(){}

        private static List<NamedComponent> newList(Iterator<Class<?>> iterator){
            List<NamedComponent> namedComponents = new ArrayList<>();
            iterator.forEachRemaining(it -> {
                NamedComponent namedComponent = new NamedComponent();
                namedComponent.setComponent(it);
                Annotation annotation = it.getAnnotation(Component.class);
                Class type = annotation.annotationType();
                for (Method method : type.getDeclaredMethods()) {
                    Object value;
                    try {
                        value = method.invoke(annotation);
                        namedComponent.setName((String)value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                namedComponents.add(namedComponent);
            });
            return namedComponents;
        }
    }
}
