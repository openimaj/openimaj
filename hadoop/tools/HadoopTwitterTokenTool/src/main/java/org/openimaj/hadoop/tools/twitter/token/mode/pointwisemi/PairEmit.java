package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.CountTweetsInTimeperiod;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.TwitterStatus;

/**
 * For each pair of tokens in a given document emit a count. Also defines a combiner
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PairEmit extends Mapper<LongWritable, Text, Text, BytesWritable> {
	private static HadoopTwitterTokenToolOptions options;
	private static long timeDeltaMillis;

	protected static synchronized void loadOptions(Mapper<LongWritable, Text, Text, BytesWritable>.Context context) throws IOException {
		if (options == null) {
			try {
				options = new HadoopTwitterTokenToolOptions(context.getConfiguration().getStrings(HadoopTwitterTokenToolOptions.ARGS_KEY));
				options.prepare();
				timeDeltaMillis = context.getConfiguration().getLong(PairMutualInformation.TIMEDELTA, -1) * 60 * 1000;
				
			} catch (CmdLineException e) {
				throw new IOException(e);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void setup(Mapper<LongWritable, Text, Text, BytesWritable>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}
	
	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable,Text,Text,BytesWritable>.Context context) throws IOException ,InterruptedException {
		List<String> tokens = null;
		DateTime time = null;
		try {
			TwitterStatus status = options.readStatus(value.toString());
			time = status.createdAt();
			tokens = options.readStatusPart(value.toString());
		} catch (Exception e) {
			return;
		}
		long timeIndex = -1;
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
				tpc.totalpaircounts.first = tokens.size();
				tpc.totalpaircounts.second = tokens.size();
				String outname = tpc.toString();
				if(timeIndex > 0) outname = timeIndex + "->"+outname;
				context.write(new Text(outname), new BytesWritable(IOUtils.serialize(tpc)));
			}
		}
		int paircount = (tokens.size() * tokens.size() - (tokens.size())) / 2;
		context.getCounter(PairEnum.PAIR).increment(paircount);
	};
}
