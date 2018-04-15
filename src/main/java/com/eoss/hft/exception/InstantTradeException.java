package com.eoss.hft.exception;

import com.eoss.hft.Currency;
import com.eoss.hft.Pair;

public class InstantTradeException extends ExchangeException {
	
	public final Pair pair;
	
	public final long orderId;
		
	public final double rate;
	
	public final Currency amount;
	
	public InstantTradeException(String message, Pair pair, long orderId, double rate, Currency amount) {
		super(message);
		this.pair = pair;		
		this.orderId = orderId;
		this.rate = rate;
		this.amount = amount;
	}
	
	public InstantTradeException(Pair pair, long orderId, double rate, Currency amount) {
		this("Instant Trade Failed", pair, orderId, rate, amount);
	}
	
	public InstantTradeException(InstantTradeException i, String string) {
		this(i.getMessage()+System.lineSeparator()+string, i.pair, i.orderId, i.rate, i.amount);
	}
	
}
