package com.azwave.androidzwave.distant;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Object representing a Node with only information which can interest distant applications.
 * It's serializable to allow to send it with sockets.
 */
public class NodeToSend implements Serializable {

    // id of the Node
    private int id;
    // Name of the Node
    private String name;
    // All the informations send by the Node
    private ArrayList<Information> informations;
    // Boolean to know which component we need to display to manage this node
    private Boolean onOffController = false,
            sliderController = false,
            mainController = false;

    /**
     * Constructor of a NodeToSend, don't have the Informations of the node in param,
     * so we just create an empty ArrayList<Information>.
     * @param _id of the node
     * @param _name of the node
     * @param _onOffController true if the node is a binary switch, else false
     * @param _sliderController true if the node is a multilevel switch, else false
     * @param _mainController true if the node is a controller, else false
     */
    public NodeToSend(int _id, String _name, Boolean _onOffController,
                      Boolean _sliderController, Boolean _mainController){
        id = _id;
        name = _name;
        informations = new ArrayList<Information>();
        onOffController = _onOffController;
        sliderController = _sliderController;
        mainController = _mainController;
    }

    /**
     * Constructor of a NodeToSend
     * @param _id of the node
     * @param _name of the node
     * @param _info ArrayList<Information> which contains all the informations return by the node
     * @param _onOffController true if the node is a binary switch, else false
     * @param _sliderController true if the node is a multilevel switch, else false
     * @param _mainController true if the node is a controller, else false
     */
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

