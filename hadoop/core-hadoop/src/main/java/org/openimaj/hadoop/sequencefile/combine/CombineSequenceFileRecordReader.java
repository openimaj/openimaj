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