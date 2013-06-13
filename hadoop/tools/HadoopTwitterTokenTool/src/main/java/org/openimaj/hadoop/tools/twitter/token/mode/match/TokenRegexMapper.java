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
package org.openimaj.hadoop.tools.twitter.token.mode.match;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.JsonPathFilterSet;

import com.jayway.jsonpath.JsonPath;

/**
 * For each tweet match each token against each regex. if the tweet matches at
 * all, emit the tweet.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TokenRegexMapper extends Mapper<LongWritable, Text, NullWritable, Text> {
	/**
		 * 
		 */
	public TokenRegexMapper() {
	}

	private static ArrayList<Pattern> regexes;
	private static HadoopTwitterTokenToolOptions options;
	private static JsonPath jsonPath;
	private static JsonPathFilterSet filters;

	@Override
	protected void setup(Mapper<LongWritable, Text, NullWritable, Text>.Context context) throws java.io.IOException,
			InterruptedException
	{
		load(context);
	};

	private static synchronized void load(JobContext context) throws IOException {
		if (regexes == null) {
			try {
				regexes = new ArrayList<Pattern>();
				final String[] rstrings = context.getConfiguration().getStrings(TokenRegexStage.REGEX_KEY);
				for (final String regex : rstrings) {
					regexes.add(Pattern.compile(regex));
				}
				options = new HadoopTwitterTokenToolOptions(context.getConfiguration().getStrings(
						HadoopTwitterTokenToolOptions.ARGS_KEY));
				options.prepare();
				jsonPath = JsonPath.compile(options.getJsonPath());
				filters = options.getFilters();
			} catch (final CmdLineException e) {
				throw new IOException(e);
			} catch (final Exception e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void cleanup(org.apache.hadoop.mapreduce.Mapper<LongWritable, Text, NullWritable, Text>.Context context)
			throws IOException, InterruptedException
	{
		regexes = null;
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, NullWritable, Text>.Context context)
			throws java.io.IOException, InterruptedException
	{
		List<String> tokens = null;
		try {
			final String svalue = value.toString();
			if (!filters.filter(svalue))
				return;
			final Object found = jsonPath.read(svalue);
			if (found == null) {
				// System.err.println("Couldn't read the tokens from the tweet");
				return;
			}
			if (found instanceof String) {
				tokens = new ArrayList<String>();
				tokens.add((String) found);
			}
			else if (found instanceof List) {
				tokens = (List<String>) found;
			}
			else if (found instanceof Map) {
				final Map<String, Object> things = (Map<String, Object>) found;
				tokens = new ArrayList<String>();
				for (final Object v : things.values()) {
					tokens.add(v.toString());
				}
			}
			if (tokens.size() == 0) {
				return; // Quietly quit, value exists but was empty
			}

		} catch (final Exception e) {
			System.out.println("Couldn't get tokens from:\n" + value + "\nwith jsonpath:\n" + jsonPath);
			return;
		}
		boolean found = false;
		for (final String token : tokens) {
			for (final Pattern regex : regexes) {
				found = regex.matcher(token).find();
				if (found)
					break;
			}
			if (found)
				break;
		}
		if (found) {
			context.write(NullWritable.get(), value);
		}
	};
}
