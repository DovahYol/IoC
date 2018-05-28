package com.zb.ioc;

import com.zb.ioc.annotation.Autowired;
import com.zb.ioc.annotation.Component;

@Component
public class ChineseSong implements Song{
    @Autowired
    public Genre genre;

    public String getName() {
        return genre.getName() + "Chinese";
    }
}
