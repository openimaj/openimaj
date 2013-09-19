package org.openimaj.webservice.twitter;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.FileUtils;
import org.openimaj.io.HttpUtils;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.StreamTwitterStatusList;


/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestTwittePreprocessingWebService {
	
	/**
	 * the output folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private TwitterPreprocessingWebService server;
	@Before
	public void createServer() throws Exception{
		this.server = new TwitterPreprocessingWebService();
		this.server.start();
		
	}
	
	@After
	public void shutdownServer() throws Exception{
		this.server.stop();
	}
	
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	
	@Test
	public void testSimpleService() throws IOException {
		File tweetfile = folder.newFile("tweets.json");
		InputStream ts = TestTwittePreprocessingWebService.class.getResourceAsStream(JSON_TWITTER);
		FileUtils.copyStreamToFile(ts, tweetfile);
		
		final HttpClient client = new HttpClient();
		String uri = "http://localhost:8080/process/twitter.usmf?m=SENTIMENT&om=CONDENSED&te=text";
		final PostMethod method = new PostMethod(uri);
		Part[] parts = new Part[1];
		PartSource filePartSource = new FilePartSource(tweetfile);
		parts[0] = new FilePart("data", filePartSource, "application/json", "UTF-8");
		final HttpMethodParams params = new HttpMethodParams();
		
		method.setRequestEntity(new MultipartRequestEntity(parts, params ));

		HttpState state = new HttpState();
		HostConfiguration hostconfig = new HostConfiguration();
		client.executeMethod(hostconfig, method, state);
		InputStream is = method.getResponseBodyAsStream();
		List<USMFStatus> list = StreamTwitterStatusList.readUSMF(is, USMFStatus.class, "UTF-8");
		int count = 0;
		for (USMFStatus usmfStatus : list) {
			Map<String,Object> sent = usmfStatus.getAnalysis("sentiment");
			if(sent == null) {
				continue;
			}
			double pos = (double) sent.get("sentiment_positive");
			if(pos > 0){
				count ++;
			}
		}
		assertTrue(count == 24);
	}
	
//	@Test
//	public void testTwoWayService() throws IOException {
//		File tweetfile = folder.newFile("tweets.json");
//		InputStream ts = TestTwittePreprocessingWebService.class.getResourceAsStream(JSON_TWITTER);
//		FileUtils.copyStreamToFile(ts, tweetfile);
//		
//		final HttpClient client = new HttpClient();
//		
//		
//		String uri = "http://localhost:8080/job/sina.twitter.usmf?m=SENTIMENT&om=CONDENSED&te=text";
//		final PostMethod method = new PostMethod(uri);
//		Part[] parts = new Part[1];
//		PartSource filePartSource = new FilePartSource(tweetfile);
//		parts[0] = new FilePart("data", filePartSource, "application/json", "UTF-8");
//		final HttpMethodParams params = new HttpMethodParams();
//		
//		method.setRequestEntity(new MultipartRequestEntity(parts, params ));
//
//		HttpState state = new HttpState();
//		HostConfiguration hostconfig = new HostConfiguration();
//		client.executeMethod(hostconfig, method, state);
//		
//	}
}
