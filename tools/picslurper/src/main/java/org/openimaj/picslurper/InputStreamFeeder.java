package org.openimaj.picslurper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.openimaj.tools.FileToolsUtil;
import org.openimaj.twitter.collection.StreamJSONStatusList;
import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.GlobalExecutorPool.DaemonThreadFactory;
import org.openimaj.util.parallel.partition.FixedSizeChunkPartitioner;

import twitter4j.TwitterException;
import twitter4j.internal.json.z_T4JInternalJSONImplFactory;
import twitter4j.internal.org.json.JSONObject;

/**
 * Single threaded read an input stream and hand to a consumer
 * @author Jonathan Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class InputStreamFeeder implements StatusFeeder {
	private static final Logger logger = Logger
			.getLogger(InputStreamFeeder.class);
	private z_T4JInternalJSONImplFactory factory;

	/**
	 * Initialise a feeder on a slurper
	 * @param slurper
	 * @throws IOException
	 */
	public InputStreamFeeder(PicSlurper slurper) throws IOException {
		factory = new z_T4JInternalJSONImplFactory(null);
		if (FileToolsUtil.isStdin(slurper)) {
			slurper.stdin = true;
		} else {
			slurper.inputFiles = FileToolsUtil.validateLocalInput(slurper);
			slurper.fileIterator = slurper.inputFiles.iterator();
		}
	}

	@Override
	public void feedStatus(final PicSlurper slurper) throws IOException {
		for (InputStream inStream : slurper) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inStream));
			String line = null;
			while ((line = reader.readLine()) != null) {
				try {
					slurper.handleStatus(factory.createStatus(new JSONObject(line)));
				} catch (Exception e) {
					logger.error("Failed transforming status");
				}
			}
		}
	}

}
