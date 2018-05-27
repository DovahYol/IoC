package com.zb.ioc;

import com.zb.ioc.annotation.Component;

@Component
public class JayChou implements Singer{
    @Override
    public String name() {
        return "Jay Chou";
    }
}
