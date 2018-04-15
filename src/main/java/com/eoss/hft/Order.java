package com.eoss.hft;

public class Order {
	
	public enum Type {
		Bid, Ask
	}
	
	public static class Book {
		
		public final Type type;
		public final double rate;
		public final double amount;
		
		public Book(Type type, double rate, double amount) {
			this.type = type;
			this.rate = rate;
			this.amount = amount;
		}
		
		public final double total() {
			return rate * amount;
		}
		
		public String toString() {
			return type + ":" + rate + ":" + amount;
		}

	}
	
	public final Book bid;
	public final Book ask;
	
	public Order (Book bid, Book ask) {
		this.bid = bid;
		this.ask = ask;
	}
	
	public String toString() {
		return "[" + bid + "]" + "[" + ask + "]";
	}

}
