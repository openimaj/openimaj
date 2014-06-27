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
/**
 * 
 */
package org.openimaj.tools.ocr;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.general.GeneralFontRenderer;
import org.openimaj.image.typography.general.GeneralFontStyle;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 *	Class that will generate images containing a rendering of a String in
 *	a random font that has been randomly affected - the idea is to simulate
 *	real-world images in a controlled way for training or testing OCR.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Aug 2011
 *	
 *
 *	@param <Q> The pixel type
 *	@param <I> The concrete image type
 */
public class FontSimulator<Q,I extends Image<Q,I>>
{
	/**
	 *	This is an interface for objects that are interested in listening
	 *	for when the {@link FontSimulator} creates an image during one of its
	 *	runs.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 19 Aug 2011
	 *	
	 *  @param <I> @param <I> Type of {@link Image}
	 */
	public interface FontSimListener<I>
	{
		/**
		 * 	Called when an image is created during one of its runs.
		 *	@param img The image containing the rendering of the text string
		 */
		public void imageCreated( I img );
	}

	/** The text string to draw on each simulation */
	private String textString = null;

	/** The number of characters being drawn */
	private int nChars = 0;

	/**
	 * The amount of size jitter to impose on the simulation.
	 * This value gives the number of points either side of the standard
	 * point size by which the size may be altered. i.e. if this value was
	 * 10 and the standard point size 72, the size may randomly alter between
	 * 62 and 82.
	 */
	private double sizeJitter = 0;

	/**
	 * The amount of angle jitter to impose on the simulation (in degrees).
	 * This value gives the maximum angle that may be applied to a transform
	 * of the font in either direction (clockwise or anti-clockwise). i.e.
	 * if this is given the value 45, the text may be rotated -45 to +45
	 * degrees (around its centre point).
	 */
	private double angleJitter = 0;

	/**
	 * The amount of shear jitter to impose on the simulation (in degrees)
	 * The value of this is the maximum angle by which the font may be randomly
	 * sheared in either direction. So if this variable is 35 degrees, the shear
	 * may randomly alter between -35 and +35 degrees.
	 */
	private double shearJitter = 0;

	/**
	 * The amount of perspective jitter to impose on the simulation.
	 * If this value is not zero, perspective is randomly added to the render.
	 * This is done by distorting the rectangle to a parallelogram. The side
	 * which is altered is chosen randomly and is altered randomly up to a
	 * maximum percentage reduction given by this variable. So if this variable
	 * is 0.5 (50%) then one side of the image will end up at a minimum size
	 * of 50% of its original size. The image is always shrunk.
	 */
	private double perspectiveJitter = 0;

	/** Whether to randomise the type of the font (plain,bold,italic) */
	private boolean typeJitter = false;

	/** A list of font family names to avoid (perhaps they contain symbols only? */
	private List<String> fontsToAvoid = new ArrayList<String>();

	/** The font size to draw the text in */
	private int fontPointSize = 18;

	/** The amount of padding (in pixels) to add around the edge of the render */
	private int padding = 5;

	/**
	 * 	Create a font simulator that will output the given text string.
	 *	@param textString The text string to draw
	 */
	public FontSimulator( final String textString )
	{
		this.setTextString( textString );
	}

	/**
	 * 	Make a number of runs of creating text renders
	 * 
	 *	@param nRuns The number of runs to make
	 *	@param fsl The listener that will receive the images
	 *	@param imgExample An example of the type of image <I>
	 */
	public void makeRuns( final int nRuns, final FontSimListener<I> fsl, final I imgExample )
	{
		for( int i = 0; i < nRuns; i++ )
		{
			fsl.imageCreated( this.generate( imgExample ) );
		}
	}

	/**
	 * 	Generates a render of the text string in some random font with some
	 * 	random transform.  The input image is not affected or used - it is only
	 * 	used as a seed for generating new instances of that type of image. A new
	 * 	image of the same type is returned.
	 * 
	 *	@param imgExample An example of the image to which to draw the text.
	 *	@return An image containing the rendered text
	 */
	public I generate( final I imgExample )
	{
		// Get a random font from the system
		GeneralFont font = this.pickRandomFont();
		font = this.getJitteredFont( font );

		// Create an image of the right size to fit the text into.
		// To do that we must create an instance of an image renderer
		// so that we can create a FontStyle. With the style we can create
		// a FontRenderer which can give us the bounds of the text to draw.
		// From that we can create an image into which the text will fit.
		final ImageRenderer<Q, I> r = imgExample.createRenderer();

		GeneralFontStyle<Q> gfs =
				new GeneralFontStyle<Q>( font, r, false );
		gfs = this.getJitteredFontStyle( gfs );

		final GeneralFontRenderer<Q> gfr = new GeneralFontRenderer<Q>();
		final Rectangle b = gfr.getSize( this.textString, gfs );

		// Create an image into which the text will fit.
		I img = imgExample.newInstance(
				(int)b.width + this.padding*2,
				(int)b.height + this.padding*2 );

		// Draw the characters to the image
		img.drawText( this.textString, this.padding, (int)(this.padding + b.height), gfs );

		// Transform the image
		final Matrix transform = this.getJitterTransform( img );
		img = ProjectionProcessor.project( img, transform );

		return img;
	}

	/**
	 * 	Picks a random font from your system and returns it. Checks
	 * 	it against the fontsToAvoid list and will not return a font
	 * 	that is on that list. If a font cannot be returned, the method
	 * 	will return null. The returned font will have plain type and
	 * 	a size of 18 point.
	 * 
	 *	@return A random Font from the system.
	 */
	public GeneralFont pickRandomFont()
	{
		final List<String> fontNames = new ArrayList<String>();
		final GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for( final String font : e.getAvailableFontFamilyNames() )
			if( !this.fontsToAvoid.contains( font ) )
				fontNames.add( font );

		if( fontNames.size() == 0 )
			return null;

		final int r = (int)(Math.random() * fontNames.size());
		return new GeneralFont( fontNames.get(r),
				java.awt.Font.PLAIN );
	}

	/**
	 * 	Returns a font that is somewhat randomised from the
	 * 	initial font. Side-affects the incoming font and returns it.
	 * 
	 *	@param font The font to jitter
	 *	@return The incoming font altered by jitter specification
	 */
	public GeneralFont getJitteredFont( final GeneralFont font )
	{
		if( this.isTypeJitter() )
		{
			final double r = Math.random();
			if( r > 0.3 )
				if( r > 0.6 )
					font.setType( Font.PLAIN );
				else	font.setType( Font.BOLD );
			else		font.setType( Font.ITALIC );
		}

		return font;
	}

	/**
	 * 	Returns an affine transform matrix based on the jitter
	 * 	specifications.
	 * 
	 *  @param img image to get bounds from
	 * 
	 *	@return An affine transform matrix
	 */
	public Matrix getJitterTransform( final I img )
	{
		Matrix m = Matrix.identity( 3, 3 );

		if( this.angleJitter > 0 )
		{
			final double r = this.angleJitter*(Math.random()*2-1) / 57.2957795;
			final Matrix rm = TransformUtilities.centeredRotationMatrix(
					r, img.getWidth(), img.getHeight() );
			m = m.times( rm );
		}

		if( this.shearJitter > 0 )
		{
			final double r = this.shearJitter*(Math.random()*2-1) / 57.2957795;
			final Matrix rm = new Matrix( new double[][]{
					{1, Math.tan(r), 0},
					{0,1,0},
					{0,0,1}
			});
			m = m.times( rm );
		}

		if( this.perspectiveJitter > 0 )
		{
			// Get the image bounds to distort
			final Rectangle rect = img.getBounds();

			// Start points
			final Point2d p1 = new Point2dImpl( rect.x, rect.y );
			final Point2d p2 = new Point2dImpl( rect.x, rect.y+rect.height );
			final Point2d p3 = new Point2dImpl( rect.x+rect.width, rect.y+rect.height );
			final Point2d p4 = new Point2dImpl( rect.x+rect.width, rect.y );

			// End points
			Point2d p1p = p1;
			Point2d p2p = p2;
			Point2d p3p = p3;
			Point2d p4p = p4;

			final float s = (float)(this.perspectiveJitter/2f);

			// Randomly choose a side to reduce
			switch( (int)(Math.random()*4) )
			{
			// top
			case 0:
				p1p = new Point2dImpl( rect.x+rect.width*s, rect.y );
				p4p = new Point2dImpl( rect.x+rect.width-rect.width*s, rect.y );
				break;
				// left
			case 1:
				p1p = new Point2dImpl( rect.x, rect.y+rect.height*s );
				p2p = new Point2dImpl( rect.x, rect.y+rect.height-rect.height*s );
				break;
				// bottom
			case 2:
				p2p = new Point2dImpl( rect.x+rect.width*s, rect.y+rect.height );
				p3p = new Point2dImpl( rect.x+rect.width-rect.width*s, rect.y+rect.height );
				break;
				// right
			case 3:
				p3p = new Point2dImpl( rect.x+rect.width, rect.y+rect.height*s );
				p4p = new Point2dImpl( rect.x+rect.width, rect.y+rect.height-rect.height*s );
			}

			final List<IndependentPair<Point2d, Point2d>> d = new
					ArrayList<IndependentPair<Point2d,Point2d>>();

			d.add( new IndependentPair<Point2d,Point2d>( p1, p1p ) );
			d.add( new IndependentPair<Point2d,Point2d>( p2, p2p ) );
			d.add( new IndependentPair<Point2d,Point2d>( p3, p3p ) );
			d.add( new IndependentPair<Point2d,Point2d>( p4, p4p ) );

			final Matrix hm = TransformUtilities.homographyMatrixNorm( d );
			m = m.times( hm );
		}

		return m;
	}

	/**
	 * 	Get a jittered font style
	 *	@param gfs The input font style
	 *	@return The jittered font style
	 */
	public GeneralFontStyle<Q> getJitteredFontStyle( final GeneralFontStyle<Q> gfs )
	{
		//		if( colourJitter > 0 )
		//		{
		//			double r = Math.random() * colourJitter;
		//			Q col = gfs.getColour();
		//		}

		if( this.sizeJitter > 0 )
		{
			final double r = Math.random() * this.sizeJitter - this.sizeJitter/2;
			gfs.setFontSize( (int)(this.fontPointSize + r) );
		}

		return gfs;
	}

	/**
	 * 	Set the text string to use. Can be set to null for random
	 * 	characters.
	 *	@param textString the text string to use
	 */
	public void setTextString( final String textString )
	{
		this.textString = textString;
		this.nChars = textString.length();
	}

	/**
	 * 	Get the text string in use.
	 *	@return the text string in use
	 */
	public String getTextString()
	{
		return this.textString;
	}

	/**
	 * 	Set the amount of size jitter to use.
	 *	@param sizeJitter the amount of size jitter to use
	 */
	public void setSizeJitter( final double sizeJitter )
	{
		this.sizeJitter = sizeJitter;
	}

	/**
	 * 	Get the amount of size jitter in use.
	 *	@return the amount of size jitter in use.
	 */
	public double getSizeJitter()
	{
		return this.sizeJitter;
	}

	/**
	 * 	Set the amount of angle jitter to use.
	 *	@param angleJitter the amount of angle jitter to use
	 */
	public void setAngleJitter( final double angleJitter )
	{
		this.angleJitter = angleJitter;
	}

	/**
	 * 	Get the amount of angle jitter in use.
	 *	@return the amount of angle jitter in use.
	 */
	public double getAngleJitter()
	{
		return this.angleJitter;
	}

	/**
	 * 	Set the amount of shear jitter
	 *	@param shearJitter the amount of shear jitter to use
	 */
	public void setShearJitter( final double shearJitter )
	{
		this.shearJitter = shearJitter;
	}

	/**
	 * 	Get the amount of shear jitter in use.
	 *	@return the amount of shear jitter in use
	 */
	public double getShearJitter()
	{
		return this.shearJitter;
	}

	/**
	 * 	Set the amount of perspective jitter to use.
	 *	@param perspectiveJitter the amount of perspective jitter to use
	 */
	public void setPerspectiveJitter( final double perspectiveJitter )
	{
		this.perspectiveJitter = perspectiveJitter;
	}

	/**
	 * 	Get the amount of perspective jitter being simulated
	 *	@return the amount of perspective jitter
	 */
	public double getPerspectiveJitter()
	{
		return this.perspectiveJitter;
	}

	/**
	 * 	Set the list of fonts to avoid.
	 *	@param fontsToAvoid the list of fonts to avoid
	 */
	public void setFontsToAvoid( final List<String> fontsToAvoid )
	{
		this.fontsToAvoid = fontsToAvoid;
	}

	/**
	 * 	Get the list of fonts to avoid.
	 * 
	 *	@return the fonts to avoid
	 */
	public List<String> getFontsToAvoid()
	{
		return this.fontsToAvoid;
	}

	/**
	 * 	Avoid the font with the given name.
	 *	@param f The font to avoid
	 */
	public void addFontToAvoid( final String f )
	{
		this.fontsToAvoid.add( f );
	}

	/**
	 * 	Set the number of characters to randomly generate.
	 *	@param nChars the number of chars to generate
	 */
	public void setnChars( final int nChars )
	{
		this.nChars = nChars;
	}

	/**
	 * 	Get the number of characters being generated.
	 *	@return the number of characters to generate
	 */
	public int getnChars()
	{
		return this.nChars;
	}

	/**
	 * 	Set whether to jitter the font type or not.
	 * 
	 *	@param typeJitter Whether to jitter the font type
	 */
	public void setTypeJitter( final boolean typeJitter )
	{
		this.typeJitter = typeJitter;
	}

	/**
	 * 	Get whether the font type is being randomised.
	 * 
	 *	@return Whether the font type is being randomised.
	 */
	public boolean isTypeJitter()
	{
		return this.typeJitter;
	}

	/**
	 * 	Set the point size of the font to draw
	 *	@param fontPointSize the font point size to use
	 */
	public void setFontPointSize( final int fontPointSize )
	{
		this.fontPointSize = fontPointSize;
	}

	/**
	 * 	Get the size of the font that is being drawn
	 *	@return the size of the font being drawn
	 */
	public int getFontPointSize()
	{
		return this.fontPointSize;
	}

	/**
	 * 	Set the padding around the outside of the image.
	 *	@param padding the padding amount
	 */
	public void setPadding( final int padding )
	{
		this.padding = padding;
	}

	/**
	 * 	Get the amount of padding around the image.
	 *	@return the padding amount
	 */
	public int getPadding()
	{
		return this.padding;
	}

	/**
	 * 	Simple main that runs a simulation of 5 runs with the text "ABC" or
	 * 	text supplied on the command line.
	 * 
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		String text = "ABC";
		if( args.length > 0 )
			text = args[0];

		// Create an FImage font simulator
		final FontSimulator<Float,FImage> fs =
				new FontSimulator<Float,FImage>( text );
		fs.setFontPointSize( 72 );

		fs.setAngleJitter( 10 );
		fs.setShearJitter( 20 );
		fs.setPerspectiveJitter( 0.5 );

		// Make 5 runs
		fs.makeRuns( 5, new FontSimListener<FImage>()
				{
			@Override
			public void imageCreated( final FImage img )
			{
				// display the result
				DisplayUtilities.display( img );
			}
				}, new FImage(1,1) );
	}
}
