package org.openimaj.demos.irc;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openimaj.demos.irc.WikipediaEditStreamingDataset.WikipediaEdit;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WikipediaEditStreamingDataset extends IRCStreamingDataset<WikipediaEdit>{
//	private static final String RCPMTPA_REGEX = "/\\x0314\\[\\[\\x0307(.+?)\\x0314\\]\\]\\x034 (.*?)\\x0310.*\\x0302(.*?)\\x03.+\\x0303(.+?)\\x03.+\\x03 (.*) \\x0310(.*)\\u0003.*/";
	// 14[[07Gregory Barker14]]4 10 02http://en.wikipedia.org/w/index.php?diff=557160781&oldid=552535205 5* 03Paul MacDermott 5* (+323) 10/* Personal life */ Otto
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
	private static Map<String,String> languageChannels;
	static{
		languageChannels = new HashMap<String, String>();
		languageChannels.put("en", "#en.wikipedia");
	}

	private static Pattern regex = Pattern.compile(RCPMTPA_REGEX);

	protected WikipediaEditStreamingDataset(BlockingDroppingQueue<WikipediaEdit> buffer, String language) throws IOException
	{
		super(buffer, "irc.wikimedia.org", languageChannels.get(language));
	}

	/**
	 * Initialises this edit streaming with an {@link ArrayBlockingDroppingQueue} of
	 * capacity 1.
	 * @param lang
	 * @throws IOException
	 */
	public WikipediaEditStreamingDataset(String lang) throws IOException {
		this(new ArrayBlockingDroppingQueue<WikipediaEdit>(1),lang);
	}

	/**
	 * An edit
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class WikipediaEdit{

		/** **/
		public int change;
		/** **/
		public String user;
		/** **/
		public boolean anon;
		/** **/
		public String flag;
		/** **/
		public boolean isRobot;
		/** **/
		public boolean isNewPage;
		/** **/
		public boolean isUnpatrolled;
		/** **/
		public String page;
		/** **/
		public URL wikipedia;
		/** **/
		public String wikipediaUrl;
		/** **/
		public URL pageUrl;
		/** **/
		public URL userUrl;
		/** **/
		public String comment;

		/**
		 * @param message
		 * @throws IOException
		 */
		public WikipediaEdit(String message) throws IOException {
			Matcher m = regex.matcher(message);
			if(!m.matches()) throw new IOException("Wikipedia message not parseable");
			String group1 = m.group(1);
			String group2 = m.group(2);
			String group3 = m.group(3);
			String group4 = m.group(4);
			String group5 = m.group(5).replace("+", "").replace("-", "");
			int neg = m.group(5).contains("-") ? -1 : 1;
			String group6 = m.group(6);
			change = neg * Integer.parseInt(group5);
//
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
			if(!anon)
				userUrl = new URL(wikipediaUrl + "/wiki/User:" + user.replace(" ", "_"));
			else
				userUrl = null;
			comment = group6;
		}

		@Override
		public String toString() {
			return String.format("User: %s, Change: %d",user,change);
		}

	}

	@Override
	public WikipediaEdit construct(String channel, String sender, String login, String hostname, String message) {
		if(!sender.equals("rc-pmtpa"))
			return null;

		try{
			return new WikipediaEdit(message);
		}catch(Exception e){
			return null;
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Stream<WikipediaEdit> s = new WikipediaEditStreamingDataset(new ArrayBlockingDroppingQueue<WikipediaEdit>(1), "en");
		s.forEach(new Operation<WikipediaEdit>() {

			@Override
			public void perform(WikipediaEdit object) {
				System.out.println(object);
			}
		});
	}
}
