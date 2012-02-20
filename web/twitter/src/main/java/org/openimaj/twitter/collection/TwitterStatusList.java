package org.openimaj.twitter.collection;

import org.openimaj.io.Writeable;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.util.list.RandomisableList;

public interface TwitterStatusList extends RandomisableList<TwitterStatus>, Writeable {
}
