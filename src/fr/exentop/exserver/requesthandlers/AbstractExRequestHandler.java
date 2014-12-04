package fr.exentop.exserver.requesthandlers;

import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.exceptions.ExServerException;
import fr.exentop.exserver.requests.ExRequest;
import fr.exentop.exserver.requests.HTTPRequest;
import fr.exentop.exserver.requests.WebSocketRequest;
import fr.exentop.exserver.templates.ExTemplate;
import fr.exentop.exserver.templates.ExTemplates;

public abstract class AbstractExRequestHandler implements ExRequestHandler{
	
	protected ExTemplates mTemplate = new ExTemplates("templates");

	public void handleRequest(ExClient client) throws ExServerException{
		if(client.getHeaders().has("upgrade") && client.getHeaders().get("upgrade").equalsIgnoreCase("websocket")){
			handleWebSocketRequest(new WebSocketRequest(this, client));
		}
		else{
			handleHTTPRequest(new HTTPRequest(this, client));
		}
	}

	public void render(ExTemplate template, ExRequest request) throws ExConnectionClosed{
		request.sendTextResponse(mTemplate.render(template));
	}

	public ExTemplates getExTemplates() {
		return mTemplate;
	}
	/**
	 * Gestion d'une requête HTTP
	 * @param request Requête HTTP à gérer
	 */
	public abstract void handleHTTPRequest(HTTPRequest request) throws ExServerException;
	
	/**
	 * Gestion d'une requête WebSocket
	 * @param request Request WebSocket à gérer
	 */
	public abstract void handleWebSocketRequest(WebSocketRequest request) throws ExServerException;
	
}
