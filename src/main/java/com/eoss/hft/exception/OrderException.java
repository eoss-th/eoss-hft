package com.eoss.hft.exception;

import com.eoss.hft.Currency;
import com.eoss.hft.Pair;

public class OrderException extends ExchangeException {
	
	public final String type;
	public final Pair pair;
	public final double rate;
	public final Currency amount;

	public OrderException(String message, String type, Pair pair, double rate, Currency amount) {
		super(message);
		this.type = type;
		this.pair = pair;
		this.rate = rate;
		this.amount = amount;
	}
	
	public OrderException(OrderException o, String message) {
		this(o.getMessage()+System.lineSeparator()+message, o.type, o.pair, o.rate, o.amount);
	}

}
