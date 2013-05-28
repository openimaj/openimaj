package org.openimaj.demos.irc;

import java.io.IOException;

import org.openimaj.demos.irc.WikipediaEditStreamingDataset.WikipediaEdit;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.text.geo.WorldPlace;
import org.openimaj.text.geo.WorldPolygons;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.function.context.ContextExtractionStrategy;
import org.openimaj.util.function.context.ContextFunction;
import org.openimaj.util.function.context.ContextGenerator;
import org.openimaj.util.function.context.ContextPredicate;
import org.openimaj.video.VideoDisplay;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WikipediaChangesGeo {
	private static final class NotNull implements Predicate<Object> {
		@Override
		public boolean test(Object object) {
			return object!=null;
		}
	}

	public static void main(String[] args) throws IOException {
		WorldVis wordVis = new WorldVis(1800,600);
		VideoDisplay.createVideoDisplay(wordVis);
		
		new WikipediaEditStreamingDataset("en")
		.map(new ContextGenerator<WikipediaEdit>("wikiedit"))
		.map(
			new ContextFunction<String, FreeGeoIPLocation>(new ContextExtractionStrategy<String>() {
				@Override
				public String extract(Context c) {
					WikipediaEdit edit = ((WikipediaEdit)c.get("wikiedit"));
					if(edit.anon)
						return edit.user;
					else
						return null;
				}
			},
			"geolocation",
			new IPAsGeolocation())
		)
		.filter(new ContextPredicate<Object>("geolocation", new NotNull()))
		.forEach(new Operation<Context>() {

			@Override
			public void perform(Context object) {
				FreeGeoIPLocation geoip = object.getTyped("geolocation");
				System.out.println(geoip.country_code);
			}
		});
	}
}
