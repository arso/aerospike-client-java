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

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.async.AsyncClient;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.WritePolicy;

public final class InsertTaskAsync extends InsertTask implements WriteListener {
	
	private final AsyncClient client;
	
	public InsertTaskAsync(
		AsyncClient client, 
		String namespace,
		String setName,
		int startKey, 
		int nKeys, 
		int keySize, 
		int nBins, 
		WritePolicy policy, 
		DBObjectSpec[] spec, 
		CounterStore counters,
		boolean debug
	) {
		super(namespace, setName, startKey, nKeys, keySize, nBins, policy, spec, counters, debug);
		this.client = client;
	}
	
	protected void put(WritePolicy policy, Key key, Bin[] bins) throws AerospikeException {
		// If an error occurred, yield thread to back off throttle.
		// Fail counters are reset every second.
		if (counters.write.timeouts.get() > 0) {
			Thread.yield();
		}
		
		if (counters.write.latency != null) {		
			client.put(policy, new LatencyWriteHandler(), key, bins);
		}
		else {
			client.put(policy, this, key, bins);
		}
	}

	@Override
	public void onSuccess(Key key) {
		counters.write.count.getAndIncrement();
	}

	@Override
	public void onFailure(AerospikeException ae) {
		writeFailure(ae);
	}
	
	private final class LatencyWriteHandler implements WriteListener {
		private long begin;
		
		public LatencyWriteHandler() {
			this.begin = System.currentTimeMillis();
		}
		
		@Override
		public void onSuccess(Key key) {
			long elapsed = System.currentTimeMillis() - begin;
			counters.write.count.getAndIncrement();			
			counters.write.latency.add(elapsed);
		}

		@Override
		public void onFailure(AerospikeException ae) {
			writeFailure(ae);
		}		
	}
}
