package edu.scripps.fl.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressWriter {

	private static final Logger log = LoggerFactory.getLogger(ProgressWriter.class);

	private AtomicInteger counter = new AtomicInteger();
	private int order = 100;
	private long sum = 0;
	private long start = System.currentTimeMillis();
	private String message = "Progress";

	public ProgressWriter(String message) {
		this.message = message;
	}

	public void increment() {
		long end = System.currentTimeMillis();
		sum += end - start;
		start = end;
		int count = counter.incrementAndGet();
		if (count % order == 0) {
			notifyProgress(count);
			if (String.valueOf(count).length() > String.valueOf(order).length())
				order *= 10;
		}
	}

	public void notifyProgress(int progress) {
		log.debug(String.format("[%s] Count: %s, Total time: %s, Average time: %.3f(s)", message, counter, hms(sum / 1000), getAverageSeconds()));
	}

	protected double getAverageSeconds() {
		return ((double) sum / 1000D) / (double) counter.get();
	}

	public int getCount() {
		return counter.get();
	}

	static String hms(Number secs) {
		long hours = secs.intValue() / 3600;
		long remainder = secs.intValue() % 3600;
		long minutes = remainder / 60;
		long seconds = remainder % 60;
		return ((hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
	}
}