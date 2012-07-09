package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.FourierTemplateMatcher;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

/**
 *	A CLM Tracker that is able to deal with multiple tracks within the same
 *	video. To instantiate use {@link #Load(InputStream)} to get a {@link TrackerVars}
 *	object which can be used to construct the Tracker.
 *	<p>
 *	<code><pre>Tracker t = new Tracker( Tracker.Load( new File("face.tracker.file") ) );</pre></code> 
 */
public class Tracker 
{
	/**
	 *	Encapsulates the variables for a single tracked face.
	 *	This includes the model, the shape parameters, the last-matched
	 *	template and the bounding rectangle.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 4 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class TrackedFace
	{
		/** The constrained local model */
		public CLM clm;
		
		/** The current shape */
		public Matrix shape;
		
		/** The reference shape */
		public Matrix referenceShape;
		
		/** The template image */
		public FImage templateImage;
		
		/** The last matched bounds: _rect */
		public Rectangle lastMatchBounds;
		
		/** The redetected bounds: R */
		public Rectangle redetectedBounds;
		
		/**
		 *	@param r The rectangle in which the initial face was found
		 * 	@param tv The initial tracker vars to use 
		 */
		public TrackedFace( Rectangle r, TrackerVars tv )
		{
			this.redetectedBounds = r;			
			this.clm = tv.clm.copy();
			this.shape = tv.shape.copy();
			this.referenceShape = tv.referenceShape.copy();
		}
		
		@Override
		public String toString()
		{
		    return "Face["+(redetectedBounds==null?"null":redetectedBounds.toString())+"]";
		}
	}
	
	/**
	 * 	This class is used to store the tracker variables when they are
	 * 	loaded from a file. These variables can then be copied to make
	 * 	specific trackers.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 5 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class TrackerVars
	{
		/** The constrained local model */
		public CLM clm;
		
		/** The current shape */
		public Matrix shape;
		
		/** The reference shape */
		public Matrix referenceShape;
		
		/** The Face detector */
		public FDet faceDetector;
		
		/** The failure checker */
		public MFCheck failureCheck;
		
		/** Initialisation similarity */
		double[] similarity;		
	}
	
	/** Scaling of template for template matching */
	private static final double TSCALE = 0.3;
	
	/** */
	public List<TrackedFace> trackedFaces = new ArrayList<TrackedFace>();
	
	/** The initial tracker */
	private TrackerVars initialTracker = null;
	
	/**< Frame number since last detection */
	private long framesSinceLastDetection;
	
	/** The frame currently being processed */
	private FImage currentFrame;

	private FImage small_;

	/**
	 * 	Create a tracker using the given model, face detector, failure checker,
	 * 	reference shape and similarity measures. These values will be copied
	 * 	into all trackers.
	 * 
	 *	@param clm The local model
	 *	@param fdet The face detector
	 *	@param fcheck The failure checker
	 *	@param rshape The reference shape
	 *	@param simil The similarity measures
	 */
	public Tracker( CLM clm, FDet fdet, MFCheck fcheck, Matrix rshape, double[] simil )
	{
		this.initialTracker = new TrackerVars();
		this.initialTracker.clm = clm;
		this.initialTracker.clm._pdm.Identity( clm._plocal, clm._pglobl );
		this.initialTracker.faceDetector = fdet; 
		this.initialTracker.failureCheck = fcheck;
		this.initialTracker.referenceShape = rshape.copy(); 
		this.initialTracker.similarity = simil;
		this.initialTracker.shape = new Matrix( 2* clm._pdm.nPoints(), 1);
		framesSinceLastDetection = -1;
	}

	/**
	 * 	Create a tracker with the given variables.
	 *	@param tv The tracker variables to use for all face trackers.
	 */
	public Tracker( TrackerVars tv )
	{
		this.initialTracker = tv;
		framesSinceLastDetection = -1; 
	}
	
	/**
	 *	Constructor for making a tracker when loading data.
	 */
	protected Tracker() 
	{
	}
	
	/** 
	 * 	Reset frame number (will perform detection in next image) 
	 */
    public void frameReset() 
    {
    	framesSinceLastDetection = -1;
    }

	//===========================================================================
    /**
     * 	Track faces from a previous frame to the given frame.
     * 
     *	@param im The video frame
     *	@param wSize The window size
     *	@param fpd The number of frames between forced redetecs
     *	@param nIter The number of iterations for model fitting
     *	@param clamp The number s.d.'s in which a model must fit
     *	@param fTol The tolerance for model fitting
     *	@param fcheck Whether to automatically check for failed tracking
     * 	@param searchAreaSize The size of the template match search area 
     *	@return 0 for success, -1 for failure.
     */
    public int Track(FImage im, int[] wSize, final int fpd, final int nIter, 
			final double clamp,final double fTol, final boolean fcheck,
			float searchAreaSize )
	{ 
		currentFrame = im;

		boolean gen = false; 
		
		if( (framesSinceLastDetection < 0) || 
			(fpd >= 0 && fpd < framesSinceLastDetection) ) 
		{
			framesSinceLastDetection = 0;
			List<Rectangle> RL = initialTracker.faceDetector.Detect( currentFrame );

			// Convert the detected rectangles into face trackers
			trackedFaces.clear();
			for( Rectangle r : RL )
				trackedFaces.add( new TrackedFace( r, initialTracker ) );
			
			gen = true;
		} 
		else 
		{
			// Updates the tracked faces
			trackRedetect( currentFrame, searchAreaSize );
			gen = false;
		}
		
		// Didn't find any faces in this frame? Try again next frame.
		if( trackedFaces.size() == 0 )
			return -1;
		
		boolean resize = true;
		for( TrackedFace f : trackedFaces )
		{
			if( (f.redetectedBounds.width == 0) || 
				(f.redetectedBounds.height == 0) ) 
			{ 
				trackedFaces.remove( f );
				framesSinceLastDetection = -1;
				return -1;
			}
			
			if( gen ) 
			{
				initShape( f.redetectedBounds, f.shape, f.referenceShape );		
				f.clm._pdm.CalcParams( f.shape, f.clm._plocal, f.clm._pglobl );
			} 
			else 
			{
				double tx = f.redetectedBounds.x - f.lastMatchBounds.x;
				double ty = f.redetectedBounds.y - f.lastMatchBounds.y;
				
				f.clm._pglobl.getArray()[4][0] += tx;
				f.clm._pglobl.getArray()[5][0] += ty;
				
				resize = false; 
			}
			
			f.clm.Fit( currentFrame, wSize, nIter, clamp, fTol );
			f.clm._pdm.CalcShape2D( f.shape, f.clm._plocal, f.clm._pglobl );
			
			if( fcheck ) 
			{
				if( !initialTracker.failureCheck.Check( 
						f.clm.GetViewIdx(), currentFrame, f.shape ) )
				{
					trackedFaces.remove( f );
					return -1;
				}
			}
			
			f.lastMatchBounds = this.updateTemplate( f, currentFrame, f.shape, resize );

			if( (f.lastMatchBounds.width == 0) || 
				(f.lastMatchBounds.height == 0) ) 
			{ 
				trackedFaces.remove( f );
				framesSinceLastDetection = -1;
				return -1;
			}				
		}
		
		framesSinceLastDetection++;
		
		return 0;
	}
	//===========================================================================
	private void initShape( final Rectangle r, final Matrix shape, final Matrix _rshape )
	{
		assert( (shape.getRowDimension() == _rshape.getRowDimension()) 
				&& (shape.getColumnDimension() == _rshape.getColumnDimension()));
		
		int n = _rshape.getRowDimension() / 2; 
		
		double a = r.width * Math.cos( initialTracker.similarity[1] ) 
				* initialTracker.similarity[0] + 1;
		double b = r.width * Math.sin( initialTracker.similarity[1] ) 
				* initialTracker.similarity[0];
		
		double tx = r.x + (int)(r.width/2)  + r.width * initialTracker.similarity[2];
		double ty = r.y + (int)(r.height/2) + r.height* initialTracker.similarity[3];
		
		double[][] s = _rshape.getArray();
		double[][] d = shape.getArray();
		
		for (int i = 0; i < n; i++) {
			d[i][0] = a*s[i][0] - b*s[i+n][0] + tx; 
			d[i+n][0] = b*s[i][0] + a*s[i+n][0] + ty;
		}
	}
	
	//===========================================================================
	/**
	 * 	Redetect the faces in the new frame.
	 * 
	 *	@param im The new frame.
	 *	@param searchAreaSize The search area size
	 */
	private void trackRedetect( FImage im, float searchAreaSize )
	{
		final int ww = im.width;
		final int hh = im.height;

		// Resize the frame so processing is quicker.
		small_ = ResizeProcessor.resample( 
				im, (int)(TSCALE*ww), (int)(TSCALE*hh) );
		
		for( TrackedFace f : trackedFaces )
		{
			// Get the new search area nearby to the last match
			Rectangle searchAreaBounds = f.lastMatchBounds.clone();
			searchAreaBounds.scale( (float)TSCALE );
			searchAreaBounds.scaleCOG( searchAreaSize );
			searchAreaBounds = searchAreaBounds.overlapping( small_.getBounds() );
			
			// Get the search image
			final FImage searchArea = small_.extractROI( searchAreaBounds );
			
			// Template match the template over the reduced size image.
			FourierTemplateMatcher matcher = new FourierTemplateMatcher(
					f.templateImage, FourierTemplateMatcher.Mode.NORM_CORRELATION_COEFFICIENT);
			matcher.analyseImage( searchArea );
			
			// Get the response map
			float[][] ncc_ = matcher.getResponseMap().pixels;
			
//			DisplayUtilities.displayName( matcher.getResponseMap(), "responseMap" );
//			DisplayUtilities.displayName( f.templateImage, "template" );
			
			f.redetectedBounds = f.templateImage.getBounds();
			
			// Find the maximum template match in the image
			final int h = searchArea.height - f.templateImage.height + 1;
			final int w = searchArea.width - f.templateImage.width + 1;	
			float vb = -2;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					final float v = ncc_[y][x];
					if (v > vb) 
					{
						vb = v; 
						f.redetectedBounds.x = x + searchAreaBounds.x; 
						f.redetectedBounds.y = y + searchAreaBounds.y;
					}
				}
			}

			// Rescale the rectangle to full-size image coordinates.
			f.redetectedBounds.scale( (float)(1d/TSCALE) );
		}
	}
	//===========================================================================
	protected Rectangle updateTemplate( TrackedFace f, FImage im, Matrix s, boolean resize )
	{
		final int n = s.getRowDimension() / 2; 
		
		double[][] sv = s.getArray();
		double xmax=sv[0][0], ymax=sv[n][0], xmin=sv[0][0], ymin=sv[n][0];
		
		for (int i = 0; i < n; i++) 
		{
			double vx = sv[i  ][0];
			double vy = sv[i+n][0];
			
			xmax = Math.max(xmax, vx); 
			ymax = Math.max(ymax, vy);
			
			xmin = Math.min(xmin, vx); 
			ymin = Math.min(ymin, vy);
		}
		
		if ((xmin < 0) || (ymin < 0) || (xmax >= im.width) || (ymax >= im.height) ||
				Double.isNaN(xmin) || Double.isInfinite(xmin) || 
				Double.isNaN(xmax) || Double.isInfinite(xmax) ||
				Double.isNaN(ymin) || Double.isInfinite(ymin) || 
				Double.isNaN(ymax) || Double.isInfinite(ymax)) 
		{
			return new Rectangle(0, 0, 0, 0);
		} 
		else 
		{
			xmin *= TSCALE; 
			ymin *= TSCALE; 
			xmax *= TSCALE; 
			ymax *= TSCALE;
			
			Rectangle R = new Rectangle(
					(float)Math.floor(xmin), 
					(float)Math.floor(ymin), 
					(float)Math.ceil(xmax-xmin), 
					(float)Math.ceil(ymax-ymin));
			
			final int ww = im.width;
			final int hh = im.height;
			
			if( resize ) 
				small_ = ResizeProcessor.resample( im, 
						(int)(TSCALE*ww), (int)(TSCALE*hh) );
			
			f.templateImage = small_.extractROI(R);
			
			R.x *= 1.0 / TSCALE;
			R.y *= 1.0 / TSCALE;
			R.width *= 1.0 / TSCALE; 
			R.height *= 1.0 / TSCALE; 
			
			return R;
		}
	}

	//===========================================================================
	/**
	 * 	Load a tracker from a file.
	 * 
	 *	@param fname File name to read from
	 *	@return A tracker variable class
	 *	@throws FileNotFoundException
	 */
	public static TrackerVars Load( final String fname ) 
			throws FileNotFoundException
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			Scanner sc = new Scanner(br);
			return Read(sc, true);
		} finally {
			try { br.close(); } catch (IOException e) {}
		}
	}
	
	/**
	 * 	Load a tracker from an input stream.
	 * 
	 *	@param in The input stream
	 *	@return a tracker
	 */
	public static TrackerVars Load(final InputStream in)
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			Scanner sc = new Scanner(br);
			return Read(sc, true);
		} finally {
			try { br.close(); } catch (IOException e) {}
		}
	}

	//===========================================================================
	/**
	 * 	Save the tracker variables to a file.
	 * 
	 *	@param fname The filename to write to
	 * 	@param tv The tracker variables to write 
	 *	@throws IOException
	 */
	public void Save(final String fname, final TrackerVars tv ) throws IOException
	{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));

			Write(bw,tv);
		} finally {
			try {
				if (bw != null) bw.close();
			} catch (IOException e) {}
		}
	}

	//===========================================================================
	/**
	 * 	Write tracker variables to a stream
	 * 
	 *	@param s the stream to write to
	 * 	@param tv The tracker variables to write 
	 *	@throws IOException
	 */
	public void Write( BufferedWriter s, TrackerVars tv ) throws IOException
	{
		s.write( IO.Types.TRACKER.ordinal() + " " );
		
		tv.clm.Write(s); 
		tv.faceDetector.Write(s); 
		tv.failureCheck.Write(s); 
		IO.WriteMat(s, tv.referenceShape); 
		
		s.write( tv.similarity[0] + " " + tv.similarity[1] + " " 
				+ tv.similarity[2] + " " + tv.similarity[3] + " ");
	}
	//===========================================================================
	/**
	 * 
	 *	@param s
	 *	@param readType
	 *	@return
	 */
	private static TrackerVars Read(Scanner s, boolean readType)
	{
		if (readType) { 
			int type = s.nextInt();
			assert(type == IO.Types.TRACKER.ordinal());
		}
		TrackerVars trackerVars = new TrackerVars();
		trackerVars.clm =  CLM.Read(s, true);
		trackerVars.faceDetector = FDet.Read(s, true);
		trackerVars.failureCheck = MFCheck.Read(s, true); 
		trackerVars.referenceShape = IO.ReadMat(s);
		trackerVars.similarity = new double[] {s.nextDouble(), s.nextDouble(), s.nextDouble(), s.nextDouble()}; 
		trackerVars.shape = new Matrix( 2 * trackerVars.clm._pdm.nPoints(), 1 );
		trackerVars.clm._pdm.Identity( trackerVars.clm._plocal, trackerVars.clm._pglobl );
		
		return trackerVars;
	}
	
	/**
	 * 	Returns the initial variables used for each face tracker.
	 *	@return The initial variables
	 */
	public TrackerVars getInitialVars()
	{
		return this.initialTracker;
	}
}
