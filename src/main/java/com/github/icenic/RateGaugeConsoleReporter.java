package com.github.icenic;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.github.icenic.RateGauge.RateData;

public class RateGaugeConsoleReporter extends ScheduledReporter {
	private PrintStream output = System.out;

	protected final String toTimeString(long timestamp){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(timestamp));
	}
	
	protected RateGaugeConsoleReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
			TimeUnit durationUnit) {
		super(registry, name, filter, rateUnit, durationUnit);
	}

	protected void printRateData(RateData rateData) {
		if (rateData == null || rateData.getValues() == null){
			return;
		}			

		StringBuilder sb = new StringBuilder();

		long interval = rateData.getInterval();
		long timestamp = rateData.getTimestamp();
		double[] values = rateData.getValues();
		
		for (int i = values.length - 1; i >= 0; i--) {
			sb.append("[").append(toTimeString(timestamp - i * interval)).append("] ")
					.append(values[i]).append('\n');
		}

		this.output.println(sb.toString());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		long timestamp = System.currentTimeMillis();
		if (!gauges.isEmpty()) {
			for (Entry<String, Gauge> entry : gauges.entrySet()) {
				if (entry.getValue() instanceof RateGauge) {
					output.println(toTimeString(timestamp)+" RateGauge: " + entry.getKey());
					RateGauge rateGauge = (RateGauge) entry.getValue();
					printRateData(rateGauge.getValue());
				}
			}
		}
	}
}
