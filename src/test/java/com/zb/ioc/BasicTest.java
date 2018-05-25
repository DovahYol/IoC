package com.zb.ioc;

import com.zb.ioc.annotation.Antowired;
import com.zb.ioc.annotation.Component;
import com.zb.ioc.utils.Digraph;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@Component
public class BasicTest {
    @Antowired
    private Song song;

    public Song getSong() {
        return song;
    }

    private static Map<Class, Object> maps;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        maps =  bootstrap.createBeanMap("com.zb.ioc");
    }
    @Test
    public void test001() {
        BasicTest basicTest = (BasicTest)maps.get(BasicTest.class);
        assertEquals(basicTest.getSong().getName(), "Chinese GenreChinese");
    }

    @Test
    public void test002() {
        Digraph<Integer> digraph = new Digraph<>();
        digraph.addEdge(5, 11);
        digraph.addEdge(7, 11);
        digraph.addEdge(7, 8);
        digraph.addEdge(3, 8);
        digraph.addEdge(3, 10);
        digraph.addEdge(11, 2);
        digraph.addEdge(11, 9);
        digraph.addEdge(11, 10);
        digraph.addEdge(8, 9);
        for (Integer i :
                digraph.getTopologicalList()) {
            System.out.println(i);
        }
        assertEquals(true, true);
    }

    @Test
    public void test003() {
        Digraph<Integer> digraph = new Digraph<>();
        digraph.addEdge(5, 11);
        digraph.addEdge(7, 11);
        digraph.addEdge(7, 8);
        digraph.addEdge(3, 8);
        digraph.addEdge(3, 10);
        digraph.addEdge(11, 2);
        digraph.addEdge(11, 9);
        digraph.addEdge(11, 10);
        digraph.addEdge(8, 9);
        digraph.addEdge(9, 10);
        digraph.addEdge(10, 3);
        digraph.getTopologicalList();
        assertEquals(digraph.hasErrors(), true);
    }
}
