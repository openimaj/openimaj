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
