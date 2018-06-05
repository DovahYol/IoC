package com.zb.ioc;

import com.zb.ioc.annotation.*;
import com.zb.ioc.utils.AnnotationPropertyResolver;
import com.zb.ioc.utils.Digraph;
import com.zb.ioc.utils.NamedComponentScanner;
import com.zb.ioc.utils.TypedComponentScanner;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class Bootstrap {
    private final Set<Class<?>> annotated;
    private final NamedComponentScanner namedComponentScanner;
    private final TypedComponentScanner typedComponentScanner;
    //BeanMap缓存
    private final Map<Class, Object> cachedBeanMap = new HashMap<>();
    //Scope字典
    private final Map<Class, ScopeType> scopeTypeMap = new HashMap<>();
    //属性依赖字典
    private final Map<Class, Map<Field, Class>> fieldDependencyMap = new HashMap<>();
    //方法依赖字典
    private final Map<Class, Map<Method, List<Class>>> methodDependencyMap = new HashMap<>();
    //构造函数依赖字典
    private final Map<Class, Map<Constructor, List<Class>>> constructorDependencyMap = new HashMap<>();
    //存放各Component之间的依赖
    private final Digraph<Class> digraph = new Digraph<>();
    //依赖的拓扑排序
    private List<Class> topologicalList;

    Bootstrap(String packageName) throws Exception {
        Reflections reflections = new Reflections(packageName);
        //支持类注解提供依赖
        annotated = reflections.getTypesAnnotatedWith(Component.class);
        //Named Component
        namedComponentScanner = new NamedComponentScanner(annotated.iterator());
        //Typed Component
        typedComponentScanner = new TypedComponentScanner(annotated.iterator());
        //构建依赖
        buildDependency();
        buildCachedBeanMap();
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
            Map< Field, Class > fieldMap = new HashMap<>();
            fieldDependencyMap.put(t, fieldMap);
            Field[] fields = t.getDeclaredFields();
            for (Field f:
                 fields) {
                if(f.isAnnotationPresent(Autowired.class)){
                    f.setAccessible(true);//private的field也可以注入
                    Class concreteClass;
                    if(f.isAnnotationPresent(Qualifier.class)){
                        String name = AnnotationPropertyResolver.getQualifierValue(f);
                        concreteClass = namedComponentScanner.scanImpl(name, f.getType());
                    }else{
                        concreteClass = typedComponentScanner.scanImpl(f.getType());
                    }
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
                    List<Class> classes = getParametersClasses(m);
                    methodMap.put(m, classes);
                    digraph.addEdge(classes, t);
                }
            }
            //构造构造函数依赖字典
            Map< Constructor, List<Class> > constructorMap = new HashMap<>();
            constructorDependencyMap.put(t, constructorMap);
            Optional<Constructor> c = scanConstructor(t);
            if (c.isPresent()){
                c.get().setAccessible(true);
                List<Class> classes = getParametersClasses(c.get());
                constructorMap.put(c.get(), classes);
                digraph.addEdge(classes, t);
            }
        }
        topologicalList = digraph.getTopologicalList();
        if(digraph.hasErrors()){
            throw new Exception(digraph.getAllErrors().get(0));
        }
    }

    private void buildCachedBeanMap() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for (Class t :
                topologicalList) {
            putBean(t, cachedBeanMap);
        }
    }

    private void putBean(Class t, Map<Class, Object> requiredMap) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (digraph.getAllStartpoints(t).size() == 0) {
            /**
             * 以后要做检查，看看是否有无参数构造函数。
             * 现在先用Class来做识别，以后有来自运行时jar包的注入时，会改动
             */
            requiredMap.put(t, t.getConstructor().newInstance());
        }else{
            //创建一个实例
            Object object = null;
            if(constructorDependencyMap.get(t).size() == 0){
                object = t.getConstructor().newInstance();
            }
            for (Map.Entry< Constructor, List<Class> > e:
                    constructorDependencyMap.get(t).entrySet()) {
                Object[] parameters = e.getValue().stream()
                        .map(requiredMap::get)
                        .toArray();
                object = e.getKey().newInstance(parameters);
            }
            //设置property的值
            for (Map.Entry< Field, Class > e:
                    fieldDependencyMap.get(t).entrySet()) {
                if(requiredMap.get(e.getValue()) == null){
                    throw new NullPointerException();
                }
                e.getKey().set(object, requiredMap.get(e.getValue()));
            }
            //调用被@Autowired注解的方法
            for (Map.Entry< Method, List<Class> > e:
                    methodDependencyMap.get(t).entrySet()) {
                Object[] parameters = e.getValue().stream()
                        .map(requiredMap::get)
                        .toArray();
                e.getKey().invoke(object, parameters);
            }

            requiredMap.put(t, object);
        }
    }

    public Object getBean(Class requiredType){
        if(scopeTypeMap.get(requiredType) == ScopeType.SINGLETON){
            return cachedBeanMap.get(requiredType);
        }else{
            Map<Class, Object> temp = new HashMap<>();
            for (Class t :
                    topologicalList) {
                //如果是单例模式的话就直接取用，否则就重新构造
                if(scopeTypeMap.get(t) == ScopeType.SINGLETON){
                    temp.put(t, cachedBeanMap.get(t));
                    continue;
                }
                try{
                    putBean(t, temp);
                }catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {

                }
                if(t == requiredType) break;
            }
            return temp.get(requiredType);
        }
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

    private List<Class> getParametersClasses(Method m) throws Exception {
        Class[] classes = m.getParameterTypes();
        Annotation[][] annotationMatrix = m.getParameterAnnotations();
        return getParametersClasses(classes, annotationMatrix);
    }

    private List<Class> getParametersClasses(Constructor m) throws Exception {
        Class[] classes = m.getParameterTypes();
        Annotation[][] annotationMatrix = m.getParameterAnnotations();
        return getParametersClasses(classes, annotationMatrix);
    }

    private List<Class> getParametersClasses(Class[] classes, Annotation[][] annotationMatrix) throws Exception {
        List<Class> results = new ArrayList<>();
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
                    results.add(concreteClass);
                    break;
                }
            }
            if(!isQualifier){
                concreteClass = typedComponentScanner.scanImpl(classes[i]);
                results.add(concreteClass);
            }
        }
        return results;
    }
}
