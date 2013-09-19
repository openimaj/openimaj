package org.openimaj.webservice.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

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
import org.openimaj.io.HttpUtils;


/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PreProcessClient {
	static class ISConsumer implements Runnable{

		private InputStream is;

		public ISConsumer(InputStream is) {
			this.is = is;
		}

		@Override
		public void run() {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String line = null;
			try {
				while((line = reader.readLine())!=null){
					System.out.println(line);
				}
			} catch (IOException e) {
			}
		}
		
	}
	public static void main(String[] args) throws HttpException, IOException {
		
		final HttpClient client = new HttpClient();
		String uri = "http://localhost:8080/process/twitter.usmf";
		final PostMethod method = new PostMethod(uri);
		Part[] parts = new Part[1];
		File file = new File("/Users/ss/Experiments/trendminer/tweets.2012-01-23.1mil.head100");
		PartSource filePartSource = new FilePartSource(file);
		parts[0] = new FilePart("data", filePartSource, "application/json", "UTF-8");
		final HttpMethodParams params = new HttpMethodParams();
		
		method.setRequestEntity(new MultipartRequestEntity(parts, params ));
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					HttpState state = new HttpState();
					HostConfiguration hostconfig = new HostConfiguration();
					client.executeMethod(hostconfig, method, state);
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		InputStream is = null;
		while(is==null){
			is=method.getResponseBodyAsStream();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		new Thread(new ISConsumer(is)).start();
		
	}
}
