package com.zb.ioc.utils;

import com.zb.ioc.annotation.Bean;
import com.zb.ioc.annotation.Qualifier;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeanMethodScanner {

    private static class BeanMethod{
        private Class<?> enclosingClass;
        private Method beanMethod;
        private Class<?> returnValueType;
        private String beanName;
    }

    private final List<BeanMethod> beanMethods = new ArrayList<>();

    public BeanMethodScanner(Iterator<Class<?>> iterator){
        while (iterator.hasNext()){
            Class<?> component = iterator.next();
            for (Method m:
                 component.getDeclaredMethods()) {
                if(m.isAnnotationPresent(Bean.class)){
                    BeanMethod beanMethod = new BeanMethod();
                    beanMethod.enclosingClass = component;
                    beanMethod.beanMethod = m;
                    beanMethod.returnValueType = m.getReturnType();
                    //先从@Bean里取，若有@Qualifier，再从其中取
                    String beanName;
                    Bean beanAnnotation = m.getAnnotation(Bean.class);
                    beanName = beanAnnotation.value();
                    if(m.isAnnotationPresent(Qualifier.class)){
                        Qualifier qualifierAnnotation = m.getAnnotation(Qualifier.class);
                        beanName = qualifierAnnotation.value();
                    }
                    beanMethod.beanName = beanName;
                    beanMethods.add(beanMethod);
                }
            }
        }
    }

    public BeanDependency scanDependency(String name, Class<?> declaringType) throws Exception {
        List<BeanDependency> results = new ArrayList<>();
        for (int i = 0; i < beanMethods.size(); i++) {
            if((name == null || name.equals(beanMethods.get(i).beanName))
                    && declaringType.isAssignableFrom(beanMethods.get(i).returnValueType)){
                results.add(new BeanDependency(beanMethods.get(i).enclosingClass, beanMethods.get(i).beanMethod));
            }
        }
        if(results.size() == 0){
            throw new Exception(String.format("并未找到名字为%s的%s的实现类", name, declaringType));
        }else if(results.size() > 1){
            throw new Exception(String.format("存在多个名字为%s的%s的实现类", name, declaringType));
        }else{
            return results.get(0);
        }
    }

    public BeanDependency scanDependency(Class<?> declaringType) throws Exception {
        return scanDependency(null, declaringType);
    }
}
