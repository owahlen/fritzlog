package com.infinit.fritzlog.exceptions

/**
 * Exception thrown when unable to log into FritzBox
 */
class AuthenticationException extends RuntimeException {
	public AuthenticationException() {
		super();
	}

	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthenticationException(Throwable cause) {
		super(cause);
	}

}
