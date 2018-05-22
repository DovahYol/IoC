package com.zb.ioc;

import com.zb.ioc.annotation.Component;

@Component
public class ChineseSong implements Song{
    public String getName() {
        return "Chinese";
    }
}
