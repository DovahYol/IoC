package com.zb.ioc.utils;

import java.util.Objects;

public class ComponentDependency implements Dependency {
    private Class<?> component;

    @Override
    public Class<?> getCmp() {
        return component;
    }

    public ComponentDependency(Class<?> _component){
        component = _component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentDependency that = (ComponentDependency) o;
        return Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {

        return Objects.hash(component);
    }
}
