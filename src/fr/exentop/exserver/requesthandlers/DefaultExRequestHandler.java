package fr.exentop.exserver.requesthandlers;

import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requests.HTTPRequest;
import fr.exentop.exserver.requests.WebSocketRequest;

public class DefaultExRequestHandler extends AbstractExRequestHandler {

	@Override
	public void handleHTTPRequest(HTTPRequest request) throws ExConnectionClosed {
		request.sendTextResponse("Hello World");
	}

	@Override
	public void handleWebSocketRequest(WebSocketRequest request) throws ExConnectionClosed {
		request.sendTextResponse("Hello Websocket");
	}

}
