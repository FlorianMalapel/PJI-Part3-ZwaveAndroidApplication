/**
 * 
 */
package com.azwave.androidzwave.distant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.azwave.androidzwave.distant.NodeToSend;
import com.azwave.androidzwave.distant.Information;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerCommand;
import com.azwave.androidzwave.zwave.nodes.Controller;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.MainActivity;

/**
 * @author florian.malapel@gmail.com
 *
 */
public class Server extends AsyncTask<Void, Void, Void> {
	
	private Context context;
    private ServerSocket serverSocket; 
    private MainActivity activity;
    private BufferedReader reader;
    private PrintWriter writer;
    private ObjectInputStream objectReader;
    private ObjectOutputStream objectWriter;
    private static int PORT;

    public Server(MainActivity activity, int port) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.PORT = port;
    }

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
            Log.e("DEBiG", e.getMessage());
        }
		
		Socket client = null;
		try {
			while(true){
				client = serverSocket.accept();
				new Request(client).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
    }

	
	@Override
	public void onPostExecute(Void result){
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Internal Class
	 */
	public class Request extends Thread {
	    protected Socket client;

	    public Request(Socket clientSocket) {
	        this.client = clientSocket;
	    }

	    public void run() {
	        try {
	        	reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				writer = new PrintWriter(client.getOutputStream());
				objectWriter = new ObjectOutputStream(client.getOutputStream());
				objectWriter.flush();
				objectReader = new ObjectInputStream(client.getInputStream());
				String query = reader.readLine();
				if(query == null){
					return;
				}
				// If the request is "NODES", the server send the list of nodes serializable
				if(query.equals("NODES")){
					ArrayList<NodeToSend> nodes = new ArrayList<NodeToSend>();
					for(int i=0; i<activity.getAdapter().getCount(); i++){
						nodes.add(activity.getAdapter().getItem(i).getNodeToSend());
					}
					objectWriter.writeObject(nodes);
					objectWriter.flush();
				}
				else if(query.equals("ADD")) {
					Controller controller = activity.getManager()
							.getController();
					controller.getQueueManager().sendControllerCommand(
							ControllerCommand.AddDevice, false,
							controller.getNodeId());
				}
				else if(query.equals("REMOVE")) {
					Controller controller = activity.getManager()
							.getController();
					controller.getQueueManager().sendControllerCommand(
							ControllerCommand.RemoveDevice, false,
							controller.getNodeId());
				}
				else {
					if(query.contains("#")){
						String[] keywords = query.split("#");
						if (keywords.length == 2 && (keywords[1].equals("ON"))) {
							Node node = activity.getAdapter().getNodeById(Integer.parseInt(keywords[0]));
							node.setOn();
						}
						else if (keywords.length == 2 && (keywords[1].equals("OFF"))) {
							Node node = activity.getAdapter().getNodeById(Integer.parseInt(keywords[0]));
							node.setOff();
						}
						else if(keywords.length == 3 && (keywords[1].equals("DIMMER"))){
							Node node = activity.getAdapter().getNodeById(Integer.parseInt(keywords[0]));
							node.setLevel(keywords[2].getBytes()[0]);
						}
					}
				}
				
				client.close();
				reader.close();
				writer.close();
				objectWriter.close();
				objectReader.close();
	        } catch (IOException e) {
	            return;
	        }
			return;
	    }
	}

}
