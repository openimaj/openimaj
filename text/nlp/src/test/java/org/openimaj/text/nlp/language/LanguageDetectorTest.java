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
package org.openimaj.text.nlp.language;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.IOUtils;
import org.openimaj.text.nlp.language.LanguageDetector.WeightedLocale;

/**
 * 
 * Test the language detector
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class LanguageDetectorTest {
	/**
	 * The temporary output folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	/**
	 * Load the language model by constructing a new detector. Check the values
	 * are readable.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLanguageModel() throws IOException {
		final LanguageDetector det = new LanguageDetector();

		final String[] englishStrings = new String[] { "This is an english sentence",
				"The uninstaller it downloads for you is 33.4MB ! For an *un*installer? #wtf" };
		assertLanguage(det, englishStrings, Locale.ENGLISH);

		final String[] germanStrings = new String[] {
				"in der josefstadt scheint es offenbar sehr knapp zu werden warum dauert die ausz\u00e4hlung im kleinsten bezirk wiens so lange",
				"bezirk sch\u00f6nbrunner allee \u2013 stauraum vor hetzendorfer stra\u00dfe wird saniert",
				"das erlebnis im wahllokal im bezirk um im \u00f6sterreich ticker ist heftig" };
		assertLanguage(det, germanStrings, Locale.GERMAN);

		final String[] japaneseStrings = new String[] {
				"@yuekoo \u304a\u306f\u3088\u30fc\u3002\u5730\u30c7\u30b8\u2025\u306a\u3093\u304b\u3001\u4eca\u306f\u5165\u308b\u2025\u3002\u610f\u5473\u304c\u308f\u304b\u3089\u3093(&gt;_&lt;)",
				"\u3053\u308C\u306F\u79C1\u304C\u65E5\u672C\u8A9E\u3067\u8A18\u8FF0\u3059\u308B\u6587\u5B57\u5217\u3067\u3059\u3002" };
		assertLanguage(det, japaneseStrings, Locale.JAPANESE);

		final String[] hindiStrings = new String[] {
				"\u092F\u0939 \u090F\u0915 \u0938\u094D\u091F\u094D\u0930\u093F\u0902\u0917 \u0939\u0948 \u0915\u093F \u092E\u0948\u0902 \u0939\u093F\u0902\u0926\u0940 \u092E\u0947\u0902 \u0932\u093F\u0916\u0928\u093E \u0939\u0948",
				"\u0924\u0947\u0939\u0930\u093E\u0928. \u0908\u0930\u093E\u0928 \u0915\u0947 \u0930\u093E\u0937\u094D\u091F\u094D\u0930\u092A\u0924\u093F \u092E\u0939\u092E\u0942\u0926 \u0905\u0939\u092E\u0926\u0940\u0928\u0947\u091C\u093E\u0926 \u0928\u0947 \u092C\u0941\u0927\u0935\u093E\u0930 \u0915\u094B \u0930\u093E\u0937\u094D\u091F\u094D\u0930 \u0915\u094B \u0938\u0902\u092C\u094B\u0927\u093F\u0924 \u0915\u0930\u0924\u0947 \u0939\u0941\u090F \u0915\u0939\u093E \u0915\u093F \u0908\u0930\u093E\u0928 \u092C\u092E \u0928\u0939\u0940\u0902 \u092C\u0928\u093E \u0930\u0939\u093E \u0939\u0948\u0964 \u092A\u0930\u092E\u093E\u0923\u0941 \u0915\u093E \u092E\u0924\u0932\u092C \u0938\u093F\u0930\u094D\u092B \u092C\u092E \u0939\u0940 \u0928\u0939\u0940\u0902 \u0939\u094B\u0924\u093E \u0939\u0948\u0964\u0905\u092E\u0947\u0930\u093F\u0915\u093E \u0914\u0930 \u0907\u091C\u0930\u093E\u0907\u0932 \u092A\u0930 \u0924\u0940\u0916\u093E \u092A\u094D\u0930\u0939\u093E\u0930 \u0915\u0930\u0924\u0947 \u0939\u0941\u090F \u0905\u0939\u092E\u0926\u0940\u0928\u0947\u091C\u093E\u0926 \u0928\u0947 \u0915\u0939\u093E \u0915\u093F \u0935\u094B \u0908\u0930\u093E\u0928\u0940 \u0935\u0948\u091C\u094D\u091E\u093E\u0928\u093F\u0915\u094B\u0902 \u0915\u094B \u0907\u0938\u0932\u093F\u090F \u092E\u093E\u0930 \u0930\u0939\u0947 \u0939\u0948\u0902 \u0915\u094D\u092F\u094B\u0902\u0915\u093F \u0935\u094B \u0928\u0939\u0940\u0902 \u091A\u093E\u0939\u0924\u0947 \u0915\u093F \u0915\u094B\u0908 \u0914\u0930 \u092E\u0941\u0932\u094D\u0915 \u0906\u0917\u0947 \u092C\u0922\u093C\u0947\u0964 \u0939\u092E\u093E\u0930\u0947 \u0935\u0948\u091C\u094D\u091E\u093E\u0928\u093F\u0915\u094B\u0902 \u0928\u0947 \u0907\u0938 \u0909\u092A\u0932\u092C\u094D\u0927\u093F \u0915\u094B \u0939\u093E\u0938\u093F\u0932 \u0915\u0930\u0928\u0947 \u092E\u0947\u0902 \u092C\u0939\u0941\u0924 \u092E\u0947\u0939\u0928\u0924 \u0915\u0940 \u0939\u0948\u0964" };
		assertLanguage(det, hindiStrings, new Locale("hi"));
	}

	/**
	 * Testing the read/write binary code of the language model
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLanguageModelReadWrite() throws IOException {
		final LanguageDetector det = new LanguageDetector();
		final File out = folder.newFile("languagemodel.binary");
		GZIPOutputStream os = new GZIPOutputStream(new FileOutputStream(out));
		IOUtils.writeBinary(os, det.getLanguageModel());
		os.flush();
		os.close();

		final InputStream is = new FileInputStream(out);
		final LanguageModel readModel = IOUtils.read(new GZIPInputStream(is), LanguageModel.class);
		final LanguageDetector newdet = new LanguageDetector(readModel);

		assertTrue(readModel.equals(det.getLanguageModel()));

		final String[] hindiStrings = new String[] {
				"\u092F\u0939 \u090F\u0915 \u0938\u094D\u091F\u094D\u0930\u093F\u0902\u0917 \u0939\u0948 \u0915\u093F \u092E\u0948\u0902 \u0939\u093F\u0902\u0926\u0940 \u092E\u0947\u0902 \u0932\u093F\u0916\u0928\u093E \u0939\u0948",
				"\u0924\u0947\u0939\u0930\u093E\u0928. \u0908\u0930\u093E\u0928 \u0915\u0947 \u0930\u093E\u0937\u094D\u091F\u094D\u0930\u092A\u0924\u093F \u092E\u0939\u092E\u0942\u0926 \u0905\u0939\u092E\u0926\u0940\u0928\u0947\u091C\u093E\u0926 \u0928\u0947 \u092C\u0941\u0927\u0935\u093E\u0930 \u0915\u094B \u0930\u093E\u0937\u094D\u091F\u094D\u0930 \u0915\u094B \u0938\u0902\u092C\u094B\u0927\u093F\u0924 \u0915\u0930\u0924\u0947 \u0939\u0941\u090F \u0915\u0939\u093E \u0915\u093F \u0908\u0930\u093E\u0928 \u092C\u092E \u0928\u0939\u0940\u0902 \u092C\u0928\u093E \u0930\u0939\u093E \u0939\u0948\u0964 \u092A\u0930\u092E\u093E\u0923\u0941 \u0915\u093E \u092E\u0924\u0932\u092C \u0938\u093F\u0930\u094D\u092B \u092C\u092E \u0939\u0940 \u0928\u0939\u0940\u0902 \u0939\u094B\u0924\u093E \u0939\u0948\u0964\u0905\u092E\u0947\u0930\u093F\u0915\u093E \u0914\u0930 \u0907\u091C\u0930\u093E\u0907\u0932 \u092A\u0930 \u0924\u0940\u0916\u093E \u092A\u094D\u0930\u0939\u093E\u0930 \u0915\u0930\u0924\u0947 \u0939\u0941\u090F \u0905\u0939\u092E\u0926\u0940\u0928\u0947\u091C\u093E\u0926 \u0928\u0947 \u0915\u0939\u093E \u0915\u093F \u0935\u094B \u0908\u0930\u093E\u0928\u0940 \u0935\u0948\u091C\u094D\u091E\u093E\u0928\u093F\u0915\u094B\u0902 \u0915\u094B \u0907\u0938\u0932\u093F\u090F \u092E\u093E\u0930 \u0930\u0939\u0947 \u0939\u0948\u0902 \u0915\u094D\u092F\u094B\u0902\u0915\u093F \u0935\u094B \u0928\u0939\u0940\u0902 \u091A\u093E\u0939\u0924\u0947 \u0915\u093F \u0915\u094B\u0908 \u0914\u0930 \u092E\u0941\u0932\u094D\u0915 \u0906\u0917\u0947 \u092C\u0922\u093C\u0947\u0964 \u0939\u092E\u093E\u0930\u0947 \u0935\u0948\u091C\u094D\u091E\u093E\u0928\u093F\u0915\u094B\u0902 \u0928\u0947 \u0907\u0938 \u0909\u092A\u0932\u092C\u094D\u0927\u093F \u0915\u094B \u0939\u093E\u0938\u093F\u0932 \u0915\u0930\u0928\u0947 \u092E\u0947\u0902 \u092C\u0939\u0941\u0924 \u092E\u0947\u0939\u0928\u0924 \u0915\u0940 \u0939\u0948\u0964" };
		// assertLanguage(det,hindiStrings,new Locale("hi"));
		assertLanguage(newdet, hindiStrings, new Locale("hi"));
	}

	private void assertLanguage(LanguageDetector det, String[] statements, Locale language) {
		for (final String statement : statements) {
			final WeightedLocale estimateLanguage = det.classify(statement);
			System.out.println(estimateLanguage);
			Assert.assertEquals(language, estimateLanguage.getLocale());
		}
	}
}
