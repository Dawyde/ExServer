package fr.exentop.exserver.actions;

import fr.exentop.exserver.exceptions.ExConnectionClosed;
import fr.exentop.exserver.requesthandlers.ExRequestHandler;
import fr.exentop.exserver.requests.HTTPRequest;

public interface ExAction {
	public void runAction(HTTPRequest request, String[] parameters) throws ExConnectionClosed;
}
