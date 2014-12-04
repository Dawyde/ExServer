package fr.exentop.exserver.requests;

import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;

public class WebSocketRequest extends ExRequest {

	public WebSocketRequest(ExRequestHandler handler, ExClient client) {
		super(handler, client);
	}

}
