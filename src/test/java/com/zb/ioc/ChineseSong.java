package com.zb.ioc;

import com.zb.ioc.annotation.Autowired;
import com.zb.ioc.annotation.Component;

@Component
public class ChineseSong implements Song{
    @Autowired
    private Genre genre;

    private Singer singer;

    @Autowired
    public ChineseSong(Singer _singer){
        this.singer = _singer;
    }

    public String getName() {
        return genre.getName() + "Chinese";
    }

    public Singer getSinger() {
        return singer;
    }
}
