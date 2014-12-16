package fr.exentop.exserver.requests;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Date;

import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;

public class HTTPRequest extends ExRequest {

	private final static int FILE_BUFFER = 8000;

	public HTTPRequest(ExRequestHandler handler, ExClient client) {
		super(handler, client);
	}

	protected String getMimeType(File f){
		String[] s = f.getName().split("\\.");
		if(s.length <= 1) return "octet/stream";
		String ext = s[s.length-1].toLowerCase();
		if(ext.equals("html")) return "text/html";
		else if(ext.equals("js")) return "application/javascript";
		else if(ext.equals("png")) return "image/png";
		else if(ext.equals("gif")) return "image/gif";
		else if(ext.equals("mp3")) return "audio/mpeg";
		return "octet/stream";
	}
	
	public void sendFile(File file) throws ExConnectionClosed {
		try {
			Date last_modified = new Date(file.lastModified()/1000*1000);
			//last_modified.
			//On vérifie si on doit vraiment renvoyer l'image
			if(mClient.getHeaders().has("if-modified-since")){
				try{
					Date d = sDateFormat.parse(mClient.getHeaders().get("if-modified-since"));
					if(!last_modified.after(d)){
						//Pas de modification
						setCode(HTTP_NOT_MODIFIED);
						sendHeaders();
						return;
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			long len = file.length();
			mHeaders.put("Content-Length", String.valueOf(len));
			mHeaders.put("Content-Type", getMimeType(file));
			mHeaders.put("Last-Modified", sDateFormat.format(last_modified));
			
			//On envoie le headers
			sendHeaders();
			
			OutputStream output = mClient.getOutputStream();
			FileInputStream input = new FileInputStream(file);
			
			byte[] buffer = new byte[FILE_BUFFER];
			int read = 0;
			while(len > 0){
				read = input.read(buffer);
				output.write(buffer, 0, read);
				output.flush();
				len -= read;
			}
			input.close();
		} catch (Exception e) {
			
			e.printStackTrace();
			throw new ExConnectionClosed();
		}
		
	}

}
