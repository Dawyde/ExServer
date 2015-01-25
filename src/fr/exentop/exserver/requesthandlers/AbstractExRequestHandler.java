package fr.exentop.exserver.requesthandlers;

import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.exceptions.ExServerException;
import fr.exentop.exserver.requests.HTTPRequest;
import fr.exentop.exserver.requests.WebSocketRequest;

public abstract class AbstractExRequestHandler implements ExRequestHandler {

	@Override
	public void handleRequest(ExClient client) throws ExServerException {
		if (client.getHeaders().has("upgrade") && client.getHeaders().get("upgrade").equalsIgnoreCase("websocket")) {
			handleWebSocketRequest(new WebSocketRequest(this, client));
		}
		else {
			handleHTTPRequest(new HTTPRequest(this, client));
		}
	}

	/**
	 * Gestion d'une requête HTTP
	 * 
	 * @param request
	 *            Requête HTTP à gérer
	 */
	public abstract void handleHTTPRequest(HTTPRequest request) throws ExServerException;

	/**
	 * Gestion d'une requête WebSocket
	 * 
	 * @param request
	 *            Request WebSocket à gérer
	 */
	public abstract void handleWebSocketRequest(WebSocketRequest request) throws ExServerException;

}
