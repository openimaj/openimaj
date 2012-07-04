package org.openimaj.tools.twitter.options;

import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;

/**
 * The social media status to be read in (useful for tools which control reading)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum StatusType {
	/**
	 * the twitter JSON status type
	 */
	TWITTER {
		@Override
		public Class<? extends GeneralJSON> type() {
			return GeneralJSONTwitter.class;
		}
	},
	/**
	 * the USMF json status type
	 */
	USMF {
		@Override
		public Class<? extends GeneralJSON> type() {
			return USMFStatus.class;
		}
	};
	/**
	 * @return the status type class which can instantiate USMF instances
	 */
	public abstract Class<? extends GeneralJSON> type();
}
