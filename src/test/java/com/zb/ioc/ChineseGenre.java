package com.zb.ioc;

import com.zb.ioc.annotation.Component;

@Component
public class ChineseGenre implements Genre{
    @Override
    public String getName() {
        return "Chinese Genre";
    }
}
