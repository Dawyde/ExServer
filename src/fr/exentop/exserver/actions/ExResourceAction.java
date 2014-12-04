package fr.exentop.exserver.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;
import fr.exentop.exserver.requests.ExRequest;
import fr.exentop.exserver.requests.HTTPRequest;

public class ExResourceAction implements ExAction {

	private String mDirectory;
	public ExResourceAction(String directory){
		mDirectory = directory;
	}
	
	@Override
	public void runAction(HTTPRequest request, String[] parameters)
			throws ExConnectionClosed {
		String path = "";
		try {
			path = URLDecoder.decode(parameters[0],"UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		char c;
		int ptc = 0;
		boolean valid = true;
		//On vérifie que le chemin est valide
		for(int i=0;i<path.length();i++){
			c = path.charAt(i);
			if(c == '.'){
				ptc++;
				if(ptc >= 2) valid = false;
			}
			else ptc = 0;
		}
		if(!valid){
			request.setCode(ExRequest.HTTP_NOT_FOUND);
			request.sendTextResponse("Page Introuvable");
		}
		File file = new File(mDirectory+"/"+path);
		if(!file.exists()){
			request.setCode(ExRequest.HTTP_NOT_FOUND);
			request.sendTextResponse("Page Introuvable");
		}
		else request.sendFile(file);
	}

}
