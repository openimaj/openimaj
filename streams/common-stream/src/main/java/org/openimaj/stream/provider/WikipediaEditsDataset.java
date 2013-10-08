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
package org.openimaj.stream.provider;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openimaj.stream.provider.WikipediaEditsDataset.WikipediaEdit;
import org.openimaj.stream.provider.irc.AbstractIRCStreamDataset;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.concurrent.BlockingDroppingQueue;

/**
 * Streaming dataset based on the Wikipedia/Wikimedia edits published in
 * real-time on the wikimedia IRC channels.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class WikipediaEditsDataset extends AbstractIRCStreamDataset<WikipediaEdit> {
	private static final String RCPMTPA_REGEX = "" +
			"\\x0314\\[\\[\\x0307(.+?)\\x0314\\]\\]" +
			"\\x034 (.*?)" +
			"\\x0310.*" +
			"\\x0302(.*?)" +
			"\\x03.+" +
			"\\x0303(.+?)" +
			"\\x03.+" +
			"\\x03 [(](.*)[)] " +
			"\\x0310(.*)\\u0003.*";
	private static Map<String, String> languageChannels;

	static {
		languageChannels = new HashMap<String, String>();
		languageChannels.put("en", "#en.wikipedia");
	}

	private static Pattern regex = Pattern.compile(RCPMTPA_REGEX);

	/**
	 * Construct the edit stream with the given buffer and language.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param language
	 *            the language id; currently only English "en" is supported
	 * @throws IOException
	 *             if there is a problem connecting
	 */
	public WikipediaEditsDataset(BlockingDroppingQueue<WikipediaEdit> buffer, String language)
			throws IOException
	{
		super(buffer, "irc.wikimedia.org", languageChannels.get(language));
	}

	/**
	 * Construct the edit stream with an {@link ArrayBlockingDroppingQueue} of
	 * capacity 1.
	 * 
	 * @param lang
	 *            the language id; currently only English "en" is supported
	 * @throws IOException
	 *             if there is a problem connecting
	 */
	public WikipediaEditsDataset(String lang) throws IOException {
		this(new ArrayBlockingDroppingQueue<WikipediaEdit>(1), lang);
	}

	/**
	 * An edit
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class WikipediaEdit {

		/** The change ID **/
		public int change;
		/** The user who made the change **/
		public String user;
		/** Was the edit anonymous? **/
		public boolean anon;
		/** The raw flags **/
		public String flag;
		/** Was it a robot that made the change? **/
		public boolean isRobot;
		/** Was a new page created? **/
		public boolean isNewPage;
		/** Is the edit unpatrolled? **/
		public boolean isUnpatrolled;
		/** The page that was edited **/
		public String page;
		/** The URL **/
		public URL wikipedia;
		/** The URL as a string **/
		public String wikipediaUrl;
		/** The URL of the page **/
		public URL pageUrl;
		/** The URL of the user **/
		public URL userUrl;
		/** The edit comment **/
		public String comment;

		/**
		 * Default constructor
		 * 
		 * @param message
		 *            the raw message string
		 * @throws IOException
		 *             if an error occurs during parsing
		 */
		protected WikipediaEdit(String message) throws IOException {
			final Matcher m = regex.matcher(message);
			if (!m.matches())
				throw new IOException("Wikipedia message not parseable");
			final String group1 = m.group(1);
			final String group2 = m.group(2);
			final String group3 = m.group(3);
			final String group4 = m.group(4);
			final String group5 = m.group(5).replace("+", "").replace("-", "");
			final int neg = m.group(5).contains("-") ? -1 : 1;
			final String group6 = m.group(6);
			change = neg * Integer.parseInt(group5);

			user = group4;
			anon = Pattern.matches("\\d+.\\d+.\\d+.\\d+", user);
			flag = group2;
			isRobot = flag.contains("M");
			isNewPage = flag.contains("N");
			isUnpatrolled = flag.contains("!");
			page = group1;
			wikipedia = new URL(group3);
			wikipediaUrl = "http://" + wikipedia.getHost();
			pageUrl = new URL(wikipediaUrl + "/wiki/" + page.replace(" ", "_"));
			if (!anon)
				userUrl = new URL(wikipediaUrl + "/wiki/User:" + user.replace(" ", "_"));
			else
				userUrl = null;
			comment = group6;
		}

		@Override
		public String toString() {
			return String.format("User: %s, Change: %d", user, change);
		}

	}

	@Override
	public WikipediaEdit construct(String channel, String sender, String login, String hostname, String message) {
		if (!sender.equals("rc-pmtpa"))
			return null;

		try {
			return new WikipediaEdit(message);
		} catch (final Exception e) {
			return null;
		}
	}
}
