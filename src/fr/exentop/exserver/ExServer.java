package fr.exentop.exserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;

import fr.exentop.exserver.requesthandlers.DefaultExRequestHandler;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;

public class ExServer implements Runnable{

	public static final String SERVER_NAME = "ExServer";
	protected ServerSocket mServer;
	protected Thread mThread;
	protected boolean mRunning;
	protected InetAddress mIp = null;
	protected int mPort;
	
	protected ExRequestHandler mRequestHandler = new DefaultExRequestHandler();
	
	protected ArrayList<ExClient> mClients = new ArrayList<ExClient>();

	/**
	 * Création d'un ExServer écoutant sur le port indiqué
	 * @param port Port sur lequel doit écouter ExServer
	 */
	public ExServer(int port){
		mThread = new Thread(this);
		mRunning = false;
		mPort = port;
	}
	
	/**
	 * Création d'un ExServer écoutant sur le port et l'interface indiqués
	 * @param ip Ip sur laquelle doit écouter ExServer
	 * @param port Port sur lequel doit écouter ExServer
	 */
	public ExServer(InetAddress ip, int port){
		mThread = new Thread(this);
		mRunning = false;
		mPort = port;
		mIp = ip;
	}
	
	/**
	 * Retourne le Thread du serveur HTTP
	 * @return Thread du serveur HTTP
	 */
	public Thread getThread(){
		return mThread;
	}
	
	/**
	 * Démarre le serveur HTTP
	 */
	public void start(){
		if(mRunning) return;
		try {
			if(mIp == null) mServer = new ServerSocket(mPort);
			else mServer = new ServerSocket(mPort, 0, mIp);
			mRunning = true;
			mThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		ExClient c;
		try {
			while(mRunning){
				c = new ExClient(this, mServer.accept());
				synchronized (mClients) {
					mClients.add(c);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
}
