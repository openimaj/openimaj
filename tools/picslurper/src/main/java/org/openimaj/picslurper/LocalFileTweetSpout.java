package org.openimaj.picslurper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;

public class LocalFileTweetSpout extends LocalTweetSpout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4651675626654237689L;
	
	private String[] files;
	private int nextFileIndex;


	public LocalFileTweetSpout(String... files) {
		this.files = files;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context,SpoutOutputCollector collector) {
		super.open(conf, context, collector);
		this.nextFileIndex = 0;
	}

	@Override
	protected InputStream nextInputStream() throws FileNotFoundException {
		if(this.nextFileIndex >= this.files.length) return null;
		this.nextFileIndex++;
		return new FileInputStream(this.files[this.nextFileIndex-1]);
	}

}
