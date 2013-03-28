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
package org.openimaj.image.objectdetection.haar;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.Parallel.IntRange;

/**
 * Multi-threaded version of the {@link Detector}. The search algorithm is
 * identical, but the image is separated into multiple vertical stripes for each
 * thread to process independently.
 * <p>
 * <strong>Important note:</strong> This detector is NOT thread-safe due to the
 * fact that {@link StageTreeClassifier}s are not themselves thread-safe. Do not
 * attempt to use it in a multi-threaded environment!
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MultiThreadedDetector extends Detector {
	private ThreadPoolExecutor threadPool;

	/**
	 * Construct the {@link MultiThreadedDetector} with the given parameters.
	 * 
	 * @param cascade
	 *            the cascade or tree of stages.
	 * @param scaleFactor
	 *            the amount to change between scales (multiplicative)
	 * @param smallStep
	 *            the amount to step when there is a hint of detection
	 * @param bigStep
	 *            the amount to step when there is definitely no detection
	 * @param threadPool
	 *            the thread pool. If <code>null</code> the global pool is used.
	 */
	public MultiThreadedDetector(StageTreeClassifier cascade, float scaleFactor, int smallStep, int bigStep,
			ThreadPoolExecutor threadPool)
	{
		super(cascade, scaleFactor, smallStep, bigStep);

		if (threadPool == null)
			threadPool = GlobalExecutorPool.getPool();

		this.threadPool = threadPool;
	}

	/**
	 * Construct the {@link MultiThreadedDetector} with the given tree of stages
	 * and scale factor. The default step sizes are used.
	 * 
	 * @param cascade
	 *            the cascade or tree of stages.
	 * @param scaleFactor
	 *            the amount to change between scales
	 */
	public MultiThreadedDetector(StageTreeClassifier cascade, float scaleFactor) {
		this(cascade, scaleFactor, DEFAULT_SMALL_STEP, DEFAULT_BIG_STEP, null);
	}

	/**
	 * Construct the {@link MultiThreadedDetector} with the given tree of
	 * stages, and the default parameters for step sizes and scale factor.
	 * 
	 * @param cascade
	 *            the cascade or tree of stages.
	 */
	public MultiThreadedDetector(StageTreeClassifier cascade) {
		this(cascade, DEFAULT_SCALE_FACTOR, DEFAULT_SMALL_STEP, DEFAULT_BIG_STEP, null);
	}

	@Override
	protected void detectAtScale(final SummedSqTiltAreaTable sat, final int startX, final int stopX, final int startY,
			final int stopY, final float ystep, final int windowWidth, final int windowHeight,
			final List<Rectangle> results)
	{
		Parallel.forRange(startY, stopY, 1, new Operation<IntRange>() {
			@Override
			public void perform(IntRange range) {
				for (int iy = range.start; iy < range.stop; iy += range.incr) {
					final int y = Math.round(iy * ystep);

					for (int ix = startX, xstep = 0; ix < stopX; ix += xstep) {
						final int x = Math.round(ix * ystep);

						final int result = cascade.classify(sat, x, y);

						if (result > 0) {
							synchronized (results) {
								results.add(new Rectangle(x, y, windowWidth, windowHeight));
							}
						}

						// if there is no hint of detection, then increase the
						// step size
						xstep = result == 0 ? smallStep : bigStep;
					}
				}
			}
		}, threadPool);
	}
}
