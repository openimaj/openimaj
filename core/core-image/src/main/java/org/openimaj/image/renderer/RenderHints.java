package org.openimaj.image.renderer;

/**
 * Hints for {@link ImageRenderer} implementations to (optionally) 
 * use when drawing.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class RenderHints {
	/**
	 * Different approaches to drawing
	 */
	public static enum DrawingAlgorithm {
		/**
		 * Fast drawing, no anti-aliasing
		 */
		FAST,
		/**
		 * Anti-aliased drawing 
		 */
		ANTI_ALIASED
	}
	
	/**
	 * Fast drawing 
	 */
	public static final RenderHints FAST = new RenderHints(DrawingAlgorithm.FAST);

	/**
	 * Anti-aliased drawing
	 */
	public static final RenderHints ANTI_ALIASED = new RenderHints(DrawingAlgorithm.ANTI_ALIASED);
	
	protected DrawingAlgorithm drawingAlgorithm = DrawingAlgorithm.FAST;
	
	/**
	 * Default constructor. Uses fastest drawing algorithm.
	 */
	public RenderHints() {
		
	}
	
	/**
	 * Construct with the given drawing algorithm
	 * @param drawingAlgorithm the drawing algorithm 
	 */
	public RenderHints(DrawingAlgorithm drawingAlgorithm) {
		this.drawingAlgorithm = drawingAlgorithm;
	}
}
