package com.zb.ioc;

import com.zb.ioc.annotation.Component;

@Component("TaylorSwift")
public class TaylorSwift implements Singer{
    @Override
    public String name() {
        return "Taylor Swift";
    }
}
