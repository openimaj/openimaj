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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openimaj.io.ReadWriteable;

import com.google.gson.Gson;

/**
 * A USMFstatus. A java object representation of the Unified Social Media
 * Format. This object can be empty constructed to do default reads from USMF
 * JSON, or be given a GeneralJSON class for a JSON object it should expect to
 * read from JSON and convert to USMF. Translation from alternative JSON sources
 * relies on the extension of the GeneralJSON class for that format.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk), Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * 
 */
public class USMFStatus implements ReadWriteable, Cloneable, GeneralJSON {

	private transient Gson gson = new Gson();
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
	 * List of links
	 */
	public ArrayList<Link> links;

	/**
	 * analysos held in the object
	 */
	public Map<String, Object> analysis = new HashMap<String, Object>();
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
	 * @return the USMF is either a delete notice, a scrub geo notice or some
	 *         other non-status USMF
	 */
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		String line = (in.nextLine());
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
			jsonInstance = gson.fromJson(line, generalJSONclass);
		} catch (Throwable e) {
			// Could not parse the line, invalid json.
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

	/*
	 * Helper method that populates this instance of a USMFStatus with the data
	 * from a USMFStatus constructed from json
	 */
	private void fillFrom(USMFStatus read) {
		for (Field field : USMFStatus.class.getFields()) {
			if (Modifier.isPublic(field.getModifiers())) {
				try {
					field.set(this, field.get(read));
				} catch (IllegalArgumentException e) {					
					e.printStackTrace();
				} catch (IllegalAccessException e) {					
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		gson.toJson(this, out);
	}

	@Override
	public String toString() {
		return this.text;
	}

	/**
	 * Add analysis to the analysis object. This is where all non twitter stuff
	 * should go
	 * 
	 * @param <T>
	 *            The type of data being saved
	 * @param annKey
	 *            the key
	 * @param annVal
	 *            the value
	 */
	public <T> void addAnalysis(String annKey, T annVal) {
		if (annVal instanceof Number)
			this.analysis.put(annKey, ((Number) annVal).doubleValue());
		else
			this.analysis.put(annKey, annVal);
	}

	/**
	 * @param <T>
	 * @param name
	 * @return the analysis under the name
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAnalysis(String name) {
		return (T) this.analysis.get(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof USMFStatus))
			return false;
		USMFStatus status = (USMFStatus) obj;
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
		Map<String, Object> thatanal = status.analysis;
		Map<String, Object> thisanal = this.analysis;
		for (String key : thatanal.keySet()) {
			// if this contains the same key, and the values for the key are
			// equal
			if (!thisanal.containsKey(key))
				return false;
			Object thisobj = thisanal.get(key);
			Object thatobj = thatanal.get(key);
			if (thisobj.equals(thatobj))
				continue;
			return false;
		}
		return true;
	}

	private boolean equalNonAnalysed(USMFStatus that) {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getName() == "analysis"
					|| Modifier.isStatic(field.getModifiers())
					|| Modifier.isPrivate(field.getModifiers()))
				continue;
			Object thisval;
			try {
				thisval = field.get(this);
				Object thatval = field.get(that);
				// If they are both null, or they are equal, continue
				if (thisval == null || thatval == null) {
					if (thisval == null && thatval == null)
						continue;
					else
						return false;

				}
				if (thisval.equals(thatval))
					continue;

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] binaryHeader() {
		return "BINARYHEADER".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();

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
	 * Convenience to allow writing of just the analysis to a writer
	 * 
	 * @param outputWriter
	 * @param selectiveAnalysis
	 */
	public void writeASCIIAnalysis(PrintWriter outputWriter,
			List<String> selectiveAnalysis) {
		writeASCIIAnalysis(outputWriter, selectiveAnalysis,
				new ArrayList<String>());
	}

	/**
	 * Convenience to allow writing of just the analysis and some status
	 * information to a writer
	 * 
	 * @param outputWriter
	 * @param selectiveAnalysis
	 * @param selectiveStatus
	 */
	public void writeASCIIAnalysis(PrintWriter outputWriter,
			List<String> selectiveAnalysis, List<String> selectiveStatus) {
		Map<String, Object> toOutput = new HashMap<String, Object>();
		Map<String, Object> analysisBit = new HashMap<String, Object>();
		toOutput.put("analysis", analysisBit);
		for (String analysisKey : selectiveAnalysis) {
			analysisBit.put(analysisKey, getAnalysis(analysisKey));
		}
		for (String status : selectiveStatus) {
			try {

				Field f = this.getClass().getField(status);
				toOutput.put(status, f.get(this));
			} catch (SecurityException e) {
				System.err.println("Invalid field: " + status);
			} catch (NoSuchFieldException e) {
				System.err.println("Invalid field: " + status);
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid field: " + status);
			} catch (IllegalAccessException e) {
				System.err.println("Invalid field: " + status);
			}
		}
		gson.toJson(toOutput, outputWriter);
	}

	/**
	 * @return get the created_at date as a java date
	 * @throws ParseException
	 */
	public DateTime createdAt() throws ParseException {
		DateTimeFormatter parser = DateTimeFormat
				.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
		if (date == null)
			return null;
		return parser.parseDateTime(date);
	}

	/**
	 * Container object to hold user information
	 * @author laurence
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
				User in = (User) obj;
				for (Field field : User.class.getFields()) {
					try {
						if (field.get(this) == null && field.get(in) == null)
							continue;
						else if (field.get(this) == null
								|| field.get(in) == null)
							return false;
						else if (!field.get(this).equals(field.get(in)))
							return false;
					} catch (IllegalArgumentException e) {
						
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						
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
	 * @author laurence
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
				Link in = (Link) obj;
				for (Field field : Link.class.getFields()) {
					try {
						if (field.get(this) == null && field.get(in) == null)
							continue;
						else if (field.get(this) == null
								|| field.get(in) == null)
							return false;
						else if (!field.get(this).equals(field.get(in)))
							return false;
					} catch (IllegalArgumentException e) {
						
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						
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

}
