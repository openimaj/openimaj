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
package org.openimaj.feature;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.io.IOUtils;
import org.openimaj.io.WriteableBinary;

/**
 * A simple wrapper for a feature extractor that caches the extracted feature to
 * disk. If a feature has already been generated for a given object, it will be
 * re-read from disk rather than being re-generated.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <FEATURE>
 *            Type of feature
 * @param <OBJECT>
 *            Type of object
 */
public class DiskCachingFeatureExtractor<FEATURE, OBJECT extends Identifiable>
		implements
		FeatureExtractor<FEATURE, OBJECT>
{
	private static Logger logger = Logger.getLogger(DiskCachingFeatureExtractor.class);

	private File cacheDir;
	private FeatureExtractor<FEATURE, OBJECT> extractor;
	private boolean force;

	/**
	 * Construct the cache in the given directory. There will be one file
	 * created per object. The given extractor will be used to generate the
	 * features.
	 *
	 * @param cacheDir
	 *            the location of the cache
	 * @param extractor
	 *            the feature extractor
	 */
	public DiskCachingFeatureExtractor(File cacheDir, FeatureExtractor<FEATURE, OBJECT> extractor) {
		this(cacheDir, extractor, false);
	}

	/**
	 * Construct the cache in the given directory. There will be one file
	 * created per object. The given extractor will be used to generate the
	 * features. Optionally, all features can be regenerated.
	 *
	 * @param cacheDir
	 *            the location of the cache
	 * @param extractor
	 *            the feature extractor
	 * @param force
	 *            if true, then all features will be regenerated and saved,
	 *            rather than being loaded.
	 */
	public DiskCachingFeatureExtractor(File cacheDir, FeatureExtractor<FEATURE, OBJECT> extractor, boolean force) {
		this.cacheDir = cacheDir;
		this.extractor = extractor;
		this.force = force;

		this.cacheDir.mkdirs();
	}

	@Override
	public FEATURE extractFeature(OBJECT object) {
		final File cachedFeature = new File(cacheDir, object.getID() + ".dat");
		cachedFeature.getParentFile().mkdirs();

		FEATURE feature = null;
		if (!force && cachedFeature.exists()) {
			feature = load(cachedFeature);

			if (feature != null)
				return feature;
		}

		feature = extractor.extractFeature(object);

		try {
			return write(feature, cachedFeature);
		} catch (final IOException e) {
			logger.warn("Caching of the feature for the " + object.getID() + " object was disabled", e);
			return feature;
		}
	}

	private FEATURE write(FEATURE feature, File cachedFeature) throws IOException {
		if (feature instanceof WriteableBinary) {
			IOUtils.writeBinaryFull(cachedFeature, (WriteableBinary) feature);
		} else {
			IOUtils.writeToFile(feature, cachedFeature);
		}

		return feature;
	}

	@SuppressWarnings("unchecked")
	private FEATURE load(File cachedFeature) {
		try {
			return (FEATURE) IOUtils.read(cachedFeature);
		} catch (final Exception e) {
			try {
				return (FEATURE) IOUtils.readFromFile(cachedFeature);
			} catch (final IOException e1) {
				logger.warn("Error reading from cache. Feature will be regenerated.");
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return this.extractor.toString();
	}
}
