/**
 * Copyright 2011 The University of Southampton, Yahoo Inc., and the
 * individual contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openimaj.util.auth.web;

import org.openimaj.util.api.auth.Parameter;
import org.openimaj.util.api.auth.Token;

/**
 * API token for tumblr for methods using the api_key parameter
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Token(name = "Tumblr API", url = "http://www.tumblr.com/oauth/apps")
public class TumblrAPIToken {
	/**
	 * The api key
	 */
	@Parameter(name = "API Key")
	public String apikey;
}