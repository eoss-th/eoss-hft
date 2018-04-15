package com.eoss.hft;

import com.eoss.hft.Pair;
import com.eoss.hft.exchange.bx.BXPair;

public class PairCapacityTester {
	public static void main(String[]args) {
		/*
BTC/THB
Bid	Ask
Rate		550651.0		554980.0
Volume	0.66417225	0.44352586
Total	365727.11363475	246147.9817828
ETH/THB
Bid	Ask
Rate		22731.0	23028.0
Volume	0.48819311	7.72928343
Total	11097.11758341	177989.93882604
ETH/BTC
Bid	Ask
Rate		0.0422	0.0423
Volume	0.01701896	0.38451296
Total	7.18200112E-4	0.016264898208

THB->ETH->BTC->THB:1.0015470947550047MIN:393.87954686716785:0.6093689810618219
THB->BTC->ETH->THB:0.9610357756626559MIN:9071.996489947514:-353.4833064219136
THB->ETH->BTC->THBAvailable Balance THB:4735.43*/
		double fee = 0.0025;
		
		Pair btc_thb = new BXPair("1", "BTC", "THB", fee, 0.0, 0.0, 0);
		
		Pair eth_thb = new BXPair("21", "ETH", "THB", fee, 0.0, 0.0, 0);
		
		Pair eth_btc = new BXPair("20", "ETH", "BTC", fee, 0.0, 0.0, 0);
		
		btc_thb.bid = 550651.0; btc_thb.ask = 554980.0;
		btc_thb.bidAmount = 0.66417225; btc_thb.askAmount = 0.44352586;
		
		eth_thb.bid = 22731.0; eth_thb.ask = 23028.0;
		eth_thb.bidAmount = 0.48819311; eth_thb.askAmount = 7.72928343;
		
		eth_btc.bid = 0.0422; eth_btc.ask = 0.0423;
		eth_btc.bidAmount = 0.01701896; eth_btc.askAmount = 0.38451296;

		/*
		btc_thb.bid = ; btc_thb.ask = ;
		btc_thb.bidAmount = ; btc_thb.askAmount = ;
		
		eth_thb.bid = ; eth_thb.ask = ;
		eth_thb.bidAmount = ; eth_thb.askAmount = ;
		
		eth_btc.bid = ; eth_btc.ask = ;
		eth_btc.bidAmount = ; eth_btc.askAmount = ;
		*/
/*		
		System.out.println("THB->ETH->BTC->THB");
		
		System.out.println(Pair.capacity("THB", new Pair[] {eth_thb, eth_btc, btc_thb}));
		
		System.out.println(Pair.capacity("THB", new Pair[] {eth_thb, eth_btc}));
		
		System.out.println(Pair.capacity("THB", new Pair[] {eth_thb}));
		
		System.out.println();
		
		System.out.println("THB->BTC->ETH->THB");
		
		System.out.println(Pair.capacity("THB", new Pair[] {btc_thb, eth_btc, eth_thb}));
		
		System.out.println(Pair.capacity("THB", new Pair[] {btc_thb, eth_btc}));
		
		System.out.println(Pair.capacity("THB", new Pair[] {btc_thb}));
				
		System.out.println();
*/
		System.out.println("BTC/THB");
		System.out.println("Bid=" + btc_thb.bid + "\t\t\t\t\t\t" + "Ask=" + btc_thb.ask);
		System.out.println("Amt=" + btc_thb.bidAmount + "\t\t\t\t\t\t" + "Amt=" + btc_thb.askAmount);
		System.out.println("TotalBid=" + btc_thb.totalBid() + "\t\t\t\t\t" + "TotalAsk=" + btc_thb.totalAsk());
		
		System.out.println();
		
		System.out.println("ETH/THB");
		System.out.println("Bid=" + eth_thb.bid + "\t\t\t\t\t\t" + "Ask=" + eth_thb.ask);
		System.out.println("Amt=" + eth_thb.bidAmount + "\t\t\t\t\t\t" + "Amt=" + eth_thb.askAmount);
		System.out.println("TotalBid=" + eth_thb.totalBid() + "\t\t\t\t\t" + "TotalAsk=" + eth_thb.totalAsk());
		
		System.out.println();
		
		System.out.println("ETH/BTC");
		System.out.println("Bid=" + eth_btc.bid + "\t\t\t\t\t\t" + "Ask=" + eth_btc.ask);
		System.out.println("Amt=" + eth_btc.bidAmount + "\t\t\t\t\t\t" + "Amt=" + eth_btc.askAmount);
		System.out.println("TotalBid=" + eth_btc.totalBid() + "\t\t\t\t\t" + "TotalAsk=" + eth_btc.totalAsk());
		
		System.out.println();
		
		System.out.println("ETH->BTC->THB->ETH");
		
		//System.out.println("ETH->BTC->THB->ETH\t" + Pair.capacity("ETH", new Pair[] {eth_btc, btc_thb, eth_thb}));
		
		//System.out.println("ETH->BTC->THB\t" + Pair.capacity("ETH", new Pair[] {eth_btc, btc_thb}));
		
		//System.out.println("ETH->BTC\t\t" + Pair.capacity("ETH", new Pair[] {eth_btc}));
		
		/*
		System.out.println("ETH->BTC->THB->ETH");

		
		System.out.println("ETH->THB->BTC->ETH\t" + Pair.capacity("ETH", new Pair[] {eth_thb, btc_thb, eth_btc}));
		
		System.out.println("ETH->THB->BTC\t" + Pair.capacity("ETH", new Pair[] {eth_thb, btc_thb}));
		
		System.out.println("ETH->THB\t\t" + Pair.capacity("ETH", new Pair[] {eth_thb}));
*/		
	}
}
