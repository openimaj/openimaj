/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.twitter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openimaj.io.IOUtils;

import com.google.gson.Gson;

/**
 * A USMFstatus. A java object representation of the Unified Social Media
 * Format. This object can be empty constructed to do default reads from USMF
 * JSON, or be given a GeneralJSON class for a JSON object it should expect to
 * read from JSON and convert to USMF. Translation from alternative JSON sources
 * relies on the extension of the GeneralJSON class for that format.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
 *         (ss@ecs.soton.ac.uk), Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 *
 */
public class USMFStatus extends GeneralJSON implements Cloneable {
	private static final Logger logger = Logger.getLogger(USMFStatus.class);
	private transient Class<? extends GeneralJSON> generalJSONclass; // class of
	// the
	// source.

	/**
	 * Service Name
	 */
	public String service;
	/**
	 * Unique ID
	 */
	public long id;
	/**
	 * Latitude/Longitude content creation location
	 */
	public double[] geo;
	/**
	 * Application used to create this posting
	 */
	public String application;
	/**
	 * Plain Language content creation location
	 */
	public String location;
	/**
	 * Date posted
	 */
	public String date;
	/**
	 * User friendly link to content
	 */
	public String source;
	/**
	 * Microblog text / Video Title / Etc
	 */
	public String text;
	/**
	 * Full post text / Decription
	 */
	public String description;
	/**
	 * Related Keywords
	 */
	public ArrayList<String> keywords;
	/**
	 * Category of content
	 */
	public String category;
	/**
	 * Duration of content (if video)
	 */
	public long duration;
	/**
	 * Number of users who "liked" this
	 */
	public int likes;
	/**
	 * Number of users who "disliked" this
	 */
	public int dislikes;
	/**
	 * Number of users who "favorited" this
	 */
	public int favorites;
	/**
	 * Number of users who "commented" this
	 */
	public int comments;
	/**
	 * Number of users who "rated" this
	 */
	public int rates;
	/**
	 * Average "rating" of content
	 */
	public int rating;
	/**
	 * Minimum "rating" of content
	 */
	public int min_rating;
	/**
	 * Maximum "rating" of content
	 */
	public int max_rating;
	/**
	 * User object for User Fields
	 */
	public User user;
	/**
	 * List of to users
	 */
	public ArrayList<User> to_users;

	/**
	 * Reply to
	 */
	public User reply_to;
	/**
	 * List of links
	 */
	public ArrayList<Link> links;

	/**
	 * the ISO A2 country code
	 */
	public String country_code;

	private boolean invalid = false;

	/**
	 * Constructor used if the input JSON is not a USMF json string.
	 *
	 * @param generalJSONclass
	 *            : The class of the GeneralJSON extension.
	 */
	public USMFStatus(Class<? extends GeneralJSON> generalJSONclass) {
		this.generalJSONclass = generalJSONclass;
		this.to_users = new ArrayList<USMFStatus.User>();
		this.links = new ArrayList<USMFStatus.Link>();
		this.user = new User();
		this.keywords = new ArrayList<String>();
	}

	/**
	 * Empty constructor for reading from USMF json strings.
	 */
	public USMFStatus() {
		this.generalJSONclass = USMFStatus.class;
		this.to_users = new ArrayList<USMFStatus.User>();
		this.links = new ArrayList<USMFStatus.Link>();
		this.user = new User();
		this.keywords = new ArrayList<String>();
	}

	/**
	 * @return the type of json that backs this instance (used primarily for
	 *         reading)
	 */
	public Class<? extends GeneralJSON> getGeneralJSONClass() {
		return this.generalJSONclass;
	}

	/**
	 * set the type of json that backs this instance (used primarily for
	 * reading)
	 *
	 * @param g
	 */
	public void setGeneralJSONClass(Class<? extends GeneralJSON> g) {
		this.generalJSONclass = g;
	}

	/**
	 * @return the USMF is either a delete notice, a scrub geo notice or some
	 *         other non-status USMF
	 */
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		final String line = (in.nextLine());
		fillFromString(line);
	}

	/**
	 * Used by readASCII(), and available for external use to fill this
	 * USMFStatus with the information held in the line
	 *
	 * @param line
	 *            = json string in the format specified by the constructor of
	 *            this USMFStatus (if empty constructor, expects a USMFSStatus
	 *            json string)
	 */
	public void fillFromString(String line) {
		GeneralJSON jsonInstance = null;
		try {
			jsonInstance = IOUtils.newInstance(generalJSONclass);
			jsonInstance = jsonInstance.instanceFromString(line);
		} catch (final Throwable e) {
			logger.debug("Error parsing USMF: " + e.getMessage());
		}

		if (jsonInstance == null) {
			this.text = line;
		} else {
			jsonInstance.fillUSMF(this);
		}

		if (this.text == null && this.analysis.size() == 0) {
			this.invalid = true;
			return;
		}
		this.invalid = false;
	}

	@Override
	public GeneralJSON instanceFromString(String line) {
		GeneralJSON jsonInstance = null;
		try {
			jsonInstance = gson.fromJson(line, generalJSONclass);
		} catch (final Throwable e) {
			logger.debug("Error parsing USMF: " + e.getMessage());
		}
		return jsonInstance;
	}

	/*
	 * Helper method that populates this instance of a USMFStatus with the data
	 * from a USMFStatus constructed from json
	 */
	private void fillFrom(USMFStatus read) {
		for (final Field field : USMFStatus.class.getFields()) {
			if (Modifier.isPublic(field.getModifiers())) {
				try {
					field.set(this, field.get(read));
				} catch (final IllegalArgumentException e) {
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.text;
	}

	/**
	 * @return convert this {@link USMFStatus} to JSON using {@link Gson}
	 */
	public String toJson() {
		return gson.toJson(this, this.getClass());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof USMFStatus))
			return false;
		final USMFStatus status = (USMFStatus) obj;
		// String statusStr = gson.toJson(status);
		// String thisStr = gson.toJson(this);
		boolean equal = true;
		equal = equalNonAnalysed(status);
		if (!equal)
			return false;
		equal = equalAnalysed(status);
		return equal;
	}

	private boolean equalAnalysed(USMFStatus status) {
		final Map<String, Object> thatanal = status.analysis;
		final Map<String, Object> thisanal = this.analysis;
		for (final String key : thatanal.keySet()) {
			// if this contains the same key, and the values for the key are
			// equal
			if (!thisanal.containsKey(key))
				return false;
			final Object thisobj = thisanal.get(key);
			final Object thatobj = thatanal.get(key);
			if (thisobj.equals(thatobj))
				continue;
			return false;
		}
		return true;
	}

	private boolean equalNonAnalysed(USMFStatus that) {
		final Field[] fields = this.getClass().getDeclaredFields();
		for (final Field field : fields) {
			if (field.getName() == "analysis"
					|| Modifier.isStatic(field.getModifiers())
					|| Modifier.isPrivate(field.getModifiers()))
				continue;
			Object thisval;
			try {
				thisval = field.get(this);
				final Object thatval = field.get(that);
				// If they are both null, or they are equal, continue
				if (thisval == null || thatval == null) {
					if (thisval == null && thatval == null)
						continue;
					else
						return false;

				}
				if (thisval.equals(thatval))
					continue;

			} catch (final Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public USMFStatus clone() {
		return clone(USMFStatus.class);
	}

	/**
	 * Clones the tweet to the given class.
	 *
	 * @param <T>
	 * @param clazz
	 * @return a clone of the status
	 */
	public <T extends USMFStatus> T clone(Class<T> clazz) {
		return gson.fromJson(gson.toJson(this), clazz);
	}

	/**
	 * @return get the created_at date as a java date
	 * @throws ParseException
	 */
	public DateTime createdAt() throws ParseException {
		final DateTimeFormatter parser = DateTimeFormat
				.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
		if (date == null)
			return null;
		return parser.parseDateTime(date);
	}

	/**
	 * Container object to hold user information
	 *
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 *
	 */
	public static class User {
		/**
		 * User Name
		 */
		public String name;
		/**
		 * Real name of user
		 */
		public String real_name;
		/**
		 * Unique User ID
		 */
		public double id;
		/**
		 * Spoken language of user
		 */
		public String language;
		/**
		 * UTC time offset of user
		 */
		public double utc;
		/**
		 * Latitude/Logitude User location
		 */
		public double[] geo;
		/**
		 * User profile description
		 */
		public String description;
		/**
		 * Direct href to avatar image
		 */
		public String avatar;
		/**
		 * Plain Language User location
		 */
		public String location;
		/**
		 * Number of subscribers
		 */
		public double subscribers;
		/**
		 * Number of subscriptions
		 */
		public int subscriptions;
		/**
		 * Number of postings made
		 */
		public double postings;
		/**
		 * Href to user profile
		 */
		public String profile;
		/**
		 * Href to user website
		 */
		public String website;

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof User) {
				final User in = (User) obj;
				for (final Field field : User.class.getFields()) {
					try {
						if (field.get(this) == null && field.get(in) == null)
							continue;
						else if (field.get(this) == null
								|| field.get(in) == null)
							return false;
						else if (!field.get(this).equals(field.get(in)))
							return false;
					} catch (final IllegalArgumentException e) {

						e.printStackTrace();
					} catch (final IllegalAccessException e) {

						e.printStackTrace();
					}
				}
				return true;
			}
			return false;
		}

	}

	/**
	 * Container object for holding link information
	 *
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 *
	 */
	public static class Link {
		/**
		 * Title of item
		 */
		public String title;
		/**
		 * Direct href to thumbnail for item
		 */
		public String thumbnail;
		/**
		 * Direct href to item
		 */
		public String href;

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Link) {
				final Link in = (Link) obj;
				for (final Field field : Link.class.getFields()) {
					try {
						if (field.get(this) == null && field.get(in) == null)
							continue;
						else if (field.get(this) == null
								|| field.get(in) == null)
							return false;
						else if (!field.get(this).equals(field.get(in)))
							return false;
					} catch (final IllegalArgumentException e) {

						e.printStackTrace();
					} catch (final IllegalAccessException e) {

						e.printStackTrace();
					}
				}
				return true;
			}
			return false;
		}
	}

	@Override
	public void fillUSMF(USMFStatus status) {
		status.fillFrom(this);
	}

	@Override
	public void fromUSMF(USMFStatus status) {
		this.fillFrom(status);
	}

}
