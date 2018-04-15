package com.eoss.hft.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eoss.hft.Currency;
import com.eoss.hft.Exchange;
import com.eoss.hft.Fall;
import com.eoss.hft.Pair;
import com.eoss.hft.Route;
import com.eoss.hft.Strategy;
import com.eoss.hft.datastore.FallDAO;
import com.eoss.hft.exception.CancelException;
import com.eoss.hft.exception.ExchangeException;
import com.eoss.hft.exception.OrderException;
import com.eoss.hft.exception.InstantTradeException;

public abstract class TriangularArbitrageStrategy implements Strategy {
	
	public static double maxRate = Double.MIN_VALUE;
	public static Route maxRoute;
	public static long maxTime;
	
	public final double candidatedScore;
	public final Map<Route, Double> candidatedRoutes = new HashMap<>();	
	public final Map<Route, Double> suspendedRoutes = new HashMap<>();
	
	public final List<Route> routes;
	private List<Route> selectedRoutes;	
	
	private Exchange ex;
	
	private long delay;
	
	private FallDAO fallDAO;
		
	private int routeIndex;
	
	private StringBuilder tradeLog;
	
	public TriangularArbitrageStrategy(Exchange ex, Route [] routes, long delay, FallDAO fallDAO, double candidatedScore) {
		
		this.ex = ex;
		this.delay = delay;
		this.fallDAO = fallDAO;
			
		this.routes = new ArrayList<>(Arrays.asList(routes));
		this.selectedRoutes = this.routes;
		this.candidatedScore = candidatedScore;
	}
	
	public final boolean process() {
		
		try {			
			if (routeIndex>selectedRoutes.size()-1) {
				
				if (candidatedRoutes.isEmpty()) {
					selectedRoutes = routes;
				} else {
					selectedRoutes = new ArrayList<>(candidatedRoutes.keySet());
				}
				
				routeIndex = 0;				
			}
			
			Route route = selectedRoutes.get(routeIndex);
			
			if (route.fetch(ex)==null) return false;
			
			Currency maximum = route.calculateMaximum();
			
			double testRate = route.test(maximum).getAmount() / maximum.getAmount();
			
			System.out.println(route+":"+testRate);
									
			if (testRate > maxRate) {
				maxRate = testRate;
				maxRoute = route;
				maxTime = System.currentTimeMillis();
			}
			
			if (testRate<1.001) {
				
				if (testRate >= candidatedScore) {
					
					suspendedRoutes.remove(route);
					candidatedRoutes.put(route, testRate);
					
				} else {
					
					candidatedRoutes.remove(route);
					suspendedRoutes.put(route, testRate);
					
				}
				
				return false;
			}
			
			Currency balance = ex.getAvailableBalance(route.targetCurrencyName);
			
			Currency start = maximum.getAmount() <= balance.getAmount() ? maximum : balance;
			
			if (start==maximum || route.test(start).getAmount() > 0) {
								
				try {
										
					tradeLog = new StringBuilder("Executed " + route + " at Rate:" +  + (testRate-1) * 100 + "%" + System.lineSeparator());
					
					Currency end = execute(route, start);
					
					if (end==null) return false;
					
					end = ex.getAvailableBalance(end.getName());	
					
					maxRate = Double.MIN_VALUE;
					
					decreaseFalls(start);			
					
					onTradeSuccess(route, balance, start, end);
					
					return true;
									
				} catch (ExchangeException e) {
					
					Currency fall = null;
					
					if (e instanceof InstantTradeException) {
						
						InstantTradeException tradeException = (InstantTradeException) e;
						
						fall = tradeException.amount;
						
						e = new InstantTradeException(tradeException, tradeLog.toString() + System.lineSeparator() +"Waiting for Fallback:" + fall + "->" + start);
						
					} else if (e instanceof OrderException) {
						
						OrderException orderException = (OrderException) e;
						
						fall = orderException.amount;
						
						e = new OrderException(orderException, tradeLog.toString() + System.lineSeparator() +"Waiting for Fallback:" + fall + "->" + start);
					}
					
					if (fall!=null)
						increaseFall(fall, start);
					
					onTradeFail(e);
					
				} 
				
			} else {
						
				candidatedRoutes.remove(route);
				suspendedRoutes.put(route, testRate);
						
			}	
			
		} finally {
			routeIndex ++;
		}
				
		return false;
	}

	private void increaseFall(Currency startCurrency, Currency targetCurrency) {
		if (fallDAO!=null) {
			Fall fall = fallDAO.get(startCurrency.getName(), targetCurrency.getName());
			if (fall==null) {
				fall = new Fall(startCurrency, targetCurrency);
			} else {
				fall = fall.add(new Fall(startCurrency, targetCurrency));
			}
			
			fallDAO.put(fall);						
		}
	}
	
	private void decreaseFalls(Currency start) {
		List<Fall> fallList = fallDAO.get(start.getName());
		double startSharedAmount = start.getAmount() / fallList.size();
		for (Fall fall:fallList) {
			fallDAO.put(fall.reduct(startSharedAmount));
		}		
	}

	protected Currency execute(Route route, Currency start) throws ExchangeException {
				
		/**
		 * For buy orders you must have enough available balance including fees. Available balance > price * quantity * (1 + takeLiquidityRate)
		 */
		Currency money = start;
		
		for (Pair pair:route.pairs) {
						
			tradeLog.append(pair+" => "+money);
			
			money = trade(route, start, money, pair);
												
			if (money==null) break;
			
			tradeLog.append(" => "+money+System.lineSeparator());
						
		}
				
		return money;
	}
	
	private final Currency trade(Route route, Currency start, Currency currency, Pair pair) throws ExchangeException {
		
		long elapsedTime;		
		long orderId;
		
		if (currency.getName().equals(pair.base)) {
			
			tradeLog.append(" x " + pair.bid + " (" + pair.bidAmount + ")");
			
			orderId = ex.sell(pair, pair.bid, currency);
			
			elapsedTime = (System.currentTimeMillis() - ex.invokedTime()) / 2;			
			try { Thread.sleep(delay > elapsedTime?delay - elapsedTime:0); } catch (InterruptedException e) {}
			
			if (orderId==0) {
				return pair.sell(currency);
			} 
			
			try {
				ex.cancel(pair, orderId);
				
				if (route.position(pair)!=0) {						
					decreaseFalls(start);			
					pair = pair.fetch(ex);
					throw new InstantTradeException(pair, orderId, pair.bid, currency);
				}
				
			} catch (CancelException e) {
				return pair.sell(currency);
			}
			
			return null;
			
		} else if (currency.getName().equals(pair.counter)) {
			
			tradeLog.append(" / " + pair.ask + " (" + pair.askAmount + "," + pair.totalAsk() + ")");
			
			orderId = ex.buy(pair, pair.ask, currency);
			
			elapsedTime = (System.currentTimeMillis() - ex.invokedTime()) / 2;			
			try { Thread.sleep(delay > elapsedTime?delay - elapsedTime:0); } catch (InterruptedException e) {}
			
			if (orderId==0) {
				return pair.buy(currency);
			}
			
			try {
				ex.cancel(pair, orderId);
				
				if (route.position(pair)!=0) {
					decreaseFalls(start);					
					pair = pair.fetch(ex);
					throw new InstantTradeException(pair, orderId, pair.ask, currency);
				}
				
			} catch (CancelException e) {
				return pair.buy(currency);
			}
			
			return null;
		}			
		
		throw new IllegalArgumentException(pair +" is not support for " + currency + ":" + currency.getName().equals(pair.base) + ":" + currency.getName().equals(pair.counter));	
	}

	protected abstract void onTradeSuccess(Route bestRoute, Currency startBalance, Currency amount, Currency endBalance);
	
	protected abstract void onTradeFail(ExchangeException e);
}
