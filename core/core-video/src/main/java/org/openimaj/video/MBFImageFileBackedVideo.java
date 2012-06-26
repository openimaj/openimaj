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
package org.openimaj.video;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 * A video backed by a image files on disk. Each image file
 * is a single frame. Images are of the RGB MBFImage type.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MBFImageFileBackedVideo extends FileBackedVideo<MBFImage> {
	/**
	 * Construct the video from the provided files. Assumes a frame rate
	 * of 30 FPS
	 * @param files the image files
	 */
	public MBFImageFileBackedVideo(List<File> files){
		super(files);
	}
	
	/**
	 * Construct the video from the provided files.
	 * @param files the image files
	 * @param fps the frame rate
	 */
	public MBFImageFileBackedVideo(List<File> files, double fps) {
		super(files, fps);
	}
	
	/**
	 * Construct videos from numbered files using the given format string and
	 * indices. The format string should contain a single %d substitution.
	 *  
	 * @param filenameFormat format string
	 * @param start starting index (inclusive)
	 * @param stop stop index (exclusive)
	 */
	public MBFImageFileBackedVideo(String filenameFormat, int start, int stop) {
		super(filenameFormat, start, stop);
	}

	@Override
	protected MBFImage loadImage(File f) throws IOException {
		return ImageUtilities.readMBF(f);
	}
}