package com.azwave.androidzwave.distant;

import java.io.Serializable;

/**
 * Created by florian on 20/05/2015.
 */
public class Information implements Serializable {
    private String name, value, unit;

    public Information(){
    	
    }
    
    public Information(String _name, String _value, String _unit){
        name = _name;
        value = _value;
        unit = _unit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {

        return name;
    }

    public String getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }
}
