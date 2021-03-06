/*******************************************************************************
 * Copyright 2012-2014 by Aerospike.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.aerospike.benchmarks;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

public final class LatencyManager {
    private final AtomicInteger[] buckets;
    private final int lastBucket;
    private final int bitShift;

    public LatencyManager(int columns, int bitShift) {
    	this.lastBucket = columns - 1;
    	this.bitShift = bitShift;
		buckets = new AtomicInteger[columns];
		
		for (int i = 0; i < columns; i++) {
			buckets[i] = new AtomicInteger();
		}
    }
    
	public void add(long elapsed) {
		int index = getIndex(elapsed);
		buckets[index].incrementAndGet();
	}

	private int getIndex(long elapsed) {
		long limit = 1L;
		
		for (int i = 0; i < lastBucket; i++) {
			if (elapsed <= limit) {
				return i;
			}
			limit <<= bitShift;
		}
		return lastBucket;
	}
	
	public void printHeader(PrintStream stream) {		
		int limit = 1;
		stream.print("      <=1ms >1ms");
		
		for (int i = 2; i < buckets.length; i++) {			
			limit <<= bitShift;
			String s = " >" + limit + "ms";
			stream.print(s);
		}
		stream.println();
	}
	
	/**
	 * Print latency percents for specified cumulative ranges.
	 * This function is not absolutely accurate for a given time slice because this method 
	 * is not synchronized with the add() method.  Some values will slip into the next iteration.  
	 * It is not a good idea to add extra locks just to measure performance since that actually 
	 * affects performance.  Fortunately, the values will even out over time
	 * (ie. no double counting).
	 */
	public void printResults(PrintStream stream, String prefix) {
		// Capture snapshot and make buckets cumulative.
		int[] array = new int[buckets.length];
		int sum = 0;
		int count;
		
		for (int i = buckets.length - 1; i >= 1 ; i--) {
			 count = buckets[i].getAndSet(0);
			 array[i] = count + sum;
			 sum += count;
		}
		// The first bucket (<=1ms) does not need a cumulative adjustment.
		count = buckets[0].getAndSet(0);
		array[0] = count;
		sum += count;
		
		// Print cumulative results.
		stream.print(prefix);
        int spaces = 6 - prefix.length();

        for (int j = 0; j < spaces; j++) {
        	stream.print(' ');
        }

        double sumDouble = (double)sum;
        int limit = 1;

        printColumn(stream, limit, sumDouble, array[0]);
        printColumn(stream, limit, sumDouble, array[1]);

        for (int i = 2; i < array.length; i++) {
            limit <<= bitShift;
            printColumn(stream, limit, sumDouble, array[i]);
        }
		stream.println();        
	}
	
    private void printColumn(PrintStream stream, int limit, double sum, int value) {
        long percent = 0;

        if (value > 0) {
            percent = Math.round((double)value * 100.0 / sum);
        }
        String percentString = Long.toString(percent) + "%";      
        int spaces = Integer.toString(limit).length() + 4 - percentString.length();

        for (int j = 0; j < spaces; j++) {
        	stream.print(' ');
        }
        stream.print(percentString);
    }
}
