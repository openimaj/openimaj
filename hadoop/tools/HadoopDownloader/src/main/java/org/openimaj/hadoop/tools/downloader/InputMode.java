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
package org.openimaj.hadoop.tools.downloader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.MD5Hash;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.util.pair.IndependentPair;

/**
 * Different types of input file formats.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum InputMode implements CmdLineOptionsProvider {
	/**
	 * Plain list-of-urls file. One URL per line.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	PLAIN {
		@Option(name = "-hash-keys", usage = "use the MD5SUM of the URL as the key, rather than the URL itself.")
		boolean hashKeys = false;

		@Override
		public Parser getOptions() {
			return new Parser() {
				@Override
				public IndependentPair<String, List<URL>> parse(String data) throws Exception {
					String key = data;

					if (hashKeys) {
						key = MD5Hash.digest(key).toString();
					}

					final ArrayList<URL> value = new ArrayList<URL>();
					value.add(new URL(data));

					return new IndependentPair<String, List<URL>>(key, value);
				}
			};
		}
	},
	/**
	 * List of URLs in the form provided by <a
	 * href="http://www.image-net.org">image-net</a>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	IMAGE_NET {
		@Override
		public Parser getOptions() {
			return new Parser() {
				@Override
				public IndependentPair<String, List<URL>> parse(String data) throws Exception {
					// we expect a format [id]\t[url] as with the image-net url
					// set
					final String[] split = data.split("\t");
					if (split.length != 2) {
						throw new RuntimeException("Record is in the wrong format");
					}

					final String id = split[0].trim();
					final String url = split[1].trim();

					final ArrayList<URL> value = new ArrayList<URL>();
					value.add(new URL(url));

					return new IndependentPair<String, List<URL>>(id, value);
				}
			};
		}
	},
	/**
	 * Wikipedia image URLs dump format
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	WIKIPEDIA_IMAGES_DUMP {
		@Override
		public Parser getOptions() {
			return new Parser() {
				@Option(
						name = "--wikipedia-baseurl",
						aliases = "-wbase",
						required = false,
						usage = "wikipedia upload files base urls. add many urls to check different locations for each image. defaults to upload.wikimedia.org/wikipedia/commons and upload.wikimedia.org/wikipedia/en",
						multiValued = true)
				private List<String> wikipediaBase;

				@Override
				public IndependentPair<String, List<URL>> parse(String data) throws Exception {
					if (wikipediaBase == null) {
						wikipediaBase = new ArrayList<String>();
						wikipediaBase.add("http://upload.wikimedia.org/wikipedia/commons");
						wikipediaBase.add("http://upload.wikimedia.org/wikipedia/en");
					}

					final String[] split = data.split(":");
					if (split.length != 2) {
						throw new RuntimeException("Record is in the wrong format");
					}

					final String hash = MD5Hash.digest(split[1]).toString();
					final String dirStructure = String.format("%s/%s", hash.substring(0, 1), hash.substring(0, 2));

					final ArrayList<URL> value = new ArrayList<URL>();
					for (final String base : wikipediaBase) {
						final String completeURL = String.format("%s/%s/%s", base, dirStructure,
								split[1].replace(" ", "_"));
						value.add(new URL(completeURL));
					}

					return new IndependentPair<String, List<URL>>(data, value);
				}
			};
		}
	},
	/**
	 * Parse urls and keys from a csv record.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	CSV {
		@Override
		public Parser getOptions() {
			return new CsvParser() {
				@Option(name = "--key-field")
				int keyField;
				@Option(name = "--url-field")
				int urlField;

				@Override
				public int getKeyField() {
					return keyField;
				}

				@Override
				public int getUrlField() {
					return urlField;
				}
			};
		}
	},
	/**
	 * Parse the FlickrCrawler csv file to get the medium url of the image.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	FLICKR_CSV_MEDIUM {
		@Override
		public Parser getOptions() {
			return new CsvParser() {
				@Override
				public int getKeyField() {
					return 2;
				}

				@Override
				public int getUrlField() {
					return 5;
				}
			};
		}
	};

	@Override
	public abstract Parser getOptions();

	/**
	 * Options for the {@link InputMode}
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static abstract class Parser {
		/**
		 * Parse a record into a key and list of potential URLs. In most cases
		 * there will only be a single potential URL in the list. The downloader
		 * will work through the list until it finds a working URL, or exhausts
		 * its options.
		 * 
		 * @param data
		 *            the data record from the input file
		 * @return the key and potential URLs
		 * @throws Exception
		 *             if an error occurs
		 */
		public abstract IndependentPair<String, List<URL>> parse(String data) throws Exception;
	}

	private static abstract class CsvParser extends Parser {
		final static String CVS_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))";

		public abstract int getKeyField();

		public abstract int getUrlField();

		@Override
		public IndependentPair<String, List<URL>> parse(String data) throws Exception {
			final String[] parts = data.split(CVS_REGEX);

			final String key = unescapeCSV(parts[getKeyField()]);
			final URL url = new URL(unescapeCSV(parts[getUrlField()]));

			final ArrayList<URL> value = new ArrayList<URL>();
			value.add(url);

			return new IndependentPair<String, List<URL>>(key, value);
		}

		private String unescapeCSV(String input) {
			if (input == null)
				return input;
			else if (input.length() < 2)
				return input;
			else if (input.charAt(0) != '"' || input.charAt(input.length() - 1) != '"')
				return input;
			else {
				String quoteless = input.substring(1, input.length() - 1);

				if (quoteless.contains(",") || quoteless.contains("\n") || quoteless.contains("\"")) {
					quoteless = quoteless.replace("\"\"", "\"");
				}

				return quoteless;
			}
		}
	}
}
