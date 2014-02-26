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
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;

/**
 * A video backed by a image files on disk. Each image file is a single frame.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the image type of the frames
 */
public abstract class FileBackedVideo<T extends Image<?, T>> extends Video<T> {
	private List<File> files;
	private T heldCurrentFrame;
	private int heldCurrentFrameIndex = -1;
	private boolean loop;
	private double fps = 30;

	/**
	 * Construct the video from the provided files. Assumes a frame rate of 30
	 * FPS
	 * 
	 * @param files
	 *            the image files
	 */
	public FileBackedVideo(List<File> files) {
		this.files = files;
		this.fps = 30;
		this.loop = false;
	}

	/**
	 * Construct the video from the provided files.
	 * 
	 * @param files
	 *            the image files
	 * @param fps
	 *            the frame rate
	 */
	public FileBackedVideo(List<File> files, double fps) {
		this.files = files;
		this.fps = fps;
		this.loop = false;
	}

	/**
	 * Construct videos from numbered files using the given format string and
	 * indices. The format string should contain a single %d substitution.
	 * 
	 * @param filenameFormat
	 *            format string
	 * @param start
	 *            starting index (inclusive)
	 * @param stop
	 *            stop index (exclusive)
	 */
	public FileBackedVideo(String filenameFormat, int start, int stop) {
		this(getFilesList(filenameFormat, start, stop));
	}

	@Override
	public synchronized T getNextFrame() {
		final T frame = getCurrentFrame();
		incrementFrame();
		return frame;
	}

	private void incrementFrame() {
		if (this.currentFrame + 1 < this.files.size() || loop) {
			this.currentFrame++;
		}
	}

	@Override
	public boolean hasNextFrame() {
		return loop || this.getCurrentFrameIndex() + 1 < this.files.size();
	}

	@Override
	public T getCurrentFrame() {
		try {
			if (this.currentFrame != heldCurrentFrameIndex) {
				this.heldCurrentFrame = loadImage(files.get(currentFrame % this.files.size()));
				this.heldCurrentFrameIndex = currentFrame;
			}
		} catch (final IOException e) {
			this.heldCurrentFrameIndex = currentFrame;
			this.heldCurrentFrame = null;
		}
		return this.heldCurrentFrame;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.Video#getWidth()
	 */
	@Override
	public int getWidth()
	{
		return getCurrentFrame().getWidth();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.Video#getHeight()
	 */
	@Override
	public int getHeight()
	{
		return getCurrentFrame().getHeight();
	}

	protected abstract T loadImage(File f) throws IOException;

	@Override
	public long countFrames() {
		return this.files.size();
	}

	@Override
	public void reset()
	{
		this.currentFrame = 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.Video#getTimeStamp()
	 */
	@Override
	public long getTimeStamp()
	{
		return (long) (getCurrentFrameIndex() / this.fps) * 1000;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.video.Video#getFPS()
	 */
	@Override
	public double getFPS()
	{
		return fps;
	}

	/**
	 * Get a list of numbered files using the given format string and indices.
	 * The format string should contain a single %d substitution.
	 * 
	 * @param filenameFormat
	 *            format string
	 * @param start
	 *            starting index (inclusive)
	 * @param stop
	 *            stop index (exclusive)
	 * 
	 * @return list of files
	 */
	public static List<File> getFilesList(String filenameFormat, int start, int stop) {
		final List<File> files = new ArrayList<File>();

		for (int i = start; i < stop; i++) {
			files.add(new File(String.format(filenameFormat, i)));
		}

		return files;
	}
}
