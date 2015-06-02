package com.azwave.androidzwave.distant;

import java.io.Serializable;
import java.util.ArrayList;
import com.azwave.androidzwave.distant.Information;

/**
 * Created by florian on 20/05/2015.
 */
public class NodeToSend implements Serializable {

    private int id;
    private String name;
    private ArrayList<Information> informations;
    private Boolean onOffController = false,
            sliderController = false,
            mainController = false;

    public NodeToSend(int _id, String _name, Boolean _onOffController,
                      Boolean _sliderController, Boolean _mainController){
        id = _id;
        name = _name;
        informations = new ArrayList<Information>();
        onOffController = _onOffController;
        sliderController = _sliderController;
        mainController = _mainController;
    }

    public NodeToSend(int _id, String _name, ArrayList<Information> _info, Boolean _onOffController,
                      Boolean _sliderController, Boolean _mainController){
        id = _id;
        name = _name;
        informations = _info;
        onOffController = _onOffController;
        sliderController = _sliderController;
        mainController = _mainController;
    }

    public Boolean getOnOffController() {
        return onOffController;
    }

    public void setOnOffController(Boolean onOffController) {
        this.onOffController = onOffController;
    }

    public Boolean getSliderController() {
        return sliderController;
    }

    public void setSliderController(Boolean sliderController) {
        this.sliderController = sliderController;
    }

    public Boolean getMainController() {
        return mainController;
    }

    public void setMainController(Boolean mainController) {
        this.mainController = mainController;
    }

    public void addInformation(Information info){
        informations.add(info);
    }

    public Information getInformation(int i){
        return informations.get(i);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInformations(ArrayList<Information> informations) {
        this.informations = informations;
    }

    public int getId() {

        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Information> getInformations() {
        return informations;
    }
}
