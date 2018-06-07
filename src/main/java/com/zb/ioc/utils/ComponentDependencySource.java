package com.zb.ioc.utils;

import java.util.Objects;

public class ComponentDependencySource implements DependencySource{
    private Class<?> component;

    @Override
    public Class<?> getComponentClass() {
        return component;
    }

    public ComponentDependencySource(Class<?> _component){
        component = _component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentDependencySource that = (ComponentDependencySource) o;
        return Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {

        return Objects.hash(component);
    }
}
