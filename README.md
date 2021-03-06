# RateGuage
## How To Use
You need the metrics-core library as a dependency:
```xml
<dependency>
	<groupId>io.dropwizard.metrics</groupId>
	<artifactId>metrics-core</artifactId>
	<version>4.0.0</version>
</dependency>
```
#### Demo codes
```java
package com.github.icenic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.github.icenic.RateGauge.Type;

public class Demo {
	static final MetricRegistry metrics = new MetricRegistry();

	static RateGauge newRateGauge(String name, long interval, int number, Type type) {
		RateGaugeFactory factory = RateGaugeFactory.build(interval);
		return (RateGauge) metrics.gauge(name, () -> factory.newInstance(number, type));
	}

	static void startReport(long interval) {
		@SuppressWarnings("resource")
		RateGaugeConsoleReporter reporter = new RateGaugeConsoleReporter(metrics, "rate-gauge-reporter", MetricFilter.ALL,
				TimeUnit.SECONDS, TimeUnit.SECONDS);
		reporter.start(interval, TimeUnit.MILLISECONDS);
	}

	static void startMarkPerSecond(RateGauge gauge, long n) {
		(new Thread() {
			public void run() {
				while (true) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					System.out.println("["+format.format(new Date())+"] mark "+n);
					gauge.mark(n);
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						throw new RuntimeException();
					}
				}
			}
		}).start();
	}
	
	public static void main(String[] args) {
		/**
		 * measure the rate of requests in every second of last 3(number) second, 
		 * and report every 3(intervalReport) second.
		 */
		long intervalReport = 3000; /*millisecond*/
		long interval = 1000; /*millisecond*/
		int number = (int)(intervalReport/interval);
		RateGauge gauge = newRateGauge("test-rate-gauge", interval, number, Type.ALL);
		startReport(intervalReport);
		startMarkPerSecond(gauge, 1);
	}

}
```
#### Output
```
[2018-12-08 19:44:34] mark 1
[2018-12-08 19:44:35] mark 1
[2018-12-08 19:44:36] mark 1
2018-12-08 19:44:37 RateGauge: test-rate-gauge
[2018-12-08 19:44:34] 1.0
[2018-12-08 19:44:35] 1.0
[2018-12-08 19:44:36] 1.0

[2018-12-08 19:44:37] mark 1
[2018-12-08 19:44:38] mark 1
[2018-12-08 19:44:39] mark 1
2018-12-08 19:44:40 RateGauge: test-rate-gauge
[2018-12-08 19:44:37] 1.0
[2018-12-08 19:44:38] 1.0
[2018-12-08 19:44:39] 1.0
```
