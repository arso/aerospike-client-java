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
package com.aerospike.client;

/**
 * An object implementing this interface is passed in <code>scan()</code> calls, so the caller can
 * be notified with scan results.
 */
public interface ScanCallback {
	/**
	 * This method will be called for each record returned from a scan. The user may throw a 
	 * {@link com.aerospike.client.AerospikeException.ScanTerminated AerospikeException.ScanTerminated} 
	 * exception if the scan should be aborted.  If any exception is thrown, parallel scan threads
	 * to other nodes will also be terminated and the exception will be propagated back through the
	 * initiating scan call.
	 * <p>
	 * Multiple threads will likely be calling scanCallback in parallel.  Therefore, your scanCallback
	 * implementation should be thread safe.
	 * 
	 * @param key					unique record identifier
	 * @param record				container for bins and record meta-data
	 * @throws AerospikeException	if error occurs or scan should be terminated.
	 */
	public void scanCallback(Key key, Record record) throws AerospikeException;
}
