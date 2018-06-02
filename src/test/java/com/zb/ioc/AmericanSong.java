package com.zb.ioc;

import com.zb.ioc.annotation.Autowired;
import com.zb.ioc.annotation.Component;
import com.zb.ioc.annotation.Qualifier;

@Component("American")
public class AmericanSong implements Song{

    @Override
    public String getName() {
        return "American Song";
    }

    private Singer singer;

    private Genre genre;

    @Autowired
    public AmericanSong(@Qualifier("TaylorSwift") Singer _singer){
        this.singer = _singer;
    }

    @Autowired
    public void setGenre(@Qualifier("County") Genre genre) {
        this.genre = genre;
    }

    public Singer getSinger() {
        return singer;
    }

    public Genre getGenre() {
        return genre;
    }
}
