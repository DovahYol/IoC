package com.zb.ioc;

import com.zb.ioc.annotation.Antowired;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BasicTest {
    @Antowired
    private Song song;

    private static Map<Class, Object> maps;

    @BeforeClass
    public static void setUpBeforeClass() {
        Bootstrap bootstrap = new Bootstrap();
        maps =  bootstrap.createBeanMap("com.zb.ioc");
        bootstrap.interceptAllMethods("com.zb.ioc");
    }
    @Test
    public void test001() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (final Field field : fields) {
            if(!field.isAnnotationPresent(Antowired.class)) continue;
            final Class fieldClass = field.getType();
            maps.forEach((k, v) -> {
                if (fieldClass.isAssignableFrom(k)) {
                    try {
                        field.set(this, v);
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
        assertEquals(song.getName(), "Chinese");
    }
}
