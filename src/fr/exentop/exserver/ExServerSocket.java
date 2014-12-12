package fr.exentop.exserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocketFactory;

public class ExServerSocket implements Runnable{
	static{
		System.setProperty("javax.net.ssl.keyStore", "mySrvKeystore");
	    System.setProperty("javax.net.ssl.keyStorePassword", "maclio");
	}
	private ExServer mExServer;
	
	protected ServerSocket mServer;
	protected Thread mThread;
	protected boolean mRunning;
	protected InetAddress mIp = null;
	protected int mPort;
	protected boolean mSsl;

	public ExServerSocket(ExServer server, int port, boolean ssl){
		mExServer = server;
		mThread = new Thread(this);
		mPort = port;
		mSsl = ssl;
	}
	public ExServerSocket(ExServer server, InetAddress ip, int port, boolean ssl){
		mExServer = server;
		mThread = new Thread(this);
		mPort = port;
		mIp = ip;
		mSsl = ssl;
	}

	/**
	 * Retourne le Thread du serveur HTTP
	 * @return Thread du serveur HTTP
	 */
	public Thread getThread(){
		return mThread;
	}

	/**
	 * Démarre le serveur HTTP(S)
	 */
	public void start(){
		if(mRunning) return;
		try {
			if(mSsl){
				if(mIp == null) mServer = SSLServerSocketFactory.getDefault().createServerSocket(mPort);
				else mServer = SSLServerSocketFactory.getDefault().createServerSocket(mPort, 0, mIp);
			}
			else{
				if(mIp == null) mServer = new ServerSocket(mPort);
				else mServer = new ServerSocket(mPort, 0, mIp);
			}
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
				c = new ExClient(mExServer, mServer.accept(), mSsl);
				mExServer.addClient(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
