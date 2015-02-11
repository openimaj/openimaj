/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.hadoop.tools.twitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;

/**
 * This mapper loads arguments for the
 * {@link AbstractTwitterPreprocessingToolOptions} from the
 * {@link HadoopTwitterPreprocessingTool#ARGS_KEY} variable (once per in memory
 * mapper) and uses these to preprocess tweets.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DateTwitterPreprocessingMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
	private static HadoopTwitterPreprocessingToolOptions options = null;
	private static List<TwitterPreprocessingMode<?>> modes = null;

	protected static synchronized void loadOptions(Mapper<LongWritable, Text, LongWritable, Text>.Context context)
			throws IOException
	{
		if (options == null) {
			try {
				options = new HadoopTwitterPreprocessingToolOptions(context.getConfiguration().getStrings(
						HadoopTwitterPreprocessingTool.ARGS_KEY));
				options.prepare();
				modes = options.preprocessingMode();
			} catch (final CmdLineException e) {
				throw new IOException(e);
			} catch (final Exception e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void setup(Mapper<LongWritable, Text, LongWritable, Text>.Context context) throws IOException,
			InterruptedException
	{
		loadOptions(context);
	}

	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, LongWritable, Text>.Context context)
			throws java.io.IOException, InterruptedException
	{
		final USMFStatus status = new USMFStatus(GeneralJSONTwitter.class);
		status.fillFromString(value.toString());
		if (status.isInvalid())
			return;
		for (final TwitterPreprocessingMode<?> mode : modes) {
			mode.process(status);
		}
		final StringWriter outTweetString = new StringWriter();
		final PrintWriter outTweetWriter = new PrintWriter(outTweetString);
		try {
			options.ouputMode().output(options.convertToOutputFormat(status), outTweetWriter);
			final DateTime date = status.createdAt();
			if (date == null)
				return;
			final DateTime dayDate = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0);

			context.write(new LongWritable(dayDate.getMillis()), new Text(outTweetString.getBuffer().toString()));
		} catch (final Exception e) {
			System.err.println("Failed to write tweet: " + status.text);
			System.err.println("With error: ");
			e.printStackTrace();
		}
	}
}
