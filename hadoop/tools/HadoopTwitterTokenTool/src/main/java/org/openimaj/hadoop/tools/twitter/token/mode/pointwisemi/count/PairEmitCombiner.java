package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;
import org.openimaj.io.IOUtils;
import org.openimaj.util.pair.IndependentPair;

/**
 * Assumes each key is a timeperiod split set of words ordered by single/pair words then by word order.
 * The key is only used to get time.
 * Using this time the values are combined and used to construct new keys
 * 
 * Emit for the given time a combined version of the word's count. 
 * The word might be a pair or a unary count.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PairEmitCombiner extends Reducer<BytesWritable, BytesWritable, BytesWritable, BytesWritable> {
	
	Logger logger = Logger.getLogger(PairEmitCombiner.class);
	
	@Override
	protected void reduce(BytesWritable timeword, Iterable<BytesWritable> paircounts, Reducer<BytesWritable,BytesWritable,BytesWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
		TokenPairCollector collector = new TokenPairCollector();
//		
		long time = TokenPairCount.timeFromBinaryIdentity(timeword.getBytes());
		
//		logger.info("Combining time: " + time);
		for (BytesWritable bytesWritable : paircounts) {
			TokenPairCount paircount = IOUtils.deserialize(bytesWritable.getBytes(), TokenPairCount.class);
			TokenPairCount collectorRet = collector.add(paircount);
			if(collectorRet != null){
				context.write(new BytesWritable(collectorRet.identifierBinary(time)), new BytesWritable(IOUtils.serialize(collectorRet)));
				if(!collectorRet.isSingle){
					context.getCounter(PairEnum.PAIR_COMBINED).increment(1);
				}else{
					context.getCounter(PairEnum.UNARY_COMBINED).increment(1);
				}
			}
		}
		// Final write
		TokenPairCount collectorRet = collector.getCurrent();
		context.write(new BytesWritable(collectorRet.identifierBinary(time)), new BytesWritable(IOUtils.serialize(collectorRet)));
		if(!collectorRet.isSingle){
			context.getCounter(PairEnum.PAIR_COMBINED).increment(1);
		}else{
			context.getCounter(PairEnum.UNARY_COMBINED).increment(1);
		}
	}
}
