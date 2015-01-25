package fr.exentop.exserver.exceptions;

public class ExInvalidWebsocketException extends ExServerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -907809692665422508L;

	public ExInvalidWebsocketException() {
		super("Invalid HTTP websocket");
	}

}
