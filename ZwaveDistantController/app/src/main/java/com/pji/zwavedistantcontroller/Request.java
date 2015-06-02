package com.pji.zwavedistantcontroller;

import android.util.Log;

import com.azwave.androidzwave.distant.NodeToSend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by florian on 21/05/2015.
 */
public class Request {

    private Socket socket;
    private String line;
    private static String HOST;
    private static int PORT;
    private BufferedReader reader;
    private PrintWriter writer;
    private ObjectInputStream objectReader;
    private ObjectOutputStream objectWriter;

    /**
     * Create a object Request and connect a socket to host & port in parameters,
     * and create writers & readers to communicate with the main application.
     * @param host : IP address on the main application
     * @param port : port on which the main application is listening
     */
    public Request(String host, int port){
        PORT = port;
        HOST = host;
        InetSocketAddress addr = new InetSocketAddress(host,PORT);
        try {
            socket = new Socket();
            socket.bind(null);
            socket.connect(addr);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            objectWriter = new ObjectOutputStream(socket.getOutputStream());
            objectWriter.flush();
            objectReader = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Request to get all the Node of the Zwave network from the main application
     * @return an ArrayList<NodeToSend> of the Nodes on the network.
     */
    public ArrayList<NodeToSend> getNodesRequest(){
        ArrayList<NodeToSend> nodes = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.println("NODES");
            writer.flush();
            nodes = (ArrayList<NodeToSend>)objectReader.readObject();
        } catch (ClassNotFoundException e) {
            Log.e(e.getClass().getName(), e.getMessage());

        } catch (IOException e) {
            Log.e(e.getClass().getName(), e.getMessage());
        }
        return nodes;
    }

    /**
     * Send a request to the main application to turn on a Node of type
     * binary switch.
     * @param node the node to turn on
     */
    public void sendSwitchOn(NodeToSend node){
        writer.println(node.getId() + "#ON");
        writer.flush();
    }

    /**
     * Send a request to the main application to turn off a Node of type
     * binary switch.
     * @param node the node to turn on
     */
    public void sendSwitchOff(NodeToSend node){
        writer.println(node.getId() + "#OFF");
        writer.flush();
    }

    /**
     * Close the socket and all the writers & readers use for the request
     */
    public void close(){
        try {
            if(socket != null)
                socket.close();
            if(reader != null)
                reader.close();
            if(writer != null)
                writer.close();
            if(objectReader != null)
                objectReader.close();
            if(objectWriter != null)
                objectWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
