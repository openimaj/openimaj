package org.openimaj.picslurper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.io.HttpUtils;
import org.openimaj.picslurper.consumer.ImgurConsumer;
import org.openimaj.picslurper.consumer.InstagramConsumer;
import org.openimaj.picslurper.consumer.TmblrPhotoConsumer;
import org.openimaj.picslurper.consumer.TwitPicConsumer;
import org.openimaj.picslurper.consumer.TwitterPhotoConsumer;
import org.openimaj.text.nlp.patterns.URLPatternProvider;
import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;
import org.openimaj.util.pair.IndependentPair;

/**
 * A status consumer knows how to consume a {@link ReadableWritableJSON} and
 * output image files. Currently this {@link StatusConsumer} only understands
 * Twitter JSON, perhaps making it abstract and turning {@link #call()} into an
 * abstract function that can deal with other types of status would be sensible
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StatusConsumer implements Callable<StatusConsumption> {

	public static Logger logger = Logger.getLogger(StatusConsumer.class);

	final static Pattern urlPattern = new URLPatternProvider().pattern();
	/**
	 * the default number of redirects which will ever be followed
	 */
	public static final int DEFAULT_FOLLOW_LIMIT = 5;
	private ReadableWritableJSON status;
	/**
	 * the site specific consumers
	 */
	public static List<SiteSpecificConsumer> siteSpecific = new ArrayList<SiteSpecificConsumer>();
	static {
		siteSpecific.add(new InstagramConsumer());
		siteSpecific.add(new TwitterPhotoConsumer());
		siteSpecific.add(new TmblrPhotoConsumer());
		siteSpecific.add(new TwitPicConsumer());
		siteSpecific.add(new ImgurConsumer());
	}
	private boolean outputStats;
	private File globalStats;
	private File outputLocation;

	/**
	 * @param status
	 *            the status to consume
	 * @param outputStats
	 *            whether statistics should be outputted
	 * @param globalStats
	 *            the global statistics file
	 * @param outputLocation
	 *            the output location for this status
	 */
	public StatusConsumer(ReadableWritableJSON status, boolean outputStats, File globalStats,
			File outputLocation) {

		this.status = status;
		this.outputStats = outputStats;
		this.globalStats = globalStats;
		this.outputLocation = outputLocation;

	}

	/**
	 * for convenience
	 */
	public StatusConsumer() {
	}

	class LoggingStatus {
		List<String> strings = new ArrayList<String>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public StatusConsumption call() throws Exception {
		StatusConsumption cons = new StatusConsumption();
		cons.nTweets = 1;
		cons.nURLs = 0;

		Set<String> toProcess = new HashSet<String>();
		// First look for the media object
		List<Map<String, Object>> media = null;
		// check entities media
		if (status.containsKey("links")) {
			Map<String, Object> links = (Map<String, Object>) status.get("links");
			if (links.containsKey("media")) {
				media = (List<Map<String, Object>>) links.get("media");
			}
		}
		if (media != null)
			for (Map<String, Object> map : media) {
				if (map.containsKey("type") && map.get("type").equals("photo")) {
					addNonRepeating((String) map.get("media_url"), toProcess);
				}
			}
		// Now add all the entries from entities.urls
		List<Map<String, Object>> urls = (List<Map<String, Object>>) ((Map<String, Object>) status.get("entities")).get("urls");
		for (Map<String, Object> map : urls) {
			String eurl = (String) map.get("expanded_url");
			if (eurl == null)
				continue;
			addNonRepeating(eurl, toProcess);
		}
		// Find the URLs in the raw text
		String text = (String) status.get("text");
		Matcher matcher = urlPattern.matcher((String) text);
		while (matcher.find()) {
			String urlString = text.substring(matcher.start(), matcher.end());
			addNonRepeating(urlString, toProcess);
		}

		// now go through all the links and process them (i.e. download them)
		cons.nURLs = toProcess.size();
		for (String url : toProcess) {
			logger.debug("Resolving URL: " + url);
			File urlOut = resolveURL(new URL(url));
			if (urlOut != null) {
				cons.nImages++;
			}

		}
		if (this.outputStats)
			PicSlurper.updateStats(this.globalStats, cons);
		return cons;
	}

	private void addNonRepeating(String newURL, Set<String> toProcess) {
		boolean add = true;
		for (String string : toProcess) {
			if (string.startsWith(newURL) || newURL.startsWith(string)) {
				add = false;
				break;
			}
		}
		if (add) {
			toProcess.add(newURL);
		}
	}

	/**
	 * Find the meta refresh component of a page. this is a kind of forwarding
	 * Functions for playing with URLs. Perhaps this should be replaced with
	 * {@link HttpUtils}.
	 * 
	 * @param html
	 * @return the site to refresh to
	 */
	public static String getMetaRefresh(String html) {
		String meta = null;
		int start = html.toLowerCase().indexOf("<meta http-equiv=\"refresh\" content=\"");
		if (start > -1) {
			start += 36;
			int end = html.indexOf('"', start);
			if (end > -1) {
				meta = html.substring(start, end);
				start = meta.toLowerCase().indexOf("url=");
				if (start > -1) {
					start += 4;
					meta = new String(meta.substring(start));
				}
			}
		}

		return meta;
	}

	/**
	 * Given a URL, use {@link #urlToImage(URL)} to turn the url into a list of
	 * images and write the images into the output location using the names
	 * "image_N.png"
	 * 
	 * @param url
	 * @return the root output location
	 */
	public File resolveURL(URL url) {
		List<MBFImage> image = urlToImage(url);
		if (image == null)
			return null;
		File outputDir;
		try {
			outputDir = urlToOutput(url, this.outputLocation);
			File outStats = new File(outputDir, "status.txt");
			StatusConsumption cons = new StatusConsumption();
			cons.nTweets++;
			PicSlurper.updateStats(outStats, cons);
			int n = 0;
			for (MBFImage mbfImage : image) {
				File outImage = new File(outputDir, String.format("image_%d.png", n++));
				ImageUtilities.write(mbfImage, outImage);
			}
			return outputDir;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * calls {@link #urlToImage(URL, int)} with DEFAULT_FOLLOW_LIMIT
	 * 
	 * @param url
	 * @return a list of images if possible
	 */
	public static List<MBFImage> urlToImage(URL url) {
		return urlToImage(url, DEFAULT_FOLLOW_LIMIT);
	}

	/**
	 * @param url
	 * @param refreshLimit
	 *            the number of refreshes to follow
	 * @return a list of images or null if this URL is not to an image
	 */
	public static List<MBFImage> urlToImage(URL url, int refreshLimit) {
		return urlToImage(url, new HashSet<URL>());
	}

	static class StatusConsumerRedirectStrategy extends DefaultRedirectStrategy {
		@Override
		public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
			boolean isRedirect = super.isRedirected(request, response, context);
			Header locationHeader = response.getFirstHeader("location");
			if (isRedirect) {
				logger.debug("It is a redirect, we're being redirected to: " + locationHeader.getValue());
			}
			else {
				logger.debug("Aparently NOT a redirect: " + locationHeader.getValue());
			}
			return false;
		}
	}

	/**
	 * First, try all the {@link SiteSpecificConsumer} instances loaded into
	 * {@link #siteSpecific}. If any consumer takes control of a link the
	 * consumer's output is used
	 * 
	 * if this fails construct a {@link HttpURLConnection} and see if the
	 * content type is text. if not, try to read the URL as an image using
	 * {@link ImageUtilities#readMBF(java.io.InputStream)}. If it is text, try
	 * to see if there is a refresh in the meta using
	 * {@link #getMetaRefresh(String)}. Finally, if there is no refresh, return
	 * null.
	 * 
	 * 
	 * @param url
	 * @param followed
	 * @return a list of images or null
	 */
	public static List<MBFImage> urlToImage(URL url, HashSet<URL> followed) {
		logger.debug("Attempting site specific consumers");
		List<MBFImage> image = null;
		for (SiteSpecificConsumer consumer : siteSpecific) {
			if (consumer.canConsume(url)) {
				logger.debug("Site specific consumer: " + consumer.getClass().getName() + " working on link");
				image = consumer.consume(url);
				if (image != null) {
					logger.debug("Site specific consumer returned non-null, using it");
					break;
				}
			}
		}
		if (image == null) {
			try {
				logger.debug("Site specific consumers failed, trying the raw link");
				IndependentPair<HttpEntity, ByteArrayInputStream> headersBais = HttpUtils.readURLAsByteArrayInputStream(url, new StatusConsumerRedirectStrategy());
				HttpEntity headers = headersBais.firstObject();
				ByteArrayInputStream bais = headersBais.getSecondObject();
				if (headers.getContentType().getValue().contains("text")) {
					logger.debug("Link resolved, text, returning null.");
					return null;
				}
				else {
					// Not text? try reading it as an image!
					image = Arrays.asList(ImageUtilities.readMBF(bais));
					logger.debug("Link resolved, returning image.");
					return image;
				}
			} catch (Throwable e) { // This input might not be an image! deal
									// with that
				logger.debug("Link failed, returning null.");
				return null;
			}
		}
		else {
			return image;
		}
	}

	/**
	 * Construct a file in the output location for a given url
	 * 
	 * @param url
	 * @param outputLocation
	 * @return a file that looks like: outputLocation/protocol/path/query/...
	 * @throws IOException
	 */
	public static synchronized File urlToOutput(URL url, File outputLocation) throws IOException {
		String urlPath = url.getProtocol() + File.separator +
				url.getHost() + File.separator;
		if (!url.getPath().equals(""))
			urlPath += url.getPath() + File.separator;
		if (url.getQuery() != null)
			urlPath += url.getQuery() + File.separator;

		String outPath = outputLocation.getAbsolutePath() + File.separator + urlPath;
		File outFile = new File(outPath);
		if (outFile.exists()) {
			if (outFile.isDirectory()) {
				return outFile;
			}
			else {
				createURLOutDir(outFile);
			}
		} else {
			createURLOutDir(outFile);
		}
		return outFile;
	}

	static void createURLOutDir(File outFile) throws IOException {
		if (!((!outFile.exists() || outFile.delete()) && outFile.mkdirs())) {
			throw new IOException("Couldn't create URL output: " + outFile.getAbsolutePath());
		}
	}

}
