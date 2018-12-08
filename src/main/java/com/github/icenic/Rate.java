package com.github.icenic;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class Rate {
	/*circular array*/
	private LongAdder[] buckets;
	private int current;
	private int numberOfBuckets;
	private RateRegistry registry;
	
	public static class RateRegistry {
		private long bucketSizeInMilliseconds = 1000;
		private CopyOnWriteArrayList<Rate> rateList = new CopyOnWriteArrayList<>();
		private ScheduledExecutorService scheduledExecutorService;
	    private ScheduledFuture<?> scheduledFuture;
		public RateRegistry(long bucketSizeInMillseconds, boolean run){
			this.bucketSizeInMilliseconds = bucketSizeInMillseconds;
			this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
			if(run){
				this.run();
			}
		}
		
		public CopyOnWriteArrayList<Rate> getRateList() {
			return rateList;
		}

		public void setRateList(CopyOnWriteArrayList<Rate> rateList) {
			this.rateList = rateList;
		}

		public ScheduledExecutorService getScheduledExecutorService() {
			return scheduledExecutorService;
		}

		public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
			this.scheduledExecutorService = scheduledExecutorService;
		}		

		public long getBucketSizeInMillseconds() {
			return bucketSizeInMilliseconds;
		}

		public void run() {
			if(this.scheduledFuture==null){
				this.scheduledFuture = this.scheduledExecutorService.scheduleAtFixedRate(() -> {
					rateList.forEach(rate->{						
						int next = (rate.current+1)%rate.buckets.length;
						rate.buckets[next].reset();
						rate.current = next;
					});
				}, 0, this.bucketSizeInMilliseconds, TimeUnit.MILLISECONDS);
			}
		}
	}
	
 	protected Rate(int numberOfBuckets) {
		this.current = 0;
		this.numberOfBuckets = numberOfBuckets;
		this.buckets = new LongAdder[numberOfBuckets + 1];
		for (int i = 0; i < (numberOfBuckets + 1); i++) {
			this.buckets[i] = new LongAdder();
		}
	} 	

	public int getNumberOfBuckets() {
		return numberOfBuckets;
	}

	public RateRegistry getRegistry() {
		return this.registry;
	}

	public void register(RateRegistry registry){
		this.registry = registry;
		registry.rateList.add(this);
	}

	public void mark() {
		this.buckets[this.current].increment();
	}

	public void mark(long value) {
		this.buckets[this.current].add(value);
	}

	public long[] get(int offset, int len) {
		if ((offset+len) > this.numberOfBuckets) {
			throw new IllegalArgumentException("sum of offset and len should not greater than " + this.numberOfBuckets);
		}
		
		int arrayLength = this.buckets.length;
		int c = this.current-offset;
		c=c<0?(c+arrayLength):c;
		
		long[] values = new long[len];
		for (int i = 0; i < len; i++) {
			int index = c - i;
			index = index<0?(index+arrayLength):index;			
			values[i] = this.buckets[index].sum();
		}
		return values;
	}
	
	public long[] getAll() {
		return this.get(0, this.numberOfBuckets);
	}
}
