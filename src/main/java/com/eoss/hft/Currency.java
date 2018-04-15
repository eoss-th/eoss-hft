package com.eoss.hft;

public class Currency {

	private final String name;
	private final double amount;
	
	public Currency(String name, double amount) {
		this.name = name;
		this.amount = amount;
	}
	
	public Currency(Currency c) {
		this(c.name, c.amount);
	}
	
	public String getName() {
		return name;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public String toString() {
		return String.format("%.8f", amount) + " " + name;
	}

}
