package com.azwave.androidzwave;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.azwave.androidzwave.distant.Server;
import com.azwave.androidzwave.module.NodeGridAdapter;
import com.azwave.androidzwave.zwave.Manager;
import com.azwave.androidzwave.zwave.driver.UsbSerialDriver;
import com.azwave.androidzwave.zwave.driver.UsbSerialProber;
import com.azwave.androidzwave.zwave.items.ControllerActionListener;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerError;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerState;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.nodes.NodeListener;

public class MainActivity extends Activity implements NodeListener, ControllerActionListener {

	// --- View 
	private NodeGridAdapter nodeGridAdapter;
	private ListViewUpdate listViewUpdate;
	private GridView zwaveNodeList;
	private Button refresh;
	private Button serverReady;
	// --------
	
	// --- Zwave
	private UsbManager usbManager;
	private UsbSerialDriver serialDriver;
	private Manager zwaveManager;
	private Boolean finish = false;
	private long initEndTime;
	private long initStartTime;
	// Allow to have permissions to access to the usb key
		private static final String ACTION_USB_PERMISSION = "com.azwave.androidzwave.USB_PERMISSION";
		private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        if (ACTION_USB_PERMISSION.equals(action)) {
		            synchronized (this) {
		                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
		                	Log.d("DEBUG", "permission accepted for device " + device);
		                } 
		                else { Log.d("DEBUG", "permission denied for device " + device); }
		            }
		        }
		    }
		};
	// ---------------------
	
	// --- Server for distant application
	private static int PORT = 50506;
	private NsdManager.RegistrationListener mRegistrationListener;
	private String mServiceName;
	private static String SERVICE_NAME = "ZwaveControllerInfo";
	private NsdManager mNsdManager;
	// -----------------------------------
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		zwaveNodeList = (GridView) findViewById(R.id.gridview);
		
		initUsbDriver();
		refresh = (Button) findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				zwaveManager.refreshValuesAllNodes();
			}
		});
		serverReady = (Button) findViewById(R.id.serverState);
		
		registerService(PORT);
		Server server = new Server(this, PORT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
		else
			server.execute((Void[])null);
	}

	@Override
	public void finish() {
		if (serialDriver != null) {
			try {
				serialDriver.close();
				zwaveManager.close();
				listViewUpdate.close();
			} catch (Exception x) {
			}
		}
		unregisterReceiver(mUsbReceiver);
		super.finish();
	}

	private void initUsbDriver() {
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		serialDriver = UsbSerialProber.acquire(usbManager, mPermissionIntent);
		try {
			zwaveManager = new Manager(this, serialDriver);			

			listViewUpdate = new ListViewUpdate();
			listViewUpdate.execute(zwaveManager);

			zwaveManager.setNodeListener(this);
			zwaveManager.setControllerActionListener(this);

			serialDriver.open();
			initStartTime = System.currentTimeMillis();
			zwaveManager.open();
			
			nodeGridAdapter = new NodeGridAdapter(this, R.layout.main_grid_item_node);
			nodeGridAdapter.setNotifyOnChange(true);
			zwaveNodeList.setAdapter(nodeGridAdapter);
			
		} catch (Exception x) {
			finish();
		}
	}
	
	private class ListViewUpdate extends AsyncTask<Manager, ArrayList<Node>, Void> {

		public volatile boolean lock = true;
		public volatile int size = 0;
		public volatile boolean foundupdate = false;

		@Override
		protected Void doInBackground(Manager... arg0) {
			ArrayList<Node> nodes = null;
			while (lock) {
				if (foundupdate) {
					nodes = arg0[0].getAllNodesAlive();
					publishProgress(nodes);
					if (foundupdate) {
						foundupdate = false;
					}
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(ArrayList<Node>... progress) {
			if (progress[0] != null) {
				nodeGridAdapter.clear();
				for (int i = 0; i < progress[0].size(); i++) {
					nodeGridAdapter.add(progress[0].get(i));
				}
				nodeGridAdapter.notifyDataSetChanged();
			}
		}
		public synchronized void close() {
			lock = false;
		}
	}

	@Override
	public void onNodeAliveListener(boolean alive) {
		listViewUpdate.foundupdate = true;
	}

	@Override
	public void onNodeQueryStageCompleteListener() {
		listViewUpdate.foundupdate = true;
		final Activity nowActivity = this;
		if (zwaveManager != null && zwaveManager.isAllNodesQueried()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					nowActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							initEndTime = System.currentTimeMillis();
							if(!finish){
								finish = true;
								zwaveManager.refreshValuesAllNodes();
							}
							else {
								serverReady.setBackgroundColor(Color.rgb(153, 204, 0));
								serverReady.setText("Server ready");
							}
						}
					});
				}
			}).run();
		}
	}

	@Override
	public void onNodeAddedToList() {
	}

	@Override
	public void onNodeRemovedToList() {
		listViewUpdate.foundupdate = true;
	}

	@Override
	public void onAction(final ControllerState state, ControllerError error, Object context) {
		final Activity nowActivity = this;
		new Thread(new Runnable() {
			public void run() {
				nowActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switch (state) {
						case Waiting:
							Toast.makeText(nowActivity, "Waiting for node initiator ...", Toast.LENGTH_LONG).show();
							break;
						case InProgress:
							Toast.makeText(nowActivity, "Plase wait ...", Toast.LENGTH_LONG).show();
							break;
						case Completed:
							Toast.makeText(nowActivity, "Controller command complete ...", Toast.LENGTH_LONG).show();
							break;
						case Failed:
							Toast.makeText(nowActivity, "Controller command failed ...", Toast.LENGTH_LONG).show();
							break;
						}
					}
				});
			}
		}).run();
	}
	
	public void registerService(int port) {
		// Create the NsdServiceInfo object, and populate it.
		NsdServiceInfo serviceInfo = new NsdServiceInfo();
		// The name is subject to change based on conflicts
		// with other services advertised on the same network.
		serviceInfo.setServiceName(SERVICE_NAME);
		serviceInfo.setServiceType("_http._tcp.");
		serviceInfo.setPort(PORT);

		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& !inetAddress.isLinkLocalAddress()
							&& inetAddress.isSiteLocalAddress()) {
						serviceInfo.setHost(inetAddress);
					}

				}
			}
		} catch (SocketException ex) {
			Log.e("DEBUG", ex.toString());
		}
		
		initializeRegistrationListener();
		
		mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
		mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
	}

		public void initializeRegistrationListener() {
			mRegistrationListener = new NsdManager.RegistrationListener() {
	
				@Override
				public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
					// Save the service name. Android may have changed it in order
					// to
					// resolve a conflict, so update the name you initially
					// requested
					// with the name Android actually used.
					mServiceName = NsdServiceInfo.getServiceName();
					Log.d("DEBUG", "Service name: " + mServiceName);
				}
	
				@Override
				public void onRegistrationFailed(NsdServiceInfo serviceInfo,
						int errorCode) {
					// Registration failed! Put debugging code here to determine
					// why.
					Log.d("DEBUG", "Registration failed");
				}
	
				@Override
				public void onServiceUnregistered(NsdServiceInfo arg0) {
					// Service has been unregistered. This only happens when you
					// call
					// NsdManager.unregisterService() and pass in this listener.
					Log.d("DEBUG", "Service has been unregistered");
				}
	
				@Override
				public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
						int errorCode) {
					// Unregistration failed. Put debugging code here to determine
					// why.
					Log.d("DEBUG", "Unregistration failed");
				}
			};
		}
	
	public void tearDown() {
		mNsdManager.unregisterService(mRegistrationListener);
	}
	
	@Override
	protected void onDestroy() {
		tearDown();
		super.onDestroy();
	}
	
	public NodeGridAdapter getAdapter(){
		return this.nodeGridAdapter;
	}
	
	public Manager getManager(){
		return zwaveManager;
	}
}
