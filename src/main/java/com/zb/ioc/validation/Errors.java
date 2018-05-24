package com.zb.ioc.validation;

import java.util.ArrayList;
import java.util.List;

public class Errors {
    private List<String> errors = new ArrayList<>();

    public boolean hasErrors(){
        return errors.size() == 0;
    }

    public List<String> getAllErrors(){
        return errors;
    }

    public void setError(String error){
        errors.add(error);
    }
}
