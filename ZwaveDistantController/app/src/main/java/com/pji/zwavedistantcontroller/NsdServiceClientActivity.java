package com.pji.zwavedistantcontroller;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.azwave.androidzwave.distant.NodeToSend;

import java.net.InetAddress;

import pji.zwavedistantcontroller.R;


/**
 * Main activity of the application, first this activity discover an android NsdService
 * to get the IP address and Port on which the main application's server is listening, once
 * this informations found, a request to get all the Nodes is sent and a ListView is built
 * with all this nodes.
 */
public class NsdServiceClientActivity extends Activity {

    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdServiceInfo mService;
    private static String SERVICE_NAME = "ZwaveControllerInfo";
    private static String SERVICE_TYPE = "_http._tcp.";
    private Request request;
    private ListView list;
    private NodeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link to components in XML file
        list = (ListView) findViewById(R.id.list);
        adapter = new NodeListAdapter(this, R.layout.item_listview);
        list.setAdapter(adapter);

        // Create the NsdManager
        mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        // Initialize the listeners
        initializeResolveListener();
        initializeDiscoveryListener();
        // Launch the search of the NsdService
        mNsdManager.discoverServices( SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }


    /**
     * Initialize the discovery listener 'mDiscoveryListener'
     */
    public void initializeDiscoveryListener() {
        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d("DEBUG", "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d("DEBUG", "Service discovery success " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d("DEBUG", "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().contains(SERVICE_NAME)){
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e("DEBUG", "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i("DEBUG", "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("DEBUG", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e("DEBUG", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    /**
     * Initialize the resolveListener 'mResolveListerner' which allow, once found the service,
     * to get the port & IP address on which the main application is listening, and so to send
     * a request to this application to get all the Node on the Zwave network.
     */
    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e("DEBUG", "Resolve failed:" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
                // Create a new Request
                request = new Request(host.getHostAddress(), port);
                /*Send the request to get all the nodes on the Zwave
                network and add them to the adapter*/
                for(final NodeToSend node: request.getNodesRequest()){
                    NsdServiceClientActivity.this.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    adapter.add(node);
                                }
                            }
                    );
                }
                // Transmit the port & IP address to the adapter
                adapter.setHOST(host.getHostAddress());
                adapter.setPORT(port);
                // Close the request to close all the services it used
                request.close();
            }
        };
    }

    @Override
    protected void onDestroy() {
        tearDown();
        super.onDestroy();
    }

    /**
     * Stop the NsdManager to use 'mDiscoverylistener' to find services.
     */
    public void tearDown() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        request.close();
    }

}
