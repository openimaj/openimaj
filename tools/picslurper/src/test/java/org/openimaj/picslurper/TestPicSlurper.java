package org.openimaj.picslurper;


/**
 * Test the various functions of the {@link PicSlurper}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TestPicSlurper {
	// Logger logger = Logger.getLogger(TestPicSlurper.class);
	//
	// /**
	// * Turn off console login to make the tests work without stopping
	// */
	// @Before
	// public void before() {
	// System.setProperty(PicSlurper.ALLOW_CONSOLE_LOGIN,
	// Boolean.toString(false));
	// }
	//
	// /**
	// * Test whether the URLs can be translated to directories specifically
	// * {@link StatusConsumer#urlToOutput(URL, File)}
	// */
	// @Test
	// public void testURLDir() {
	// try {
	// final File testOut = File.createTempFile("dir", "out");
	// testOut.delete();
	// testOut.mkdirs();
	// testOut.deleteOnExit();
	//
	// File out = StatusConsumer.urlToOutput(new URL("http://www.google.com"),
	// testOut);
	// System.out.println(out);
	// out = StatusConsumer.urlToOutput(new URL("http://www.google.com/?bees"),
	// testOut);
	// System.out.println(out);
	// out = StatusConsumer.urlToOutput(new
	// URL("http://www.google.com/some/long/path.html?bees"), testOut);
	// System.out.println(out);
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	// }
	//
	// /**
	// * Test a few URLs that seem to forward forever if not dealt with properly
	// */
	// @Test
	// public void testForeverForwarding() {
	// try {
	// final String[] urls = new String[] {
	// "http://www.thegatewaypundit.com/2012/08/out-of-touch/",
	// "http://i.imgur.com/Y1fMz.jpg",
	// "http://www.timlo.net/?random&random_year=2012&random_month=9&random_cat_id=2&random_cat_id=3&random_cat_id=4&random_cat_id=5&random_cat_id=6&random_cat_id=7&random_cat_id=8"
	// };
	// final StatusConsumer consumer = new StatusConsumer();
	// for (final String string : urls) {
	// consumer.add(string);
	// consumer.processAll(null);
	// }
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * Test the images in /images-10.txt
	// */
	// @Test
	// public void testImageTweets() {
	// try {
	// final File testIn = File.createTempFile("image", ".txt");
	// final File testOut = File.createTempFile("image", "out");
	// System.out.println("output location: " + testOut);
	// testIn.delete();
	// testOut.delete();
	// FileUtils.copyStreamToFile(TestPicSlurper.class.getResourceAsStream("/images-10.txt"),
	// testIn);
	// PicSlurper.main(new String[] { "-i", testIn.getAbsolutePath(), "-o",
	// testOut.getAbsolutePath() });
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * Check the images in /images-10.txt using storm
	// */
	// @Test
	// public void testImageTweetsStorm() {
	// try {
	// final File testIn = File.createTempFile("image", ".txt");
	// final File testOut = File.createTempFile("image", "out");
	// System.out.println("output location: " + testOut);
	// testIn.delete();
	// testOut.delete();
	// FileUtils.copyStreamToFile(TestPicSlurper.class.getResourceAsStream("/images-10.txt"),
	// testIn);
	//
	// StormPicSlurper.main(new String[] { "-i", testIn.getAbsolutePath(), "-o",
	// testOut.getAbsolutePath() });
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// */
	// @Test
	// public void testImageTweetsStormStream() {
	// try {
	// final File testOut = File.createTempFile("image", "out");
	// System.out.println("output location: " + testOut);
	// testOut.delete();
	// System.setIn(TestPicSlurper.class.getResourceAsStream("/images-10.txt"));
	//
	// StormPicSlurper.main(new String[] { "-o", testOut.getAbsolutePath() });
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * Check the {@link InstagramConsumer}
	// */
	// @Test
	// public void testInstagramConsumer() {
	// try {
	// final InstagramConsumer consumer = new InstagramConsumer();
	// System.out.println(consumer.consume(new
	// URL("http://instagr.am/p/MbsBS_SkJo/")));
	// System.out.println(consumer.consume(new
	// URL("http://instagr.am/p/PE1Or4mbNf/")));
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * Check the {@link TwitterPhotoConsumer}
	// */
	// @Test
	// public void testTwitterPhotoConsumer() {
	// try {
	// final TwitterPhotoConsumer consumer = new TwitterPhotoConsumer();
	// System.out.println(consumer
	// .consume(new
	// URL("http://twitter.com/sentirsevilla/status/222772198987927553/photo/1")));
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// // /**
	// // * Check {@link TmblrPhotoConsumer}
	// // *
	// // * @
	// // */
	// // @Test
	// // public void testTmblrConsumer() {
	// // PicSlurper.loadConfig();
	// // TmblrPhotoConsumer consumer = new TmblrPhotoConsumer();
	// // URL im2 = consumer.consume(new
	// // URL("http://www.tumblr.com/ZZXIbxP4nbZH")).get(0);
	// // URL im3 = consumer.consume(new
	// //
	// URL("http://fashion-freedom-and-no-regrets.tumblr.com/post/26923653329")).get(0);
	// // assertTrue(im2.equals(im3));
	// // }
	//
	// /**
	// * Check the {@link ImgurConsumer}
	// */
	// @Test
	// public void testImgurConsumer() {
	// try {
	// final ImgurConsumer consumer = new ImgurConsumer();
	// System.out.println(consumer.consume(new
	// URL("http://imgur.com/a/ijrTZ")));
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * Check the {@link TwitPicConsumer}
	// */
	// @Test
	// public void testTwitPicConsumer() {
	// try {
	// final TwitPicConsumer consumer = new TwitPicConsumer();
	// System.out.println(consumer.canConsume(new
	// URL("http://twitpic.com/au680l")));
	// System.out.println(consumer.consume(new
	// URL("http://twitpic.com/au680l")));
	// System.out.println(consumer.consume(new
	// URL("http://twitpic.com/a67733")));
	// System.out.println(consumer.consume(new
	// URL("http://twitpic.com/a67dei")));
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * Check the {@link TwitPicConsumer}
	// */
	// @Test
	// public void testTwipleConsumer() {
	// try {
	// final TwipleConsumer consumer = new TwipleConsumer();
	// final URL testURL = new URL("http://p.twipple.jp/hUaUl");
	// System.out.println(consumer.canConsume(testURL));
	// System.out.println(consumer.consume(testURL));
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * Check the {@link TwitPicConsumer}
	// */
	// @Test
	// public void testSimpleHTMLScraper() {
	// try {
	// SiteSpecificConsumer consumer = CommonHTMLConsumers.FOTOLOG;
	// URL testURL = new
	// URL("http://www.fotolog.com/brenanatalia/233000000000016162/");
	// System.out.println(consumer.canConsume(testURL));
	// System.out.println(consumer.consume(testURL));
	//
	// // e.g. http://photonui.com/3Hbh
	// consumer = CommonHTMLConsumers.PHOTONUI;
	// testURL = new URL("http://photonui.com/3Hbh");
	// System.out.println(consumer.canConsume(testURL));
	// System.out.println(consumer.consume(testURL));
	//
	// consumer = CommonHTMLConsumers.PICS_LOCKERZ;
	// testURL = new URL("http://pics.lockerz.com/s/243838341");
	// System.out.println(consumer.canConsume(testURL));
	// System.out.println(consumer.consume(testURL));
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * Test the {@link FacebookConsumer}
	// */
	// @Test
	// public void testFacebookConsumer() {
	// try {
	// final FacebookConsumer consumer = new FacebookConsumer();
	// final String[] facebookImages = new String[] {
	// "http://www.facebook.com/dreddyclinic/posts/434840279891662",
	// "https://www.facebook.com/photo.php?pid=1005525&l=8649b56ff5&id=100001915865131",
	// "http://www.facebook.com/photo.php?pid=4097023&l=14a33b4930&id=1159082025",
	// "http://www.facebook.com/photo.php?pid=888026&l=5e0079b36f&id=131625683584436",
	// "http://www.facebook.com/photo.php?pid=1436406&l=893506478b&id=179699385416365"
	//
	// };
	// for (final String string : facebookImages) {
	// System.out.println("trying: " + string);
	// final List<URL> images = consumer.consume(new URL(string));
	// assertTrue(images != null && images.size() > 0);
	// }
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	//
	// }
	//
	// /**
	// * things
	// */
	// @Test
	// public void testTwitter4jMode() {
	// try {
	// final File testOut = File.createTempFile("image", "out");
	// System.out.println("output location: " + testOut);
	// testOut.delete();
	// PicSlurper.main(new String[] { "-oauth", "-o", testOut.getAbsolutePath()
	// });
	// Thread.sleep(10000);
	// } catch (final Exception e) {
	// logger.error("Test failed: " + e.getMessage());
	// }
	// }
}
