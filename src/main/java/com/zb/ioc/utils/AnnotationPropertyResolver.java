package com.zb.ioc.utils;

import com.zb.ioc.annotation.Component;
import com.zb.ioc.annotation.Qualifier;
import com.zb.ioc.annotation.Scope;
import com.zb.ioc.annotation.ScopeType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AnnotationPropertyResolver {

    public static String getQualifierValue(Field f){
        Qualifier annotation = f.getAnnotation(Qualifier.class);
        return annotation.value();
    }

    public static String getQualifierValue(Annotation it){
        return ((Qualifier)it).value();
    }

    public static String getComponentValue(Class it){
        Component annotation = (Component)it.getAnnotation(Component.class);
        return annotation.value();
    }

    public static ScopeType getScopeType(Class it){
        Scope annotation = (Scope)it.getAnnotation(Scope.class);
        return annotation.value();
    }

    public static ScopeType getScopeType(Method it){
        Scope annotation = it.getAnnotation(Scope.class);
        return annotation.value();
    }
}
