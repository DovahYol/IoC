package com.zb.ioc;

import com.zb.ioc.annotation.Component;

@Component("County")
public class CountyGenre implements Genre{
    @Override
    public String getName() {
        return "County Genre";
    }
}
