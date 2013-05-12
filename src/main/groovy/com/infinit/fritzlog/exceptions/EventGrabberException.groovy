package com.infinit.fritzlog.exceptions

/**
 * Exception thrown when unable to log into FritzBox
 */
class EventGrabberException extends RuntimeException {
	public EventGrabberException() {
		super();
	}

	public EventGrabberException(String message) {
		super(message);
	}

	public EventGrabberException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventGrabberException(Throwable cause) {
		super(cause);
	}

}
