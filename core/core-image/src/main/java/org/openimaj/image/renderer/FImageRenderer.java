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
package org.openimaj.image.renderer;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * {@link ImageRenderer} for {@link FImage} images.
 * Supports both anti-aliased and fast rendering.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FImageRenderer extends ImageRenderer<Float, FImage> {
	
	/**
	 * Construct with given target image.
	 * @param targetImage the target image.
	 */
	public FImageRenderer(FImage targetImage) {
		super(targetImage);
	}
	
	/**
	 * Construct with given target image and rendering hints.
	 * @param targetImage the target image.
	 * @param hints the render hints
	 */
	public FImageRenderer(FImage targetImage, RenderHints hints) {
		super(targetImage, hints);
	}

	@Override
	public Float defaultForegroundColour() {
		return 1f;
	}

	@Override
	public Float defaultBackgroundColour() {
		return 0f;
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, double, int, int, java.lang.Object)
	 */
	@Override
	public void drawLine( int x1, int y1, double theta, int length, int thickness, Float grey )
	{
		int x2 = x1 + (int) Math.round( Math.cos( theta ) * length );
		int y2 = y1 + (int) Math.round( Math.sin( theta ) * length );

		drawLine( x1, y1, x2, y2, thickness, grey );
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, int, int, int, java.lang.Object)
	 */
	@Override
	public void drawLine( int x0, int y0, int x1, int y1, int thickness, Float grey ) {
		drawLine( (float)x0, (float)y0, (float)x1, (float)y1, thickness, grey );
	}
	
	/**
	 * Draw a line from the coordinates specified by <code>(x0,y0)</code> to 
	 * the coordinates specified by <code>(x1,y1)</code> using the given 
	 * color and thickness. Side-affects this image.
	 * 
	 * @param x0 The x-coordinate at the start of the line.
	 * @param y0 The y-coordinate at the start of the line. 
	 * @param x1 The x-coordinate at the end of the line.
	 * @param y1 The y-coordinate at the end of the line.
	 * @param thickness The thickness which to draw the line.
	 * @param grey The colour in which to draw the line.
	 */
	public void drawLine( float x0, float y0, float x1, float y1, int thickness, Float grey ) {
		switch (hints.drawingAlgorithm) {
		case ANTI_ALIASED:
			if (thickness <= 1) {
				drawLineXiaolinWu( x0, y0, x1, y1, grey );
			} else {
				double theta = Math.atan2(y1-y0, x1-x0);
				double t = thickness / 2;
				double sin = t*Math.sin(theta);
				double cos = t*Math.cos(theta);
				
				Polygon p = new Polygon();
				p.addVertex(new Point2dImpl((float) (x0-sin), (float) (y0+cos)));
				p.addVertex(new Point2dImpl((float) (x0+sin), (float) (y0-cos)));
				p.addVertex(new Point2dImpl((float) (x1+sin), (float) (y1-cos)));
				p.addVertex(new Point2dImpl((float) (x1-sin), (float) (y1+cos)));
				
				drawPolygonFilled(p, grey);
			}
			break;
		default:
			drawLineBresenham( Math.round(x0), Math.round(y0), Math.round(x1), Math.round(y1), thickness, grey );						
		}
	}
	
	private float fpart(float f) {
		return f - (int)f;
	}
	
	private float rfpart(float f) {
		return 1 - fpart(f);
	}
	
	private void plot(int a, int b, float c, float grey, boolean reversed) {
		int x, y;
		if (reversed) {
			y = a; 
			x = b;
		} else {
			x = a;
			y = b;
		}
		
		if (x >= 0 && x < targetImage.width && y >= 0 && y < targetImage.height) {
			targetImage.pixels[y][x] = c*grey + (1 - c) * targetImage.pixels[y][x];
		}
	}
	
	/*
	 * Implementation of Xiaolin Wu's anti-aliased line drawing algorithm.
	 * Based on the wikipedia article: http://en.wikipedia.org/wiki/Xiaolin_Wu's_line_algorithm
	 */
	protected void drawLineXiaolinWu(float x1, float y1, float x2, float y2, Float grey) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		boolean reversed = false;
		
	    if (Math.abs(dx) < Math.abs(dy)) {
	    	float tmp;
	    	tmp = x1; x1 = y1; y1 = tmp; 
	    	tmp = x2; x2 = y2; y2 = tmp;
	    	tmp = dx; dx = dy; dy = tmp;
	    	reversed = true;
	    }
	    
	    if (x2 < x1) {
	    	float tmp;
	    	tmp = x1; x1 = x2; x2 = tmp; 
	    	tmp = y1; y1 = y2; y2 = tmp;
	    }
	    
	    float gradient = dy / dx;
	    
	    // handle first endpoint
	    int xend = Math.round(x1);
	    float yend = y1 + gradient * (xend - x1);
	    float xgap = rfpart(x1 + 0.5f);
	    int xpxl1 = xend; // this will be used in the main loop
	    int ypxl1 = (int)(yend);
	    plot(xpxl1, ypxl1, rfpart(yend) * xgap, grey, reversed);
	    plot(xpxl1, ypxl1 + 1, fpart(yend) * xgap, grey, reversed);
	    float intery = yend + gradient; // first y-intersection for the main loop
	    
	    // handle second endpoint
	    xend = Math.round(x2);
	    yend = y2 + gradient * (xend - x2);
	    xgap = fpart(x2 + 0.5f);
	    int xpxl2 = xend;  // this will be used in the main loop
	    int ypxl2 = (int)(yend);
	    plot (xpxl2, ypxl2, rfpart (yend) * xgap, grey, reversed);
	    plot (xpxl2, ypxl2 + 1, fpart (yend) * xgap, grey, reversed);
	    
	    // main loop
	    for (int x = xpxl1 + 1; x<= xpxl2 - 1; x++) {
	        plot (x, (int)(intery), rfpart (intery), grey, reversed);
	        plot (x, (int)(intery) + 1, fpart (intery), grey, reversed);
	        intery += gradient;
	    }
	}

	/*
	 * Implementation of Bresenham's fast line drawing algorithm.
	 * Based on the wikipedia article: http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
	 */
	protected void drawLineBresenham( int x0, int y0, int x1, int y1, int thickness, Float grey )
	{
		Line2d line = new Line2d(new Point2dImpl(x0,y0),new Point2dImpl(x1,y1)).lineWithinSquare(targetImage.getBounds());
		if(line == null)
			return;
		
		x0 = (int) line.begin.getX();
		y0 = (int) line.begin.getY();
		x1 = (int) line.end.getX();
		y1 = (int) line.end.getY();
		
		double theta = Math.atan2(y1-y0, x1-x0);
		thickness = (int) Math.round(thickness * Math.max(Math.abs(Math.cos(theta)), Math.abs(Math.sin(theta))));
		
		int offset = thickness / 2;
		int extra = thickness % 2; 
		

		// implementation of Bresenham's algorithm from Wikipedia.
		int Dx = x1 - x0;
		int Dy = y1 - y0;
		boolean steep = (Math.abs( Dy ) >= Math.abs( Dx ));
		if( steep )
		{
			int tmp;
			// SWAP(x0, y0);
			tmp = x0;
			x0 = y0;
			y0 = tmp;
			// SWAP(x1, y1);
			tmp = x1;
			x1 = y1;
			y1 = tmp;

			// recompute Dx, Dy after swap
			Dx = x1 - x0;
			Dy = y1 - y0;
		}
		int xstep = 1;
		if( Dx < 0 )
		{
			xstep = -1;
			Dx = -Dx;
		}
		int ystep = 1;
		if( Dy < 0 )
		{
			ystep = -1;
			Dy = -Dy;
		}
		int TwoDy = 2 * Dy;
		int TwoDyTwoDx = TwoDy - 2 * Dx; // 2*Dy - 2*Dx
		int E = TwoDy - Dx; // 2*Dy - Dx
		int y = y0;
		int xDraw, yDraw;
		for( int x = x0; x != x1; x += xstep )
		{
			if( steep )
			{
				xDraw = y;
				yDraw = x;
			}
			else
			{
				xDraw = x;
				yDraw = y;
			}
			// plot
			if( xDraw >= 0 && xDraw < targetImage.width && yDraw >= 0 && yDraw < targetImage.height ) {
				if (thickness == 1 ) {
					targetImage.pixels[yDraw][xDraw] = grey;
				} else if (thickness > 1) {
					for (int yy=yDraw-offset; yy<yDraw+offset+extra; yy++)
						for (int xx=xDraw-offset; xx<xDraw+offset+extra; xx++)
							if (xx >= 0 && yy >= 0 && xx < targetImage.width && yy< targetImage.height) targetImage.pixels[yy][xx] = grey;
				}
			}

			// next
			if( E > 0 )
			{
				E += TwoDyTwoDx; // E += 2*Dy - 2*Dx;
				y = y + ystep;
			}
			else
			{
				E += TwoDy; // E += 2*Dy;
			}
		}
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.renderer.ImageRenderer#drawPoint(org.openimaj.math.geometry.point.Point2d, java.lang.Object, int)
	 */
	@Override
	public void drawPoint( Point2d p, Float grey, int size )
	{
		if(!targetImage.getBounds().isInside(p)) return;
		//TODO anti-aliased point rendering
		int x = Math.round( p.getX() );
		int y = Math.round( p.getY() );

		if( x > targetImage.width || y > targetImage.height ) return;

		for( int j = y; j < Math.min( y + size, targetImage.height ); j++ )
		{
			for( int i = x; i < Math.min( x + size, targetImage.width ); i++ )
			{
				targetImage.pixels[j][i] = grey;
			}
		}
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.renderer.ImageRenderer#drawPolygon(org.openimaj.math.geometry.shape.Polygon, int, java.lang.Object)
	 */
	@Override
	public void drawPolygon( Polygon p, int thickness, Float grey )
	{
		if( p.nVertices() < 2 ) return;

		Point2d p1, p2;
		for( int i = 0; i < p.nVertices() - 1; i++ )
		{
			p1 = p.getVertices().get( i );
			p2 = p.getVertices().get( i + 1 );
			drawLine( p1.getX(), p1.getY(), p2.getX(), p2.getY(), thickness, grey );
		}

		p1 = p.getVertices().get( p.nVertices() - 1 );
		p2 = p.getVertices().get( 0 );
		drawLine( p1.getX(), p1.getY(), p2.getX(), p2.getY(), thickness, grey );
	}
}
