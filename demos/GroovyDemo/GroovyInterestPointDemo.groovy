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
@GrabResolver(name='openimaj-releases', root='http://maven.openimaj.org/')
@Grab('org.openimaj:core-image:1.1-SNAPSHOT')
import org.openimaj.io.*
import org.openimaj.image.*
import org.openimaj.image.colour.*
import org.openimaj.math.geometry.shape.*

@Grab('org.openimaj:image-local-features:1.1-SNAPSHOT')
import org.openimaj.image.feature.local.interest.*

//Load an image
img = ImageUtilities.readMBF( args.length>0 ? new File(args[0]) : getClass().getResource("/org/openimaj/OpenIMAJ.png"))

//make a grey version
gimg = Transforms.calculateIntensityNTSC(img)

//set the scales (std.dev of Gaussian)
float integrationScale = 2.5f
float differentiationScale = 0.6f * integrationScale

//calculate the variance
float integrationScaleVar = integrationScale**2
float differentiationScaleVar = differentiationScale**2

//set up a list of detectors
ipds = [
    [ipd: new HarrisIPD(differentiationScaleVar, integrationScaleVar), colour:RGBColour.RED],
    [ipd: new HessianIPD(differentiationScaleVar, integrationScaleVar), colour:RGBColour.MAGENTA],
    [ipd: new LaplaceIPD(differentiationScaleVar, integrationScaleVar), colour:RGBColour.GREEN],
]

//loop through detectors and draw 100 best points found
ipds.each{rec ->
    rec.ipd.findInterestPoints(gimg)
    img.drawPoints(rec.ipd.getInterestPoints(100), rec.colour, 3)
}
        
//display the result
DisplayUtilities.display(img)