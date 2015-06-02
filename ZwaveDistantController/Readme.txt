Ajout de la partie serveur de l'application, permettant de recevoir des requêtes depuis une application android distante afin de gérer les devices Zwave connecté au réseau.

Voici le lien de la documentation android officelle à ce sujet:
http://developer.android.com/training/connect-devices-wirelessly/nsd.html

-----------------------------------------------
Enregistrer un NsdService Android
-----------------------------------------------

- Déclarer les variables nécessaires dans l'activité qui utilise ce service:

	private static int PORT = 50506;
	private NsdManager.RegistrationListener mRegistrationListener;
	private String mServiceName;
	private static String SERVICE_NAME = "ZwaveControllerInfo";
	private NsdManager mNsdManager;


- Utiliser la methode registerService ci-dessous dans la methode onCreate() de votre activité. Elle permet d'initialiser les informations du service: nom, type, port et addresse IP.

(serviceInfo est l'objet qui sera disponible sur le wifi.)

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

Les deux dernières lignes de la méthode ci-dessus permettent de récupérer le NsdManager du système et ensuite d'enregistrer sur le réseau le service initialisé.


- La methode registerService() requiert la methode initializeRegistrationListener() qui permet d'initialiser un RegistrationListener permettant de savoir si l'enregistrement du service sur le réseau c'est bien déroulé. Elle permet aussi de récupérer le nouveau nom du NsdServiceInfo qui peut changer si en service porte déjà ce nom. En général ils sont renommés en ajoutant un nombre à la fin : nom (1), nom (2)...

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

- Avant de quitter l'application il ne faut pas oublier de supprimer le service, afin que celui-ci ne reste pas en activité, c'est dans ce cas ou des conflits de nom ont lieu entre les mêmes services de différente instance de l'application:

	public void tearDown() {
		mNsdManager.unregisterService(mRegistrationListener);
	}
	
	@Override
	protected void onDestroy() {
		tearDown();
		super.onDestroy();
	}



-----------------------------------------------
Détecter un NsdService Android
-----------------------------------------------

- Déclarer dans l'activité dans laquelle vous souhaitez récupérer le service le nom et le type du service recherché:

	private static String SERVICE_NAME = "ZwaveControllerInfo";
    private static String SERVICE_TYPE = "_http._tcp.";


- Récupérer dans la methode onCreate() de la même activité le NsdManager de l'application: 

	mNsdManager = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);


- Ecrire les méthode d'initialisation des deux listeners permettant de la recherche et la récupération du service: 

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


- Utilisez ces deux initialisations dans la méthode onCreate() de l'activité puis lancer la recherche de Service :

	// Initialize the listeners
    initializeResolveListener();
    initializeDiscoveryListener();
    // Launch the search of the NsdService
    mNsdManager.discoverServices( SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener); 


- Si la recherche réussie alors les valeurs relative au NsdService seront disponible dans la methode onServiceResolved() de la methode initializeResolveListener(). Dans notre cas, c'est à cet endroit où l'on peut récupérer l'adresse IP de l'application Zwave principale et le port sur laquelle elle écoute et attend des requêtes.
Une fois ces deux informations récupérées, on peut créer un objet Request qui permettra d'initialiser les objets de connection au serveur de l'application principale (Socket, PrintWriter, BufferedReader...).


- Arrêter le service de recherche de service avant l'arrêt de l'application :

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


- Il faut toujours vérifier le nom du NsdService renvoyé par l'application qui le créé, dans le cas où ce nom de service est déjà utilisé il sera remplacé par ' nom (1) ', ce qui provequera une erreur et un arrêt de l'application cliente  si le nom de service n'est pas identique.


----------------------------------------------
Requêtes supportées dans l'objet Request.java
----------------------------------------------

	public ArrayList<NodeToSend> getNodesRequest(): 
		Envoie d'un String "NODES" à l'application principale, qui une fois reçu, renvoie un ArrayList d'objet NodeToSend correspondant au Nodes actuellement enregistré sur le réseau Zwave.


	public void sendSwitchOn(NodeToSend node):
		Envoie d'un String "NODEID#ON" à l'application principale, une fois reçu, l'application activera le Node de type SwitchBinary qui a l'id passé dans la requête.


	public void sendSwitchOff(NodeToSend node):
		Envoie d'un String "NODEID#OFF" à l'application principale, une fois reçu, l'application désactivera le Node de type SwitchBinary qui a l'id passé dans la requête.