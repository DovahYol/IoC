package com.zb.ioc;

import com.zb.ioc.annotation.Antowired;
import com.zb.ioc.annotation.Component;

@Component
public class ChineseSong implements Song{
    @Antowired
    public Genre genre;

    public String getName() {
        return genre.getName() + "Chinese";
    }
}
