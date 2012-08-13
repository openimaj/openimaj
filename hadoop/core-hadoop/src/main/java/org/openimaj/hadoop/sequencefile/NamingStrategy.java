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

import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.semanticdesktop.aperture.mime.identifier.magic.MagicMimeTypeIdentifier;

/**
 * Strategies for naming files extracted from SequenceFiles.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum NamingStrategy {
	/**
	 * Use the key value as the filename
	 */
	KEY {
		@Override
		public <K, V> String getNameInternal(K key, V value, ExtractionState state) {
			return key.toString();
		}
	},
	/**
	 * Number the files sequentially
	 */
	NUMERICAL {
		@Override
		public <K, V> String getNameInternal(K key, V value, ExtractionState state) {
			return "" + state.getCount();
		}
	};

	protected abstract <K, V> String getNameInternal(K key, V value, ExtractionState state);

	/**
	 * Generate the filename for the given record (key-value pair). If the
	 * addExtension flag is true AND the value is a {@link BytesWritable}, then
	 * a mime-type sniffing process takes place in order to "guess" an extension
	 * which will be added to the end of the generated filename.
	 * 
	 * @param <K>
	 *            key type
	 * @param <V>
	 *            value type
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param state
	 *            the extraction state
	 * @param addExtension
	 *            should the file extension be guessed and added automatically
	 * @return the filename
	 */
	public <K, V> String getName(K key, V value, ExtractionState state, boolean addExtension) {
		String name = this.getNameInternal(key, value, state);

		if (addExtension && value instanceof BytesWritable) {
			final MagicMimeTypeIdentifier match = new MagicMimeTypeIdentifier();

			final String mime = match.identify(((BytesWritable) value).getBytes(), key.toString(), null);

			@SuppressWarnings("unchecked")
			final List<String> exts = match.getExtensionsFor(mime);

			if (exts.size() > 0)
				name += "." + exts.get(0);
		}

		return name;
	}
}
