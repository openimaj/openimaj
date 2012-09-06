package org.openimaj.picslurper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.io.HttpUtils;
import org.openimaj.io.HttpUtils.MetaRefreshRedirectStrategy;
import org.openimaj.picslurper.consumer.FacebookConsumer;
import org.openimaj.picslurper.consumer.ImgurConsumer;
import org.openimaj.picslurper.consumer.InstagramConsumer;
import org.openimaj.picslurper.consumer.OwlyImageConsumer;
import org.openimaj.picslurper.consumer.TmblrPhotoConsumer;
import org.openimaj.picslurper.consumer.TwitPicConsumer;
import org.openimaj.picslurper.consumer.TwitterPhotoConsumer;
import org.openimaj.picslurper.consumer.YfrogConsumer;
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

	/**
	 * The logger
	 */
	public static Logger logger = Logger.getLogger(StatusConsumer.class);

	final static Pattern urlPattern = new URLPatternProvider().pattern();
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
		siteSpecific.add(new FacebookConsumer());
		siteSpecific.add(new YfrogConsumer());
		siteSpecific.add(new OwlyImageConsumer());
	}
	private boolean outputStats;
	private File globalStats;
	private File outputLocation;

	private Set<String> toProcess;

	private HashSet<String> previouslySeen;

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
		this();
		this.status = status;
		this.outputStats = outputStats;
		this.globalStats = globalStats;
		this.outputLocation = outputLocation;

	}

	/**
	 * for convenience
	 */
	public StatusConsumer() {
		this.previouslySeen = new HashSet<String>();
		this.toProcess = new HashSet<String>();
	}

	class LoggingStatus {
		List<String> strings = new ArrayList<String>();
	}

	@Override
	@SuppressWarnings("unchecked")
	public StatusConsumption call() throws Exception {
		StatusConsumption cons;


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
					add((String) map.get("media_url"));
				}
			}
		// Now add all the entries from entities.urls
		if(status.get("entities")!=null){
			if(((Map<String, Object>) status.get("entities")).get("urls") !=null){
				List<Map<String, Object>> urls = (List<Map<String, Object>>) ((Map<String, Object>) status.get("entities")).get("urls");
				for (Map<String, Object> map : urls) {
					String eurl = (String) map.get("expanded_url");
					if (eurl == null)
						continue;
					add(eurl);
				}
			}
		}
		// Find the URLs in the raw text
		String text = (String) status.get("text");
		if(text != null){ // why was text null?
			Matcher matcher = urlPattern.matcher(text);
			while (matcher.find()) {
				String urlString = text.substring(matcher.start(), matcher.end());
				add(urlString);
			}
		}

		// now go through all the links and process them (i.e. download them)
		cons = processAll();

		if (this.outputStats)
			PicSlurper.updateStats(this.globalStats, cons);
		return cons;
	}

	/**
	 * Process all added URLs
	 * @return the {@link StatusConsumption} statistics
	 * @throws IOException
	 */
	public StatusConsumption processAll() throws IOException {
		StatusConsumption cons = new StatusConsumption();
		cons.nTweets = 1;
		cons.nURLs = 0;
		while(toProcess.size() > 0){
			String url = toProcess.iterator().next();
			toProcess.remove(url);
			cons.nURLs++;
			File urlOut = resolveURL(new URL(url));
			if (urlOut != null) {
				cons.nImages++;
				PicSlurper.updateTweets(urlOut, status);
			}

		}
		return cons;
	}


	/**
	 * Add a URL to process without allowing already seen URLs to be added
	 * @param newURL
	 */
	public void add(String newURL) {
		boolean add = true;
		for (String string : previouslySeen) {
			if (string.startsWith(newURL) || newURL.startsWith(string)) {
				add = false;
				break;
			}
		}
		if (add) {
			logger.debug("New URL added to list: " + newURL);
			toProcess.add(newURL);
			previouslySeen.add(newURL);
		}else{
			logger.debug("URL not added, already exists: " + newURL);
		}
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
			if(this.outputLocation==null) return null;
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
	 * An extention of the {@link MetaRefreshRedirectStrategy} which disallows all redirects
	 * and instead remembers a redirect for use later on.
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class StatusConsumerRedirectStrategy extends MetaRefreshRedirectStrategy {
		private boolean wasRedirected = false;
		private URL redirection;
		@Override
		public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
			wasRedirected = super.isRedirected(request, response, context);

			if (wasRedirected) {
				try {
					this.redirection = this.getRedirect(request, response, context).getURI().toURL();
				} catch (MalformedURLException e) {
					this.wasRedirected = false;
				}
			}
			return false;
		}

		/**
		 * @return whether a redirect was found
		 */
		public boolean wasRedirected() {
			return wasRedirected;
		}

		/**
		 * @return the redirection
		 */
		public URL redirection() {
			return redirection;
		}
	}

	/**
	 * First, try all the {@link SiteSpecificConsumer} instances loaded into
	 * {@link #siteSpecific}. If any consumer takes control of a link the
	 * consumer's output is used
	 *
	 * if this fails use {@link HttpUtils#readURLAsByteArrayInputStream(URL, org.apache.http.client.RedirectStrategy)} with
	 * a {@link StatusConsumerRedirectStrategy} which specifically disallows redirects to be dealt with automatically and
	 * forces this function to be called for each redirect.
	 *
	 *
	 * @param url
	 * @return a list of images or null
	 */
	public List<MBFImage> urlToImage(URL url) {
		logger.debug("Resolving URL: " + url);
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
				StatusConsumerRedirectStrategy redirector = new StatusConsumerRedirectStrategy();
				IndependentPair<HttpEntity, ByteArrayInputStream> headersBais = HttpUtils.readURLAsByteArrayInputStream(url, redirector);
				if(redirector.wasRedirected()){
					logger.debug("Redirect intercepted, adding redirection to list");
					String redirect = redirector.redirection().toString();
					if(!redirect.equals(url.toString())) this.add(redirect);
					return null;
				}
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
