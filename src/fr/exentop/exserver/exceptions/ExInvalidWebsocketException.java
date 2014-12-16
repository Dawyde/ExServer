package fr.exentop.exserver.exceptions;

public class ExInvalidWebsocketException extends ExServerException {

	public ExInvalidWebsocketException() {
		super("Invalid HTTP websocket");
	}

}
