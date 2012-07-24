package org.openimaj.picslurper;

import java.net.URL;
import java.util.List;

import org.openimaj.image.MBFImage;

public interface SiteSpecificConsumer {
	public boolean canConsume(URL url);
	public List<MBFImage> consume(URL url);
}
