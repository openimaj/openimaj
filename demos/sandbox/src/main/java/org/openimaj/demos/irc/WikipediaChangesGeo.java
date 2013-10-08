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
package org.openimaj.demos.irc;

import java.io.IOException;

import org.openimaj.stream.provider.WikipediaEditsDataset;
import org.openimaj.stream.provider.WikipediaEditsDataset.WikipediaEdit;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.function.context.ContextExtractor;
import org.openimaj.util.function.context.ContextFunctionAdaptor;
import org.openimaj.util.function.context.ContextGenerator;
import org.openimaj.util.function.context.ContextPredicateAdaptor;
import org.openimaj.util.function.context.KeyContextInsertor;
import org.openimaj.video.VideoDisplay;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WikipediaChangesGeo {
	private static final class NotNull implements Predicate<Object> {
		@Override
		public boolean test(Object object) {
			return object != null;
		}
	}

	public static void main(String[] args) throws IOException {
		final WorldVis wordVis = new WorldVis(1800, 600);
		VideoDisplay.createVideoDisplay(wordVis);

		new WikipediaEditsDataset("en")
				.map(new ContextGenerator<WikipediaEdit>("wikiedit"))
				.map(
						new ContextFunctionAdaptor<String, FreeGeoIPLocation>(new IPAsGeolocation(),
								new ContextExtractor<String>() {
									@Override
									public String extract(Context c) {
										final WikipediaEdit edit = ((WikipediaEdit) c.get("wikiedit"));
										if (edit.anon)
											return edit.user;
										else
											return null;
									}
								},
								new KeyContextInsertor<FreeGeoIPLocation>("geolocation")
						)
				)
				.filter(new ContextPredicateAdaptor<Object>(new NotNull(), "geolocation"))
				.forEach(new Operation<Context>() {
					@Override
					public void perform(Context object) {
						final FreeGeoIPLocation geoip = object.getTyped("geolocation");
						wordVis.activate(geoip);
					}
				});
	}
}
