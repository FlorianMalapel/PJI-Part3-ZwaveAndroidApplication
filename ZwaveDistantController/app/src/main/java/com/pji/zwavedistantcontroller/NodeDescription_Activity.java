package com.pji.zwavedistantcontroller;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.azwave.androidzwave.distant.Information;
import com.azwave.androidzwave.distant.NodeToSend;

import pji.zwavedistantcontroller.R;


/**
 * Activity displaying a single Node after select it in an list of Node,
 * display all the informations of this Node, the components to manage this Node and
 * send Request to the main application.
 */
public class NodeDescription_Activity extends Activity {

    private NodeToSend node;
    private int PORT;   // On which the main application is listening
    private String HOST; // Ip of the main application
    private Request request; // Allow to control the Nodes from this application

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_description);

        // Get the data from the NodeListAdapter
        Bundle bundle = getIntent().getBundleExtra("bundle");
        node = (NodeToSend) bundle.getSerializable("node");
        PORT = bundle.getInt("port");
        HOST = bundle.getString("host");

        // Link the components
        final Switch nodeSwitch = (Switch) findViewById(R.id.node_switch);
        nodeSwitch.setVisibility(View.GONE);
        final SeekBar nodeSeekbar = (SeekBar) findViewById(R.id.node_seek);
        nodeSeekbar.setVisibility(View.GONE);
        final LinearLayout nodeController = (LinearLayout) findViewById(R.id.node_controller);
        nodeController.setVisibility(View.GONE);
        final Button nodeAdd = (Button) findViewById(R.id.node_add_controller);
        nodeAdd.setVisibility(View.GONE);
        final Button nodeRemove = (Button) findViewById(R.id.node_remove_controller);
        nodeRemove.setVisibility(View.GONE);
        final LinearLayout listValues = (LinearLayout) findViewById(R.id.listValues);
        createListValues(listValues);

        // Display the good components depending of the functionalities of the Node
        // if the node is a Controller
        if (node.getMainController()) {
            nodeController.setVisibility(View.VISIBLE);
            nodeAdd.setVisibility(View.VISIBLE);
            nodeRemove.setVisibility(View.VISIBLE);
        }
        // else if the node is a binarySwitch
        else if (node.getOnOffController()) {
            // Get the value of the Switch and adapt the value of the switch
            for(Information i : node.getInformations()){
                if(i.getName().equals("Switch") && i.getValue().equals("true")){
                    nodeSwitch.setChecked(true);
                }
                else if(i.getName().equals("Switch") && i.getValue().equals("false")){
                    nodeSwitch.setChecked(false);
                }
            }
            nodeSwitch.setVisibility(View.VISIBLE);

            // Listener to send Request on action
            nodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                request = new Request(HOST, PORT);
                                request.sendSwitchOn(node);
                                request.close();
                                return null;
                            }
                        };

                        task.execute();

                        for(Information i : node.getInformations()) {
                            if (i.getName().equals("Switch")) {
                                i.setValue("true");
                            }
                        }
                    } else {
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                request = new Request(HOST, PORT);
                                request.sendSwitchOff(node);
                                request.close();
                                return null;
                            }
                        };

                        task.execute();

                        for(Information i : node.getInformations()) {
                            if (i.getName().equals("Switch")) {
                                i.setValue("false");
                            }
                        }
                    }
                }
            });
        } else if (node.getSliderController()) {
            nodeSeekbar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Put all the data in the ArrayList informations in a LinearLayout
     * to display them.
     * @param listValues : The LinearLayout which will contains all the Informations
     */
    public void createListValues(LinearLayout listValues){
        for(Information i : node.getInformations()){
            TextView value = new TextView(this);
            TextView label = new TextView(this);
            TextView unit = new TextView(this);
            value.setText( i.getValue());
            label.setText( i.getName());
            unit.setText( i.getUnit());
            value.setGravity(Gravity.RIGHT);
            unit.setGravity(Gravity.RIGHT);
            value.setTextColor(Color.rgb(0, 0, 0));
            label.setTextColor(Color.rgb(0, 0, 0));
            unit.setTextColor(Color.rgb(0, 0, 0));
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams containerParam = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
            containerParam.setMargins(0, 2, 0, 0);
            container.setBackgroundColor(Color.rgb(250, 250, 250));
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams
                    (0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            container.setLayoutParams(containerParam);
            value.setLayoutParams(param);
            label.setLayoutParams(param);
            unit.setLayoutParams(param);
            container.addView(label);
            container.addView(value);
            container.addView(unit);
            listValues.addView(container);
        }
    }
}
