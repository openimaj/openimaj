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
package org.openimaj.hadoop.tools.download;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class ImageDownloadMapper extends Mapper<LongWritable, Text, Text, BytesWritable> {
	@Override
	public void map(LongWritable index, Text urlLine, Context context) {
//		System.out.println("Attempting to download: " + urlLine);
		try{
			// we expect a format [id]\t[url] as with the image-net url set
			String[] split = urlLine.toString().split("\t");
			String id = split[0];
			String url = split[1];
			// A horrible way of finding the image type and therefore our keys look like: [id].[extentions]
			int lastDot = url.lastIndexOf(".");
			String extention = url.substring(lastDot);
			URI imageURL = new URI(url);
			// Apached HTTPClient stuff. We set a timeout of initial request of 10 seconds, and timeout between packets of 10 seconds
			HttpClient httpclient = new DefaultHttpClient();
			HttpParams params = httpclient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setSoTimeout(params, 10000);
			HttpGet httpget = new HttpGet(imageURL);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			
			// If the image is still there, go grab it!
			if (entity != null) {
				InputStream imageInputStream = entity.getContent();
				ByteArrayOutputStream baos = null;
				baos = new ByteArrayOutputStream();
				IOUtils.copyBytes(imageInputStream, baos , new Configuration(), false);
				context.write(new Text(id+extention), new BytesWritable(baos.toByteArray()));
			}
		}
		catch(Exception e){
			synchronized(this){
				System.err.println("Failed to download: " + urlLine);
			}
		}
	}
}
