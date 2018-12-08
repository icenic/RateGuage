package com.github.icenic;

import java.util.Arrays;

import com.codahale.metrics.Gauge;
import com.github.icenic.RateGauge.RateData;

public class RateGauge implements Gauge<RateData> {
	private Rate rate;
	private Type type = Type.MEAN;
	
	public enum Type{
		ALL, MEAN, MAX, MIN
	}
	
	public static class RateData{
		private double[] values;
		private long interval;
		private long timestamp;			
		public double[] getValues() {
			return values;
		}
		public void setValues(double[] values) {
			this.values = values;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		public long getInterval() {
			return interval;
		}
		public void setInterval(long interval) {
			this.interval = interval;
		}
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(this.interval).append(':');
			if(this.values!=null){
				for(int i=0; i<this.values.length; i++){
					sb.append(i==0?"":",").append(this.values[i]);
				}
			}
			return sb.toString();
		}
	}
	
	public RateGauge(Rate rate){
		this.rate = rate;
	}
	
	public RateGauge(Rate rate, Type type){
		this.rate = rate;
		this.type = type;
	}
	
	public void mark(){
		this.rate.mark();
	}
	
	public void mark(long value){
		this.rate.mark(value);
	}
	
	@Override
	public RateData getValue() {
		RateData data = new RateData();
		int offset = 1;
		data.setTimestamp(System.currentTimeMillis()-(this.rate.getRegistry().getBucketSizeInMillseconds())*offset);
		data.setInterval(this.rate.getRegistry().getBucketSizeInMillseconds());
		/*ignore current bucket*/
		double[] buckets =  Arrays.stream(this.rate.get(offset, this.rate.getNumberOfBuckets()-offset)).asDoubleStream().toArray();
		double[] singalValue = new double[1];
		switch(this.type){
		case ALL: 
			data.setValues(buckets);
			break;
		case MAX:
			singalValue[0] = Arrays.stream(buckets).max().orElse(0);
			data.setValues(singalValue);
			break;
		case MIN:
			singalValue[0] = Arrays.stream(buckets).min().orElse(0);
			data.setValues(singalValue);
			break;
		default:
			singalValue[0] = Arrays.stream(buckets).average().orElse(0);
			data.setValues(singalValue);
		}
		return data;
	}
}
