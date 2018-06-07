package com.zb.ioc;

import com.zb.ioc.annotation.*;
import com.zb.ioc.utils.*;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class Bootstrap {
    private final Set<Class<?>> annotated;
    private final NamedComponentScanner namedComponentScanner;
    private final TypedComponentScanner typedComponentScanner;
    private final BeanMethodScanner beanMethodScanner;
    //BeanMap缓存
    private final Map<DependencySource, Object> cachedBeanMap = new HashMap<>();
    //Scope字典
    private final Map<Class, ScopeType> scopeTypeMap = new HashMap<>();
    //属性依赖字典
    private final Map<Class, Map<Field, DependencySource>> fieldDependencyMap = new HashMap<>();
    //方法依赖字典
    private final Map<Class, Map<Method, List<DependencySource>>> methodDependencyMap = new HashMap<>();
    //构造函数依赖字典
    private final Map<Class, Map<Constructor, List<DependencySource>>> constructorDependencyMap = new HashMap<>();
    //存放各Component之间的依赖
    private final Digraph<DependencySource> digraph = new Digraph<>();

    public Bootstrap(String packageName) throws Exception {
        Reflections reflections = new Reflections(packageName);
        //支持类注解提供依赖
        annotated = reflections.getTypesAnnotatedWith(Component.class);
        //Named Component
        namedComponentScanner = new NamedComponentScanner(annotated.iterator());
        //Typed Component
        typedComponentScanner = new TypedComponentScanner(annotated.iterator());
        beanMethodScanner = new BeanMethodScanner(annotated.iterator());
        //构建依赖
        buildDependency();
    }

    private void buildDependency() throws Exception {
        for (Class<?> t:
                annotated) {
            //构造Scope字典
            if(t.isAnnotationPresent(Scope.class)){
                scopeTypeMap.put(t, AnnotationPropertyResolver.getScopeType(t));
            }else{
                scopeTypeMap.put(t, ScopeType.SINGLETON);
            }
            //构造属性依赖字典
            Map< Field, DependencySource > fieldMap = new HashMap<>();
            fieldDependencyMap.put(t, fieldMap);
            Field[] fields = t.getDeclaredFields();
            for (Field f:
                 fields) {
                if(f.isAnnotationPresent(Autowired.class)){
                    f.setAccessible(true);//private的field也可以注入
                    Class concreteClass;
                    DependencySource dependencySource;
                    if(f.isAnnotationPresent(Qualifier.class)){
                        String name = AnnotationPropertyResolver.getQualifierValue(f);
                        concreteClass = namedComponentScanner.scanImpl(name, f.getType());
                        if(concreteClass != null){
                            dependencySource = new ComponentDependencySource(concreteClass);
                        }else{
                            dependencySource = beanMethodScanner.scanDependency(name, f.getType());
                        }
                    }else{
                        concreteClass = typedComponentScanner.scanImpl(f.getType());
                        if(concreteClass != null){
                            dependencySource = new ComponentDependencySource(concreteClass);
                        }else{
                            dependencySource = beanMethodScanner.scanDependency(f.getType());
                        }
                    }

                    fieldMap.put(f, dependencySource);
                    digraph.addEdge(dependencySource, new ComponentDependencySource(t));
                }
            }
            //构造方法依赖字典
            Map< Method, List<DependencySource> > methodMap = new HashMap<>();
            methodDependencyMap.put(t, methodMap);
            Method[] methods = t.getDeclaredMethods();
            for (Method m:
                    methods) {
                if (m.isAnnotationPresent(Autowired.class)){
                    m.setAccessible(true);
                    List<DependencySource> dependencySources = getParametersDependencySource(m);
                    methodMap.put(m, dependencySources);
                    digraph.addEdge(dependencySources, new ComponentDependencySource(t));
                }
            }
            //构造构造函数依赖字典
            Map< Constructor, List<DependencySource> > constructorMap = new HashMap<>();
            constructorDependencyMap.put(t, constructorMap);
            Optional<Constructor> c = scanConstructor(t);
            if (c.isPresent()){
                c.get().setAccessible(true);
                List<DependencySource> dependencySources = getParametersDependencySource(c.get());
                constructorMap.put(c.get(), dependencySources);
                digraph.addEdge(dependencySources, new ComponentDependencySource(t));
            }
        }
        if(digraph.hasErrors()){
            throw new Exception(digraph.getAllErrors().get(0));
        }
    }

    private Object putBean(DependencySource t, Map<DependencySource, Object> singletonMap) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object object = null;
        if (digraph.getAllStartpoints(t).size() == 0) {
            /*
              必须是一个Component
             */
            object = ((ComponentDependencySource)t).getComponentClass().getConstructor().newInstance();
        }else{
            if(t instanceof BeanDependencySource){
                ComponentDependencySource enclosingSource = new ComponentDependencySource(t.getComponentClass());
                Object enclosingObject;
                if((enclosingObject = singletonMap.get(enclosingSource)) == null){
                    enclosingObject = putBean(enclosingSource, singletonMap);
                }
                List<DependencySource> dependencySources = methodDependencyMap.get(t.getComponentClass()).get(((BeanDependencySource)t).getBeanMethod());
                List<Object> parameters = new ArrayList<>();
                for (DependencySource item :
                        dependencySources) {
                    if(singletonMap.get(item) == null){
                        parameters.add(putBean(item, singletonMap));
                    }else{
                        parameters.add(singletonMap.get(item));
                    }
                }
                return ((BeanDependencySource)t).getBeanMethod().invoke(enclosingObject, parameters);
            }
            //创建一个实例
            if(constructorDependencyMap.get(t).size() == 0){
                object = t.getComponentClass().getConstructor().newInstance();
            }
            for (Map.Entry< Constructor, List<DependencySource> > e:
                    constructorDependencyMap.get(t).entrySet()) {
                List<Object> parameters = new ArrayList<>();
                for (DependencySource item :
                        e.getValue()) {
                    if(singletonMap.get(item) == null){
                        parameters.add(putBean(item, singletonMap));
                    }else{
                        parameters.add(singletonMap.get(item));
                    }
                }
                object = e.getKey().newInstance(parameters.toArray());
            }
            //设置property的值
            for (Map.Entry< Field, DependencySource > e:
                    fieldDependencyMap.get(t).entrySet()) {
                if(singletonMap.get(e.getValue()) == null){
                    e.getKey().set(object, putBean(e.getValue(), singletonMap));
                }else{
                    e.getKey().set(object, singletonMap.get(e.getValue()));
                }
            }
            //调用被@Autowired注解的方法
            for (Map.Entry< Method, List<DependencySource> > e:
                    methodDependencyMap.get(t).entrySet()) {
                List<Object> parameters = new ArrayList<>();
                for (DependencySource item :
                        e.getValue()) {
                    if(singletonMap.get(item) == null){
                        parameters.add(putBean(item, singletonMap));
                    }else{
                        parameters.add(singletonMap.get(item));
                    }
                }
                e.getKey().invoke(object, parameters.toArray());
            }
        }
        //若是单例模式，才缓存
        if(scopeTypeMap.get(t) == ScopeType.SINGLETON){
            singletonMap.put(t, object);
        }
        return object;
    }

    public Object getBean(Class requiredType){
        try {
            return putBean(new ComponentDependencySource(requiredType), cachedBeanMap);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return cachedBeanMap.get(requiredType);
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

    private List<DependencySource> getParametersDependencySource(Method m) throws Exception {
        Class[] classes = m.getParameterTypes();
        Annotation[][] annotationMatrix = m.getParameterAnnotations();
        return getParametersDependencySource(classes, annotationMatrix);
    }

    private List<DependencySource> getParametersDependencySource(Constructor m) throws Exception {
        Class[] classes = m.getParameterTypes();
        Annotation[][] annotationMatrix = m.getParameterAnnotations();
        return getParametersDependencySource(classes, annotationMatrix);
    }

    private List<DependencySource> getParametersDependencySource(Class[] classes, Annotation[][] annotationMatrix) throws Exception {
        List<DependencySource> results = new ArrayList<>();
        //classes和annotationMatrix的长度应该是相同的
        for (int i = 0; i < classes.length; i++) {
            Annotation[] annotations = annotationMatrix[i];
            boolean isQualifier = false;
            Class concreteClass;
            for (Annotation annotation:
                    annotations) {
                if(Qualifier.class.isAssignableFrom(annotation.annotationType())){
                    isQualifier = true;
                    String name = AnnotationPropertyResolver.getQualifierValue(annotation);
                    concreteClass = namedComponentScanner.scanImpl(name, classes[i]);
                    if(concreteClass == null){
                        results.add(beanMethodScanner.scanDependency(name, classes[i]));
                    }else{
                        results.add(new ComponentDependencySource(concreteClass));
                    }

                    break;
                }
            }
            if(!isQualifier){
                concreteClass = typedComponentScanner.scanImpl(classes[i]);
                results.add(new ComponentDependencySource(concreteClass));
            }
        }
        return results;
    }
}
