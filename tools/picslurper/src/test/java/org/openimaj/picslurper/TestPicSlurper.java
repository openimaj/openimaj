package org.openimaj.picslurper;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.io.FileUtils;
import org.openimaj.picslurper.consumer.FacebookConsumer;
import org.openimaj.picslurper.consumer.ImgurConsumer;
import org.openimaj.picslurper.consumer.InstagramConsumer;
import org.openimaj.picslurper.consumer.SimpleHTMLScrapingConsumer;
import org.openimaj.picslurper.consumer.TmblrPhotoConsumer;
import org.openimaj.picslurper.consumer.TwipleConsumer;
import org.openimaj.picslurper.consumer.TwitPicConsumer;
import org.openimaj.picslurper.consumer.TwitterPhotoConsumer;
import org.openimaj.text.nlp.TweetTokeniserException;

/**
 * Test the various functions of the {@link PicSlurper}
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestPicSlurper {

	/**
	 * Turn off console login to make the tests work without stopping
	 */
	@Before
	public void before() {
		System.setProperty(PicSlurper.ALLOW_CONSOLE_LOGIN, Boolean.toString(false));
	}

	/**
	 * Test whether the URLs can be translated to directories specifically
	 * {@link StatusConsumer#urlToOutput(URL, File)}
	 *
	 * @throws Exception
	 */
	@Test
	public void testURLDir() throws Exception {
		File testOut = File.createTempFile("dir", "out");
		testOut.delete();
		testOut.mkdirs();
		testOut.deleteOnExit();

		File out = StatusConsumer.urlToOutput(new URL("http://www.google.com"), testOut);
		System.out.println(out);
		out = StatusConsumer.urlToOutput(new URL("http://www.google.com/?bees"), testOut);
		System.out.println(out);
		out = StatusConsumer.urlToOutput(new URL("http://www.google.com/some/long/path.html?bees"), testOut);
		System.out.println(out);
	}

	/**
	 * Test a few URLs that seem to forward forever if not dealt with properly
	 *
	 * @throws IOException
	 */
	@Test
	public void testForeverForwarding() throws IOException {
		String[] urls = new String[] {
				"http://www.thegatewaypundit.com/2012/08/out-of-touch/",
				"http://i.imgur.com/Y1fMz.jpg",
				"http://www.timlo.net/?random&random_year=2012&random_month=9&random_cat_id=2&random_cat_id=3&random_cat_id=4&random_cat_id=5&random_cat_id=6&random_cat_id=7&random_cat_id=8"
		};
		StatusConsumer consumer = new StatusConsumer();
		for (String string : urls) {
			consumer.add(string);
			consumer.processAll(null);
		}
	}

	/**
	 * Test the images in /images-10.txt
	 *
	 * @throws Exception
	 */
	@Test
	public void testImageTweets() throws Exception {
		File testIn = File.createTempFile("image", ".txt");
		File testOut = File.createTempFile("image", "out");
		System.out.println("output location: " + testOut);
		testIn.delete();
		testOut.delete();
		FileUtils.copyStreamToFile(TestPicSlurper.class.getResourceAsStream("/images-10.txt"), testIn);

		PicSlurper.main(new String[] { "-i", testIn.getAbsolutePath(), "-o", testOut.getAbsolutePath() });
	}

	/**
	 * Check the images in /images-10.txt using storm
	 *
	 * @throws Exception
	 */
	@Test
	public void testImageTweetsStorm() throws Exception {
		File testIn = File.createTempFile("image", ".txt");
		File testOut = File.createTempFile("image", "out");
		System.out.println("output location: " + testOut);
		testIn.delete();
		testOut.delete();
		FileUtils.copyStreamToFile(TestPicSlurper.class.getResourceAsStream("/images-10.txt"), testIn);

		StormPicSlurper.main(new String[] { "-i", testIn.getAbsolutePath(), "-o", testOut.getAbsolutePath() });
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testImageTweetsStormStream() throws Exception {
		File testOut = File.createTempFile("image", "out");
		System.out.println("output location: " + testOut);
		testOut.delete();
		System.setIn(TestPicSlurper.class.getResourceAsStream("/images-10.txt"));

		StormPicSlurper.main(new String[] { "-o", testOut.getAbsolutePath() });
	}

	/**
	 * Check the {@link InstagramConsumer}
	 *
	 * @throws Exception
	 */
	@Test
	public void testInstagramConsumer() throws Exception {
		InstagramConsumer consumer = new InstagramConsumer();
		System.out.println(consumer.consume(new URL("http://instagr.am/p/MbsBS_SkJo/")));
		System.out.println(consumer.consume(new URL("http://instagr.am/p/PE1Or4mbNf/")));

	}

	/**
	 * Check the {@link TwitterPhotoConsumer}
	 *
	 * @throws Exception
	 */
	@Test
	public void testTwitterPhotoConsumer() throws Exception {
		TwitterPhotoConsumer consumer = new TwitterPhotoConsumer();
		System.out.println(consumer
				.consume(new URL("http://twitter.com/sentirsevilla/status/222772198987927553/photo/1")));
	}

	/**
	 * Check {@link TmblrPhotoConsumer}
	 *
	 * @throws Exception
	 */
	@Test
	public void testTmblrConsumer() throws Exception {
		PicSlurper.loadConfig();
		TmblrPhotoConsumer consumer = new TmblrPhotoConsumer();
		URL im2 = consumer.consume(new URL("http://www.tumblr.com/ZZXIbxP4nbZH")).get(0);
		URL im3 = consumer.consume(new URL("http://fashion-freedom-and-no-regrets.tumblr.com/post/26923653329")).get(0);
		assertTrue(im2.equals(im3));
	}

	/**
	 * Check the {@link ImgurConsumer}
	 *
	 * @throws Exception
	 */
	@Test
	public void testImgurConsumer() throws Exception {
		ImgurConsumer consumer = new ImgurConsumer();
		System.out.println(consumer.consume(new URL("http://imgur.com/a/ijrTZ")));
	}

	/**
	 * Check the {@link TwitPicConsumer}
	 *
	 * @throws Exception
	 */
	@Test
	public void testTwitPicConsumer() throws Exception {
		TwitPicConsumer consumer = new TwitPicConsumer();
		System.out.println(consumer.canConsume(new URL("http://twitpic.com/au680l")));
		System.out.println(consumer.consume(new URL("http://twitpic.com/au680l")));
		System.out.println(consumer.consume(new URL("http://twitpic.com/a67733")));
		System.out.println(consumer.consume(new URL("http://twitpic.com/a67dei")));
	}

	/**
	 * Check the {@link TwitPicConsumer}
	 *
	 * @throws Exception
	 */
	@Test
	public void testTwipleConsumer() throws Exception {
		TwipleConsumer consumer = new TwipleConsumer();
		URL testURL = new URL("http://p.twipple.jp/hUaUl");
		System.out.println(consumer.canConsume(testURL));
		System.out.println(consumer.consume(testURL));
	}

	/**
	 * Check the {@link TwitPicConsumer}
	 *
	 * @throws Exception
	 */
	@Test
	public void testSimpleHTMLScraper() throws Exception {
		SiteSpecificConsumer consumer = new SimpleHTMLScrapingConsumer("fotolog","#flog_img_holder img");
		URL testURL = new URL("http://www.fotolog.com/brenanatalia/233000000000016162/");
		System.out.println(consumer.canConsume(testURL));
		System.out.println(consumer.consume(testURL));

		//e.g. http://photonui.com/3Hbh
		consumer = new SimpleHTMLScrapingConsumer("photonui","#image-box img");
		testURL = new URL("http://photonui.com/3Hbh");
		System.out.println(consumer.canConsume(testURL));
		System.out.println(consumer.consume(testURL));

		consumer = new SimpleHTMLScrapingConsumer("pics.lockerz","#photo");
		testURL = new URL("http://pics.lockerz.com/s/243838341");
		System.out.println(consumer.canConsume(testURL));
		System.out.println(consumer.consume(testURL));
	}


	/**
	 * Test the {@link FacebookConsumer}
	 *
	 * @throws IOException
	 */
	@Test
	public void testFacebookConsumer() throws IOException {
		FacebookConsumer consumer = new FacebookConsumer();
		String[] facebookImages = new String[] {
				"https://www.facebook.com/fsdeventos/posts/251363008318613",
				"http://www.facebook.com/dreddyclinic/posts/434840279891662",
				"https://www.facebook.com/photo.php?pid=1005525&l=8649b56ff5&id=100001915865131",
				"http://www.facebook.com/photo.php?pid=4097023&l=14a33b4930&id=1159082025",
				"http://www.facebook.com/photo.php?pid=888026&l=5e0079b36f&id=131625683584436",
				"http://www.facebook.com/photo.php?pid=1436406&l=893506478b&id=179699385416365"

		};
		for (String string : facebookImages) {
			List<URL> images = consumer.consume(new URL(string));
			assertTrue(images != null && images.size() > 0);
		}
	}

	/**
	 * things
	 *
	 * @throws InterruptedException
	 * @throws TweetTokeniserException
	 * @throws IOException
	 */
	@Test
	public void testTwitter4jMode() throws IOException, TweetTokeniserException, InterruptedException {
		File testOut = File.createTempFile("image", "out");
		System.out.println("output location: " + testOut);
		testOut.delete();
		PicSlurper.main(new String[] { "-oauth", "-o", testOut.getAbsolutePath() });
		Thread.sleep(10000);
	}

}
