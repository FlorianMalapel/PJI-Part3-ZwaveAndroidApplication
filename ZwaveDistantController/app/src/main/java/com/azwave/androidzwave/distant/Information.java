package com.azwave.androidzwave.distant;

import java.io.Serializable;

/**
 * Object representing an information of a Node with a name, a value and an unit.
 * It's serializable to allow to send it with sockets.
 */
public class Information implements Serializable {
    private String name, value, unit;

    /**
     * Default Constructor
     */
    public Information(){

    }

    /**
     * Constructor of an information
     * @param _name of the information
     * @param _value of the information
     * @param _unit of the information
     */
    public Information(String _name, String _value, String _unit){
        name = _name;
        value = _value;
        unit = _unit;
    }

    /**
     * Set the name of the information
     * @param name the name of this information
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the value of the information
     * @param value of this information
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Set the unit of the information's value
     * @param unit of the information's value
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Return the name of this information
     * @return name of the information
     */
    public String getName() {
        return name;
    }

    /**
     * Return the value of this information
     * @return value of the information
     */
    public String getValue() {
        return value;
    }

    /**
     * Return the unit of the information's value
     * @return unit of the information's value
     */
    public String getUnit() {
        return unit;
    }
}

