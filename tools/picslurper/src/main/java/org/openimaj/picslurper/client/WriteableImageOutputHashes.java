package org.openimaj.picslurper.client;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.picslurper.output.WriteableImageOutput;

public class WriteableImageOutputHashes {
	WriteableImageOutput image;
	List<int[]> hashes;

	public WriteableImageOutputHashes(WriteableImageOutput instance) {
		this.image = instance;
		hashes = new ArrayList<int[]>();
	}
}