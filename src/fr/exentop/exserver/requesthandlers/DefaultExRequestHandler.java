package fr.exentop.exserver.requesthandlers;

import fr.exentop.exserver.ExRouter;
import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requests.ExRequest;
import fr.exentop.exserver.requests.HTTPRequest;
import fr.exentop.exserver.requests.WebSocketRequest;
import fr.exentop.exserver.templates.ExTemplate;
import fr.exentop.exserver.templates.ExTemplateInstance;
import fr.exentop.exserver.templates.ExTemplates;

public class DefaultExRequestHandler extends AbstractExRequestHandler {
	
	private ExRouter mRouter;
	
	public DefaultExRequestHandler(){
		mRouter = new ExRouter();
	}
	
	@Override
	public void handleHTTPRequest(HTTPRequest request) throws ExConnectionClosed {
		if(!mRouter.handleRequest(request)){
			request.setCode(ExRequest.HTTP_NOT_FOUND);
			request.sendTextResponse("Page Introuvable");
		}
	}
	
	public ExRouter getRouter(){
		return mRouter;
	}

	@Override
	public void handleWebSocketRequest(WebSocketRequest request) throws ExConnectionClosed {
		request.sendTextResponse("Hello Websocket");
	}


}
