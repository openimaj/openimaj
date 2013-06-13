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
package org.openimaj.image.annotation.evaluation.dataset;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.ml.annotation.Annotated;

class ImageWrapper implements Identifiable {
	private String id;
	private File imageFile;

	public ImageWrapper(String id, File imageFile) {
		this.id = id;
		this.imageFile = imageFile;
	}

	@Override
	public String getID() {
		return id;
	}

	public MBFImage getImage() {
		try {
			return ImageUtilities.readMBF(imageFile);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}

public class CorelAnnotatedImage implements Annotated<ImageWrapper, String>, Identifiable {
	private List<String> annotations;
	private ImageWrapper wrapper;

	public CorelAnnotatedImage(String id, File imageFile, File keywordFile) throws IOException {
		this.wrapper = new ImageWrapper(id, imageFile);

		annotations = FileUtils.readLines(keywordFile);
	}

	@Override
	public ImageWrapper getObject() {
		return wrapper;
	}

	@Override
	public Collection<String> getAnnotations() {
		return annotations;
	}

	@Override
	public String getID() {
		return wrapper.getID();
	}
}
