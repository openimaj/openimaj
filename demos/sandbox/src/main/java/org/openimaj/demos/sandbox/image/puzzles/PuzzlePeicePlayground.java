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
import org.openimaj.image.processing.resize.ResizeProcessor;

public class PuzzlePeicePlayground {
	String dropBoxImagesRoot = "/Users/ss/Dropbox/Camera Uploads/";
	String workspaceRoot = "/Users/ss/Dropbox/Projects/Puzzles";
	String completedLoc = "2013-01-06 16.39.56.jpg";
	String texturedPeicesLoc = "2013-01-06 17.10.53-4.jpg";
	String plainPeicesLoc = "2013-01-06 17.02.40-1.jpg";
	String completedSelectedLoc = "butterflys/box";

	ResizeProcessor rp = new ResizeProcessor(800);
	private MBFImage boardImg;
	private MBFImage texturedImg;
	private MBFImage plainImg;

	public PuzzlePeicePlayground() throws IOException {
		initImages();
	}

	private void initImages() throws IOException {
		this.boardImg = ImageUtilities.readMBF(new File(dropBoxImagesRoot,completedLoc));
//		this.texturedImg = ImageUtilities.readMBF(new File(dropBoxImagesRoot,texturedPeicesLoc));
//		this.plainImg = ImageUtilities.readMBF(new File(dropBoxImagesRoot,plainPeicesLoc));

		boardImg.processInplace(rp);
//		texturedImg.processInplace(rp);
//		plainImg.processInplace(rp);

	}

	public static void main(String[] args) throws IOException {
		PuzzlePeicePlayground playground = new PuzzlePeicePlayground();
		playground.selectBoard();
	}

	private void selectBoard() throws IOException {
		SelectedImage selectedImage = SelectedImage.selectPolygon(boardImg);
	}
}
