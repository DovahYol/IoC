package com.zb.ioc.utils;

import java.lang.reflect.Method;
import java.util.Objects;

public class BeanDependencySource implements DependencySource{

    private Class<?> component;

    private Method beanMethod;

    public BeanDependencySource(Class<?> _component, Method _beanMethod){
        component = _component;
        beanMethod = _beanMethod;
    }

    @Override
    public Class<?> getComponentClass() {
        return component;
    }

    public Method getBeanMethod(){
        return beanMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanDependencySource that = (BeanDependencySource) o;
        return Objects.equals(component, that.component) &&
                Objects.equals(beanMethod, that.beanMethod);
    }

    @Override
    public int hashCode() {

        return Objects.hash(component, beanMethod);
    }
}
