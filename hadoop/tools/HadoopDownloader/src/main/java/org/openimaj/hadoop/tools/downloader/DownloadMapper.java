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
package org.openimaj.hadoop.tools.downloader;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;
import org.openimaj.hadoop.tools.downloader.InputMode.Parser;
import org.openimaj.io.HttpUtils;
import org.openimaj.util.pair.IndependentPair;

/**
 * A Hadoop {@link Mapper} for downloading files.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DownloadMapper extends Mapper<LongWritable, Text, Text, BytesWritable> {
	private static Logger logger = Logger.getLogger(DownloadMapper.class);

	private Parser parser;
	private long sleep;
	private boolean followRedirects;
	private static FSDataOutputStream failureWriter = null;

	protected enum Counters {
		DOWNLOADED,
		FAILED,
		PARSE_ERROR
	}

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		final HadoopDownloaderOptions options = new HadoopDownloaderOptions(context.getConfiguration().getStrings(
				HadoopDownloader.ARGS_KEY));
		options.prepare(false);

		parser = options.getInputParser();
		sleep = options.getSleep();
		followRedirects = options.followRedirects();

		synchronized (DownloadMapper.class) {
			if (options.writeFailures() && failureWriter != null) {
				final String[] taskId = context.getConfiguration().get("mapred.task.id").split("_");
				Path workPath = FileOutputFormat.getWorkOutputPath(context);
				workPath = workPath.suffix("/failures" + "-" + taskId[4].substring(1));
				failureWriter = workPath.getFileSystem(context.getConfiguration()).create(workPath);
			}
		}
	}

	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException {
		if (failureWriter != null) {
			failureWriter.close();
			failureWriter = null;
		}

		super.cleanup(context);
	}

	@Override
	public void map(LongWritable index, Text urlLine, Context context) {
		logger.info("Attempting to download: " + urlLine);

		try {
			final IndependentPair<String, List<URL>> urlData = parser.parse(urlLine.toString());

			if (urlData == null) {
				logger.trace("parser returned null; record skipped.");
				return;
			}

			boolean downloaded = false;
			for (final URL potential : urlData.secondObject()) {
				downloaded = tryDownload(urlData.firstObject(), potential, context);

				if (downloaded) {
					logger.info("Dowloaded: " + potential);
					context.getCounter(Counters.DOWNLOADED).increment(1);
					return;
				}

				logger.trace("Not found; trying next");
			}

			if (!downloaded) {
				logger.info("Failed to download: " + urlLine);
				context.getCounter(Counters.FAILED).increment(1);
				writeFailure(urlLine, context);
			} else {
				context.getCounter(Counters.DOWNLOADED).increment(1);
			}
		} catch (final Exception e) {
			logger.info("Error parsing: " + urlLine);
			logger.trace(e);
			context.getCounter(Counters.PARSE_ERROR).increment(1);
			writeFailure(urlLine, context);
		}

		if (sleep > 0) {
			try {
				logger.trace("Waiting before continuing");
				Thread.sleep(sleep);
			} catch (final InterruptedException e) {
				logger.trace("Wait was interupted; ignoring");
			}
		}
	}

	private synchronized static void writeFailure(Text urlLine, Context context) {
		if (failureWriter != null) {
			try {
				failureWriter.writeUTF(urlLine + "\n");
			} catch (final IOException e) {
				logger.error(e);
			}
		}
	}

	private boolean tryDownload(String key, URL url, Context context) throws InterruptedException {
		try {
			final byte[] bytes = HttpUtils.readURLAsBytes(url, followRedirects);

			if (bytes == null)
				return false;

			final BytesWritable bw = new BytesWritable(bytes);
			context.write(new Text(key), bw);
		} catch (final IOException e) {
			logger.trace(e);
			return false;
		}

		return true;
	}
}
