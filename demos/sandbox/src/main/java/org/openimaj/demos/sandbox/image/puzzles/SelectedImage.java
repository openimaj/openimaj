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
package org.openimaj.demos.sandbox.image.puzzles;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Polygon;

public class SelectedImage {
	private static final String IMAGE_OUTPUT = "img.png";
	private static final String SELECTION_OUTPUT = "selection";
	private Polygon poly;
	private MBFImage img;
	public SelectedImage(MBFImage img, Polygon polygon) {
		this.img = img;
		this.poly = polygon;
	}
	public static SelectedImage selectPolygon(MBFImage boardImg) throws IOException {
		ImageSelectionFrame isf = new ImageSelectionFrame(boardImg);
		try {
			isf.waitForPolygonSelection();
			return isf.getSelectedImage();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	public void read(File f) throws IOException{
		File imgOutout = new File(f,IMAGE_OUTPUT);
		File selectionOutput = new File(f,SELECTION_OUTPUT);

		this.img = ImageUtilities.readMBF(imgOutout);
		this.poly = IOUtils.readFromFile(selectionOutput);
	}

	/**
	 * @param f
	 * @throws IOException
	 */
	public void write(File f) throws IOException{
		if(!f.exists()){
			f.mkdirs();
		}
		else{
			throw new IOException("Location exists");
		}
		File imgOutout = new File(f,IMAGE_OUTPUT);
		File selectionOutput = new File(f,SELECTION_OUTPUT);

		ImageUtilities.write(img, imgOutout);
		IOUtils.writeToFile(this.poly, selectionOutput);
	}
}
