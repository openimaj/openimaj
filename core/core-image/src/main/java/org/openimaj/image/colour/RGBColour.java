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
package org.openimaj.image.colour;

import java.awt.Color;

/**
 * Convenience constants and methods for RGB colours for 
 * use in MBFImages
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class RGBColour {
	/** White colour as RGB */
	public final static Float[] WHITE 		= {1f, 1f, 1f};
    
	/** Light gray colour as RGB */
	public final static Float[] LIGHT_GRAY 	= {0.75f, 0.75f, 0.75f};
	
	/** Gray colour as RGB */
    public final static Float[] GRAY		= {0.5f, 0.5f, 0.5f};
    
    /** Dark gray colour as RGB */
    public final static Float[] DARK_GRAY  	= {0.25f, 0.25f, 0.25f};
    
    /** Black colour as RGB */
    public final static Float[] BLACK	 	= {0f, 0f, 0f};
    
    /** Red colour as RGB */
    public final static Float[] RED			= {1f, 0f, 0f};
    
    /** Pink colour as RGB */
    public final static Float[] PINK		= {1f, 175f/256f, 175f/256f};
    
    /** Orange colour as RGB */
    public final static Float[] ORANGE 		= {1f, 200f/256f, 0f};
    
    /** Yellow colour as RGB */
    public final static Float[] YELLOW	 	= {1f, 1f, 0f};
    
    /** Green colour as RGB */
    public final static Float[] GREEN	 	= {0f, 1f, 0f};
    
    /** Magenta colour as RGB */
    public final static Float[] MAGENTA		= {1f, 0f, 1f};
    
    /** Cyan colour as RGB */
    public final static Float[] CYAN 		= {0f, 1f, 1f};    
    
    /** Blue colour as RGB */
    public final static Float[] BLUE	 	= {0f, 0f, 1f};
	
	/**
	 * Make an OpenImaj colour from a java.awt.Color.
	 * @param c the color to convert
	 * @return a colour for using as RGB MBFImages
	 */
	public static Float[] fromColor(Color c) {
		Float r = c.getRed() / 255f;
		Float g = c.getRed() / 255f;
		Float b = c.getRed() / 255f;
		
		return new Float[] {r,g,b};
	}	
}
