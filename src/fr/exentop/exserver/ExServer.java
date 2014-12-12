package fr.exentop.exserver;

import java.util.ArrayList;

import fr.exentop.exserver.requesthandlers.DefaultExRequestHandler;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;

public class ExServer{

	public static final String SERVER_NAME = "Exentop Server";
	
	protected ExRequestHandler mRequestHandler = new DefaultExRequestHandler();
	
	private int mHttpPort = 80;
	private boolean mEnableHttp = true;
	private int mHttpsPort = 443;
	private boolean mEnableHttps = false;

	private ExServerSocket mHttpServer = null;
	private ExServerSocket mHttpsServer = null;
	
	private boolean mIsRunning = false;
	
	
	protected ArrayList<ExClient> mClients = new ArrayList<ExClient>();

	public ExServer(){
	}
	

	public void setHttpEnable(boolean enable){
		mEnableHttp = enable;
	}
	public void setHttpsEnable(boolean enable){
		mEnableHttps = enable;
	}
	public void setHttpPort(int port){
		mHttpPort = port;
	}
	public void setHttpsPort(int port){
		mHttpsPort = port;
	}
	public ExServerSocket getHttpServer(){
		return mHttpServer;
	}
	public ExServerSocket getHttpsServer(){
		return mHttpsServer;
	}
	
	/**
	 * Démarre le(s) serveur(s) HTTP(S)
	 */
	public void start(){
		if(mIsRunning) return;
		if(mEnableHttp){
			mHttpServer = new ExServerSocket(this, mHttpPort, false);
			mHttpServer.start();
		}
		if(mEnableHttps){
			mHttpsServer = new ExServerSocket(this, mHttpsPort, true);
			mHttpsServer.start();
		}
		mIsRunning = true;
	}
	/**
	 * Fin de connexion d'un client
	 * @param client Client dont la connexion est terminée
	 */
	void removeClient(ExClient client) {
		synchronized (mClients) {
			mClients.remove(client);
		}
	}
	
	/**
	 * Retourne l'objet ExRequestHandler utilisé par ExServer pour traiter les requêtes qu'il reçoit.
	 * @return Le RequestHandler
	 */
	public ExRequestHandler getRequestHandler(){
		return mRequestHandler;
	}
	
	/**
	 * Remplace l' ExRequestHandler utilisé par ExServer par celui fourni en paramètre
	 * @param requestHandler Nouvelle instance d'ExRequestHandler
	 */
	public void setRequestHandler(ExRequestHandler requestHandler){
		mRequestHandler = requestHandler;
	}

	public synchronized void addClient(ExClient c) {
		mClients.add(c);
	}
}
