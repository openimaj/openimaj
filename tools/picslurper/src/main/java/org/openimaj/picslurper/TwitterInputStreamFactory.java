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
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * Factory provides input streams to twitter, managing the disconnection of old streams
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class TwitterInputStreamFactory {
	private static TwitterInputStreamFactory staticFactory;
	private InputStream inputStream;
	private DefaultHttpClient client;
	
	/**
	 * @return a new source of twitter input streams
	 */
	public static TwitterInputStreamFactory streamFactory(){
		if(staticFactory == null){
			try {
				staticFactory = new TwitterInputStreamFactory();
			} catch (Exception e) {
				return null;
			}
		}
		return staticFactory;
	}
	
	private TwitterInputStreamFactory() throws FileNotFoundException, IOException{
		PicSlurper.loadConfig();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("https", 443,SSLSocketFactory.getSocketFactory()));
		
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params,registry);
		
		DefaultHttpClient client = new DefaultHttpClient(manager, params);
		client.getCredentialsProvider().setCredentials(new AuthScope("stream.twitter.com", 443), new UsernamePasswordCredentials(System.getProperty("twitter.user"), System.getProperty("twitter.password")));
		client.getParams().setParameter("http.socket.timeout", new Integer(5000)); // wait 5 seconds before it times out!
		this.inputStream = null;
		this.client = client;
	}
	
	private void reconnect() throws ClientProtocolException, IOException{
		HttpGet get = new HttpGet("https://stream.twitter.com/1/statuses/sample.json");
		HttpResponse resp = client.execute(get);
		this.inputStream = resp.getEntity().getContent();
	}
	
	/**
	 * @return Attempt to disconnect any previous streams and reconnect
	 * @throws IOException
	 */
	public InputStream nextInputStream() throws IOException{
		if(this.inputStream!=null){
			this.inputStream.close();
		}
		reconnect();
		return this.inputStream;
	}
}
