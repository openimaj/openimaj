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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.twitter.USMFStatus.Link;
import org.openimaj.twitter.USMFStatus.User;

/**
 * GeneralJSONTwitter extends GeneralJSON to provide an object that GSon can
 * fill from a twitter json string. It can also then be used by USMFFStatus to
 * fill a USMFSStatus with the relevant twitter fields.
 *
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GeneralJSONTwitter extends GeneralJSON {



	/*
	 * Twitter has no service field, therefore created.
	 */
	/**
	 *
	 */
	public String s = "twitter";

	/**
	 * New style retweets
	 */
	public GeneralJSONTwitter retweeted_status;

	/*
	 * Named fields used by Gson to build object from JSON text
	 */
	/**
	 * This is a string because sometimes retweet_count can look like: "100+"
	 */
	public String retweet_count;
	/**
	 *
	 */
	public String in_reply_to_screen_name;
	/**
	 *
	 */
	public String text;
	/**
	 *
	 */
	public Map<String, List<Map<String, Object>>> entities = null;
	/**
	 *
	 */
	public Map<String, Object> user = null;

	/**
	 *
	 */
	public Map<String, Object> place = null;
	/**
	 *
	 */
	public Object geo;
	/**
	 *
	 */
	public Object coordinates = null;
	/**
	 *
	 */
	public boolean retweeted;
	/**
	 *
	 */

	public double in_reply_to_status_id;
	/**
	 *
	 */
	public double in_reply_to_user_id;
	/**
	 *
	 */
	public boolean truncated;
	/**
	 *
	 */
	public long id;
	/**
	 *
	 */
	public String created_at;

	/**
	 *
	 */
	public String source;

	/**
	 *
	 */
	public String id_str;

	@SuppressWarnings("unchecked")
	@Override
	public void fillUSMF(USMFStatus status) {
		// Populate message fields
		status.application = this.source;
		status.date = this.created_at;
		if (this.coordinates != null) {
			double[] coords = new double[2];
			ArrayList<Double> coordList = null;
			if(coordinates instanceof Map){
				coordList = (ArrayList<Double>)((Map<?,?>)coordinates).get("coordinates");
			}
			coords[0] = coordList.get(0);
			coords[1] = coordList.get(1);
			status.geo = coords;
		}
		if(this.place != null && place.containsKey("name") && place.containsKey("country_code")){
			status.location = (String) place.get("name");
			status.country_code = (String) place.get("country_code");
		}
		status.id = this.id;
		status.text = this.text;
		status.service = "Twitter";

		// Check if user is null, and make invalid if it is
		if (this.user != null)
		{
			// Populate the User
			String key = "profile_image_url";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.avatar = (String) this.user.get(key);
			key = "description";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.description = (String) this.user.get(key);
			key = "id";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.id = (Double) this.user.get("id");
			key = "lang";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.language = (String) this.user.get("lang");
			key = "statuses_count";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.postings = (Double) this.user.get("statuses_count");
			key = "name";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.real_name = (String) this.user.get("name");
			key = "screen_name";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.name = (String) this.user.get("screen_name");
			key = "followers_count";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.subscribers = (Double) this.user.get("followers_count");
			key = "utc_offset";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.utc = (Double) this.user.get("utc_offset");
			key = "url";
			if (this.user.containsKey(key) && this.user.get(key) != null)
				status.user.website = (String) this.user.get("url");
		}



		// Populate the links
		if (entities != null) {
			for (Map<String, Object> link : entities.get("urls")) {
				USMFStatus.Link l = new USMFStatus.Link();
				Object url = link.get("expanded_url");
				if (url != null) {
					l.href = (String) url;
					status.links.add(l);
				}
			}

			// Populate the keywords from hashtags
			for (Map<String, Object> tag : entities.get("hashtags")) {
				Object st = tag.get("text");
				if (st != null) {
					status.keywords.add((String) st);
				}
			}

			// Populate the to users from user mentions
			for (Map<String, Object> user : entities.get("user_mentions")) {
				USMFStatus.User u = new USMFStatus.User();
				u.name = (String) user.get("screen_name");
				u.real_name = (String) user.get("name");
				u.id = (Double) user.get("id");
				status.to_users.add(u);
			}
			if(this.in_reply_to_screen_name != null){
				status.reply_to = new User();
				status.reply_to.name = this.in_reply_to_screen_name;
				status.reply_to.id = this.in_reply_to_user_id;

			}
		}
		this.fillAnalysis(status);
	}

	@Override
	public void fromUSMF(USMFStatus status) {
		// Populate message fields
		this.source = status.application;
		this.created_at = status.date;
		this.geo = fillCoord(status.geo);

		this.id = status.id;
		this.text = status.text;
		if(status.reply_to!=null){
			this.in_reply_to_screen_name = status.reply_to.name;
			this.in_reply_to_user_id = status.reply_to.id;
		}
		this.user = fillUserMap(status.user);
		this.entities = fillEntities(status.links,status.keywords,status.to_users);
		status.fillAnalysis(this);

	}

	private static Map<String, Object> fillCoord(double[] geo) {
		Map<String, Object> coord = new HashMap<String,Object>();
		coord.put("type", "Point");
		coord.put("coordinates", Arrays.asList(geo));
		return coord;
	}

	private static Map<String, List<Map<String, Object>>> fillEntities(ArrayList<Link> links, ArrayList<String> keywords,ArrayList<User> to_users) {
		Map<String, List<Map<String, Object>>> ents = new HashMap<String, List<Map<String,Object>>>();
		ents.put("urls", fillURLsList(links));
		ents.put("hashtags", fillHashtagsList(keywords));
		ents.put("user_mentions", fillMentionsList(to_users));
		return ents ;
	}

	private static List<Map<String, Object>> fillMentionsList(ArrayList<User> to_users) {
		List<Map<String, Object>> ret = new ArrayList<Map<String,Object>>();
		for (User user : to_users) {
			ret.add(fillUserMap(user));
		}
		return ret;
	}

	private static List<Map<String, Object>> fillHashtagsList(ArrayList<String> keywords) {
		List<Map<String, Object>> ret = new ArrayList<Map<String,Object>>();
		for (String string : keywords) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("text", string);
			ret.add(item);
		}
		return ret;
	}

	private static List<Map<String, Object>> fillURLsList(ArrayList<Link> links) {
		List<Map<String, Object>> urls = new ArrayList<Map<String,Object>>();
		for (Link link : links) {
			urls.add(fillURL(link));
		}
		return urls;
	}

	private static Map<String, Object> fillURL(Link link) {
		Map<String, Object> ret = new HashMap<String, Object>();
		// Maybe get more for the extras field?
		ret.put("url", link.href);
		return ret ;
	}

	private static Map<String, Object> fillUserMap(User user) {
		Map<String, Object> map = new HashMap<String,Object>();
		// Populate the User
		String key = "profile_image_url";
		fillMapEntry(map,key,user.avatar);
		key = "description";
		fillMapEntry(map,key,user.description);
		key = "id";
		fillMapEntry(map,key,user.id);
		key = "lang";
		fillMapEntry(map,key,user.language);
		key = "statuses_count";
		fillMapEntry(map,key,user.postings);
		key = "name";
		fillMapEntry(map,key,user.real_name);
		key = "screen_name";
		fillMapEntry(map,key,user.name);
		key = "followers_count";
		fillMapEntry(map,key,user.subscribers);
		key = "utc_offset";
		fillMapEntry(map,key,user.utc);
		key = "url";
		fillMapEntry(map,key,user.website);
		return map;
	}

	private static void fillMapEntry(Map<String, Object> map, String key, Object value) {
		if(value!=null) map.put(key, value);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		USMFStatus status = new USMFStatus(this.getClass());
		status.readASCII(in);
		this.fromUSMF(status);
	}

	@Override
	public GeneralJSON instanceFromString(String line){
		GeneralJSONTwitter jsonInstance = null;
		try {
			jsonInstance = gson.fromJson(line, GeneralJSONTwitter.class);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		if (jsonInstance.id == 0) {
			GeneralJSONTwitterRawText raw = new GeneralJSONTwitterRawText();
			raw .text = jsonInstance.text;
			return raw;
		}
		return jsonInstance;
	}



}
