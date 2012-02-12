/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.hadoop.sequencefile.combine;

import java.io.IOException;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * Proxy RecordReader that CombineFileRecordReader can instantiate, which itself
 * translates a CombineFileSplit into a FileSplit.
 * 
 * @param <K> Key type 
 * @param <V> Value type
 */
public class CombineSequenceFileRecordReader<K, V> extends RecordReader<K, V> {
	private CombineFileSplit split;
	private TaskAttemptContext context;
	private int index;
	private RecordReader<K, V> rr;

	@SuppressWarnings("unchecked")
	public CombineSequenceFileRecordReader(CombineFileSplit split, TaskAttemptContext context, Integer index) throws IOException, InterruptedException {
		this.index = index;
		this.split = (CombineFileSplit) split;
		this.context = context;

		this.rr = ReflectionUtils.newInstance(SequenceFileRecordReader.class, context.getConfiguration());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(InputSplit curSplit, TaskAttemptContext curContext) throws IOException, InterruptedException {
		this.split = (CombineFileSplit) curSplit;
		this.context = curContext;

		if (null == rr) {
			rr = ReflectionUtils.newInstance(SequenceFileRecordReader.class, context.getConfiguration());
		}

		FileSplit fileSplit = new FileSplit(this.split.getPath(index),
				this.split.getOffset(index), this.split.getLength(index),
				this.split.getLocations());
		
		this.rr.initialize(fileSplit, this.context);
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return rr.getProgress();
	}

	@Override
	public void close() throws IOException {
		if (null != rr) {
			rr.close();
			rr = null;
		}
	}

	@Override
	public K getCurrentKey()
	throws IOException, InterruptedException {
		return rr.getCurrentKey();
	}

	@Override
	public V getCurrentValue()
	throws IOException, InterruptedException {
		return rr.getCurrentValue();
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		return rr.nextKeyValue();
	}
}