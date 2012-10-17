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
package org.openimaj.image.neardups.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;


public class CompressSimulation extends Simulation {
	protected float minCompression = 0.1f;
	protected float maxCompression = 1f;
	
	public CompressSimulation(int seed) {
		super(seed);
	}
	
	public CompressSimulation(int seed, float minCompression, float maxCompression) {
		super(seed);
		this.maxCompression = maxCompression;
		this.minCompression = minCompression;
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		try {
			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			
			float compressionFactor = this.randomFloatInRange(minCompression, maxCompression);
			
			iwp.setCompressionQuality(compressionFactor);

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			writer.setOutput(new MemoryCacheImageOutputStream(output));
			
			IIOImage image = new IIOImage(ImageUtilities.createBufferedImage(input), null, null);
			writer.write(null, image, iwp);
			
			return ImageUtilities.readMBF(new ByteArrayInputStream(output.toByteArray()));
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
