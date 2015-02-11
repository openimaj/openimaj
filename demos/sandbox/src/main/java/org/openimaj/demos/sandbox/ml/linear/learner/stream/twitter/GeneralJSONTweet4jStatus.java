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
package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import java.io.IOException;
import java.util.Scanner;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.USMFStatus.User;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Turns {@link Status} instances into {@link USMFStatus} instances such
 * that the statuses can be used in the {@link TwitterPreprocessingMode} and
 * other twitter preprocessing libraries
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GeneralJSONTweet4jStatus extends GeneralJSON{

	private Status status;

	/**
	 * @param status
	 */
	public GeneralJSONTweet4jStatus(Status status) {
		this.status = status;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		USMFStatus status = new USMFStatus(this.getClass());
		status.readASCII(in);
		this.fromUSMF(status);
	}

	@Override
	public void fillUSMF(USMFStatus status) {
// Populate message fields
		status.application = this.status.getSource();
		DateTimeFormatter parser = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
		status.date = parser.print(this.status.getCreatedAt().getTime());;
		if (this.status.getGeoLocation() != null) {
			GeoLocation geloc = this.status.getGeoLocation();
			double[] coords = new double[2];

			coords[0] = geloc.getLatitude();
			coords[1] = geloc.getLongitude();
			status.geo = coords;
		}
		Place place = this.status.getPlace();
		if(place!=null){
			status.location = place.getName();
			status.country_code = place.getCountryCode();
		}
		status.id = this.status.getId();
		status.text = this.status.getText();
		status.service = "Twitter";

		// Check if user is null, and make invalid if it is
		if (this.status.getUser() != null)
		{
			twitter4j.User user = this.status.getUser();
			// Populate the User

			status.user.avatar = user.getBiggerProfileImageURL();
			status.user.description = user.getDescription();
			status.user.id = user.getId();
			status.user.location = user.getLocation();
			status.user.language = user.getLang();
			status.user.postings = user.getStatusesCount();
			status.user.real_name = user.getName();
			status.user.name = user.getScreenName();
			status.user.subscribers = user.getFollowersCount();
			status.user.utc = user.getUtcOffset();
			status.user.website = user.getURL();
		}



		// Populate the links


		MediaEntity[] ents = this.status.getMediaEntities();
		for (URLEntity link : this.status.getURLEntities()) {
			USMFStatus.Link l = new USMFStatus.Link();
			l.href = link.getExpandedURL();
		}

		// Populate the keywords from hashtags
		for (HashtagEntity tag : this.status.getHashtagEntities()) {
			status.keywords.add(tag.getText());
		}

		// Populate the to users from user mentions
		for (UserMentionEntity user : this.status.getUserMentionEntities()) {
			USMFStatus.User u = new USMFStatus.User();
			u.name = user.getScreenName();
			u.real_name = user.getName();
			u.id = user.getId();
			status.to_users.add(u);
		}
		if(this.status.getInReplyToScreenName()!= null){
			status.reply_to = new User();
			status.reply_to.name = this.status.getInReplyToScreenName();
			status.reply_to.id = this.status.getInReplyToUserId();

		}
		this.fillAnalysis(status);
	}

	@Override
	public void fromUSMF(USMFStatus status) {
		// TODO Auto-generated method stub

	}

	@Override
	public GeneralJSON instanceFromString(String line) {
		GeneralJSONTweet4jStatus jsonInstance = null;
		try {
			jsonInstance = gson.fromJson(line, GeneralJSONTweet4jStatus.class);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return jsonInstance;
	}

}
