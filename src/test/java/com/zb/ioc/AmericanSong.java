package com.zb.ioc;

import com.zb.ioc.annotation.Component;

@Component("American")
public class AmericanSong implements Song{

    @Override
    public String getName() {
        return "American Song";
    }
}
