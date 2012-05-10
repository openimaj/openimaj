package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.TokenPairCount;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.TokenPairUnaryCount;
import org.openimaj.io.IOUtils;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PMISortMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
	private static double maxp = -1;
	private static float n = 1;
	/**
	 * does nothing
	 */
	public PMISortMapper(){
		
	}
	
	@Override
	public void setup(Mapper<Text, BytesWritable,Text,BytesWritable>.Context context) throws IOException ,InterruptedException {
		load(context);
	};
	private static void load(Mapper<Text, BytesWritable,Text,BytesWritable>.Context context) {
		maxp = context.getConfiguration().getFloat(PMIPairSort.MINP_KEY, -1);
		n  = context.getConfiguration().getFloat(PMIPairSort.N_PAIRS,-1);
	}

	@Override
	public void map(Text key, BytesWritable value, Mapper<Text, BytesWritable,Text,BytesWritable>.Context context) throws IOException ,InterruptedException {
		TokenPairUnaryCount tpuc = IOUtils.deserialize(value.getBytes(), TokenPairUnaryCount.class);
		double pmi = tpuc.pmi(n);
		if(new Double(pmi).equals(Double.NaN)) return;
		if(maxp == -1 || pmi > maxp){
			IndependentPair<Long, TokenPairCount> timetok = TokenPairCount.parseTimeTokenID(key.toString());
			context.write(new Text("T"+timetok.firstObject() + "#" + pmi), value);
		}
	};
}
