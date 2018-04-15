package com.eoss.hft;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PairCalucationTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 452.01364800 THB / 30400.0 (6.42932111) => 0.01483170
		// BX Calucated = 0.01483158
		
		double amount = 452.01364800;
		double fee = 0.9975;
		double rate = 30400;
		//Math.floor(value * 100) / 100
		
		BigDecimal amt = new BigDecimal("452.01364800");
		BigDecimal afterfee = amt.multiply(new BigDecimal("0.9975"));
		BigDecimal afterDeviden = afterfee.divide(new BigDecimal("30400"));
		BigDecimal result = afterDeviden.setScale(8, RoundingMode.FLOOR);
		
		System.out.println(result);
	}

}
