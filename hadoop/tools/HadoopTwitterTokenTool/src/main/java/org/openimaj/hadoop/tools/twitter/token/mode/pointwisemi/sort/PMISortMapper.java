package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.PairMutualInformation;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.TokenPairCount;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.TokenPairUnaryCount;
import org.openimaj.io.IOUtils;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PMISortMapper extends Mapper<BytesWritable, BytesWritable, BytesWritable, BytesWritable> {
	private static double minp = -1;
	private static int minPairCount;
	private static Path pairmiloc;
	private static Map<Long, Long> timecounts;
	/**
	 * does nothing
	 */
	public PMISortMapper(){
		
	}
	
	@Override
	public void setup(Mapper<BytesWritable, BytesWritable,BytesWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
		load(context);
	};
	private synchronized static void load(Mapper<BytesWritable, BytesWritable,BytesWritable,BytesWritable>.Context context) throws IOException {
		if(timecounts==null){			
			minp = context.getConfiguration().getFloat(PMIPairSort.MINP_KEY, -1);
			minPairCount = context.getConfiguration().getInt(PMIPairSort.MINPAIRCOUNT_KEY, 0);
			pairmiloc = new Path(context.getConfiguration().get(PMIPairSort.PAIRMI_LOC));
			timecounts = PairMutualInformation.loadTimeCounts(pairmiloc);
		}
	}

	@Override
	public void map(BytesWritable key, BytesWritable value, Mapper<BytesWritable, BytesWritable,BytesWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
		TokenPairUnaryCount tpuc = IOUtils.deserialize(value.getBytes(), TokenPairUnaryCount.class);
		long timet = TokenPairCount.timeFromBinaryIdentity(key.getBytes());
		long n = timecounts.get(timet);
		if( minPairCount != -1 && tpuc.paircount < minPairCount ) return;
		double pmi = tpuc.pmi(n);
		if(new Double(pmi).equals(Double.NaN)) return;
		if( minp == -1 || pmi > minp){
			byte[] serialized = PMIPairSort.timePMIBinary(timet,pmi);
			context.write(new BytesWritable(serialized), value);
		}
	};
}
