package com.eoss.hft.exception;

public class CancelException extends ExchangeException {

	public CancelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CancelException(String message, Throwable cause) {
		super(message, cause);
	}

	public CancelException(String message) {
		super(message);
	}

	public CancelException(Throwable cause) {
		super(cause);
	}

}
