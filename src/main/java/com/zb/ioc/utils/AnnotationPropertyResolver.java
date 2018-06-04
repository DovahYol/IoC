package com.zb.ioc.utils;

import com.zb.ioc.annotation.Component;
import com.zb.ioc.annotation.Qualifier;
import com.zb.ioc.annotation.Scope;
import com.zb.ioc.annotation.ScopeType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnnotationPropertyResolver {

    public static String getQualifierValue(Field f) throws Exception {
        Annotation annotation = f.getAnnotation(Qualifier.class);
        return getQualifierValue(annotation);
    }

    public static String getQualifierValue(Annotation annotation) throws Exception {
        Method method;
        String value;
        try {
            method = annotation.annotationType().getDeclaredMethod("value");
            value = (String)method.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new Exception(e);
        }
        return value;
    }

    public static String getComponentValue(Class it) throws Exception {
        Annotation annotation = it.getAnnotation(Component.class);
        Method method;
        String value;
        try {
            method = annotation.annotationType().getDeclaredMethod("value");
            value = (String)method.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new Exception(e);
        }
        return value;
    }

    public static ScopeType getScopeType(Class it) throws Exception {
        Annotation annotation = it.getAnnotation(Scope.class);
        Method method;
        ScopeType value;
        try {
            method = annotation.annotationType().getDeclaredMethod("value");
            value = (ScopeType)method.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new Exception(e);
        }
        return value;
    }
}
