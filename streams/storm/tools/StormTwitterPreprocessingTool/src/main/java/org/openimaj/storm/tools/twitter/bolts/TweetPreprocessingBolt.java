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
package org.openimaj.storm.tools.twitter.bolts;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;
import org.openimaj.twitter.USMFStatus;

import backtype.storm.spout.KestrelThriftClient;

/**
 * Instantiate a {@link AbstractTwitterPreprocessingToolOptions} and preprocess
 * tweets
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TweetPreprocessingBolt extends BaseTwitterRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3251642991777398431L;
	private String[] args;
	private AbstractTwitterPreprocessingToolOptions options;
	private List<String> kestrelHosts;
	private Iterator<KestrelThriftClient> kestrelServers;
	private String outputQueue;

	/**
	 * @param outputQueue
	 * @param hosts
	 * @param args
	 */
	public TweetPreprocessingBolt(String outputQueue, List<String> hosts, String[] args) {
		this.args = args;
		this.kestrelHosts = hosts;
		this.outputQueue = outputQueue;
	}

	@Override
	public void prepare() {
		try {
			this.options = new AbstractTwitterPreprocessingToolOptions(args) {

				@Override
				public boolean validate() throws CmdLineException {
					return true;
				}
			};
		} catch (CmdLineException e) {
			throw new RuntimeException(e);
		}
		List<KestrelServerSpec> kestrelSpecList = KestrelServerSpec.parseKestrelAddressList(kestrelHosts);
		this.kestrelServers = KestrelServerSpec.thriftClientIterator(kestrelSpecList);
	}

	private int expire = 0;

	public void setExpireTime(int expire) {
		this.expire = expire;
	}

	@Override
	public void processTweet(String statusString) throws Exception {
		USMFStatus status = new USMFStatus(options.statusType.type());
		status.fillFromString(statusString);
		List<TwitterPreprocessingMode<?>> modes = options.modeOptionsOp;
		if (status.isInvalid())
			return;

		if (options.preProcessesSkip(status))
			return;
		for (TwitterPreprocessingMode<?> mode : modes) {
			try {
				TwitterPreprocessingMode.results(status, mode);
			} catch (Exception e) {
				logger.error("Failed mode: " + mode);
			}
		}
		if (options.postProcessesSkip(status))
			return;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter ow = new OutputStreamWriter(baos, "UTF-8");
		PrintWriter outTweetWriter = new PrintWriter(ow);
		try {
			options.ouputMode().output(options.convertToOutputFormat(status), outTweetWriter);
			outTweetWriter.flush();
			byte[] toEmit = baos.toByteArray();
			KestrelThriftClient client = this.kestrelServers.next();
			client.put(this.outputQueue, new String(toEmit, Charset.forName("UTF-8")), this.expire);
		} catch (Exception e) {
			logger.error("Failed to write tweet: " + status.text);
			logger.error("With error: ");
			e.printStackTrace();
		}
	}
}
