package com.eoss.hft;

import java.util.List;

public class Fall {
	
	public final Currency start;
	
	public final Currency target;
	
	public Fall(Currency start, Currency target) {
		this.start = start;
		this.target = target;
	}
	
	public static double totalAmount(List<Fall> fallList) {
		
		if (fallList==null || fallList.isEmpty()) return 0;
		
		if (fallList.size()==1) return fallList.get(0).start.getAmount();
		
		double total = 0.0;
		
		for (Fall fall:fallList) {
			total += fall.start.getAmount();
		}
		
		return total;
	}
	
	public Fall add(Fall fall) {
		
		if (!start.getName().equals(fall.start.getName())) throw new IllegalArgumentException("Start Currency must be " + start.getName());
		
		if (!target.getName().equals(fall.target.getName())) throw new IllegalArgumentException("Target Currency must be " + target.getName());
		
		Currency newStart = new Currency(start.getName(), start.getAmount() + fall.start.getAmount());
		
		Currency newTarget = new Currency(target.getName(), target.getAmount() + fall.target.getAmount());
		
		return new Fall(newStart, newTarget);
	}
	
	public Fall reduct(double amount) {
		
		if (amount > start.getAmount()) amount = start.getAmount();
		
		Currency newStart = new Currency(start.getName(), start.getAmount() - amount);				
		double rate = newStart.getAmount() / start.getAmount();
		Currency newTarget = new Currency(target.getName(), target.getAmount() * rate);
		
		return new Fall(newStart, newTarget);
	}
	
	@Override
	public String toString() {
		return start + "->" + target;
	}
	
	public static void main(String[]args) {
		
		Fall fall = new Fall(new Currency("BTC", 1), new Currency("THB", 500000));
		fall = fall.add(new Fall(new Currency("BTC", 0.5), new Currency("THB", 255000)));
		fall = fall.add(new Fall(new Currency("BTC", 0.1), new Currency("THB", 52000)));
		
		System.out.println(fall);
		
		fall = fall.reduct(0.6);
		
		System.out.println(fall);	
	}

}
