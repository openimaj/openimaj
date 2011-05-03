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
package org.openimaj.image.processing.face.recognition;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processing.face.Face;
import org.openimaj.image.processing.face.FaceAligner;
import org.openimaj.image.processing.face.FaceDetector;
import org.openimaj.image.processing.face.FaceRecogniser;
import org.openimaj.image.processing.face.RankedObject;
import org.openimaj.image.processing.face.Representation;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.semantics.Person;
import org.openimaj.image.processor.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Polygon;


/**
 *	An implementation of the {@link FaceRecogniser} interface for detecting
 *	faces using an Eigenfaces recogniser.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 */
public class EigenfacesFaceRecogniser implements FaceRecogniser
{
	public static Logger logger = Logger.getLogger( EigenfacesFaceRecogniser.class );
	
	/** The Eigen face generator */
	private EigenFaceGenerator generator = null;
	
	/** The FeatureSpace containing all the eigenfaces */
	private FeatureSpace featureSpace = null;
	
	/** The aligner */
	private FaceAligner fa = null;
	
	/** The detector */
	private FaceDetector fd = null;
	
	/** The faces used for training */
	private List<Face> faces = null;
	
	/** The dimensions the faces will be resized to */
	private Dimension faceDimensions = new Dimension( 40, 40 );
	
	/**
	 * 	Constructor that takes a face detector.
	 * 
	 *	@param fd The face detector
	 */
	public EigenfacesFaceRecogniser( FaceDetector fd )
	{
		this.fd = fd;
		
		generator = new EigenFaceGenerator();
		
		faces = new ArrayList<Face>();
	}
	
	/**
	 *  @inheritDoc
	 *  @see org.openimaj.image.processing.face.FaceRecogniser#getFaceDetector()
	 */
	@Override
	public FaceDetector getFaceDetector()
	{
		return this.fd;
	}
	
	/**
	 * 	Returns the object used to align faces.
	 *  @return
	 */
	public FaceAligner getFaceAligner()
	{
		return this.fa;
	}

	/**
	 * 	The pre-processing function may be used to align the
	 * 	face to a particular position before putting into the
	 * 	Eigenfaces classifier 
	 * 
	 *	@param r
	 *	@return
	 */
	private List<Face> preProcess( Face r )
	{
		// Processing consists of the following:
		//    1) Find the faces within the image. Choose the largest!
		//    2) Align the main face within its subimage
		//    3) Equalise the colour (if necessary)
		
		// This is the output list
		List<Face> faces = new ArrayList<Face>();
		
		// Detect the face and crop to it.
		List<ConnectedComponent> x = fd.findFaces( r );
		
		logger.debug( "After face detection, found "+x.size()+" faces" );

		if( x.size() > 0 )
		{
			// Get the largest ConnectedComponent (in case more than 1 face is found)
			ConnectedComponent largestCC = null;
			for( ConnectedComponent cc : x )
				if( largestCC == null || (cc.calculateArea() > largestCC.calculateArea()) )
					largestCC = cc;
	
			// Extract the region in which the CC fits
			// MBFImage clonedImage = r.clone();

			// Work out the convex hull for the connected component 
			// (smoothes out the inperfections in detection)
			logger.debug( "Calculating convex hull for largest face..." );
			Polygon convexHull = largestCC.calculateConvexHull();
			
//			// Create a binary mask from the convex hull
//			FImage mask = convexHull.toConnectedComponent().calcuateBinaryMask( r );
//
//			// Multiply the images together to get the face only in the original image 
//			MBFImage justTheFace = (MBFImage) clonedImage.multiply( mask );
//			
			// Crop the image to the face only
			logger.debug( "Cropping face" );
			MBFImage img = new ConnectedComponent(convexHull).crop( r, false );
			
			// Resample face
			logger.debug( "Resampling face" );
			MBFImage img2 = img.process(new ResizeProcessor( this.faceDimensions.width, this.faceDimensions.height, true ));
			
			/*  Uncomment to write out the extracted faces
			try
			{
				File file = new File( r.getOriginalFileName()+"_extractedFace.png");
				ImageUtilities.write( img2, "png", file );
				System.out.println( "Writing "+file );
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//*/

//			fa.alignFaceImages( image1, image2 )

			// As we're only returning one face in this step,
			// we side-affect the face we're given and pass it back
			r.internalAssign( img2 );
			faces.add( r );
		}
		
		// Return just the one face (or none)
		return faces;
	}
	
	/**
	 *  @inheritDoc
	 *  @see org.openimaj.image.proc.tools.ObjectRecogniser#addRepresentation(java.lang.Object, org.openimaj.image.proc.tools.Representation)
	 */
	@Override
	public void addRepresentation( Person o, Representation r ) throws IllegalArgumentException
	{
		// The assumption is that we're processing faces.
		// The representation must therefore be a face.
		if( !(r instanceof Face) )
			throw new IllegalArgumentException( "Representation must be a face" );
		
		// Pre-process the image before adding it to the recogniser
		List<Face> faces = this.preProcess( (Face)r );

		// If there's more than one face in the image
		// we cannot know which represents the Person.
		if( faces.size() > 1 )
			throw new IllegalArgumentException( "The representation appeared to have multiple faces within it. Which one is the person?" );
		
		// If there are no faces in the image, it may
		// be an irrelevant image, or perhaps the
		// face detector's not working so well.
		if( faces.size() == 0 )
			throw new IllegalArgumentException( "The representation didn't appear to have a face in it. Perhaps the detector's not working well on this image." );

		// We add the found face to the list of training data
		Face f = faces.get(0);
		this.faces.add( f );
	}
	
	/**
	 * 	This is a convenience method for adding many faces into the list of representations.
	 *  @param faces A list of faces.
	 */
	public void addTrainingData( List<Face> faces )
	{
		for( Face f : faces )
		{
			try
			{
				this.addRepresentation( f.getBelongsTo(), f );
			}
			catch( IllegalArgumentException e )
			{
				logger.warn( "No faces found in image " );
			}
		}
		
		/*
		try
		{
			Face f = generator.getAverageFace();
			ImageIO.write( f.toBufferedImage(), "png", new File( "averageFace.png") );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		*/
	}
	
	/**
	 * 	Initiates the training based on all the representations that have been added.
	 */
	public void train()
	{
		// Send the training set to the EigenFaceGenerator
		generator.processTrainingSet( this.faces.toArray( new Face[1] ) );
		
		featureSpace = new FeatureSpace();
        for( Face f : this.faces )
        {
            //TODO : determine how many vectors are useful - look at the .65 - .80 stuff on site
            int numVecs = 10;
            double[] rslt = generator.getEigenFaces( f, numVecs ); //
            featureSpace.insertIntoDatabase(f, rslt);
        }
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.image.proc.tools.ObjectRecogniser#find(org.openimaj.image.proc.tools.Representation, int)
	 */
	@Override
	public List<RankedObject<Person>> find( Representation r, int limit )
	{
		List<RankedObject<Person>> l = new ArrayList<RankedObject<Person>>();
		return l;
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.image.proc.tools.ObjectRecogniser#findNearest(org.openimaj.image.proc.tools.Representation)
	 */
	@Override
	public Person findNearest( Representation r )
	{
		if( !(r instanceof Face) )
			throw new IllegalArgumentException( "Representation of person to find must be a face" );
		
		// We need to resize the query image to the same as the generator's database
		List<Face> facesInQuery = preProcess( (Face)r );
		
		if( facesInQuery.size() == 0 )
			throw new IllegalArgumentException( "No faces found in query image. Perhaps the detector's not working well on this image?");
		
		int numVecs = 10;
		double[] rslt = generator.getEigenFaces( facesInQuery.get(0), numVecs );
		
		FeatureVector fv = new FeatureVector();
		fv.setFeatureVector(rslt);

		int classifierThreshold = 5;
		Object classification = featureSpace.knn( FeatureSpace.EUCLIDEAN_DISTANCE, fv, classifierThreshold );

		if( classification instanceof Person )
				return (Person)classification;
		else 	throw new IllegalArgumentException( "The classifier did not return a Person object. It returned "+classification );
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.image.proc.tools.ObjectRecogniser#verify(java.lang.Object, org.openimaj.image.proc.tools.Representation)
	 */
	@Override
	public boolean verify( Person o, Representation r )
	{
		return findNearest(r).equals(o);
	}

	/**
	 * 	Reads the training set file.
	 *  @param f
	 */
	public static List<Face> readTrainingSetFile( File f )
	{
		List<Face> faces = new ArrayList<Face>();
		
		List<String> lines = new ArrayList<String>();
        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader( new FileReader(f) );
            String text = null;

            // repeat until all lines is read
            while ((text = reader.readLine()) != null)
                lines.add(text);
        } 
        catch( FileNotFoundException e )
        {
            e.printStackTrace();
        } 
        catch( IOException e )
        {
            e.printStackTrace();
        } 
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            } 
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
        
        // Now process the lines
        for( String line: lines )
        {
        	String[] s = line.split( "\t", -1 );
        	
        	// Read the person information
        	Person p = new Person();
        	p.firstName = s[1];
        	p.lastName = s[2];
        	p.email = s[3];
        	
        	// Read all the jpg files from the given directory
        	File[] files = (new File(s[0])).listFiles( new FileFilter()
			{
				@Override
				public boolean accept( File file )
				{
					return file.getName().endsWith( ".jpg" );
				}
			});
        	for( File file : files )
        	{
        		// Create a Face for each of the images
	        	try
                {
	                Face face = new Face( ImageUtilities.readMBF( file ) );
	                face.setBelongsTo( p );
	                face.setOriginalFileName( file.getAbsolutePath() );
	                faces.add( face );
	                
	                logger.debug( "Person "+p+" in "+file );
                }
                catch( IOException e )
                {
	                e.printStackTrace();
                }
        	}
        }
        
        return faces;
	}
	
	/**
	 *	@param args
	 */
	public static void main( String[] args ) throws Exception
	{
		PropertyConfigurator.configure( "logging.properties" );
		
		if( args.length < 2 )
		{
			System.err.println( "Usage: ");
			System.err.println( "   - EigenFacesRecogniser <trainingSetFile> <lookupImageFile>");
			System.err.println( "\nNote that the training set file is a text file with one folder per line.\nSeparated by tab is the first, last and email of the person for the folder" );
			
			System.exit(1);
		}
		
		File trainingSetFile = new File( args[0] );
		File lookupFile = new File( args[1] );
		
		// Read the training set file and get all the faces
		List<Face> faces = readTrainingSetFile( trainingSetFile );
		
		// Add all the training data to the recogniser
		EigenfacesFaceRecogniser efr = new EigenfacesFaceRecogniser( new HaarCascadeDetector() );
		efr.addTrainingData( faces );
		efr.train();
		
		try
        {
			// Read the image to query with
			MBFImage faceImage = ImageUtilities.readMBF( lookupFile );
			
	        // Read the face to query
	        Face face = new Face( faceImage );
	        
	        // Find the nearest
	        Person p = efr.findNearest( face );
	        System.out.println( "The nearest person appears to be:\n\t"+p.toString() );
        }
        catch( IOException e )
        {
	        e.printStackTrace();
        }
	}
}
