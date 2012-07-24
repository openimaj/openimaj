package org.openimaj.picslurper;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.openimaj.image.MBFImage;
import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.picslurper.PicSlurper;
import org.openimaj.picslurper.StatusConsumer;
import org.openimaj.picslurper.consumer.InstagramConsumer;
import org.openimaj.picslurper.consumer.TmblrPhotoConsumer;
import org.openimaj.picslurper.consumer.TwitPicConsumer;
import org.openimaj.picslurper.consumer.TwitterPhotoConsumer;

public class TestPicSlurper {
	
	@Test
	public void testURLDir() throws Exception {
		File testOut = File.createTempFile("dir", "out");
		testOut.delete();
		testOut.mkdirs();
		testOut.deleteOnExit();
		
		PicSlurper slurper = new PicSlurper();
		File out = StatusConsumer.urlToOutput(new URL("http://www.google.com"),testOut);
		System.out.println(out);
		out = StatusConsumer.urlToOutput(new URL("http://www.google.com/?bees"),testOut);
		System.out.println(out);
		out = StatusConsumer.urlToOutput(new URL("http://www.google.com/some/long/path.html?bees"),testOut);
		System.out.println(out);
	}
	
	@Test
	public void testImageTweets() throws Exception {
		File testIn = File.createTempFile("image", ".txt");
		File testOut = File.createTempFile("image", "out");
		System.out.println("output location: " + testOut);
		testIn.delete();
		testOut.delete();
		FileUtils.copyStreamToFile(TestPicSlurper.class.getResourceAsStream("/images-10.txt"), testIn);
		
		PicSlurper.main(new String[]{"-i",testIn.getAbsolutePath(),"-o",testOut.getAbsolutePath()});
	}
	
	@Test
	public void testImageTweetsStorm() throws Exception {
		File testIn = File.createTempFile("image", ".txt");
		File testOut = File.createTempFile("image", "out");
		System.out.println("output location: " + testOut);
		testIn.delete();
		testOut.delete();
		FileUtils.copyStreamToFile(TestPicSlurper.class.getResourceAsStream("/images-10.txt"), testIn);
		
		PicSlurper.main(new String[]{"-i",testIn.getAbsolutePath(),"-o",testOut.getAbsolutePath(),"--use-storm"});
	}
	
	@Test
	public void testImageTweetsStormStream() throws Exception {
		File testOut = File.createTempFile("image", "out");
		System.out.println("output location: " + testOut);
		testOut.delete();
		System.setIn(TestPicSlurper.class.getResourceAsStream("/images-10.txt"));
		
		PicSlurper.main(new String[]{"-o",testOut.getAbsolutePath(),"--use-storm"});
	}
	
	@Test
	public void testURLStream() throws MalformedURLException, IOException{
		InputStream stream = StatusConsumer.urlAsStream(new URL("http://t.co/Hbp0Ff6D")).getInputStream();
		System.out.println(FileUtils.readall(stream));
//		stream = StatusConsumer.urlAsStream(new URL("http://p.twimg.com/AwbLNdpCQAEgj5J.jpg")).getInputStream();
		stream = StatusConsumer.urlAsStream(new URL("http://bit.ly/cLYhL")).getInputStream();
		System.out.println(FileUtils.readall(stream));
	}
	@Test
	public void testInstagramConsumer() throws Exception {
		InstagramConsumer consumer = new InstagramConsumer();
		System.out.println(consumer.consume(new URL("http://instagr.am/p/MbsBS_SkJo/")));
	}
	
	@Test
	public void testTwitterPhotoConsumer() throws Exception {
		TwitterPhotoConsumer consumer = new TwitterPhotoConsumer();
		System.out.println(consumer.consume(new URL("http://twitter.com/sentirsevilla/status/222772198987927553/photo/1")));
	}
	
	@Test
	public void testTmblrConsumer() throws Exception {
		PicSlurper.loadConfig();
		TmblrPhotoConsumer consumer = new TmblrPhotoConsumer ();
		MBFImage im1 = consumer.consume(new URL("http://tmblr.co/ZZXIbxP4nbZH")).get(0);
		MBFImage im2 = consumer.consume(new URL("http://www.tumblr.com/ZZXIbxP4nbZH")).get(0);
		MBFImage im3 = consumer.consume(new URL("http://fashion-freedom-and-no-regrets.tumblr.com/post/26923653329")).get(0);
		assertTrue(im1.equals(im2));
		assertTrue(im2.equals(im3));
	}
	@Test
	public void testTwitPicConsumer() throws Exception {
		TwitPicConsumer consumer = new TwitPicConsumer();
		System.out.println(consumer.consume(new URL("http://twitpic.com/a67733")));
		System.out.println(consumer.consume(new URL("http://twitpic.com/a67dei")));
		
	}
}


