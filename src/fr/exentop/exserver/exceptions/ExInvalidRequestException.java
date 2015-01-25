package fr.exentop.exserver.exceptions;

public class ExInvalidRequestException extends ExServerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8531202691541148876L;

	public ExInvalidRequestException() {
		super("Invalid HTTP request");
	}

}
