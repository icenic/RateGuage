package com.github.icenic;

import com.github.icenic.Rate.RateRegistry;
import com.github.icenic.RateGauge.Type;

public class RateGaugeFactory {
	private RateRegistry registry;
	
	public static RateGaugeFactory build(long interval){
		RateGaugeFactory factory = new RateGaugeFactory();
		factory.registry = new RateRegistry(interval, true);
		return factory;
	}
	
	public RateGauge newInstance(int number, Type type){
 		Rate rate = new Rate(number+1);
 		rate.register(this.registry);
 		RateGauge gauge = new RateGauge(rate, type);
 		return gauge;
	}
}
