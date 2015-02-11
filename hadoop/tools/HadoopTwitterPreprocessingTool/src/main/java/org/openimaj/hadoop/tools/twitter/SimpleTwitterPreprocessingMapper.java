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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;
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
public class SimpleTwitterPreprocessingMapper extends Mapper<LongWritable, Text, NullWritable, Text> {
	private HadoopTwitterPreprocessingToolOptions options = null;
	private List<TwitterPreprocessingMode<?>> modes = null;

	@Override
	protected void setup(Mapper<LongWritable, Text, NullWritable, Text>.Context context) throws IOException,
			InterruptedException
	{
		try {
			final String[] args = HadoopToolsUtil.decodeArgs(context.getConfiguration().getStrings(
					HadoopTwitterPreprocessingTool.ARGS_KEY));
			options = new HadoopTwitterPreprocessingToolOptions(args);
			options.prepare();
			modes = options.preprocessingMode();
		} catch (final CmdLineException e) {
			throw new IOException(e);
		} catch (final Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, NullWritable, Text>.Context context)
			throws java.io.IOException, InterruptedException
	{
		final USMFStatus status = new USMFStatus(options.statusType.type());
		status.fillFromString(value.toString());
		if (status.isInvalid())
			return;

		if (options.preProcessesSkip(status))
			return;
		for (final TwitterPreprocessingMode<?> mode : modes) {
			try {
				TwitterPreprocessingMode.results(status, mode);
			} catch (final Exception e) {
				System.err.println("Failed mode: " + mode);
			}
		}
		if (options.postProcessesSkip(status))
			return;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final OutputStreamWriter ow = new OutputStreamWriter(baos, "UTF-8");
		final PrintWriter outTweetWriter = new PrintWriter(ow);
		try {
			options.ouputMode().output(options.convertToOutputFormat(status), outTweetWriter);
			outTweetWriter.flush();
			context.write(NullWritable.get(), new Text(baos.toByteArray()));
		} catch (final Exception e) {
			System.err.println("Failed to write tweet: " + status.text);
			System.err.println("With error: ");
			e.printStackTrace();
		}
	}
}
