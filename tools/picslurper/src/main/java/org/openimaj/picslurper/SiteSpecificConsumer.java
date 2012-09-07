package org.openimaj.picslurper;

import java.net.URL;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.util.pair.IndependentPair;

public interface SiteSpecificConsumer {
	public boolean canConsume(URL url);
	public List<IndependentPair<URL, MBFImage>> consume(URL url);
}
