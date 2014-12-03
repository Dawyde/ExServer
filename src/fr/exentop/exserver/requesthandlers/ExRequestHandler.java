package fr.exentop.exserver.requesthandlers;

import fr.exentop.exserver.ExClient;
import fr.exentop.exserver.exceptions.ExServerException;


public interface ExRequestHandler {
	
	/**
	 * Gestion d'une requête HTTP
	 * @param client Client HTTP
	 */
	void handleRequest(ExClient client) throws ExServerException;
}
