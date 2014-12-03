package fr.exentop.exserver.requests;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.ExServer;
import fr.exentop.exserver.exceptions.ExConnectionClosed;

public abstract class ExRequest {
	
	public static final int HTTP_OK = 302;
	
	public static final int BUFFER_LEN = 256;
	public final static SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
	
	
	protected ExClient mClient;
	protected TreeMap<String, String> mHeaders;
	protected int mCode = 302;
	
	private boolean mHeadersSent = false;
	
	public ExRequest(ExClient client){
		mClient = client;
		mHeaders = new TreeMap<String, String>();
		
		
		//On ajouter les Headers par défaut
		mHeaders.put("Date", sDateFormat.format(new Date()));
		mHeaders.put("Server", ExServer.SERVER_NAME);
		
		
	}
	protected String getMessage(int type){
		switch(type){
		case HTTP_OK:
			return "OK";
		}
		return "OK";
	}
	
	public void sendHeaders() throws ExConnectionClosed{
		if(mHeadersSent) return;
		mHeadersSent = true;
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 ").append(mCode).append(getMessage(mCode)).append("\r\n");
		for(Entry<String, String> entry : mHeaders.entrySet()){
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
		}
		//TODO SetCookie
		sb.append("\r\n");
		
		//On envoie les headers
		OutputStream output = mClient.getOutputStream();
		try{
			output.write(sb.toString().getBytes());
			output.flush();
		}
		catch(Exception e){
			throw new ExConnectionClosed();
		}
	}
	
	public void setHeader(String key, String value){
		mHeaders.put(key, value);
	}
	
	public void sendTextResponse(String text) throws ExConnectionClosed{
		byte[] datas = text.getBytes();
		mHeaders.put("Content-Length", String.valueOf(datas.length));
		if(!mHeaders.containsKey("Content-Type")) mHeaders.put("Content-Type", "text/html");
		//On envoie le headers
		sendHeaders();
		
		OutputStream output = mClient.getOutputStream();
		int len = datas.length;
		int offset = 0;
		try{
			while(len > 0){
				output.write(datas, offset, len > BUFFER_LEN ? BUFFER_LEN : len);
				offset += BUFFER_LEN;
				len -= BUFFER_LEN;
				output.flush();
			}
		}
		catch(Exception e){
			throw new ExConnectionClosed();
		}
	}
}
