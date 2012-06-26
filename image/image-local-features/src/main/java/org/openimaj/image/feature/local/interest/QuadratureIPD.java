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
package org.openimaj.image.feature.local.interest;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FImageConvolveSeparable;
import org.openimaj.image.processor.PixelProcessor;


public class QuadratureIPD extends AbstractStructureTensorIPD {
	public QuadratureIPD(float detectionScale, float integrationScale) {
		super(detectionScale, integrationScale);
	}

	@Override
	public FImage createInterestPointMap() {
		float s2 = detectionScale * detectionScale;

		int filtsize = (int) Math.max(3,Math.round(5*detectionScale));

		float [] g = new float[2*filtsize + 1];
		float [] f1 = new float[2*filtsize + 1];
		float [] f2 = new float[2*filtsize + 1];
		float [] mf2 = new float[2*filtsize + 1];
		float [] f3 = new float[2*filtsize + 1];
		float [] mf3 = new float[2*filtsize + 1];
		float [] f4 = new float[2*filtsize + 1];

		for (int i=0, t=-filtsize; t<=filtsize; t++, i++) {
			g[i] = (float) (Math.exp(-(t*t)/(2*s2))/Math.sqrt(2*Math.PI*s2));

			f1[i] = g[i] * ((t*t)/s2-1)/s2;
			f2[i] = g[i] * t/s2;
			mf2[i] = -f2[i];
			f3[i] = (float) (g[i] * (3.0-2.0/3.0*t*t/s2)*t/Math.sqrt(Math.PI)/Math.sqrt(s2)/s2);
			mf3[i] = f3[i];
			f4[i] = (float) (g[i] * (1.0-2.0/3.0*t*t/s2)/Math.sqrt(Math.PI)/Math.sqrt(s2));
		}

		FImage e1 = this.originalImage.process(new FImageConvolveSeparable(f3, g));
		FImage e2 = this.originalImage.process(new FImageConvolveSeparable(f4, mf2));
		FImage e3 = this.originalImage.process(new FImageConvolveSeparable(f2, f4));
		FImage e4 = this.originalImage.process(new FImageConvolveSeparable(g, mf3));

		FImage gx = e1.addInplace(e3).multiplyInplace(0.75f);
		FImage gy = e2.addInplace(e4).multiplyInplace(0.75f);

		FImage hxx = this.originalImage.process(new FImageConvolveSeparable(f1, g));
		FImage hxy = this.originalImage.process(new FImageConvolveSeparable(f2, mf2));
		FImage hyy = this.originalImage.process(new FImageConvolveSeparable(g, f1));

		FImage b11 = gx.multiply(gx).add(hxx.multiplyInplace(hxx));
		FImage b12 = gx.multiply(gy).add(hxy.multiplyInplace(hxy));
		FImage b22 = gy.multiply(gy).add(hyy.multiplyInplace(hyy));

		FImage ebound = b11.add(b22);
		FImage b11b22 = b11.subtractInplace(b22);
		FImage eedge = b11b22.multiplyInplace(b11b22).add(b12.multiplyInplace(b12).multiplyInplace(4f)).processInplace(new PixelProcessor<Float>() {
			@Override
			public Float processPixel(Float pixel) {
				return (float) Math.sqrt(pixel);
			}});
		FImage cimg = ebound.subtractInplace(eedge).processInplace(new PixelProcessor<Float>() {
			@Override
			public Float processPixel(Float pixel) {
				return -pixel;
			}}); 

		return cimg;
	}

	@Override
	public QuadratureIPD clone() {
		return (QuadratureIPD) super.clone();
	}
}
