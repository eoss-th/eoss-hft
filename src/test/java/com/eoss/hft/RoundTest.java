package com.eoss.hft;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundTest {

	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();
	 
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_DOWN);
	    return bd.doubleValue();	    
	}
	
	public static void main(String[] args) {
		
		double amt = 0.24525333;
		double balance = 0.24525323;
		
		System.out.println(amt > balance);
		System.out.println(0.999999 * amt);
		System.out.println(0.999999 * amt > balance);
	}

}
