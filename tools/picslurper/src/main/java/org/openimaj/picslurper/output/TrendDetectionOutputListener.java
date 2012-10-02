package org.openimaj.picslurper.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.picslurper.client.TrendDetector;
import org.openimaj.picslurper.client.TrendDetectorFeatureExtractor;
import org.openimaj.util.pair.IndependentPair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Find trends using graphs for duplicate detection and graph cardinality for trend detection
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TrendDetectionOutputListener implements OutputListener {

	private static final Logger logger = Logger.getLogger(TrendDetectionOutputListener.class);

	private transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Option(
			name = "--time-between-output",
			aliases = "-tbo",
			required = false,
			usage = "The time between trend outputs")
	long timeToWait = 10000;

	@Option(
			name = "--trend-output",
			aliases = "-to",
			required = false,
			usage = "Where to output trending images")
	String trendOutput = "trends.json";

	@Option(
			name = "--top-trends",
			aliases = "-ntrends",
			required = false,
			usage = "The number of trending sets to output")
	int ntrends = 10;

	@Option(
			name = "--feature-extractor-mode",
			aliases = "-feo",
			required = false,
			usage = "The feature extractor used by the trend detector",
			handler = ProxyOptionHandler.class,
			multiValued = true)
	List<TrendDetectorFeatureMode> feMode = new ArrayList<TrendDetectorFeatureMode>();
	List<TrendDetectorFeatureExtractor> feModeOp = new ArrayList<TrendDetectorFeatureExtractor>();

	private long lastTrendOutput;
	private List<IndependentPair<TrendDetectorFeatureMode, TrendDetector>> detectors;

	/**
	 * Construct the publishing connector
	 */
	public TrendDetectionOutputListener() {

	}

	@Override
	public void newImageDownloaded(WriteableImageOutput written) {
		try {
			for (IndependentPair<TrendDetectorFeatureMode, TrendDetector> detector : this.detectors) {
				detector.getSecondObject().indexImage(written);
			}
		} catch (IOException e) {
			logger.error("Failed to index image!",e);
		}
		if(lastTrendOutput == -1 || lastTrendOutput + timeToWait < System.currentTimeMillis()){
			outputTrends();
			lastTrendOutput = System.currentTimeMillis();
		}
	}

	@Override
	public void failedURL(URL url, String reason) {

	}

	@Override
	public void finished() {
		outputTrends();
	}
	static class CountTrend{
		long count;
		Set<WriteableImageOutput> set;
	}
	private void outputTrends() {
		for (IndependentPair<TrendDetectorFeatureMode, TrendDetector> detector : this.detectors) {
			List<Set<WriteableImageOutput>> trending = detector.getSecondObject().trending(ntrends);
			List<CountTrend> out = new ArrayList<CountTrend>();
			for (Set<WriteableImageOutput> set : trending) {
				logger.debug(String.format("[%d] %s", set.size(),set.toString()));
				CountTrend ct = new CountTrend();
				ct.count = set.size();
				ct.set = set;
				out.add(ct);
			}
			String output = gson.toJson(out);
			try {
				PrintWriter writer = new PrintWriter(trendOutput + "-" + detector.firstObject() + ".json","UTF-8");
				writer.println(output);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				logger.error("Failed to write trends", e);
			}
		}
	}

	@Override
	public void prepare() {

		detectors = new ArrayList<IndependentPair<TrendDetectorFeatureMode,TrendDetector>>();
		TrendDetector detector;
		for (int i = 0; i < this.feMode.size(); i++)
		{
			TrendDetectorFeatureExtractor extractor = this.feModeOp.get(i);
			TrendDetectorFeatureMode mode = this.feMode.get(i);
			detector = new TrendDetector();
			detector.setFeatureExtractor(extractor);
			detectors.add(IndependentPair.pair(mode, detector));
		}
		lastTrendOutput = -1;
	}

}
