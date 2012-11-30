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

	public void prepare() {
		this.options = new AbstractTwitterPreprocessingToolOptions(args) {

			@Override
			public boolean validate() throws CmdLineException {
				return true;
			}
		};
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
