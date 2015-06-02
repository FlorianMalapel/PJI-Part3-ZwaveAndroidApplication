package com.pji.zwavedistantcontroller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.azwave.androidzwave.distant.NodeToSend;

import pji.zwavedistantcontroller.R;

/**
 * Adapter of a ListView which contains all the Node sent by the main application.
 * Display only the node id and the name of the Node.
 */
public class NodeListAdapter extends ArrayAdapter<NodeToSend> {

    private Context mContext;
    private int listItemResourceId;
    private int PORT;
    private String HOST;

    /**
     * Constructor of the adapter
     * @param _context the context of the activity which create this adapter
     * @param _resource the XML resource managing the view of one item of the adapter
     */
    public NodeListAdapter(Context _context, int _resource) {
        super(_context, _resource);
        mContext = _context;
        listItemResourceId = _resource;
    }


    // Create a new component for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listView = inflater.inflate(listItemResourceId, parent, false);

        // Get the node
        final NodeToSend node = getItem(position);
        // Get the node's name
        final TextView nodeName = (TextView) listView.findViewById(R.id.name);
        // Add the id of the node in the name
        nodeName.setText(String.format("Node %d -- %s", (node.getId()), node.getName()));
        final LinearLayout item = (LinearLayout) listView.findViewById(R.id.item);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*On click on the node, a new activity is open, and different data are send to it
                through a Bundle*/
                Intent intent = new Intent(mContext, NodeDescription_Activity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("node", node);
                bundle.putInt("port", PORT);
                bundle.putString("host", HOST);
                intent.putExtra("bundle", bundle);
                mContext.startActivity(intent);
            }
        });
        return listView;
    }


    public String getHOST() {
        return HOST;
    }

    public void setHOST(String HOST) {
        this.HOST = HOST;
    }

    public int getPORT() {
        return PORT;
    }

    public void setPORT(int PORT) {
        this.PORT = PORT;
    }
}
