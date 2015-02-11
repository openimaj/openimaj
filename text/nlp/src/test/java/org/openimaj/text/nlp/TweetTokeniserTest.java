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
package org.openimaj.text.nlp;

import static org.junit.Assert.assertTrue;
import gov.sandia.cognition.text.token.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.text.nlp.patterns.ComplicatedNumberPatternProvider;
import org.openimaj.text.nlp.patterns.EdgePunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.EmailPatternProvider;
import org.openimaj.text.nlp.patterns.EmoticonPatternProvider;
import org.openimaj.text.nlp.patterns.PatternProvider;
import org.openimaj.text.nlp.patterns.PunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.TruncatedURLPatternProvider;
import org.openimaj.text.nlp.patterns.TwitterStuffPatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider.DFURLPatternProvider;
import org.openimaj.util.pair.IndependentPair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Tests for the {@link TweetTokeniser}
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@SuppressWarnings("unchecked")
public class TweetTokeniserTest {

	private static Gson gson;

	static {
		gson = new GsonBuilder().serializeNulls().create();
	}

	private ArrayList<String> allTweets;

	/**
	 * instantiate an array of tweets used in tests
	 *
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	@Before
	public void setup() throws JsonSyntaxException, IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				TweetTokeniserTest.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt")));
		String line = null;
		allTweets = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			final String json = line;
			allTweets.add(json);
		}
		reader.close();
	}

	/**
	 * Test if newlines are dealt with correctly
	 *
	 * @throws UnsupportedEncodingException
	 * @throws TweetTokeniserException
	 */
	@Test
	public void newlines() throws UnsupportedEncodingException, TweetTokeniserException {
		final String[] tweets = new String[] { "words\r\nacross new\nlines", };
		for (final String text : tweets) {
			final TweetTokeniser tokeniser = new TweetTokeniser(text);
			System.out.println("Tweet: " + text);
			final String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
			System.out.println("Tokens: " + tokens);
		}
	}

	/**
	 * Test if emoticons are dealt with properly (Using an
	 * {@link EmoticonPatternProvider})
	 */
	@Test
	public void emoticons() {
		final EmoticonPatternProvider provider = new EmoticonPatternProvider();
		final IndependentPair<String, Integer>[] teststr = new IndependentPair[] {
				IndependentPair.pair(
						"^_^ there is one :-) :) and now sad :(", 4),
				IndependentPair.pair(
						"Intelligence is only one variable in the equation... (c) Susan DePhillips", 1),
				IndependentPair
						.pair("@avlsuresh I didnt know about it :-)). I would be even more happy when you will give the old one to me.",
								1),
				IndependentPair
						.pair("RT @BThompsonWRITEZ: @libbyabrego honored?! Everybody knows the libster is nice with it...lol...(thankkkks a bunch;))",
								1), IndependentPair.pair("@glamthug well what the fuck man:(", 1), IndependentPair.pair(
						"@dezfafranco -.-' burlate u.u", 2)

		};

		testProvider(provider, teststr);
	}

	/**
	 * Test if emoticons are dealt with properly (Using an
	 * {@link EmoticonPatternProvider})
	 */
	@Test
	public void truncated() {
		final TruncatedURLPatternProvider provider = new TruncatedURLPatternProvider();
		final IndependentPair<String, Integer>[] teststr = new IndependentPair[] {
				IndependentPair
						.pair(
								"RT @SportParadise: TOMMY HILFIGER BOY PRINTED MULTI FULL SHEET SET 200 TC 100% COMBED COTTON NIP 651862293835 | eBay http://bit.l...",
								1
						),
						IndependentPair
						.pair(
								"Make money online,only you need to do is click! Be relaxed. How to be brave. Get innovatiion. Feel brilliant. #AUTOFOLLOW http://adf...",
								1
						),
								IndependentPair
						.pair(
								"RT @1DReport: #2yearsOf1D \u2665 Thank you boys for everything. No words can describe how proud I am to call myself a Directioner. http://t.c ...",
								1
						),
										IndependentPair
						.pair(
								"RT @AfterSchoolDaze: Video: #AfterSchool Performing \"Flashback\" - 2012 Olympic In London Fighting Korea Concert [Air Date 120722] http:/ ...",
								1
						),
												IndependentPair
						.pair(
								"RT @LittlecBeadles: Boston was amazing!!!  Here are the pics for the BOSTON MA Meet &amp; Greet... @LittlecBeadles @godsgirl8494 http:// ...",
								1
						),
		};

		testProvider(provider, teststr);
	}

	/**
	 * Test if URLs are matched correctly using a {@link URLPatternProvider}
	 */
	@Test
	public void urls() {
		final URLPatternProvider provider = new URLPatternProvider();
		final IndependentPair<String, Integer>[] teststr = new IndependentPair[] {
				IndependentPair.pair(
						"here we have, a url: http://woed.de some text", 1),
				IndependentPair.pair(
						"here we have, a url: http://woeD.de", 1),
				IndependentPair.pair(
						"http://foo.com/more_(than)_one_(parens)", 1),
				IndependentPair.pair(
						"http://foo.com/blah_(wikipedia)#cite-1", 1),
				IndependentPair.pair(
						"http://foo.com/blah_(wikipedia)_blah#cite-1", 1),
				IndependentPair.pair(
						"http://foo.com/unicode_(\u272A)_in_parens", 1),
				IndependentPair.pair(
						"http://foo.com/(something)?after=parens", 1),
				IndependentPair.pair(
						"here we have, a url: www.woed.de asdasd", 1),
				IndependentPair.pair(
						"here we have, a url: // www.TrueCaller.com asdasd", 1),
				IndependentPair
						.pair("http://assfsdhgftgfvkcsjtbvtbgmktyhklgbmkgskdmvdthydtyhgfyhdfht (@andreesrr live on http://twitcam.com/2bl4v",
								1),
				IndependentPair.pair("Vou aqui :http://nuke.nativa-latorre.com/Portals/0/dedia.jpg", 1) };

		testProvider(provider, teststr);

	}

	/**
	 * Test the {@link DFURLPatternProvider} for url matching
	 */
	@Test
	public void dfurls() {
		final URLPatternProvider.DFURLPatternProvider provider = new URLPatternProvider.DFURLPatternProvider();
		final IndependentPair<String, Integer>[] teststr = new IndependentPair[] { IndependentPair.pair(
				"here we have, a url: http://woed.de some text", 1), IndependentPair.pair(
						"here we have, a url: http://woeD.de", 1), IndependentPair.pair(
								"here we have, a url: www.woed.de asdasd", 1), IndependentPair.pair(
										"here we have, a url: // www.TrueCaller.com asdasd", 1), IndependentPair.pair(
												"http://foo.com/more_(than)_one_(parens)", 1), IndependentPair.pair(
														"http://foo.com/blah_(wikipedia)#cite-1", 1), IndependentPair.pair(
																"http://foo.com/blah_(wikipedia)_blah#cite-1", 1), IndependentPair.pair(
																		"http://foo.com/unicode_(\u272A)_in_parens", 1), IndependentPair.pair(
																				"http://foo.com/(something)?after=parens", 1), IndependentPair.pair(
																						"@CarideeChris ....Awwwww...lets hope rest of the day you are more lucky ;-)", 0), IndependentPair.pair(
																								"12123321 The Everglades comehttp://short.ie/m0h9q4", 1), IndependentPair.pair(
																										"Vou aqui :http://nuke.nativa-latorre.com/Portals/0/dedia.jpg", 1) };

		testProvider(provider, teststr);

	}

	/**
	 * Test the {@link DFURLPatternProvider} for url matching
	 */
	@Test
	public void edgePunc() {
		String output = EdgePunctuationPatternProvider.fixedges("The (brackets need a space)");
		System.out.println(output);
		output = EdgePunctuationPatternProvider.fixedges("The(brackets need a space)between them");
		System.out.println(output);
		output = EdgePunctuationPatternProvider.fixedges("The(brackets need a space)-between them");
		System.out.println(output);
		output = EdgePunctuationPatternProvider
				.fixedges("Behind the Story of Recording Haru OST \u2018Angel\u2019- SJ struggles in recording http://on.fb.me/cKk1eq");
		System.out.println(output);

	}

	/**
	 * See if punctuation is handeled correctly by
	 * {@link PunctuationPatternProvider}
	 */
	@Test
	public void punctuation() {
		final PunctuationPatternProvider provider = new PunctuationPatternProvider();
		final IndependentPair<String, Integer>[] teststr = new IndependentPair[] {
				IndependentPair.pair(
						"I was so IMPRESSED! ? !!", 3),
				IndependentPair.pair(
						"@CarideeChris ....Awwwww...lets hope rest of the day you are more lucky", 2),
				IndependentPair
						.pair("Maureen Green, the former TV anchor, blogs to keep up with social and workforce trends|Gloria Wright / The ..",
								5),
				IndependentPair.pair("people... so bored!!!!!!!!why u dont want 2 go 2 the campa?!!!why?!!",
						4) };

		testProvider(provider, teststr);
	}

	/**
	 * Check if emails are matched correctly {@link EmailPatternProvider}
	 */
	@Test
	public void email() {
		final EmailPatternProvider provider = new EmailPatternProvider();
		final IndependentPair<String, Integer>[] teststr = new IndependentPair[] { IndependentPair
				.pair("RT @erkthajerk: @Erkthajerk beat sale going on now til march 31st. Contact redplanetmusicgroup@gmail.com for details",
						1), };

		testProvider(provider, teststr);
	}

	/**
	 * See if twitter users, hashtags etc. are matched correctly using
	 * {@link TwitterStuffPatternProvider}
	 */
	@Test
	public void twitterStuff() {
		final TwitterStuffPatternProvider provider = new TwitterStuffPatternProvider();
		final IndependentPair<String, Integer>[] teststr = new IndependentPair[] {
				IndependentPair.pair(
						"RT @erkthajerk: @Erkthajerk beat sale going on now til march 31st. Contact for details", 3),
				IndependentPair
						.pair("you should all follow @sinjax #ff #awesomeGuy", 3),
				IndependentPair
						.pair("@_CarolineF_ *Nods, smiling* Just alright? *touches your arm, seeing flashes of your recent past and drawing my hand away quickly in shock*",
								1),
				IndependentPair.pair("#some_dirty-hashtag right here", 1),
				IndependentPair.pair(
						"you should all follow @sinjax #ff #awesomeGuy", 3),
				IndependentPair
						.pair("RT @GardenForkTV: The Labs in the park - http://bit.ly/doHueQ New on Gardenfork //they look adorable in the snow http://ff.im/-gHOF7",
								2), };

		testProvider(provider, teststr);
	}

	/**
	 * Check if numbers (money, dates etc.) are handled correctly by the
	 * {@link ComplicatedNumberPatternProvider}
	 */
	@Test
	public void numbers() {
		final ComplicatedNumberPatternProvider provider = new ComplicatedNumberPatternProvider();
		final IndependentPair<String, Integer>[] teststr = new IndependentPair[] {
				IndependentPair.pair(
						"Checking out \"HONUS WAGNER HARRISON STUDIOS 1919 $10,000\" on VINTAGE SPORTS CARDS", 1),
				IndependentPair
						.pair("Dorin Dickerson was not on Kipers top 15 TE as Sr or Jr and made Millions today with 4.4 40 time and 43.d vert and now top 1st rd lock",
								1),
				IndependentPair
						.pair("RT @Adam_Schefter: Florida QB Tim Tebow broke the combine record for QBs with a 38-inch vertical jump. He also ran an impressive 40 time ...",
								0), };

		testProvider(provider, teststr);

	}

	private void testProvider(PatternProvider provider, IndependentPair<String, Integer>[] pairs) {
		final Pattern p = provider.pattern();
		for (final IndependentPair<String, Integer> pair : pairs) {
			final String string = pair.firstObject();
			System.out.println("Original: " + string);
			// string = EdgePunctuationPatternProvider.fixedges(string);
			// System.out.println(string);
			final Matcher matches = p.matcher(string);
			final ArrayList<String> allemotes = new ArrayList<String>();
			while (matches.find()) {
				allemotes.add(string.substring(matches.start(), matches.end()));
			}
			final String found = StringUtils.join(allemotes, "====");
			System.out.format("...%s\n...[%s]\n", string, found);
			Assert.assertTrue(allemotes.size() == pair.secondObject());
		}
	}

	/**
	 * Test the {@link TweetTokeniser} as a whole on a few particularly
	 * troublesome tweets
	 *
	 * @throws UnsupportedEncodingException
	 * @throws TweetTokeniserException
	 */
	@Test
	public void testSingleTweets() throws UnsupportedEncodingException, TweetTokeniserException {
		final String[] tweets = new String[] {
				// "Listening to \"Rockin u Radio\" http://www.live365.com/stations/djmarkstevens on Live365.",
				// "Maigc.'everything' was'nt magic",
				// "Maigc.'everything' was'nt magic",
				// "Maigc.everything' 'a's magic",
				// "@CarideeChris ....Awwwww...lets hope rest of the day you are more lucky ;-)",
				// "Intelligence is only one variable in the equation... (c) Susan DePhillips",
				// "@Snuva You might be the Queen of Grump but the Wicked Witch of the South(tm) insists the weekend doesn't start til 2:00 pm tomorrow :-("
				// "RT @iAmTheGreek: everybody in Lehigh Valley (& beyond) should support @homebase610 with voting during March for the Pepsi Refresh Project"
				// "aaaaaaah naaaaaao;;; hino do corinthiaans naaao DDDDD: IOEUIAOUEAIOUOAEIUEIO' (@stevens_adam live on http://twitcam.com/2bqv)",
				// "Maureen Green,the former TV anchor, blogs to keep up with social and workforce trends|Gloria Wright / The .. http://oohja.com/x7OhR",
				// "@avlsuresh I didnt know about it :-)). I would be even more happy when you will give the old one to me.",
				// "@_CarolineF_ *Nods, smiling* Just alright? *touches your arm, seeing flashes of your recent past and drawing my hand away quickly in shock*"
				// "RT @BThompsonWRITEZ: @libbyabrego honored?! Everybody knows the libster is nice with it...lol...(thankkkks a bunch;))",
				// "Big work event tonight means I've got to dress up, mix & mingle with the donors & bust out the non-granola hippy deodorant. Hurumph",
				// "here is a #hashTag",
				// "\u30A2\u30DE\u30BE\u30F3\uFF0F\u6D0B\u66F8\u306E\u65B0\u7740\uFF08\uFF13\uFF09Alpine Glow \u3010\uFFE54,461\u3011 http://tinyurl.com/3yslnw5\u3000(http://tinyurl.com/24e8alm )",
				// "http://assfsdhgftgfvkcsjtbvtbgmktyhklgbmkgskdmvdthydtyhgfyhdfht (@andreesrr live on http://twitcam.com/2bl4v"
				// "RT @BThompsonWRITEZ: @libbyabrego honored?! Everybody knows the libster is nice with it...lol...(thankkkks a bunch;))"
				"@janecds RT _badbristal np VYBZ KARTEL - TURN & WINE&lt; WE DANCEN TO THIS LOL? http://blity.ax.lt/63HPL" };
		for (final String text : tweets) {
			final TweetTokeniser tokeniser = new TweetTokeniser(text);
			System.out.println("Tweet: " + text);
			final String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
			System.out.println("Tokens: " + tokens);
		}
	}

	// @Test
	/**
	 * print tokens, useful for eyeballing a few troublesome tweets
	 *
	 * @throws UnsupportedEncodingException
	 * @throws TweetTokeniserException
	 */
	public void testTweetTokeniser() throws UnsupportedEncodingException, TweetTokeniserException {
		for (final String text : allTweets) {
			final TweetTokeniser tokeniser = new TweetTokeniser(text);
			// System.out.println("Tweet: " + text);
			final String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
			System.out.println("Tokens: " + tokens);
		}
		// new
		// TweetTokeniser("@geektome this is where I point out the 6-mile long #Blackhawks bandwagon and the #fire players on #USMNT #supportchicagoregardlessofsport");
	}

	/**
	 * Compare against how well the python twokeniser works
	 *
	 * @throws TweetTokeniserException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void compareAgainstPython() throws TweetTokeniserException, IOException, InterruptedException {
		final String pythonScriptLocation = "/usr/bin/env python /Users/ss/Development/python/trendminer-python/twokenize.py";
		for (final String text : allTweets) {
			final Map<String, String> tweetOut = new HashMap<String, String>();
			tweetOut.put("text", text);

			final List<String> tokenisedPY = launchScript(pythonScriptLocation, gson.toJson(tweetOut));

			final TweetTokeniser tokeniser = new TweetTokeniser(text);
			final List<Token> tokenisedJ = tokeniser.getTokens();
			// String tokens = "[" + StringUtils.join(tokenisedJ, ",") + "]";
			// String pytokens = "[" + StringUtils.join(tokenisedPY,",") + "]";
			String diffString = "";

			for (int i = 0; i < Math.min(tokenisedJ.size(), tokenisedPY.size()); i++) {
				final String tpy = tokenisedPY.get(i);
				final String tj = tokenisedJ.get(i).getText();
				if (!tpy.equals(tj)) {
					diffString += String.format("%s != %s,", tpy, tj);
				}
			}
			if (tokenisedJ.size() != tokenisedPY.size()) {
				if (tokenisedPY.size() > tokenisedJ.size()) {
					diffString += ",python was LONGER";
				} else {
					diffString += ",java was LONGER";
				}
			}
			if (!diffString.equals("")) {

				// System.out.println();
				// System.out.println("======");
				// System.out.println("Tweet: " + text);
				// System.out.println("JaTokens: " + tokens);
				// System.out.println("PyTokens: " + pytokens);
				// System.out.println("DIFFERENCE: " + diffString);
			}
		}
		// new
		// TweetTokeniser("@geektome this is where I point out the 6-mile long #Blackhawks bandwagon and the #fire players on #USMNT #supportchicagoregardlessofsport");
	}

	/**
	 * Test a bunch of tweets with known outputs
	 *
	 * @throws UnsupportedEncodingException
	 * @throws TweetTokeniserException
	 */
	/*@formatter:off*/
	@Test
	public void testGoodBadAll() throws UnsupportedEncodingException, TweetTokeniserException {

		final IndependentPair<String, int[]>[] teststr = new IndependentPair[] {
				IndependentPair.pair("MyEyesHurtUgh:[iMizzHimAsFuckqq:[AlmostMy2wo GirlsBirthDay[Jeszika&Jazmin]HahaYayGunnaHangOutWith EmYayHaha:|",new int[] { 14, 8, 6 }),
				IndependentPair.pair("Intelligence is only one variable in the equation... (c) Susan DePhillips",new int[] { 12, 10, 2 }),
				IndependentPair.pair("http://assfsdhgftgfvkcsjtbvtbgmktyhklgbmkgskdmvdthydtyhgfyhdfht.com (@andreesrr live on http://twitcam.com/2bl4v",new int[] { 6, 2, 4 }),
				IndependentPair.pair("RT @BThompsonWRITEZ: @libbyabrego honored?! Everybody knows the libster is nice with it...lol...(thankkkks a bunch;))",new int[] { 21, 13, 8 }),
				IndependentPair.pair("RT @erkthajerk: @Erkthajerk beat sale going on now til march 31st. Contact for details",new int[] { 16, 11, 5 }),
				IndependentPair.pair("you should all follow @sinjax #ff #awesomeGuy", new int[] { 7, 4, 3 }),
				IndependentPair.pair("@_CarolineF_ *Nods, smiling* Just alright? *touches your arm, seeing flashes of your recent past and drawing my hand away quickly in shock*",new int[] { 29, 21, 8 }),
				IndependentPair.pair("#some_dirty-hashtag right here", new int[] { 3, 2, 1 }),
				IndependentPair.pair("RT @GardenForkTV: The Labs in the park - http://bit.ly/doHueQ New on Gardenfork //they look adorable in the snow http://ff.im/-gHOF7",new int[] { 21, 14, 7 }),
				IndependentPair.pair("RT @Adam_Schefter: Florida QB Tim Tebow broke the combine record for QBs with a 38-inch vertical jump. He also ran an impressive 40 time",new int[] { 26, 21, 5 }),
				IndependentPair.pair("I favorited a YouTube video -- 'ALCOHOL'- MILLIONAIRES OFFICIAL MUSIC VIDEO http://youtu.be/ubfWnIid5J8?a",new int[] { 14, 10, 4 }),
				IndependentPair.pair("RT @Divinelykells: My baby askin me for seconds! Lol mama mustve threw down! Ahahaha--&gt;I'll be the judge of that!!!!",new int[] { 25, 17, 8 }),
				IndependentPair.pair("&lt;b&gt;Ohio State Buckeyes&lt;/b&gt; Rout Indiana Hoosiers : World Correspondents http://bit.ly/9p3gsI",new int[] { 16, 10, 6 }),
				IndependentPair.pair("RT @KevoMaine: 1989 Honda Accord Muffler Throat Ass\u00ab~~~Lmao what?!",new int[] { 13, 8, 5 }),
				IndependentPair.pair("@SincereDreamsz  AJ&gt;&gt;&gt;&gt;&gt;Justin! I'm willing to bank on that! #checkmate baby daddy!",new int[] { 16, 9, 7 }),
				IndependentPair.pair("Behind the Story of Recording Haru OST \u2018Angel\u2019- SJ struggles in recording http://on.fb.me/cKk1eq",new int[] { 15, 12, 3 }),
				IndependentPair.pair("Something-Unprotected", new int[] { 1, 0, 1 }),
				// IndependentPair.pair("long-thing-with-lots-of-dashes", new
				// int[]{1,1,0}), # FIXME: This should work, or should not?
				// current it does something strange and unintended
				IndependentPair.pair("D'angelo=", new int[] { 2, 0, 2 }),
				IndependentPair.pair("@dezfafranco -.-' burlate u.u", new int[] { 4, 1, 3 }),
				IndependentPair.pair("RT @SportParadise: TOMMY HILFIGER BOY PRINTED MULTI FULL SHEET SET 200 TC 100% COMBED COTTON NIP 651862293835 | eBay http://bit.l...", new int[] { 21, 16, 5 }),
				IndependentPair.pair("RT @AfterSchoolDaze: Video: #AfterSchool Performing \"Flashback\" - 2012 Olympic In London Fighting Korea Concert [Air Date 120722] http:/ ...", new int[] { 24, 13, 11 })

		};

		for (final IndependentPair<String, int[]> pair : teststr) {
			final String string = pair.firstObject();
			final int[] expectedCounts = pair.secondObject();
			final TweetTokeniser tokeniser = new TweetTokeniser(string);
			System.out.println(tokeniser.getStringTokens().size() + ": " + tokeniser.getStringTokens());
			System.out.println(tokeniser.getUnprotectedStringTokens().size() + ": "
					+ tokeniser.getUnprotectedStringTokens());
			System.out.println(tokeniser.getProtectedStringTokens().size() + ": " + tokeniser.getProtectedStringTokens());
			assertTrue(expectedCounts[0] == tokeniser.getStringTokens().size());
			assertTrue(expectedCounts[1] == tokeniser.getUnprotectedStringTokens().size());
			assertTrue(expectedCounts[2] == tokeniser.getProtectedStringTokens().size());
		}

	}

	private List<String> launchScript(String pythonScriptLocation, String json) throws IOException, InterruptedException {
		final Process p = Runtime.getRuntime().exec(pythonScriptLocation);
		final PrintStream ps = new PrintStream(p.getOutputStream());
		ps.println(json);
		ps.close();
		p.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		final String line = br.readLine();
		final Map<String, Object> output = gson.fromJson(line, Map.class);
		return (List<String>) ((Map<String, Object>) output.get("analysis")).get("tokens");
	}

	/**
	 * run {@link #compareAgainstPython()}
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final TweetTokeniserTest test = new TweetTokeniserTest();
		test.setup();
		test.compareAgainstPython();
	}
}
