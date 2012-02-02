package org.openimaj.text.nlp;

import gov.sandia.cognition.text.token.AbstractTokenizer;
import gov.sandia.cognition.text.token.DefaultToken;
import gov.sandia.cognition.text.token.Token;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;



public class TweetTokeniser implements Iterable<Token>{
	
	private static final String spaceRegex = "\\s+";
	private static final String NotEdgePunct = "[a-zA-Z0-9]";
	private static final String EdgePunct = new String("[' \" Ò Ó Ô Õ < > Ç È { } ( ) \\[ \\]	]").replace(" ","");
	private static final String  EdgePunctLeft	= String.format("(\\s|^)(%s+)(%s)",EdgePunct, NotEdgePunct);
	private static final String  EdgePunctRight = String.format("(%s)(%s+)(\\s|$)",NotEdgePunct, EdgePunct);
	private static final Pattern  EdgePunctLeft_RE = Pattern.compile(EdgePunctLeft);
	private static final Pattern EdgePunctRight_RE= Pattern.compile(EdgePunctRight);
	private String text;
	private ArrayList<Token> tokenize;
	
	
	public static String regex_or(String ... items )
	{
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}
	public String pos_lookahead(String r){
		return "(?=" + r + ')';
	}
		
	public String neg_lookahead(String r) {
		return "(?!" + r + ')';
	}
	public String optional(String r){
		return String.format("(%s)?",r);
	}
	
	String[] EmoticonsDNArr = new String[] {
			":\\)",":\\(",":-\\)",">:\\]",":o\\)",":3",":c\\)",":>","=\\]","8\\)","=\\)",
			":}",":^\\)",">:D\\)",":-D",":D","8-D","8D","x-D","xD","X-D","XD","=-D","=D",
			"=-3","=3\\)","8-\\)",":-\\)\\)",":\\)\\)",">-\\[",":-\\(",":\\(",":-c",":c",":-<",":<",
			":-\\[",":\\[",":{",">.>","<.<",">.<",":-\\|\\|","D:<","D:","D8","D;","D=","DX",
			"v.v","D-\\':",">;\\]",";-\\)",";\\)","\\*-\\)","\\*\\)",";-\\]",";\\]",";D",";^\\)",">:P",
			":-P",":P","X-P","x-p","xp","XP",":-p",":p","=p",":-b",":b",">:o",">:O",":-O",
			":O",":0","o_O","o_0","o.O","8-0",">:\\",">:/",":-/",":-.",":/",":\\",
			"=/","=\\",":S",":\\|",":-\\|",">:X",":-X",":X",":-#",":#",":$","O:-\\)","0:-3",
			"0:3","O:-\\)","O:\\)","0;^\\)",">:\\)",">;\\)",">:-\\)",":\\'-\\(",":\\'\\(",":\\'-\\)",":\\'\\)",
			";\\)\\)",";;\\)","<3","8-}",">:D<","=\\)\\)","=\\(\\(","x\\(","X\\(",":-\\*",":\\*",":\\\">","~X\\(",":-?;"
		};
	String EmoticonsDN = regex_or(EmoticonsDNArr);
	String PunctChars = "['Ò\".?!,:;]";
	String Punct = String.format("%s+", PunctChars);
	String Entity = "&(amp|lt|gt|quot);";
//
//	# one-liner URL recognition:
//	#Url = r'''https?://\S+'''
	//
//	# more complex version:
	String UrlStart1 = regex_or("https?://", "www\\.");
	String CommonTLDs = regex_or("com","co\\.uk","org","net","info","ca");
	String UrlStart2 = "[a-z0-9\\.-]+?" + "\\." + CommonTLDs + pos_lookahead("[/ \\W\b]");
	String UrlBody = "[^ \t\r\n<>]*?";// * not + for case of:	"go to bla.com." -- don't want period
	String UrlExtraCrapBeforeEnd = String.format("%s+?",regex_or(PunctChars, Entity));
	String UrlEnd = regex_or( "\\.\\.+", "[<>]", "\\s", "$");
	String Url = "\b" + 
			regex_or(UrlStart1, UrlStart2) + 
			UrlBody + 
			pos_lookahead( optional(UrlExtraCrapBeforeEnd) + UrlEnd);
	
	Pattern Url_RE = Pattern.compile(String.format("(%s)" , Url), Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
//
	String Timelike = "\\d+:\\d+h{0,1}"; // removes the h trailing the hour like in 18:00h
	String Number = "^\\d+";
	String NumNum = "\\d+\\.\\d+";
	String NumberWithCommas = "(\\d+,)+?\\d{3}" + pos_lookahead(regex_or("[^,]","$"));
//
	String[] Abbrevs1 = new String[]{"am","pm","us","usa","ie","eg"};
	public String[] regexify_abbrev(String[] a){
		String[] out = new String[a.length];
		for (int i = 0 ; i < a.length; i++) {
			String s = a[i];
			String dotted = "";
			for (int j = 0; j < s.length(); j++) {
				dotted += s.substring(j,j+1).toUpperCase() + "\\.";
			}
			out[i] = dotted;
		}
		return out;
	}
	String[] Abbrevs = regexify_abbrev(Abbrevs1);
//
	String BoundaryNotDot = regex_or("\\s", "[Ò\"?!,:;]", Entity);
	String aa1 = "([A-Za-z]\\.){2,}" + pos_lookahead(BoundaryNotDot);
	String aa2 = "([A-Za-z]\\.){1,}[A-Za-z]" + pos_lookahead(BoundaryNotDot);
	String ArbitraryAbbrev = regex_or(aa1,aa2);
//
	String Separators = regex_or("--+", "\u2015");
	String Decorations = new String(" [\u266b]+ ").replace(" ","");
//
	String EmbeddedApostrophe = "\\S+'\\S+";
	String [] ProtectThese = new String[]{
			EmoticonsDN,
			Url,
			Entity,
			Timelike,
			NumNum,
			NumberWithCommas,
			Punct,
			ArbitraryAbbrev,
			Separators,
			Decorations,
			EmbeddedApostrophe,
	};
	Pattern Protect_RE = Pattern.compile(regex_or(ProtectThese));
	
	public TweetTokeniser(String s) throws UnsupportedEncodingException, TweetTokeniserException{
		this.text = new String(s);
		System.out.println("TWEET:" + text);
		fixEncoding();
		squeeze_whitespace();
		simple_tokenize();
		align();
	}
	
	private void align() {
	}

	private void simple_tokenize() throws TweetTokeniserException {
		this.tokenize = new ArrayList<Token>();
		edge_punct_munge();
		System.out.println("Expunged: " + this.text);
		
		ArrayList<String> goods = new ArrayList<String>();
		ArrayList<String> bads = new ArrayList<String>();
		int i = 0;
		Matcher matches = Protect_RE.matcher(this.text);
		if(matches!=null)
		{
			while(matches.find()) {
				String goodString = this.text.substring(i,matches.regionStart());
				goods.add(unprotected_tokenize(goodString));
				bads.add(this.text.substring(matches.regionStart(),matches.regionEnd()));
				i = matches.regionEnd();
			}
			goods.add( this.text.substring(matches.regionEnd(), this.text.length()) );
		}
		else
		{
			String goodString = this.text.substring(0, this.text.length());
			goods.add(unprotected_tokenize(goodString));
		}
		if(bads.size()+1 != goods.size()) throw new TweetTokeniserException();
		ArrayList<Token> res = new ArrayList<Token>();
		for (int j = 0; j < bads.size(); j++) {
			res.add(new DefaultToken(goods.get(i),0));
			res.add(new DefaultToken(bads.get(i),0));
		}
		res.add(new DefaultToken(goods.get(goods.size()-1),0));
			
		
		this.tokenize = post_process(res);
	}

	private ArrayList<Token> post_process(ArrayList<Token> res) {
		return null;
	}
	private String unprotected_tokenize(String goodString) {
		// TODO Auto-generated method stub
		return null;
	}
	private void edge_punct_munge() {
		String s = this.text;
		s = EdgePunctLeft_RE.matcher(s).replaceAll("\\1\\2 \\3");
		s = EdgePunctRight_RE.matcher(s).replaceAll("\\1 \\2\\3");
		this.text = s;
	}

	private void squeeze_whitespace() {
		this.text.replaceAll(spaceRegex, " ");
	}

	private void fixEncoding() throws UnsupportedEncodingException {
		this.text = new String(text.getBytes("UTF-8"),"UTF-8");
		this.text = StringEscapeUtils.unescapeHtml(this.text);
		System.out.println("UTF-8:" + text);
	}
	@Override
	public Iterator<Token> iterator() {
		return this.tokenize.iterator();
	}
	
	public List<Token> getTokens(){
		return this.tokenize;
	}

}
