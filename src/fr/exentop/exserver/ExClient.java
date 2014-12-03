package fr.exentop.exserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;

import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.exceptions.ExInvalidRequestException;

public class ExClient implements Runnable{
	
	private Socket mSocket;
	private ExServer mServer;
	private Thread mThread;
	private InputStream mInput;
	private OutputStream mOutputStream;
	
	protected String mUri;
	protected String mMethod;
	
	protected ExBag mDatas;
	protected ExBag mCookies;
	protected ExBag mQuery;
	protected ExBag mPost = null;
	


	public ExClient(ExServer server, Socket s){
		try {
			mServer = server;
			mSocket = s;
			mInput = new BufferedInputStream(s.getInputStream());
			mOutputStream = s.getOutputStream();
			mThread = new Thread(this);
			mDatas = new ExBag();
			mCookies = new ExBag();
			mQuery = new ExBag();
			mThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public OutputStream getOutputStream(){
		return mOutputStream;
	}
	public InputStream getInputStream(){
		return mInput;
	}
	
	public ExBag getHeaders(){
		return mDatas;
	}
	public ExBag getQuery(){
		return mQuery;
	}
	public ExBag getCookie(){
		return mCookies;
	}
	public ExBag getPost(){
		return mPost;
	}
	
	public String readLine() throws IOException, ExConnectionClosed{
		StringBuffer sb = new StringBuffer();
		byte[] c = new byte[1];
		int len;
		while((len = mInput.read(c)) > 0){
			if(c[0] == '\r') continue;
			if(c[0] == '\n') break;
			sb.append((char) c[0]);
		}
		if(len == -1) throw new ExConnectionClosed();
		return sb.toString();
	}
	
	public void read(byte[] c) throws IOException{
		int total = c.length;
		int read = mInput.read(c);
		if(read < total){
			byte[] buffer = new byte[1];
			while(mInput.read(buffer) > 0 && read < total){
				c[read] = buffer[0];
				read++;
				
			}
		}
	}
	
	public void headers() throws IOException, ExInvalidRequestException, ExConnectionClosed{
		String str = readLine();
		
		//Lecture de l'entete HTTP
		String[] tmp = str.split(" ");
		if(tmp.length != 3){
			throw new ExInvalidRequestException();
			
		}
		mMethod = tmp[0];
		String[] query = tmp[1].split("\\?",2); 
		mUri = query[0];
		
		
		if(query.length == 2){
			String[] gets = query[1].split("&");
			for(String get : gets){
				if(get.isEmpty()) continue;
				String[] datas = get.split("=",2);
				if(datas.length != 2) continue;
				mQuery.put(datas[0].trim(), URLDecoder.decode(datas[1].trim(), "UTF-8"));
			}
		}
		
		while(true){
			str = readLine();
			if(str.isEmpty()) break;
			tmp = str.split(":", 2);
			if(tmp.length != 2) continue;
			mDatas.put(tmp[0].trim().toLowerCase(), tmp[1].trim());
		}
		
		//Lecture des cookies
		if(mDatas.containsKey("cookie")){
			String[] cookies = mDatas.get("cookie").split(";");
			for(String cookie : cookies){
				if(cookie.isEmpty()) continue;
				String[] datas = cookie.split("=",2);
				if(datas.length != 2) continue;
				mCookies.put(datas[0].trim().toLowerCase(), URLDecoder.decode(datas[1].trim(), "UTF-8"));
			}
		}
		
		
	}
	
	public void readPostDatas() throws IOException{
		if(mPost != null) return;
		mPost = new ExBag();
		
		if(mDatas.containsKey("content-length")){
			int len = Integer.parseInt(mDatas.get("content-length"));
			if(len > 5000000) return;
			byte[] content = new byte[len];
			read(content);
			String datas = new String(content);
			String[] posts = datas.split("&");
			for(String post : posts){
				if(post.isEmpty()) continue;
				String[] d = post.split("=",2);
				if(d.length != 2) continue;
				mPost.put(d[0].trim().toLowerCase(), URLDecoder.decode(d[1].trim(),"UTF-8"));
			}
		}
	}
	private void clear(){
		mCookies.clear();
		mPost = null;
		mDatas.clear();
	}
	

	@Override
	public void run() {
		try {
			//On valide la connexion
			int i = 0;
			while(true){
				clear();
				headers();
				i++;
				System.out.println("Requête "+i+" => "+mUri);
				mServer.getRequestHandler().handleRequest(this);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			try{
				mSocket.close();
				mInput.close();
				mOutputStream.close();
				mServer.removeClient(this);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
