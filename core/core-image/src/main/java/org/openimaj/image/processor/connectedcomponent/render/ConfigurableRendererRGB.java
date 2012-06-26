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
package org.openimaj.image.processor.connectedcomponent.render;

import java.util.EnumMap;
import java.util.EnumSet;

import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.math.geometry.shape.Polygon;

/**
 *	This renderer encapsulates various other renderers in an easy to
 *	configure class, allowing multiple renderers to be called in one go.
 *	This class is designed for multiband RGB {@link MBFImage}s. 
 * 	
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 */
public class ConfigurableRendererRGB implements ConnectedComponentProcessor 
{
	/** The image to draw into */
	protected MBFImage image;

	/** The options to draw with */
	protected EnumSet<ConfigurableRenderOptions> options;

	/** The colour to draw the various options in */
	EnumMap<ConfigurableRenderOptions, Float[]> colours = new EnumMap<ConfigurableRenderOptions, Float[]>(ConfigurableRenderOptions.class);
	
	/**
	 * 	Protected constructor that takes a set of options to draw.
	 * 
	 *  @param options The options to draw
	 */
	protected ConfigurableRendererRGB(EnumSet<ConfigurableRenderOptions> options) {
		this.options = options;
		
		colours.put(ConfigurableRenderOptions.AXIS, new Float[] {0.8F, 0.0F, 0.0F});
		colours.put(ConfigurableRenderOptions.BLOB, new Float[] {0.5F, 0.5F, 0.5F});
		colours.put(ConfigurableRenderOptions.BORDER, new Float[] {0.7F, 0.0F, 0.0F});
		colours.put(ConfigurableRenderOptions.CENTROID, new Float[] {0.7F, 0.0F, 0.0F});
		colours.put(ConfigurableRenderOptions.CH_AXIS, new Float[] {0.9F, 0.0F, 0.0F});
		colours.put(ConfigurableRenderOptions.CH_BLOB, new Float[] {0.2F, 0.2F, 0.2F});
		colours.put(ConfigurableRenderOptions.CH_BORDER, new Float[] {1.0F, 0.0F, 0.0F});
		colours.put(ConfigurableRenderOptions.CH_CENTROID, new Float[] {1.0F, 0.0F, 0.0F});
	}
	
	/**
	 * 	Constructor that takes an image to draw into and a set of options to draw.
	 * 
	 *  @param image The image to draw into
	 *  @param options The options to draw
	 */
	public ConfigurableRendererRGB(MBFImage image, EnumSet<ConfigurableRenderOptions> options) {
		this(options);
		
		this.image = image;
	}
	
	/**
	 * 	Constructor that creates an image to draw into and takes a set of options
	 * 	to draw.
	 * 
	 *  @param rows The height of the image to create
	 *  @param cols The width of the image to create
	 *  @param options The set of options to draw
	 */
	public ConfigurableRendererRGB(int rows, int cols, EnumSet<ConfigurableRenderOptions> options) {
		this(options);
		
		image = new MBFImage(cols, rows, 3);
	}
	
	/**
	 * 	Get the rendered image.
	 * 
	 *  @return The rendered image.
	 */
	public MBFImage getImage() {
		return image;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor#process(org.openimaj.image.pixel.ConnectedComponent)
	 */
	@Override
	public void process(ConnectedComponent cc) {
		Polygon ch = cc.calculateConvexHull();
		ConnectedComponent chcc = new ConnectedComponent(ch);
		
		if (options.contains(ConfigurableRenderOptions.CH_BLOB))
			chcc.process(new BlobRenderer<Float[]>(image, colours.get(ConfigurableRenderOptions.CH_BLOB)));
		
		if (options.contains(ConfigurableRenderOptions.BLOB))
			cc.process(new BlobRenderer<Float[]>(image, colours.get(ConfigurableRenderOptions.BLOB)));
		
		if (options.contains(ConfigurableRenderOptions.CH_BORDER)) 
			image.createRenderer().drawPolygon(ch, colours.get(ConfigurableRenderOptions.CH_BORDER));
		
		if (options.contains(ConfigurableRenderOptions.BORDER)) 
			cc.process(new BorderRenderer<Float[]>(image, colours.get(ConfigurableRenderOptions.BLOB), ConnectMode.CONNECT_8));
		
		if (options.contains(ConfigurableRenderOptions.CH_CENTROID)) 
			chcc.process(new CentroidRenderer<Float[]>(image, colours.get(ConfigurableRenderOptions.CH_CENTROID)));
		
		if (options.contains(ConfigurableRenderOptions.CENTROID))
			cc.process(new CentroidRenderer<Float[]>(image, colours.get(ConfigurableRenderOptions.CENTROID)));

		if (options.contains(ConfigurableRenderOptions.CH_AXIS))
			chcc.process(new AxisRenderer<Float[]>(image, colours.get(ConfigurableRenderOptions.CH_AXIS)));
		
		if (options.contains(ConfigurableRenderOptions.AXIS))
			cc.process(new AxisRenderer<Float[]>(image, colours.get(ConfigurableRenderOptions.AXIS)));
	}
	
	/**
	 * 	Set the colour of a specific option to override the default.
	 * 
	 *  @param r The option to set the colour for
	 *  @param colour The colour to draw that option in.
	 */
	public void setColour(ConfigurableRenderOptions r, Float[] colour) {
		colours.put(r, colour);
	}
	
	/**
	 * 	Get the colour that a specific option will be drawn in. 
	 * 
	 *  @param r The option to get the colour of.
	 *  @return The colour the option will be drawn in.
	 */
	public Float[] getColour(ConfigurableRenderOptions r) {
		return colours.get(r);
	}
}
