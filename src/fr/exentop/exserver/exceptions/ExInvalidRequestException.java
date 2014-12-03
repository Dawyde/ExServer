package fr.exentop.exserver.exceptions;

public class ExInvalidRequestException extends ExServerException {

	public ExInvalidRequestException() {
		super("Invalid HTTP request");
	}

}
