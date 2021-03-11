package ca.mcgill.ecse.flexibook.controller;

public class InvalidInputException extends Exception {

	private static final long serialVersionUID = -238650599551429022L;

	public InvalidInputException(String errorMessage) {
		super(errorMessage);
	}

}