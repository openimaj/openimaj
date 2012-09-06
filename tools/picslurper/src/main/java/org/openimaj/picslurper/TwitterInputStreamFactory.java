package org.openimaj.picslurper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.log4j.Logger;

/**
 * Factory provides input streams to twitter, managing the disconnection of old
 * streams
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk),
 *
 */
public class TwitterInputStreamFactory {
	private static final Logger logger = Logger.getLogger(TwitterInputStreamFactory.class);
	private static final String TWITTER_SAMPLE_URL = "https://stream.twitter.com/1/statuses/sample.json";
	private static TwitterInputStreamFactory staticFactory;
	private InputStream inputStream;
	private PoolingClientConnectionManager manager;
	private BasicHttpParams params;
	private SchemeRegistry registry;

	/**
	 * @return a new source of twitter input streams
	 */
	public static TwitterInputStreamFactory streamFactory() {
		if (staticFactory == null) {
			try {
				staticFactory = new TwitterInputStreamFactory();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error creating TwitterInputStreamFactory: " + e.getMessage());
				return null;
			}
		}
		return staticFactory;
	}

	private TwitterInputStreamFactory() throws FileNotFoundException, IOException {
		PicSlurper.loadConfig();
		this.params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		HttpConnectionParams.setConnectionTimeout(params, 1000);
		HttpConnectionParams.setSoTimeout(params, 1000);
		this.registry = new SchemeRegistry();
		registry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
	}

	private void reconnect() throws ClientProtocolException, IOException {
		logger.debug("Reconnecting, closing expired clients");

		logger.debug("Constructing new client");
		DefaultHttpClient client = new DefaultHttpClient(manager, params);

		UsernamePasswordCredentials twitterUserPassword = new UsernamePasswordCredentials(
				System.getProperty("twitter.user"),
				System.getProperty("twitter.password")
				);
		client.getCredentialsProvider().setCredentials(new AuthScope("stream.twitter.com", 443), twitterUserPassword);
		HttpGet get = new HttpGet(TWITTER_SAMPLE_URL);
		HttpResponse resp = client.execute(get);
		logger.debug("Done!");
		this.inputStream = resp.getEntity().getContent();
	}

	/**
	 * @return Attempt to disconnect any previous streams and reconnect
	 * @throws IOException
	 */
	public InputStream nextInputStream() throws IOException {
		logger.debug("Attempting to get next input twitter input stream!");
		if (this.inputStream != null) {
			logger.debug("Closing old stream...");
			manager.shutdown();
			this.inputStream.close();
		}
		this.manager = new PoolingClientConnectionManager(registry);
		reconnect();
		return this.inputStream;
	}
}
