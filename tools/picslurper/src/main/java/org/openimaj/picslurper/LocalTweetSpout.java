package org.openimaj.picslurper;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openimaj.twitter.collection.StreamJSONStatusList;
import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

public abstract class LocalTweetSpout implements IRichSpout{
	/**
	 * 
	 */
	private static final long serialVersionUID = 202196766956713428L;
	private static boolean isStreamsFinished = false;
	private static boolean isProcessingFinished = false;
	private Iterator<ReadableWritableJSON> iterator;
	private Set<Object> waitingFor = new HashSet<Object>();
	
	
protected SpoutOutputCollector collector;
	
	@Override
	public void open(@SuppressWarnings("rawtypes")
	Map conf, TopologyContext context,SpoutOutputCollector collector) {
		this.collector = collector;
	}
	
	@Override
	public void close() {}

	@Override
	public void activate() {}

	@Override
	public void deactivate() {}
	
	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void nextTuple() {
		ReadableWritableJSON next = nextTweet();
		if(next==null) return;
		Object messageId = next.get("id");
		this.collector.emit(Arrays.asList((Object)next), messageId);
		addWaitingFor(messageId);
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("tweet"));
	}
	
	protected ReadableWritableJSON nextTweet() {
		if(this.iterator != null && this.iterator.hasNext()){
			return this.iterator.next();
		}
		
		try {
			InputStream nextInputStream = nextInputStream();
			if(nextInputStream == null) {
				// Also, finished!
				setStreamFinished(true);
				// For the situation where nothing was emitted 
				if(this.waitingFor.size() == 0){
					setProcessingFinished(true);
				}
				this.iterator = null;
				return null;
			}
			StreamJSONStatusList l = StreamJSONStatusList.read(nextInputStream);
			this.iterator = l.iterator();
			return this.iterator.next();
		} catch (Exception e) {
			// Something went wrong trying to get the next stream, try again!
			return nextTweet();
		}
	}

	private static synchronized void setStreamFinished(boolean b) {
		isStreamsFinished = b;		
	}
	
	private static synchronized void setProcessingFinished(boolean b) {
		isProcessingFinished = b;		
	}

	protected abstract InputStream nextInputStream() throws Exception;

	public static synchronized boolean isFinished() {
		return isStreamsFinished && isProcessingFinished;
	}
	
	protected void addWaitingFor(Object messageId) {
		this.waitingFor .add(messageId);
		setProcessingFinished(false);
	}
	


	@Override
	public void ack(Object msgId) {
		this.waitingFor.remove(msgId);
		if(this.waitingFor.size() == 0){
			setProcessingFinished(true);
		}
	}

	@Override
	public void fail(Object msgId) {
		ack(msgId);
	}
}
