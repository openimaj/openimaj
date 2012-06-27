package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.USMFStatus;

/**
 * For each pair of tokens in a given document emit a count. Also defines a combiner
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PairEmit extends Mapper<LongWritable, Text, BytesWritable, BytesWritable> {
	
	/**
	 * The string which splits times and places. Constructed to be unlikely to be an actual token (words and punctuation)
	 */
	private static final long DEFAULT_TIME = -1;
	private static HadoopTwitterTokenToolOptions options;
	private static long timeDeltaMillis = DEFAULT_TIME;
	Logger logger = Logger.getLogger(PairEmit.class);

	protected static synchronized void loadOptions(Mapper<LongWritable, Text, BytesWritable, BytesWritable>.Context context) throws IOException {
		if (options == null) {
			try {
				options = new HadoopTwitterTokenToolOptions(context.getConfiguration().getStrings(HadoopTwitterTokenToolOptions.ARGS_KEY));
				options.prepare();
				timeDeltaMillis = context.getConfiguration().getLong(PairMutualInformation.TIMEDELTA, DEFAULT_TIME) * 60 * 1000;
				
			} catch (CmdLineException e) {
				throw new IOException(e);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void setup(Mapper<LongWritable, Text, BytesWritable, BytesWritable>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}
	
	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable,Text,BytesWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
		List<String> tokens = null;
		DateTime time = null;
		try {
			USMFStatus status = options.readStatus(value.toString());
			time = status.createdAt();
			if(time == null) return;
			tokens = options.readStatusPart(value.toString());
		} catch (Exception e) {
			return;
		}
		long timeIndex = DEFAULT_TIME;
		if(timeDeltaMillis > 0)
			timeIndex = (time.getMillis() / timeDeltaMillis) * timeDeltaMillis;
		for (int i = 0; i < tokens.size(); i++) 
		{
			String tok1 = tokens.get(i);
			String tok2;
			for (int j = i+1; j < tokens.size(); j++) {
				tok2 = tokens.get(j);
				TokenPairCount tpc = null;
				int cmp = tok1.compareTo(tok2);
				if(cmp > 0){
					tpc = new TokenPairCount(tok2, tok1);
				}
				else{
					tpc = new TokenPairCount(tok1, tok2);
				}
				tpc.paircount = 1;
				BytesWritable keywrite = new BytesWritable(tpc.identifierBinary(timeIndex));
				context.write(keywrite, new BytesWritable(IOUtils.serialize(tpc)));
				context.getCounter(PairEnum.PAIR).increment(1);
			}
			TokenPairCount tpc = new TokenPairCount(tok1);
			tpc.paircount = tokens.size() - 1;
			context.write(new BytesWritable(tpc.identifierBinary(timeIndex)), new BytesWritable(IOUtils.serialize(tpc)));
			context.getCounter(PairEnum.UNARY).increment(1);
		}
		
	};
}
