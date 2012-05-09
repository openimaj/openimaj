package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.io.IOUtils;

/**
 * The input to this reducer is ordered firstly by unary/pairs then within these sets by word
 * Given a particular time period, first read all unary counts and combine for each word
 * Then for all pairs, combine pair instances for a given pair then emit onces a new pair or the end is reached
 * 
 * Once the first non unary word is found, start counting for a particular word
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PairEmitCounter extends Reducer<Text, BytesWritable, Text, BytesWritable> {
	Map<String,Long> unaryCounts = null;
	public PairEmitCounter() {
		this.unaryCounts = new HashMap<String,Long>();
	}
	@Override
	protected void reduce(Text timeS, Iterable<BytesWritable> paircounts, Reducer<Text,BytesWritable,Text,BytesWritable>.Context context) throws IOException ,InterruptedException {
		long time = Long.parseLong(timeS.toString());
		// Start with unary counting, construct the hashmap for this time period
		Iterator<BytesWritable> byteIter = paircounts.iterator();
		if(!byteIter.hasNext())return;
		// Deal with the first time, it must be a unary, impossible for it not to be.
		TokenPairCount currentcount = IOUtils.deserialize(byteIter.next().getBytes(),TokenPairCount.class);
		String currentpair = currentcount.toString();
		for (BytesWritable bytesWritable : paircounts) {
			TokenPairCount newcount = IOUtils.deserialize(bytesWritable.getBytes(), TokenPairCount.class);
			String newpair = newcount.toString();
			if(!newcount.isSingle){
				addUnaryWordCount(currentcount);
				currentcount = newcount;
				currentpair = newpair;
				break;
			}
			else{
				if(newpair.equals(currentpair)){
					currentcount.add(newcount);
				}
				else{
					addUnaryWordCount(currentcount);
					currentpair = newpair;
					currentcount = newcount;
				}
			}
		}
		
		// The currentpair/count are the first word pair, deal with it!
		for (BytesWritable bytesWritable : paircounts) {
			TokenPairCount newcount = IOUtils.deserialize(bytesWritable.getBytes(), TokenPairCount.class);
			if(newcount.isSingle){
				// The list was not sorted!
				throw new IOException("List of TokenPairCounts was not sorted such that ALL singles appeared before pairs");
			}
			String newpair = newcount.toString();
			if(!newpair.equals(currentpair)){
				// emit!
				emitPairCount(time,currentcount,context);
				// now replace
				currentcount = newcount;
				currentpair = newpair;
			}
			else{
				currentcount.add(newcount);
			}
		}
		emitPairCount(time,currentcount,context);
	}
	private void emitPairCount(long time, TokenPairCount currentcount, Reducer<Text,BytesWritable,Text,BytesWritable>.Context context) throws IOException, InterruptedException {
		long tok1count = this.unaryCounts.get(currentcount.firstObject());
		long tok2count = this.unaryCounts.get(currentcount.secondObject());
		Text key = new Text(time + PairEmit.TIMESPLIT + currentcount.toString());
		TokenPairUnaryCount tpuc = new TokenPairUnaryCount(currentcount, tok1count,tok2count);
		context.write(key, new BytesWritable(IOUtils.serialize(tpuc)));
	}
	private void addUnaryWordCount(TokenPairCount currentcount) {
		this.unaryCounts.put(currentcount.firstObject(), currentcount.paircount);
	}
}
