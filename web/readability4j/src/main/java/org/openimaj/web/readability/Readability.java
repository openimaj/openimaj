/**
 * Copyright 2010 The University of Southampton, Yahoo Inc., and the
 * individual contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openimaj.web.readability;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cyberneko.html.parsers.DOMFragmentParser;
import org.cyberneko.html.parsers.DOMParser;
import org.pojava.datetime.DateTime;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class for extracting the "content" from web-pages, and ignoring adverts, etc. 
 * Based upon readability.js (http://lab.arc90.com/experiments/readability/) and 
 * modified to behave better for certain sites (and typically better mimic Safari 
 * Reader functionality).
 *  
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Michael Matthews (mikemat@yahoo-inc.com)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class Readability
{
	/**
	 * Regular expressions for different types of content
	 */
	protected static class Regexps {

		public static String unlikelyCandidatesRe = "(?i)combx|comment|disqus|foot|header|menu|rss|shoutbox|sidebar|sponsor|story-feature|banner"; //caption?
		public static String okMaybeItsACandidateRe = "(?i)and|comments|article|body|column|main";
		public static String positiveRe = "(?i)article|body|comments|content|entry|hentry|page|pagination|post|text";
		public static String negativeRe= "(?i)combx|comment|contact|foot|footer|footnote|link|masthead|media|meta|promo|related|scroll|shoutbox|sponsor|tags|widget|warning";
		public static String divToPElementsRe = "(?i)(a|blockquote|dl|div|img|ol|p|pre|table|ul)";
		public static String replaceBrsRe = "(?i)(<br[^>]*>[ \n\r\t]*){2,}";
		public static String replaceFontsRe ="(?i)<(\\/?)font[^>]*>";
		public static String trimRe = "^\\s+|\\s+$";
		public static String normalizeRe = "\\s{2,}";
		public static String killBreaksRe = "(<br\\s*\\/?>(\\s|&nbsp;?)*){1,}";
		public static String videoRe = "(?i)http:\\/\\/(www\\.)?(youtube|vimeo)\\.com";

		public static String titleSeparatorRe = "\\|\\-\\/";

		//this is used to try and find elements that represent sub-headings (that are not h1..h6)
		public static String likelySubheadCandidateRe = "(?i)cross-head";
	}

	enum Flag {
		FLAG_STRIP_UNLIKELYS,
		FLAG_WEIGHT_CLASSES
	}

	/**
	 * Threshold for removing elements with lots of links
	 */
	public static float LINK_DENSITY_THRESHOLD = 0.33F; 


	//IVARS below
	protected Document document;
	private Node bodyCache;
	protected EnumSet<Flag> flags = EnumSet.allOf(Flag.class);

	protected String articleTitle;
	protected Element articleContent;
	protected String article_date_string;
	protected Date article_date;
	protected String article_contentType;

	protected boolean debug = false;

	protected boolean addTitle = false;

	/**
	 * Construct with the given document. Debugging is disabled. 
	 * @param document The document.
	 */
	public Readability(Document document) {
		this(document, false);
	}

	/**
	 * Construct with the given document. The second argument can be used to enable
	 * debugging output. 
	 * @param document The document.
	 * @param debug Enable debugging output.
	 */
	public Readability(Document document, boolean debug) {
		this(document, debug, false);
	}

	/**
	 * Construct with the given document. The second argument can be used to enable
	 * debugging output. The third option controls whether the title should be 
	 * included in the output.
	 * @param document The document.
	 * @param debug Enable debugging output.
	 * @param addTitle Add title to output.
	 */
	public Readability(Document document, boolean debug, boolean addTitle) {
		this.debug = debug;
		this.document = document;
		this.addTitle = addTitle;
		augmentDocument(document);
		init();
	}

	/**
	 * Iterates through all the ELEMENT nodes in a document
	 * and gives them ids if they don't already have them.
	 * 
	 * @param document
	 */
	public static void augmentDocument(Document document) {
		DocumentTraversal traversal = (DocumentTraversal) document;

		TreeWalker walker = traversal.createTreeWalker(document, NodeFilter.SHOW_ELEMENT, null, true);

		traverseLevel(walker, 0);
	}

	private static int traverseLevel(TreeWalker walker, int counter) {
		// describe current node:
		Node parend = walker.getCurrentNode();
		
		if (parend instanceof Element) {
			if (((Element)parend).getAttribute("id").length() == 0) {
				((Element)parend).setAttribute("id", "gen-id-"+counter);
				counter++;
			}
		}
		
		// traverse children:
		for (Node n = walker.firstChild(); n != null; 
		n = walker.nextSibling()) {
			counter = traverseLevel(walker, counter);
		}

		// return position to the current (level up):
		walker.setCurrentNode(parend);
		
		return counter;
	}

	protected void dbg(String s) {
		if (debug)
			System.err.println(s);
	}

	protected String getTitle() {
		NodeList l = document.getElementsByTagName("title");

		if (l.getLength() == 0) return "";

		return l.item(0).getTextContent();
	}

	/**
	 * Javascript-like String.match
	 * @param input
	 * @param regex
	 * @return
	 */
	protected String[] match(String input, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(input);
		List<String> matches = new ArrayList<String>();

		while ( matcher.find() ) {
			matches.add(matcher.group(0));
		}

		return matches.toArray(new String[matches.size()]);
	}

	/**
	 * @return True if the article has any detected content; false otherwise.
	 */
	public boolean hasContent() {
		return articleContent != null;
	}
	
	/**
	 * Javascript-like String.search
	 * @param input
	 * @param regex
	 * @return
	 */
	protected int search(String input, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(input);

		if (!matcher.find()) return -1;
		return matcher.start();
	}


	protected void findArticleEncoding() {
		NodeList nl = document.getElementsByTagName("meta");
		for (int j=0; j<nl.getLength(); j++) {
			if (((Element)nl.item(j)).getAttribute("http-equiv").equals("Content-Type")) {
				article_contentType = ((Element)nl.item(j)).getAttribute("content");
				return;
			}
		}

	}

	protected void findArticleDate() {
		//<meta name="OriginalPublicationDate" content="2010/07/12 14:08:02"/>
		//<meta name="DC.date.issued" content="2010-07-12">
		NodeList nl = document.getElementsByTagName("meta");
		for (int j=0; j<nl.getLength(); j++) {
			if (((Element)nl.item(j)).getAttribute("name").equals("OriginalPublicationDate")) {
				article_date_string = ((Element)nl.item(j)).getAttribute("content");
				article_date = DateTime.parse(article_date_string).toDate();
				return;
			}
			if (((Element)nl.item(j)).getAttribute("name").equals("DC.date.issued")) {
				article_date_string = ((Element)nl.item(j)).getAttribute("content");
				article_date = DateTime.parse(article_date_string).toDate();
				return;
			}
		}

		//<time datetime="2010-07-12T10:26BST" pubdate>Monday 12 July 2010 10.26 BST</time>
		nl = document.getElementsByTagName("time");
		for (int j=0; j<nl.getLength(); j++) {
			if (((Element)nl.item(j)).getAttributeNode("pubdate") != null) {
				article_date_string = ((Element)nl.item(j)).getAttribute("datetime");
				article_date = DateTime.parse(article_date_string).toDate();
				return;
			}
		}

		//<span class="date">14:08 GMT, Monday, 12 July 2010 15:08 UK</span>
		//<p class="date">09.07.2010 @ 17:49 CET</p>
		//<p class="date">Today @ 09:29 CET</p>
		nl = document.getElementsByTagName("*");
		for (int j=0; j<nl.getLength(); j++) {
			if ((((Element)nl.item(j)).getAttribute("class").contains("date") ||
					((Element)nl.item(j)).getAttribute("class").contains("Date") ) && 
					!(((Element)nl.item(j)).getAttribute("class").contains("update") ||
							((Element)nl.item(j)).getAttribute("class").contains("Update"))
			) {
				article_date_string = getInnerTextSep((Element)nl.item(j)).trim();
				parseDate();
				return;
			}
		}
		for (int j=0; j<nl.getLength(); j++) {
			if ((((Element)nl.item(j)).getAttribute("id").contains("date") ||
					((Element)nl.item(j)).getAttribute("id").contains("Date") ) && 
					!(((Element)nl.item(j)).getAttribute("id").contains("update") ||
							((Element)nl.item(j)).getAttribute("id").contains("Update"))
			) {
				article_date_string = getInnerTextSep((Element)nl.item(j)).trim();
				parseDate();
				return;
			}
		}

		//Last updated at 3:05 PM on 12th July 2010
		nl = document.getElementsByTagName("*");
		for (int j=0; j<nl.getLength(); j++) {
			String text = nl.item(j).getTextContent();

			if (text == null)
				continue;

			Pattern p = Pattern.compile("Last updated at (\\d+:\\d\\d [AP]M on \\d+[thsndr]+ \\w+ \\d\\d\\d\\d)");
			Matcher m = p.matcher(text);
			if (m.find()) {
				article_date_string = m.group(1);

				String cpy = article_date_string.replaceAll("th", "");
				cpy = cpy.replaceAll("st", "");
				cpy = cpy.replaceAll("nd", "");
				cpy = cpy.replaceAll("rd", "");

				SimpleDateFormat sdf = new SimpleDateFormat("h:mm a 'on' dd MMMM yyyy");
				try { article_date = sdf.parse(cpy); } catch (ParseException e) {}
				return;
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected void parseDate() {
		if (article_date_string == null || article_date_string.trim().isEmpty() ) return;

		if (article_date_string.contains("Today")) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("'Today @' HH:mm z");
				article_date = sdf.parse(article_date_string);
				Date now = new Date();
				article_date.setDate(now.getDate());
				article_date.setMonth(now.getMonth());
				article_date.setYear(now.getYear());
			} catch (ParseException e) {}
		} else {
			try { 
				SimpleDateFormat sdf = new SimpleDateFormat("h:mm z',' E',' dd M yyyy");
				article_date = sdf.parse(article_date_string); 
			} catch (ParseException e) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy '@' HH:mm z");
					article_date = sdf.parse(article_date_string);
				} catch (ParseException ee) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						article_date = sdf.parse(article_date_string);
					} catch (ParseException eee) {
						try {
							article_date = DateTime.parse(article_date_string).toDate();
						} catch (IllegalArgumentException ie) {
						} catch (java.lang.ArrayIndexOutOfBoundsException ie) {
							System.out.println(article_date_string);
						}							
					}	
				}
			}
		}
	}

	/**
	 * Get the article title.
	 *
	 * @return void
	 **/
	protected String findArticleTitle() {
		String curTitle = "", origTitle = "";

		curTitle = origTitle = getTitle();

		//
		List<String> potentialTitles = new ArrayList<String>();
		for (int i=1; i<=6; i++) {
			NodeList nl = document.getElementsByTagName("h"+i);
			if (nl.getLength() > 0) {
				for (int j=0; j<nl.getLength(); j++)
					potentialTitles.add(nl.item(j).getTextContent().trim());
			}
		}

		String potentialTitle = null;
		int score = 0;
		for (String s : potentialTitles) {
			if (s.length()>score && curTitle.contains(s)) {
				potentialTitle = s;
				score = s.length();
			}
		}
		if (potentialTitle != null) return potentialTitle;
		//

		if(match(curTitle, " ["+Regexps.titleSeparatorRe+"]+ ").length > 0)
		{
			curTitle = origTitle.replaceAll("(.*) ["+Regexps.titleSeparatorRe+"]+ .*", "$1");

			if(curTitle.split(" ").length < 3) {
				curTitle = origTitle.replaceAll("(?i)[^"+Regexps.titleSeparatorRe+"]*["+Regexps.titleSeparatorRe+"]+(.*)", "$1");
			}
		}
		else if(curTitle.indexOf(": ") != -1)
		{
			curTitle = origTitle.replaceAll("(?i).*:(.*)", "$1");

			if(curTitle.split(" ").length < 3) {
				curTitle = origTitle.replaceAll("(?i)[^:]*[:](.*)", "$1");
			}
		}
		else if(curTitle.length() > 150 || curTitle.length() < 15)
		{
			NodeList hOnes = document.getElementsByTagName("h1");
			if(hOnes.getLength() == 1)
			{
				curTitle = getInnerText((Element) hOnes.item(0));
			}
		}

		curTitle = curTitle.replaceAll( Regexps.trimRe, "" );

		if(curTitle.split(" ").length <= 3) {
			curTitle = origTitle;
		}

		return curTitle;
	}	

	/**
	 * Equivalent to document.body in JS
	 * @return
	 */
	protected Element getBody() {
		NodeList nl = document.getElementsByTagName("body");

		if (nl.getLength() == 0) 
			return null;
		else 
			return (Element) nl.item(0);
	}

	/**
	 * Runs readability.
	 * 
	 * Workflow:
	 *  1. Prep the document by removing script tags, css, etc.
	 *  2. Build readability"s DOM tree.
	 *  3. Grab the article content from the current dom tree.
	 *  4. Replace the current DOM tree with the new one.
	 *  5. Read peacefully.
	 *
	 **/
	protected void init() {
		if(getBody() != null && bodyCache == null) {
			bodyCache = getBody().cloneNode(true); }

		findArticleDate(); //must be done before prepDocument() 

		findArticleEncoding();

		prepDocument();

		/* Build readability"s DOM tree */
		articleTitle = findArticleTitle();
		articleContent = grabArticle();

		/**
		 * If we attempted to strip unlikely candidates on the first run through, and we ended up with no content,
		 * that may mean we stripped out the actual content so we couldn"t parse it. So re-run init while preserving
		 * unlikely candidates to have a better shot at getting our content out properly.
		 **/
		if(getInnerText(articleContent, false).length() < 250)
		{
			if (flags.contains(Flag.FLAG_STRIP_UNLIKELYS)) {
				flags.remove(Flag.FLAG_STRIP_UNLIKELYS);
				getBody().getParentNode().replaceChild(bodyCache, getBody());
				init();
				return;
			}
			else if (flags.contains(Flag.FLAG_WEIGHT_CLASSES)) {
				flags.remove(Flag.FLAG_WEIGHT_CLASSES);
				getBody().getParentNode().replaceChild(bodyCache, getBody());
				init();
				return; 
			}
			else {
				articleContent = null;
			}
		}

		if (addTitle && articleContent != null) {
			Element titleNode = document.createElement("h1");
			titleNode.setAttribute("id", "title");
			titleNode.appendChild(document.createTextNode(getArticleTitle()));
			articleContent.insertBefore(titleNode, articleContent.getFirstChild());
		}
	}

	/**
	 * Prepare the HTML document for readability to scrape it.
	 * This includes things like stripping javascript, CSS, and handling terrible markup.
	 * 
	 **/
	protected void prepDocument() {
		/**
		 * In some cases a body element can"t be found (if the HTML is totally hosed for example)
		 * so we create a new body node and append it to the document.
		 */
		if(getBody() == null)
		{
			Node body = document.createElement("body");
			document.appendChild(body);
		}

		//frames are not supported in this version!
		//        NodeList frames = document.getElementsByTagName("frame");
		//        if(frames.length > 0)
		//        {
		//            Node bestFrame = null;
		//            int bestFrameSize = 0;
		//            for(int frameIndex = 0; frameIndex < frames.getLength(); frameIndex++)
		//            {
		//                int frameSize = frames.item(frameIndex).offsetWidth + frames[frameIndex].offsetHeight;
		//                var canAccessFrame = false;
		//                try {
		//                    frames[frameIndex].contentWindow.document.body;
		//                    canAccessFrame = true;
		//                }
		//                catch(eFrames) {
		//                    dbg(eFrames);
		//                }
		//                
		//                if(canAccessFrame && frameSize > bestFrameSize)
		//                {
		//                    bestFrame = frames[frameIndex];
		//                    bestFrameSize = frameSize;
		//                }
		//            }
		//
		//            if(bestFrame)
		//            {
		//                var newBody = document.createElement("body");
		//                newBody.innerHTML = bestFrame.contentWindow.document.body.innerHTML;
		//                newBody.style.overflow = "scroll";
		//                document.body = newBody;
		//                
		//                var frameset = document.getElementsByTagName("frameset")[0];
		//                if(frameset) {
		//                    frameset.parentNode.removeChild(frameset); }
		//                    
		//                readability.frameHack = true;
		//            }
		//        }

		/* remove all scripts that are not readability */
		NodeList scripts = document.getElementsByTagName("script");
		for(int i = scripts.getLength()-1; i >= 0; i--)
		{
			scripts.item(i).getParentNode().removeChild(scripts.item(i));          
		}

		/* Remove all style tags in head */
		NodeList styleTags = document.getElementsByTagName("style");
		for (int st=0;st < styleTags.getLength(); st++) {
			styleTags.item(st).getParentNode().removeChild(styleTags.item(st));
		}

		/* Remove all meta tags  */
		NodeList metaTags = document.getElementsByTagName("meta");
		for (int mt=0;mt < metaTags.getLength(); mt++) {
			metaTags.item(mt).getParentNode().removeChild(metaTags.item(mt));
		}

		/* Turn all double br's into p's */
		/* Note, this is pretty costly as far as processing goes. Maybe optimize later. */
		//document.body.innerHTML = document.body.innerHTML.replace(readability.regexps.replaceBrsRe, '</p><p>').replace(readability.regexps.replaceFontsRe, '<$1span>');
		Element body = getBody();
		//		Node rep = stringToNode(nodeToString(body).replaceAll(Regexps.replaceBrsRe, "</P><P>").replaceAll(Regexps.replaceFontsRe, "<$1span>"));
		//		body.getParentNode().replaceChild(rep, body);

		//This is slow!
		Node frag = stringToNode(getInnerHTML(body).replaceAll(Regexps.replaceBrsRe, "</P><P>").replaceAll(Regexps.replaceFontsRe, "<$1span>"));
		removeChildren(body);
		body.appendChild(frag);

		/* Remove all comments */
		removeComments(document);
	}

	protected void removeComments(Node n) {
		if (n.getNodeType() == Node.COMMENT_NODE) {
			n.getParentNode().removeChild(n);
		} else {
			NodeList nl = n.getChildNodes();
			for (int i=0; i<nl.getLength(); i++) 
				removeComments(nl.item(i));
		}
	}

	/**
	 * Prepare the article node for display. Clean out any inline styles,
	 * iframes, forms, strip extraneous <p> tags, etc.
	 *
	 * @param Element
	 **/
	protected void prepArticle(Element articleContent) {
		cleanStyles(articleContent);
		killBreaks(articleContent);

		/* Clean out junk from the article content */
		clean(articleContent, "form");
		clean(articleContent, "object");
		clean(articleContent, "h1");
		/**
		 * If there is only one h2, they are probably using it
		 * as a header and not a subheader, so remove it since we already have a header.
		 ***/
		if(articleContent.getElementsByTagName("h2").getLength() == 1) {
			clean(articleContent, "h2"); 
		}
		clean(articleContent, "iframe");

		cleanHeaders(articleContent);

		/* Do these last as the previous stuff may have removed junk that will affect these */
		cleanConditionally(articleContent, "table");
		cleanConditionally(articleContent, "ul");
		cleanConditionally(articleContent, "div");

		/* Remove extra paragraphs */
		NodeList articleParagraphs = articleContent.getElementsByTagName("p");
		for(int i = articleParagraphs.getLength()-1; i >= 0; i--)
		{
			int imgCount    = ((Element) articleParagraphs.item(i)).getElementsByTagName("img").getLength();
			int embedCount  = ((Element) articleParagraphs.item(i)).getElementsByTagName("embed").getLength();
			int objectCount = ((Element) articleParagraphs.item(i)).getElementsByTagName("object").getLength();

			if(imgCount == 0 && embedCount == 0 && objectCount == 0 && getInnerText((Element) articleParagraphs.item(i), false) == "")
			{
				articleParagraphs.item(i).getParentNode().removeChild(articleParagraphs.item(i));
			}
		}

		//articleContent.innerHTML = articleContent.innerHTML.replace(/<br[^>]*>\s*<p/gi, "<p");
		Node n = stringToNode(getInnerHTML(articleContent).replaceAll("(?i)<br[^>]*>\\s*<p", "<P"));
		removeChildren(articleContent);
		articleContent.appendChild(n);

		//now remove empty p's and tidy up
		NodeList nl = articleContent.getElementsByTagName("p");
		for (int i=nl.getLength()-1; i>=0; i--) {
			if (nl.item(i).getTextContent().trim().length() == 0) 
			{
				nl.item(i).getParentNode().removeChild(nl.item(i));
			} else if (nl.item(i).getChildNodes().getLength() == 1 && nl.item(i).getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
				nl.item(i).setTextContent("\n" + nl.item(i).getTextContent().trim() + "\n");
			}
			else if (((Element) nl.item(i)).getAttribute("class").equals("readability-styled")) 
			{
				nl.item(i).getParentNode().replaceChild(document.createTextNode(nl.item(i).getTextContent()), nl.item(i));
			}
		}

	}

	protected void removeChildren(Node n) {
		NodeList nl = n.getChildNodes();
		int nn = nl.getLength();
		for (int i=0; i<nn; i++)
			n.removeChild(nl.item(0));
	}

	/**
	 * Initialize a node with the readability object. Also checks the
	 * className/id for special names to add to its score.
	 *
	 * @param Element
	 **/
	protected void initializeNode(Element node) {
		float contentScore = 0;         

		if (node.getTagName() == "DIV") {
			contentScore += 5;
		} else if (node.getTagName() == "PRE" || node.getTagName() == "TD" || node.getTagName() == "BLOCKQUOTE") {
			contentScore += 3;
		} else if (node.getTagName() == "ADDRESS" || node.getTagName() == "OL" || node.getTagName() == "UL"
			|| node.getTagName() == "DL" || node.getTagName() == "DD" || node.getTagName() == "DT"
				|| node.getTagName() == "LI" || node.getTagName() == "FORM") {
			contentScore -= 3;
		} else if (node.getTagName() == "H1" || node.getTagName() == "H2" || node.getTagName() == "H3"
			|| node.getTagName() == "H4" || node.getTagName() == "H5" || node.getTagName() == "H6"
				|| node.getTagName() == "TH") {
			contentScore -= 5;
		}

		contentScore += getClassWeight(node);
		node.setUserData("readability", contentScore, null);
	}

	/**
	 * Get an elements class/id weight. Uses regular expressions to tell if this 
	 * element looks good or bad.
	 *
	 * @param Element
	 * @return number (Integer)
	 **/
	protected int getClassWeight(Element e) {
		if (!flags.contains(Flag.FLAG_WEIGHT_CLASSES)) {
			return 0;
		}

		int weight = 0;

		/* Look for a special classname */
		if (e.getAttribute("class") != "")
		{
			if(search(e.getAttribute("class"), Regexps.negativeRe) != -1) {
				weight -= 25;
			}

			if(search(e.getAttribute("class"), Regexps.positiveRe) != -1) {
				weight += 25;
			}
		}

		/* Look for a special ID */
		if (e.getAttribute("id") != "")
		{
			if(search(e.getAttribute("id"), Regexps.negativeRe) != -1) {
				weight -= 25;
			}

			if(search(e.getAttribute("id"), Regexps.positiveRe) != -1) {
				weight += 25;
			}
		}

		return weight;
	}

	protected void cleanStyles() {
		cleanStyles((Element) document);
	}

	/**
	 * Remove the style attribute on every e and under.
	 * TODO: Test if getElementsByTagName(*) is faster.
	 *
	 * @param Element
	 **/
	protected void cleanStyles(Element e) {
		if(e == null) return; 
		Node cur = e.getFirstChild();

		// Remove any root styles, if we"re able.
		if (!e.getAttribute("class").equals("readability-styled"))
			e.removeAttribute("style");

		// Go until there are no more child nodes
		while ( cur != null ) {
			if ( cur.getNodeType() == Element.ELEMENT_NODE) {
				// Remove style attribute(s) :
				if(!((Element) cur).getAttribute("class").equals("readability-styled")) {
					((Element) cur).removeAttribute("style");
				}
				cleanStyles( (Element) cur );
			}
			cur = cur.getNextSibling();
		}  
	}

	/**
	 * Remove extraneous break tags from a node.
	 *
	 * @param Element
	 **/
	protected void killBreaks(Element e) {
		//e.innerHTML = e.innerHTML.replace(readability.regexps.killBreaksRe,"<br />");       

		Node n = stringToNode(getInnerHTML(e).replaceAll(Regexps.killBreaksRe,"<BR />"));
		removeChildren(e);
		e.appendChild(n);
	}

	/**
	 * Clean a node of all elements of type "tag".
	 * (Unless it"s a youtube/vimeo video. People love movies.)
	 *
	 * @param Element
	 * @param string tag to clean
	 **/
	protected void clean(Element e, String tag) {
		NodeList targetList = e.getElementsByTagName( tag );
		boolean isEmbed    = (tag.equals("object") || tag.equals("embed"));

		for (int y=targetList.getLength()-1; y >= 0; y--) {
			/* Allow youtube and vimeo videos through as people usually want to see those. */
			if(isEmbed) {
				String attributeValues = "";
				for (int i=0, il=targetList.item(y).getAttributes().getLength(); i < il; i++) {
					attributeValues += targetList.item(y).getAttributes().item(i).getNodeValue() + "|";
				}

				/* First, check the elements attributes to see if any of them contain youtube or vimeo */
				if (search(attributeValues, Regexps.videoRe) != -1) {
					continue;
				}

				/* Then check the elements inside this element for the same. */
				if (search(getInnerHTML(targetList.item(y)), Regexps.videoRe) != -1) {
					continue;
				}
			}

			targetList.item(y).getParentNode().removeChild(targetList.item(y));
		}
	}

	/**
	 * Clean out spurious headers from an Element. Checks things like classnames and link density.
	 *
	 * @param Element
	 **/
	protected void cleanHeaders(Element e) {
		for (int headerIndex = 1; headerIndex < 7; headerIndex++) {
			NodeList headers = e.getElementsByTagName("h" + headerIndex);
			for (int i=headers.getLength()-1; i >=0; i--) {
				if (getClassWeight((Element) headers.item(i)) < 0 || getLinkDensity((Element) headers.item(i)) > LINK_DENSITY_THRESHOLD) {
					headers.item(i).getParentNode().removeChild(headers.item(i));
				}
			}
		}
	}

	/**
	 * Get the density of links as a percentage of the content
	 * This is the amount of text that is inside a link divided by the total text in the node.
	 * 
	 * @param Element
	 * @return number (float)
	 **/
	protected float getLinkDensity(Element e) {
		NodeList links = e.getElementsByTagName("a");
		int textLength = getInnerText(e).length();
		int linkLength = 0;

		for(int i=0, il=links.getLength(); i<il;i++)
		{
			linkLength += getInnerText((Element) links.item(i)).length();
		}

		if (linkLength == 0) return 0;

		return (float)linkLength / (float)textLength;
	}

	/**
	 * Clean an element of all tags of type "tag" if they look fishy.
	 * "Fishy" is an algorithm based on content length, classnames, link density, number of images & embeds, etc.
	 **/
	protected void cleanConditionally(Element e, String tag) {
		NodeList tagsList = e.getElementsByTagName(tag);
		int curTagsLength = tagsList.getLength();

		/**
		 * Gather counts for other typical elements embedded within.
		 * Traverse backwards so we can remove nodes at the same time without effecting the traversal.
		 *
		 * Todo: Consider taking into account original contentScore here.
		 **/
		for (int i=curTagsLength-1; i >= 0; i--) {
			int weight = getClassWeight((Element) tagsList.item(i));
			float contentScore = (tagsList.item(i).getUserData("readability") != null) ? (Float)(tagsList.item(i).getUserData("readability")) : 0;

			dbg("Cleaning Conditionally " + tagsList.item(i) + " (" + ((Element) tagsList.item(i)).getAttribute("class")+ ":" + ((Element)tagsList.item(i)).getAttribute("id") + ")" + ((tagsList.item(i).getUserData("readability") != null) ? (" with score " + tagsList.item(i).getUserData("readability")) : ""));

			if(weight+contentScore < 0)
			{
				dbg("Removing " + tagsList.item(i) + " (" + ((Element) tagsList.item(i)).getAttribute("class")+ ":" + ((Element)tagsList.item(i)).getAttribute("id") + ")");
				tagsList.item(i).getParentNode().removeChild(tagsList.item(i));
			}
			else if ( getCharCount((Element) tagsList.item(i), ",") < 10) {
				/**
				 * If there are not very many commas, and the number of
				 * non-paragraph elements is more than paragraphs or other ominous signs, remove the element.
				 **/
				int p      = ((Element) tagsList.item(i)).getElementsByTagName("p").getLength();
				int img    = ((Element) tagsList.item(i)).getElementsByTagName("img").getLength();
				int li     = ((Element) tagsList.item(i)).getElementsByTagName("li").getLength()-100;
				int input  = ((Element) tagsList.item(i)).getElementsByTagName("input").getLength();

				int embedCount = 0;
				NodeList embeds = ((Element) tagsList.item(i)).getElementsByTagName("embed");
				for(int ei=0,il=embeds.getLength(); ei < il; ei++) {
					if (search(((Element)embeds.item(ei)).getAttribute("src"), Regexps.videoRe) == -1) {
						embedCount++; 
					}
				}

				float linkDensity = getLinkDensity((Element) tagsList.item(i));
				int contentLength = getInnerText((Element) tagsList.item(i)).length();
				boolean toRemove = false;

				if ( img > p ) {
					toRemove = true;
				} else if(li > p && tag != "ul" && tag != "ol") {
					toRemove = true;
				} else if( input > Math.floor(p/3) ) {
					toRemove = true; 
				} else if(contentLength < 25 && (img == 0 || img > 2) ) {
					toRemove = true;
				} else if(weight < 25 && linkDensity > 0.2) {
					toRemove = true;
				} else if(weight >= 25 && linkDensity > 0.5) {
					toRemove = true;
				} else if((embedCount == 1 && contentLength < 75) || embedCount > 1) {
					toRemove = true;
				}

				if ( img == 1 &&  p == 0 && contentLength == 0 ) {
					Element theImg = (Element) ((Element) tagsList.item(i)).getElementsByTagName("img").item(0);  

					String w = "";
					if (theImg.getAttribute("width") != null) w = theImg.getAttribute("width");

					String h = "";
					if (theImg.getAttribute("height") != null) h = theImg.getAttribute("height");

					if (!(w.equals("0") || h.equals("0")))
						toRemove = false; //special case - it's just an inline image
				}

				if(toRemove) {
					dbg("Removing " + tagsList.item(i) + " (" + ((Element) tagsList.item(i)).getAttribute("class")+ ":" + ((Element)tagsList.item(i)).getAttribute("id") + ")");
					tagsList.item(i).getParentNode().removeChild(tagsList.item(i));
				}
			}
		}
	}

	/**
	 * Get the number of times a string s appears in the node e.
	 *
	 * @param Element
	 * @param string - what to split on. Default is ","
	 * @return number (integer)
	 **/
	protected int getCharCount(Element e, String s) {
		return getInnerText(e).split(s).length-1;
	}

	protected int getCharCount(Element e) {
		return getCharCount(e, ",");
	}

	/**
	 * @return The article title 
	 */
	public String getArticleTitle() {
		return articleTitle;
	}

	/**
	 * @return The content type of the article
	 */
	public String getArticleContentType() {
		return article_contentType;
	}

	/***
	 * grabArticle - Using a variety of metrics (content score, classname, element types), find the content that is
	 *               most likely to be the stuff a user wants to read. Then return it wrapped up in a div.
	 *
	 * @return Element
	 **/
	protected Element grabArticle() {
		boolean stripUnlikelyCandidates = flags.contains(Flag.FLAG_STRIP_UNLIKELYS);

		/**
		 * First, node prepping. Trash nodes that look cruddy (like ones with the class name "comment", etc), and turn divs
		 * into P tags where they have been used inappropriately (as in, where they contain no other block level elements.)
		 *
		 * Note: Assignment from index for performance. See http://www.peachpit.com/articles/article.aspx?p=31567&seqNum=5
		 * Todo: Shouldn't this be a reverse traversal?
		 **/
		Element node = null;
		List<Element> nodesToScore = new ArrayList<Element>();
		for(int nodeIndex = 0; (node = (Element)document.getElementsByTagName("*").item(nodeIndex)) != null; nodeIndex++)
		{
			/* Remove unlikely candidates */
			if (stripUnlikelyCandidates) {
				String unlikelyMatchString = node.getAttribute("class") + node.getAttribute("id");
				if (search(unlikelyMatchString, Regexps.unlikelyCandidatesRe) != -1 &&
						search(unlikelyMatchString, Regexps.okMaybeItsACandidateRe) == -1 &&
						!node.getTagName().equals("BODY"))
				{
					dbg("Removing unlikely candidate - " + unlikelyMatchString);
					node.getParentNode().removeChild(node);
					nodeIndex--;
					continue;
				}               
			}

			if (node.getTagName().equals("P") || node.getTagName().equals("TD")) {
				nodesToScore.add(node);
			}

			/* Turn all divs that don't have children block level elements into p's */
			if (node.getTagName().equals("DIV")) {

				if (search(getInnerHTML(node), Regexps.divToPElementsRe) == -1) {
					dbg("Altering div to p");
					Element newNode = document.createElement("P");

					//newNode.innerHTML = node.innerHTML;
					NodeList nl = node.getChildNodes();
					for (int i=0; i<nl.getLength(); i++) newNode.appendChild(nl.item(i));

					node.getParentNode().replaceChild(newNode, node);
					nodeIndex--;
				}
				else
				{
					/* EXPERIMENTAL */
					for(int i = 0, il = node.getChildNodes().getLength(); i < il; i++) {
						Node childNode = node.getChildNodes().item(i);
						if(childNode.getNodeType() == Element.TEXT_NODE) {
							dbg("replacing text node with a p tag with the same content.");
							Element p = document.createElement("p");
							//p.innerHTML = childNode.nodeValue;
							p.setNodeValue(childNode.getNodeValue());
							p.setTextContent(childNode.getTextContent());
							//p.style.display = "inline";
							p.setAttribute("class", "readability-styled");
							childNode.getParentNode().replaceChild(p, childNode);
						}
					}
				}
			} 
		}

		/**
		 * Loop through all paragraphs, and assign a score to them based on how content-y they look.
		 * Then add their score to their parent node.
		 *
		 * A score is determined by things like number of commas, class names, etc. Maybe eventually link density.
		 **/
		List<Element> candidates = new ArrayList<Element>();
		for (int pt=0; pt < nodesToScore.size(); pt++) {
			Element parentNode      = (Element) nodesToScore.get(pt).getParentNode();
			Element grandParentNode = (Element) parentNode.getParentNode();
			String innerText        = getInnerText(nodesToScore.get(pt));

			/* If this paragraph is less than 25 characters, don't even count it. */
			if(innerText.length() < 25) {
				continue; 
			}

			/* Initialize readability data for the parent. */
			if(parentNode.getUserData("readability") == null)
			{
				initializeNode(parentNode);
				candidates.add(parentNode);
			}

			/* Initialize readability data for the grandparent. */
			if(grandParentNode.getUserData("readability") == null)
			{
				initializeNode(grandParentNode);
				candidates.add(grandParentNode);
			}

			float contentScore = 0;

			/* Add a point for the paragraph itself as a base. */
			contentScore++;

			/* Add points for any commas within this paragraph */
			contentScore += innerText.split(",").length;

			/* For every 100 characters in this paragraph, add another point. Up to 3 points. */
			contentScore += Math.min(Math.floor((float)innerText.length() / 100F), 3F);

			/* Add the score to the parent. The grandparent gets half. */
			parentNode.setUserData("readability", ((Float)(parentNode.getUserData("readability")) + contentScore), null);
			grandParentNode.setUserData("readability", ((Float)(grandParentNode.getUserData("readability"))) + (contentScore/2F), null);
		}

		/**
		 * After we've calculated scores, loop through all of the possible candidate nodes we found
		 * and find the one with the highest score.
		 **/
		Element topCandidate = null;
		for(int c=0, cl=candidates.size(); c < cl; c++)
		{
			/**
			 * Scale the final candidates score based on link density. Good content should have a
			 * relatively small link density (5% or less) and be mostly unaffected by this operation.
			 **/

			candidates.get(c).setUserData("readability", (Float)(candidates.get(c).getUserData("readability")) * (1F-getLinkDensity(candidates.get(c))), null);

			dbg("Candidate: " + candidates.get(c) + " (" + candidates.get(c).getAttribute("class")+ ":" + candidates.get(c).getAttribute("id")+ ") with score " + candidates.get(c).getUserData("readability"));

			if(topCandidate == null || (Float)(candidates.get(c).getUserData("readability")) > ((Float)topCandidate.getUserData("readability"))) {
				topCandidate = candidates.get(c);
			}
		}

		if (topCandidate != null)
			dbg("==> TOP Candidate: " + topCandidate + " (" + topCandidate.getAttribute("class")+ ":" + topCandidate.getAttribute("id")+ ") with score " + topCandidate.getUserData("readability"));
		
		/**
		 * If we still have no top candidate, just use the body as a last resort.
		 * We also have to copy the body node so it is something we can modify.
		 **/
		if (topCandidate == null || topCandidate.getTagName().equals("BODY"))
		{
			topCandidate = document.createElement("DIV");

			//topCandidate.innerHTML = document.body.innerHTML;
			NodeList nl = getBody().getChildNodes();
			for (int i=0; i<nl.getLength(); i++) topCandidate.appendChild(nl.item(i));
			//document.body.innerHTML = ""; //should be covered by above


			getBody().appendChild(topCandidate);
			initializeNode(topCandidate);
		}

		/**
		 * Now that we have the top candidate, look through its siblings for content that might also be related.
		 * Things like preambles, content split by ads that we removed, etc.
		 **/
		Element articleContent = document.createElement("DIV");
		articleContent.setAttribute("id", "readability-content");
		float siblingScoreThreshold = (float) Math.max(10F, (Float)topCandidate.getUserData("readability") * 0.2F);
		NodeList siblingNodes = topCandidate.getParentNode().getChildNodes();
		
		for(int s=0, sl=siblingNodes.getLength(); s < sl; s++)
		{
			Node siblingNode = siblingNodes.item(s);
			boolean append      = false;

			if (siblingNode instanceof Element)
				dbg("Looking at sibling node: " + siblingNode + " (" + ((Element) siblingNode).getAttribute("class") + ":" + ((Element) siblingNode).getAttribute("id") + ")" + ((siblingNode.getUserData("readability") != null) ? (" with score " + siblingNode.getUserData("readability")) : ""));
			dbg("Sibling has score " + (siblingNode.getUserData("readability") != null ? siblingNode.getUserData("readability") : "Unknown"));

			if(siblingNode == topCandidate)
			{
				append = true;
			}

			float contentBonus = 0;
			/* Give a bonus if sibling nodes and top candidates have the example same classname */
			if(siblingNode instanceof Element && ((Element) siblingNode).getAttribute("class").equals(topCandidate.getAttribute("class")) && !topCandidate.getAttribute("class").equals("")) {
				contentBonus += (Float)topCandidate.getUserData("readability") * 0.2F;
			}

			if(siblingNode.getUserData("readability") != null && ((Float)siblingNode.getUserData("readability")+contentBonus) >= siblingScoreThreshold)
			{
				append = true;
			}

			if(siblingNode.getNodeName().equals("P")) {
				float linkDensity = getLinkDensity((Element) siblingNode);
				String nodeContent = getInnerText((Element) siblingNode);
				int nodeLength  = nodeContent.length();

				if(nodeLength > 80 && linkDensity < 0.25)
				{
					append = true;
				}
				else if(nodeLength < 80 && linkDensity == 0 && search(nodeContent, "\\.( |$)") != -1)
				{
					append = true;
				}
			}

			if(append)
			{
				dbg("Appending node: " + siblingNode);

				Node nodeToAppend = null;
				if(!siblingNode.getNodeName().equals("DIV") && !siblingNode.getNodeName().equals("P")) {
					/* We have a node that isn't a common block level element, like a form or td tag. Turn it into a div so it doesn't get filtered out later by accident. */

					dbg("Altering siblingNode of " + siblingNode.getNodeName() + " to div.");
					nodeToAppend = document.createElement("div");
					if (siblingNode instanceof Element)
						((Element) nodeToAppend).setAttribute("id", ((Element) siblingNode).getAttribute("id"));

					//nodeToAppend.innerHTML = siblingNode.innerHTML;
					NodeList nl = siblingNode.getChildNodes();
					for (int i=0; i<nl.getLength(); i++) nodeToAppend.appendChild(nl.item(i));
				} else {
					nodeToAppend = siblingNode;
					s--;
					sl--;
				}

				/* To ensure a node does not interfere with readability styles, remove its classnames */
				if (nodeToAppend instanceof Element)
					((Element) nodeToAppend).setAttribute("class", "");

				/* Append sibling and subtract from our list because it removes the node when you append to another node */
				articleContent.appendChild(nodeToAppend);
			}
		}

		/**
		 * So we have all of the content that we need. Now we clean it up for presentation.
		 **/
		prepArticle(articleContent);

		return articleContent;
	}

	protected String getInnerHTML(Node n) {
		if (n.getNodeType() == Node.TEXT_NODE) return n.getTextContent();

		String result = "";		
		NodeList nl = n.getChildNodes();
		for (int i=0; i<nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.TEXT_NODE)
				result += nl.item(i).getTextContent();
			else if (nl.item(i).getNodeType() == Node.COMMENT_NODE)
				result += "<!-- " + nl.item(i).getTextContent() + " -->";
			else
				result += nodeToString(nl.item(i));				
		}

		return result;
	}

	protected String nodeToString(Node n) {
		return nodeToString(n, false);
	}

	protected String nodeToString(Node n, boolean pretty) {
		try {
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
			LSSerializer writer = impl.createLSSerializer();

			writer.getDomConfig().setParameter("xml-declaration", false);
			if (pretty) {
				writer.getDomConfig().setParameter("format-pretty-print", true);
			}

			return writer.writeToString(n);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Node stringToNode(String str) {
		try {
			DOMFragmentParser parser = new DOMFragmentParser();
			DocumentFragment fragment = document.createDocumentFragment();
			parser.parse(new InputSource(new StringReader(str)), fragment);
			return fragment;

			//try and return the element itself if possible...
			//			NodeList nl = fragment.getChildNodes();
			//			for (int i=0; i<nl.getLength(); i++) if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) return nl.item(i);
			//			return fragment;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the inner text of a node - cross browser compatibly.
	 * This also strips out any excess whitespace to be found.
	 *
	 * @param Element
	 * @return string
	 **/
	protected String getInnerText(Element e, boolean normalizeSpaces) {
		String textContent    = "";

		textContent = e.getTextContent().replaceAll( Regexps.trimRe, "" );

		if(normalizeSpaces) {
			return textContent.replaceAll( Regexps.normalizeRe, " "); 
		} else {
			return textContent; 
		}
	}

	protected String getInnerTextSep(Node e) {
		if (e.hasChildNodes()) {
			String s = "";
			NodeList nl = e.getChildNodes();
			for (int i=0; i<nl.getLength(); i++) {
				if (!nl.item(i).getNodeName().equalsIgnoreCase("script"))
					s += getInnerTextSep(nl.item(i));
			}
			return s;
		} else {
			return e.getTextContent() + " ";
		}
	}

	protected String getInnerText(Element e) {
		return getInnerText(e, true);
	}

	/**
	 * @return The article HTML content as a {@link String}.
	 */
	public String getArticleHTML() {
		if (articleContent == null) return "";
		return nodeToString(articleContent, true);
	}

	/**
	 * @return The articles HTML dom node. 
	 */
	public Node getArticleHTML_DOM() {
		return articleContent;
	}

	protected String getArticleDateString() {
		return article_date_string;
	}

	/**
	 * @return The article date.
	 */
	public Date getArticleDate() {
		return article_date;
	}

	/**
	 * @return The text of the article.
	 */
	public String getArticleText() {
		if (articleContent == null) return "Unable to find article content";
		//return getInnerText(articleContent, false);
		return articleContent.getTextContent().trim().replaceAll("[\r|\n|\r\n]{2,}", "\n\n").replaceAll(" {2,}", " ");
	}

	/**
	 * @return Any links in the article.
	 */
	public List<Anchor> getArticleLinks() {		
		List<Anchor> anchors = new ArrayList<Anchor>();
		if (articleContent == null) return anchors;

		NodeList nl = articleContent.getElementsByTagName("a");
		for (int i=0; i<nl.getLength(); i++) {
			Element a = (Element) nl.item(i);

			Anchor anchor = new Anchor(getInnerText(a), a.getAttribute("href"));
			anchors.add(anchor);
		}
		return anchors;
	}

	/**
	 * @return Any links in the document.
	 */
	public List<Anchor> getAllLinks() {		
		List<Anchor> anchors = new ArrayList<Anchor>();

		NodeList nl = document.getElementsByTagName("a");
		for (int i=0; i<nl.getLength(); i++) {
			Element a = (Element) nl.item(i);
			Anchor anchor = new Anchor(getInnerText(a), a.getAttribute("href"));
			anchors.add(anchor);
		}
		return anchors;
	}

	/**
	 * @return Any images in the article.
	 */
	public List<String> getArticleImages() {
		List<String> images = new ArrayList<String>();
		if (articleContent == null) return images;

		NodeList nl = articleContent.getElementsByTagName("img");
		for (int i=0; i<nl.getLength(); i++) {
			Element img = (Element) nl.item(i);
			images.add(img.getAttribute("src"));
		}
		return images;
	}

	/**
	 * @return Any subheadings in the article.
	 */
	public List<String> getArticleSubheadings() {
		List<String> subtitles = new ArrayList<String>();
		if (articleContent == null) return subtitles;

		for (int j=1; j<=6; j++) {
			NodeList nl = articleContent.getElementsByTagName("h"+j);
			if (nl.getLength() > 0) {
				for (int i=0; i<nl.getLength(); i++) {
					subtitles.add(nl.item(i).getTextContent());
				}
				break;
			}
		}

		if (subtitles.size() == 0) {
			//try looking for other likely-looking elements

			NodeList nl = articleContent.getElementsByTagName("*");
			for (int i=0; i<nl.getLength(); i++) {
				if (nl.item(i) instanceof Element &&
						((Element) nl.item(i)).getAttribute("class") != null && 
						search(((Element) nl.item(i)).getAttribute("class"), Regexps.likelySubheadCandidateRe) != -1)
					subtitles.add(nl.item(i).getTextContent());
			}
		}

		return subtitles;
	}

	protected List<Node> findChildNodesWithName(Node parent, String name) {
		NodeList children = parent.getChildNodes();
		List<Node> results = new ArrayList<Node>();

		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child == null)
				continue;

			String nodeName = child.getNodeName();
			if (nodeName == null)
				continue;

			if (nodeName.equals(name)) {
				results.add(child);
			}
		}
		return results;
	}

	protected int findChildNodeIndex( Node parent, Node childToFind )
	{
		for( int index = 0; index < parent.getChildNodes().getLength(); index++ )
			if( parent.getChildNodes().item( index ) == childToFind )
				return index;
		return -1;
	}
	
	protected void getArticleTextMapping(TreeWalker walker, List<MappingNode> map) throws DOMException {
		Node parend = walker.getCurrentNode();

		if( parend.getNodeType() == Node.TEXT_NODE && parend.getParentNode().getAttributes().getNamedItem("id") != null )
		{
			if( parend.getTextContent().trim().length() > 0 )
			{
				int index = findChildNodeIndex( parend.getParentNode(), parend );
				if( index != -1 )
				{
					// square brackets are not valid XML/HTML identifier characters, so we can use them here
					map.add( new MappingNode( 
							parend.getParentNode().getAttributes().getNamedItem("id").getNodeValue() + "["+index+"]", 
							parend.getNodeValue() ) );
				
//					System.out.println( "ELEMENT '"+parend.getParentNode().getAttributes().getNamedItem("id").getNodeValue() + "["+index+"]"+"'");
//					System.out.println( "VALUE:  '"+parend.getNodeValue()+"'" );
				}
			}
		}

		// traverse children:
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			getArticleTextMapping(walker, map);
		}

		// return position to the current (level up):
		walker.setCurrentNode(parend);
	}

	protected class MappingNode {
		String id;
		String text;
		
		public MappingNode(String id, String text) { this.id = id; this.text = text; }
		public String getId() { return id; }
		public String getText() { return text; }
		@Override public String toString() { return "MappingNode(" + id + " -> " + text + ")"; }
	}
	
	/**
	 * Get the mapping between bits of text in the dom & their xpaths
	 * 
	 * @return mapping from xpath to text
	 */
	public List<MappingNode> getArticleTextMapping() {
		if (articleContent == null) return null;

		List<MappingNode> map = new ArrayList<MappingNode>();

		TreeWalker walker = ((DocumentTraversal) document).createTreeWalker(articleContent, NodeFilter.SHOW_TEXT | NodeFilter.SHOW_ELEMENT, null, true);

		getArticleTextMapping(walker, map);

		return map;
	}

	/**
	 * Convenience method to build a {@link Readability} instance from an html string.
	 * @param html The html string
	 * @return new {@link Readability} instance.
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Readability getReadability(String html) throws SAXException, IOException {
		return getReadability( html, false );
	}

	/**
	 * Convenience method to build a {@link Readability} instance from an html string.
	 * @param html The html string
	 * @param addTitle Should the title be added to the generated article?
	 * @return new {@link Readability} instance.
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Readability getReadability(String html, boolean addTitle) throws SAXException, IOException {
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new StringReader(html)));

		return new Readability(parser.getDocument(), false, addTitle );
	}
	
	/**
	 * Testing
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String[] argv) throws Exception {
//		URL input = new URL("file:///home/dd/Programming/Readability4J/t.html");
								URL input = new URL("http://news.bbc.co.uk/1/hi/politics/10362367.stm");
		//						URL input = new URL("http://euobserver.com/9/30465");
		//						URL input = new URL("http://euobserver.com/?aid=23383");
		//				URL input = new URL("http://abandoninplace.squarespace.com/blog/2010/6/8/wwdc-monday.html");
		//				URL input = new URL("file:///Users/jsh2/Desktop/test.html");
		//				URL input = new URL("http://mobile.engadget.com/2010/06/17/htc-aria-review/");
		//				URL input = new URL("http://thedailywtf.com/Articles/Benched.aspx");
		//				URL input = new URL("http://www.dailymail.co.uk/news/article-1287625/Woman-sparked-150-000-manhunt-slashing-face-crying-rape-faces-jail.html");
		//URL input = new URL("http://mrpaparazzi.com/post/11619/Lindsay-Lohan-Tests-Negative-For-Alcohol-Goes-Clubbing-To-Celebrate.aspx");
		//URL input = new URL("http://www.bbc.co.uk/news/world-middle-east-11415719");
		//URL input = new URL("http://www.thebigproject.co.uk/news/");
//		URL input = new URL("http://blogs.euobserver.com/popescu/2009/12/15/on-euro-optimism-pessimism-and-failures/#more-958");
		//URL input = new URL("http://www.cnn.com/2010/WORLD/meast/09/27/west.bank.settlement.construction/index.html?hpt=T2");

		//URL input = new URL("http://www.huffingtonpost.com/steven-cohen/its-time-to-enact-congest_b_740315.html");
		//				URL input = new URL("http://uk.mac.ign.com/articles/573/573319p1.html");
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(input.openStream()));

		Readability r = new Readability(parser.getDocument(), false, true);

		//System.out.println(r.getArticleTitle());
//		System.out.println(r.getArticleHTML());
		//System.out.println(r.getAllLinks());
		System.out.println(r.getArticleText());

		System.out.println();
		System.out.println("***");
		System.out.println();
		
		for (MappingNode s : r.getArticleTextMapping())
			System.out.println(s);

		//PrintStream out = new PrintStream("news-sites");
		//for (Anchor anchor : r.getAllLinks()) {
		//	out.println(anchor.getHref() + "\t"  + anchor.getText());
		//}
		//out.close();

		//System.out.println(r.getArticleImages());
		//		System.out.println(r.getArticleSubheadings());
		//		System.out.println(r.getArticleHTML());
		//		System.out.println(r.getArticleHTML_DOM());

		//System.out.println(r.getArticleDateString());
		//System.out.println(r.getArticleDate());
	}	
}
