package org.openimaj.hadoop.tools.twitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;
import org.openimaj.twitter.TwitterStatus;

/**
 * This mapper loads arguments for the {@link AbstractTwitterPreprocessingToolOptions} from the {@link HadoopTwitterPreprocessingTool#ARGS_KEY} 
 * variable (once per in memory mapper) and uses these to preprocess tweets. 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TwitterPreprocessingMapper extends Mapper<LongWritable, Text, NullWritable, Text> {
	private static HadoopTwitterPreprocessingToolOptions options = null;
	private static List<TwitterPreprocessingMode<?>> modes = null;
	
	protected static synchronized void loadOptions(Mapper<LongWritable, Text, NullWritable, Text>.Context context) throws IOException {
		if (options == null) {
			try {
				options = new HadoopTwitterPreprocessingToolOptions(context.getConfiguration().getStrings(HadoopTwitterPreprocessingTool.ARGS_KEY));
				options.prepare();
				modes = options.preprocessingMode();
			} catch (CmdLineException e) {
				throw new IOException(e);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}
	
	@Override
	protected void setup(Mapper<LongWritable, Text, NullWritable, Text>.Context context)throws IOException, InterruptedException{
		loadOptions(context);
	}

	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, NullWritable, Text>.Context context) throws java.io.IOException, InterruptedException 
	{
		TwitterStatus status = TwitterStatus.fromString(value.toString());
		if(status.isInvalid()) return;
		for (TwitterPreprocessingMode<?> mode : modes) {
			mode.process(status);
		}
		StringWriter outTweetString = new StringWriter();
		PrintWriter outTweetWriter = new PrintWriter(outTweetString);
		try {
			options.ouputMode().output(status, outTweetWriter );
			context.write(NullWritable.get(), new Text(outTweetString.getBuffer().toString()));
		} catch (Exception e) {
			System.err.println("Failed to write tweet: " + status.text);
			System.err.println("With error: ");
			e.printStackTrace();
		}
	}
}