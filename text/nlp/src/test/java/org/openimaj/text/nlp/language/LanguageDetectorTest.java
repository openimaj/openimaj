package org.openimaj.text.nlp.language;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import junit.framework.Assert;


import org.junit.Test;
import org.openimaj.io.IOUtils;
import org.openimaj.text.nlp.language.LanguageDetector;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;

/**
 * 
 * Test the language detector 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class LanguageDetectorTest {
	
	/**
	 * Load the language model by constructing a new detector. Check the values are readable.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLanguageModel() throws IOException{
		LanguageDetector det = new LanguageDetector();
		
		String[] englishStrings = new String[]{
				"This is an english sentence",
				"The uninstaller it downloads for you is 33.4MB ! For an *un*installer? #wtf"
			};
		assertLanguage(det,englishStrings,Locale.ENGLISH);
		
		String[] germanStrings = new String[]{
			"in der josefstadt scheint es offenbar sehr knapp zu werden warum dauert die ausz\u00e4hlung im kleinsten bezirk wiens so lange",
			"bezirk sch\u00f6nbrunner allee \u2013 stauraum vor hetzendorfer stra\u00dfe wird saniert",
			"das erlebnis im wahllokal im bezirk um im \u00f6sterreich ticker ist heftig"
		};
		assertLanguage(det,germanStrings,Locale.GERMAN);
		
		String[] japaneseStrings = new String[]{
			"@yuekoo \u304a\u306f\u3088\u30fc\u3002\u5730\u30c7\u30b8\u2025\u306a\u3093\u304b\u3001\u4eca\u306f\u5165\u308b\u2025\u3002\u610f\u5473\u304c\u308f\u304b\u3089\u3093(&gt;_&lt;)",
			"\u3053\u308C\u306F\u79C1\u304C\u65E5\u672C\u8A9E\u3067\u8A18\u8FF0\u3059\u308B\u6587\u5B57\u5217\u3067\u3059\u3002"
		};
		assertLanguage(det,japaneseStrings,Locale.JAPANESE);
		
		String[] hindiStrings = new String[]{
			"\u092F\u0939 \u090F\u0915 \u0938\u094D\u091F\u094D\u0930\u093F\u0902\u0917 \u0939\u0948 \u0915\u093F \u092E\u0948\u0902 \u0939\u093F\u0902\u0926\u0940 \u092E\u0947\u0902 \u0932\u093F\u0916\u0928\u093E \u0939\u0948",
			"\u0924\u0947\u0939\u0930\u093E\u0928. \u0908\u0930\u093E\u0928 \u0915\u0947 \u0930\u093E\u0937\u094D\u091F\u094D\u0930\u092A\u0924\u093F \u092E\u0939\u092E\u0942\u0926 \u0905\u0939\u092E\u0926\u0940\u0928\u0947\u091C\u093E\u0926 \u0928\u0947 \u092C\u0941\u0927\u0935\u093E\u0930 \u0915\u094B \u0930\u093E\u0937\u094D\u091F\u094D\u0930 \u0915\u094B \u0938\u0902\u092C\u094B\u0927\u093F\u0924 \u0915\u0930\u0924\u0947 \u0939\u0941\u090F \u0915\u0939\u093E \u0915\u093F \u0908\u0930\u093E\u0928 \u092C\u092E \u0928\u0939\u0940\u0902 \u092C\u0928\u093E \u0930\u0939\u093E \u0939\u0948\u0964 \u092A\u0930\u092E\u093E\u0923\u0941 \u0915\u093E \u092E\u0924\u0932\u092C \u0938\u093F\u0930\u094D\u092B \u092C\u092E \u0939\u0940 \u0928\u0939\u0940\u0902 \u0939\u094B\u0924\u093E \u0939\u0948\u0964\u0905\u092E\u0947\u0930\u093F\u0915\u093E \u0914\u0930 \u0907\u091C\u0930\u093E\u0907\u0932 \u092A\u0930 \u0924\u0940\u0916\u093E \u092A\u094D\u0930\u0939\u093E\u0930 \u0915\u0930\u0924\u0947 \u0939\u0941\u090F \u0905\u0939\u092E\u0926\u0940\u0928\u0947\u091C\u093E\u0926 \u0928\u0947 \u0915\u0939\u093E \u0915\u093F \u0935\u094B \u0908\u0930\u093E\u0928\u0940 \u0935\u0948\u091C\u094D\u091E\u093E\u0928\u093F\u0915\u094B\u0902 \u0915\u094B \u0907\u0938\u0932\u093F\u090F \u092E\u093E\u0930 \u0930\u0939\u0947 \u0939\u0948\u0902 \u0915\u094D\u092F\u094B\u0902\u0915\u093F \u0935\u094B \u0928\u0939\u0940\u0902 \u091A\u093E\u0939\u0924\u0947 \u0915\u093F \u0915\u094B\u0908 \u0914\u0930 \u092E\u0941\u0932\u094D\u0915 \u0906\u0917\u0947 \u092C\u0922\u093C\u0947\u0964 \u0939\u092E\u093E\u0930\u0947 \u0935\u0948\u091C\u094D\u091E\u093E\u0928\u093F\u0915\u094B\u0902 \u0928\u0947 \u0907\u0938 \u0909\u092A\u0932\u092C\u094D\u0927\u093F \u0915\u094B \u0939\u093E\u0938\u093F\u0932 \u0915\u0930\u0928\u0947 \u092E\u0947\u0902 \u092C\u0939\u0941\u0924 \u092E\u0947\u0939\u0928\u0924 \u0915\u0940 \u0939\u0948\u0964"
		};
		assertLanguage(det,hindiStrings,new Locale("hi"));
	}
	
	/**
	 * Testing the read/write binary code of the language model
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLanguageModelReadWrite() throws IOException{
		LanguageDetector det = new LanguageDetector();
		File out = File.createTempFile("languagemodel", ".binary");
		IOUtils.writeBinary(new GZIPOutputStream(new FileOutputStream(out)),det.getLanguageModel());
		InputStream is = new FileInputStream(out);
		LanguageModel readModel = IOUtils.read(new GZIPInputStream(is), LanguageModel.class);
		LanguageDetector newdet = new LanguageDetector(readModel);
		assertTrue(readModel.equals(det.getLanguageModel()));
		String[] hindiStrings = new String[]{
				"\u092F\u0939 \u090F\u0915 \u0938\u094D\u091F\u094D\u0930\u093F\u0902\u0917 \u0939\u0948 \u0915\u093F \u092E\u0948\u0902 \u0939\u093F\u0902\u0926\u0940 \u092E\u0947\u0902 \u0932\u093F\u0916\u0928\u093E \u0939\u0948",
				"\u0924\u0947\u0939\u0930\u093E\u0928. \u0908\u0930\u093E\u0928 \u0915\u0947 \u0930\u093E\u0937\u094D\u091F\u094D\u0930\u092A\u0924\u093F \u092E\u0939\u092E\u0942\u0926 \u0905\u0939\u092E\u0926\u0940\u0928\u0947\u091C\u093E\u0926 \u0928\u0947 \u092C\u0941\u0927\u0935\u093E\u0930 \u0915\u094B \u0930\u093E\u0937\u094D\u091F\u094D\u0930 \u0915\u094B \u0938\u0902\u092C\u094B\u0927\u093F\u0924 \u0915\u0930\u0924\u0947 \u0939\u0941\u090F \u0915\u0939\u093E \u0915\u093F \u0908\u0930\u093E\u0928 \u092C\u092E \u0928\u0939\u0940\u0902 \u092C\u0928\u093E \u0930\u0939\u093E \u0939\u0948\u0964 \u092A\u0930\u092E\u093E\u0923\u0941 \u0915\u093E \u092E\u0924\u0932\u092C \u0938\u093F\u0930\u094D\u092B \u092C\u092E \u0939\u0940 \u0928\u0939\u0940\u0902 \u0939\u094B\u0924\u093E \u0939\u0948\u0964\u0905\u092E\u0947\u0930\u093F\u0915\u093E \u0914\u0930 \u0907\u091C\u0930\u093E\u0907\u0932 \u092A\u0930 \u0924\u0940\u0916\u093E \u092A\u094D\u0930\u0939\u093E\u0930 \u0915\u0930\u0924\u0947 \u0939\u0941\u090F \u0905\u0939\u092E\u0926\u0940\u0928\u0947\u091C\u093E\u0926 \u0928\u0947 \u0915\u0939\u093E \u0915\u093F \u0935\u094B \u0908\u0930\u093E\u0928\u0940 \u0935\u0948\u091C\u094D\u091E\u093E\u0928\u093F\u0915\u094B\u0902 \u0915\u094B \u0907\u0938\u0932\u093F\u090F \u092E\u093E\u0930 \u0930\u0939\u0947 \u0939\u0948\u0902 \u0915\u094D\u092F\u094B\u0902\u0915\u093F \u0935\u094B \u0928\u0939\u0940\u0902 \u091A\u093E\u0939\u0924\u0947 \u0915\u093F \u0915\u094B\u0908 \u0914\u0930 \u092E\u0941\u0932\u094D\u0915 \u0906\u0917\u0947 \u092C\u0922\u093C\u0947\u0964 \u0939\u092E\u093E\u0930\u0947 \u0935\u0948\u091C\u094D\u091E\u093E\u0928\u093F\u0915\u094B\u0902 \u0928\u0947 \u0907\u0938 \u0909\u092A\u0932\u092C\u094D\u0927\u093F \u0915\u094B \u0939\u093E\u0938\u093F\u0932 \u0915\u0930\u0928\u0947 \u092E\u0947\u0902 \u092C\u0939\u0941\u0924 \u092E\u0947\u0939\u0928\u0924 \u0915\u0940 \u0939\u0948\u0964"
			};
//		assertLanguage(det,hindiStrings,new Locale("hi"));
		assertLanguage(newdet,hindiStrings,new Locale("hi"));
	}

	private void assertLanguage(LanguageDetector det, String[] statements, Locale language) {
		for (String statement : statements) {
			WeightedLocale estimateLanguage = det.classify(statement);
			System.out.println(estimateLanguage);
			Assert.assertEquals(language,estimateLanguage.getLocale());
		}
	}
	
	
}
