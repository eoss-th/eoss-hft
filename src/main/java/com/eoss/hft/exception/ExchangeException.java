package com.eoss.hft.exception;

public class ExchangeException extends Exception {

	public ExchangeException() {
		super();
	}

	public ExchangeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExchangeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExchangeException(String message) {
		super(message);
	}

	public ExchangeException(Throwable cause) {
		super(cause);
	}

}
