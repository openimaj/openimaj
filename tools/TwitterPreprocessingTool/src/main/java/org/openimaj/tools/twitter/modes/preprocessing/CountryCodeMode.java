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
package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.utils.Twitter4jUtil;

import twitter4j.GeoQuery;
import twitter4j.Place;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Use the twokeniser to tokenise tweets
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CountryCodeMode extends TwitterPreprocessingMode<String> {

	Logger logger = Logger.getLogger(CountryCodeMode.class);
	final static String COUNTRY_CODE = "country_code";
	private static final long GEONAMES_ENFORCED_WAIT_TIME = (60 * 60 * 1000) / 2000;
	private static final long TWITTER_ENFORCED_WAIT_TIME = (60 * 60 * 1000) / 100;
	private static final long TWITTER_DEFAULT_ERROR_BUT_NO_WAIT_TIME = 5000;

	private Twitter twitter;
	private EnforcedWait geonamesLastCall = new EnforcedWait(GEONAMES_ENFORCED_WAIT_TIME);
	private EnforcedWait twitterLastCall = new EnforcedWait(TWITTER_ENFORCED_WAIT_TIME);

	/**
	 *
	 */
	public CountryCodeMode()  {
		this.twitter = Twitter4jUtil.create();
	}
	class EnforcedWait{
		long currentWait;
		private long minimumWait;
		private long lastCall;

		public EnforcedWait(long minWait) {
			this.minimumWait = minWait;
		}

		public void enforce() throws EnforcedWaitException{
			long timeSinceLastCall = System.currentTimeMillis() - lastCall;
			if(timeSinceLastCall < Math.max(currentWait, minimumWait))
			{
				currentWait = Math.max(currentWait, minimumWait);
				throw new EnforcedWaitException(this);
			}
			else{
				this.lastCall = System.currentTimeMillis();
			}
		}
	}
	class EnforcedWaitException extends Exception{
		private EnforcedWait wait;

		public EnforcedWaitException(EnforcedWait enforcedWait) {
			this.wait = enforcedWait;
		}
	}
	@Override
	public String process(USMFStatus stat)  {
		while(true){
			long waitTime = Long.MAX_VALUE;
			// Try using the twitter API first!
			if(stat.country_code!= null ) {
				logger .debug("Country code from status!");
				return stat.country_code;
			}
			try{
				if(stat.location!=null){
					String searchWithTwitter = searchWithTwitter(stat.location);
					logger.debug("country code from status location twitter places");
					return searchWithTwitter;
				}
				else if(stat.user.location!=null){
					String searchWithTwitter = searchWithTwitter(stat.user.location);
					logger.debug("country code from user location twitter places");
					return searchWithTwitter;
				}
			}
			catch(EnforcedWaitException e){
				waitTime = e.wait.currentWait;
			}
			// now try geonames (which we have to wait for)
			try{
				if(stat.geo == null){
					if(stat.location != null){
						String searchByString = searchByString(stat.location);
						logger.debug("country code from geonames search");
						return searchByString;
					}else{
						if(stat.user.geo!=null){
							String countryCodeByGeo = countryCodeByGeo(stat.user.geo);
							logger.debug("country code from geonames user geo");
							return countryCodeByGeo;
						}
						else if(stat.user.location!=null){
							String searchByString = searchByString(stat.user.location);
							logger.debug("country code from geonames user location");
							return searchByString;
						}
					}
				}
				else{
					String countryCodeByGeo = countryCodeByGeo(stat.geo);
					logger.debug("country code from geonames status geo");
					return countryCodeByGeo;
				}
			}catch(EnforcedWaitException e){
				waitTime = Math.min(waitTime, e.wait.currentWait);
			}
			if(waitTime == Long.MAX_VALUE){
				// ONLY IN THIS SITUATION RETURN, it means both APIs were called but none returned
				logger.debug("API called, no response!");
				return "";
			}else{
				try {
					logger.debug("APIs busy, waiting: " + waitTime);
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
				}
			}

		}
	}

	private String searchWithTwitter(String location) throws EnforcedWaitException {
		while(true){
			try {
				twitterLastCall.enforce();
				ResponseList<Place> res = this.twitter.searchPlaces(new GeoQuery(location));
				if(res.size() > 0)
					return res.get(0).getCountryCode();
				else
					return null;
			} catch (TwitterException e) {
				this.twitterLastCall.currentWait = Twitter4jUtil.handleTwitterException(e, TWITTER_DEFAULT_ERROR_BUT_NO_WAIT_TIME);
				throw new EnforcedWaitException(this.twitterLastCall);
			}
		}
	}

	@Override
	public String getAnalysisKey(){
		return CountryCodeMode.COUNTRY_CODE;
	}

	private String searchByString(String location) throws EnforcedWaitException {
		geonamesLastCall.enforce();
		ToponymSearchResult x;
		try {
			ToponymSearchCriteria search = new ToponymSearchCriteria();
			search.setQ(location);
			x = WebService.search(search);
			if(x.getTotalResultsCount() == 0)
			{
				return "";
			}
			return x.getToponyms()
					.get(0)
					.getCountryCode();
		} catch (Exception e) {
			return "";
		}
	}



	private String countryCodeByGeo(double[] geo) throws EnforcedWaitException {
		try {
			geonamesLastCall.enforce();
			return WebService.countryCode(geo[0], geo[1]);
		} catch (IOException e) {
			return null;
		}
	}
}
