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
package org.openimaj.hadoop.sequencefile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.Metadata;

public class MetadataConfiguration {
	private static final String META_PREFIX = "uk.ac.soton.ecs.jsh2.sequencefiles.metadata.";
	private static final String META_KEYS = META_PREFIX + "__metadataKeys__"; 

	//Common keys
	public static final String UUID_KEY = "UUID";
	public static final String COMMENT_KEY = "Comment";
	public static final String CONTENT_TYPE_KEY = "ContentType";

	public static Metadata getMetadata(Configuration conf) {
		Metadata metadata = new Metadata();

		String [] keys = conf.getStrings(META_KEYS);

		if (keys != null) {
			for (String key : keys) {
				String value = conf.get(META_PREFIX + key);

				if (value != null)
					metadata.set(new Text(key), new Text(value));
			}
		}

		return metadata;
	}

	public static void setMetadata(Map<String, String> metadata, Configuration conf) {
		for (Entry<String, String> entry : metadata.entrySet()) {
			conf.set(META_PREFIX + entry.getKey(), entry.getValue());
		}

		List<String> keys = new ArrayList<String>();
		if (conf.getStringCollection(META_KEYS) != null)
			keys.addAll(conf.getStringCollection(META_KEYS));

		for (String key : metadata.keySet()) {
			keys.add(key);
		}

		conf.setStrings(META_KEYS, keys.toArray(new String[keys.size()]));
	}

}
