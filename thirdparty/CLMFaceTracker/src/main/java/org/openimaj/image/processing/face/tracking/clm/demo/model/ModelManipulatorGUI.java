/**
 * 
 */
package org.openimaj.image.processing.face.tracking.clm.demo.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.Tracker.TrackedFace;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *	Provides a user interface for interacting with the parameters of the
 *	trained CLM Model.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 9 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class ModelManipulatorGUI extends JPanel
{
	/** */
    private static final long serialVersionUID = -3302684225273947693L;
    
    /**
     *	Provides a panel which draws a particular model to itself.
     *
     *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
     *  @created 9 Jul 2012
     *	@version $Author$, $Revision$, $Date$
     */
    public class ModelView extends JPanel
    {
		/** */
        private static final long serialVersionUID = 1L;
        
        /** The image of the model */
        private MBFImage vis = new MBFImage( 600,600,3 );

        /** The face being drawn */
        private TrackedFace face = null;

        /** Reference triangles */
		private int[][] triangles;

		/** Reference connections */
		private int[][] connections;
        
		/** Colour to draw the connections */
		private Float[] connectionColour = RGBColour.WHITE;
		
		/** Colour to draw the points */
		private Float[] pointColour = RGBColour.RED;
		
		/** Colour to draw the mesh */
		private Float[] meshColour = new Float[]{0.3f,0.3f,0.3f};
		
		/** Colour to draw the bounding box */
		private Float[] boundingBoxColour = RGBColour.RED;
		
		/** Whether to show mesh */
		private boolean showMesh = true;
		
		/** Whether to show connections */
		private boolean showConnections = true;

		/**
		 * 	Constructor
		 */
        public ModelView()
        {
        	this.setPreferredSize( new Dimension(600,600) );
        	
        	CLMFaceTracker t = new CLMFaceTracker();
        	this.triangles = t.getReferenceTriangles();
        	this.connections = t.getReferenceConnections();
        	this.face = new TrackedFace( 
        			new Rectangle(50,-50,500,500), 
        			t.getInitialVars() );
        	t.initialiseFaceModel( face );
        	
        	// Centre the face in the view
        	setGlobalParam( 0, 10 );
        	setGlobalParam( 4, 300 );
        	setGlobalParam( 5, 300 );
        	
        	setBackground( new Color(60,60,60) );
        }
        
        /**
         *	{@inheritDoc}
         * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        @Override
        public void paint( Graphics g )
        {
        	super.paint(g);
        	
        	vis.zero();
        	
        	// Draw the model to the image.
        	CLMFaceTracker.drawFaceModel( vis, face, showMesh, showConnections, 
        			true, true, true, triangles, connections, 1, boundingBoxColour, 
        			meshColour, connectionColour, pointColour );  
        	
        	// Draw the image to the panel
        	g.drawImage( ImageUtilities.createBufferedImage( vis ), 0, 0, null );
        }
        
        /**
         * 	Number of global parameters
         *	@return The number of global parameters
         */
        public int getNumGlobalParams()
        {
        	return face.clm._pglobl.getRowDimension();
        }
        
        /**
         * 	Get the value of the given global parameter.
         *	@param indx The index to get
         *	@return The value
         */
        public double getGlobalParam( int indx )
        {
        	return face.clm._pglobl.get( indx, 0 );
        }
        
        /**
         * 	Set the given index to the value in the global params
         * 	and update the face model.
         *	@param indx The index
         *	@param val The new value
         */
        public void setGlobalParam( int indx, double val )
        {
        	// Set the parameter
        	face.clm._pglobl.set( indx, 0, val );
        	
        	// Recalculate the shape.
			face.clm._pdm.CalcShape2D( face.shape, 
					face.clm._plocal, face.clm._pglobl );
			
			repaint();
        }
        
        /**
         * 	Number of local parameters
         *	@return The number of local parameters
         */
        public int getNumLocalParams()
        {
        	return face.clm._plocal.getRowDimension();        	
        }

        /**
         * 	Get the value of the given local parameter.
         *	@param indx The index to get
         *	@return The value
         */
        public double getLocalParam( int indx )
        {
        	return face.clm._plocal.get( indx, 0 );
        }
        
        /**
         * 	Set the given index to the value in the local params
         * 	and update the face model.
         *	@param indx The index
         *	@param val The new value
         */
        public void setLocalParam( int indx, double val )
        {
        	// Set the parameter
        	face.clm._plocal.set( indx, 0, val );
        	
        	// Recalculate the shape.
			face.clm._pdm.CalcShape2D( face.shape, 
					face.clm._plocal, face.clm._pglobl );
			
			repaint();
        }
    }

    /** The view of the model */
    private ModelView modelView = null;
    
    // -------------------------------------------------------
    // Note that all the slider values need to be 1,000 times
    // the size of the actual value as the sliders only work
    // in integer, so we divide the slider value by 1000 to get
    // the actual value.
    // -------------------------------------------------------
    
    /** Tool tip label text for each of the global sliders */
    private final String[] globalLabels = new String[]{
    		"Scale", "X Rotation", "Y Rotation", "Z Rotation", 
    		"Translate X", "Translate Y"
    };
    
    /** Tool tip label text for each of the local sliders */
    private final String[] localLabels = new String[]{	
    };
    
    /** Maximum values for each of the global sliders */
    private final int[] globalMaxs = new int[]{
    		20000, (int)(Math.PI*2000), (int)(Math.PI*2000), 
    		(int)(Math.PI*2000), 1000000, 1000000
    };
    
    /** Minimum values for each of the global sliders */
    private final int[] globalMins = new int[]{
    };
    
    /** Maximum values for each of the local sliders */
    private final int[] localMaxs = new int[]{
    };
    
    /** Minimum values for each of the local sliders */
    private final int[] localMins = new int[]{
    };
    
    /**
     * 	Defualt constructor
     */
    public ModelManipulatorGUI()
    {
    	init();
    }
    
    /**
     * 	Initialise the widgets
     */
    private void init()
    {
    	super.setLayout( new GridBagLayout() );
    	
    	modelView = new ModelView();
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = gbc.weighty = 1;
    	gbc.gridx = gbc.gridy = 1;
    	
    	this.add( modelView, gbc ); // 1,1
    	
    	// Add a panel to put the sliders on.
    	JPanel slidersPanel = new JPanel( new GridBagLayout() );
    	
    	// Add the global settings.
    	gbc.gridx = 1;
    	JPanel pGlobalSliders = new JPanel( new GridBagLayout() );
    	pGlobalSliders.setBorder( BorderFactory.createCompoundBorder( 
    			BorderFactory.createEmptyBorder( 4, 4, 4, 4 ), 
    			BorderFactory.createTitledBorder( "Pose" ) ) );
    	for( int i = 0; i < modelView.getNumGlobalParams(); i++ )
    	{
    		final int j = i;
    		int min = 0, max = 20000;
    		int val = (int)(modelView.getGlobalParam(i)*1000d);
    		System.out.println( i+" : "+val );
    		
    		if( j < globalMins.length )
    			min = globalMins[j];
    		if( j < globalMaxs.length )
    			max = globalMaxs[j];
    			
    		final JSlider s = new JSlider( min, max, val );
    		s.addChangeListener( new ChangeListener()
			{
				@Override
				public void stateChanged( ChangeEvent e )
				{
					System.out.println( "Slider "+j+" value "+s.getValue() );
					modelView.setGlobalParam( j, s.getValue()/1000d );
				}
			} );
    		
    		// Add a tooltip if we have one
    		if( i < globalLabels.length && globalLabels[i] != null )
    			s.setToolTipText( globalLabels[i] );
    		
    		pGlobalSliders.add( s, gbc );
    		gbc.gridy++;
    	}

    	gbc.gridy = 1;
    	slidersPanel.add( pGlobalSliders, gbc );
    	
    	// Add the local sliders
    	gbc.gridy = 1;
    	JPanel pLocalSliders = new JPanel( new GridBagLayout() );
    	pLocalSliders.setBorder( BorderFactory.createCompoundBorder( 
    			BorderFactory.createEmptyBorder( 4, 4, 4, 4 ), 
    			BorderFactory.createTitledBorder( "Local" ) ) );
    	for( int i = 0; i < modelView.getNumLocalParams(); i++ )
    	{
    		final int j = i;
    		int min = 0, max = 20000;
    		int val = (int)(modelView.getLocalParam(i)*1000d);
    		System.out.println( i+" : "+val );
    		
    		if( j < localMins.length )
    			min = localMins[j];
    		if( j < localMaxs.length )
    			max = localMaxs[j];
    			
    		final JSlider s = new JSlider( min, max, val );
    		s.addChangeListener( new ChangeListener()
			{
				@Override
				public void stateChanged( ChangeEvent e )
				{
					System.out.println( "Slider "+j+" value "+s.getValue() );
					modelView.setLocalParam( j, s.getValue()/1000d );
				}
			} );
    		
    		// Add a tooltip if we have one
    		if( i < localLabels.length && localLabels[i] != null )
    			s.setToolTipText( localLabels[i] );

    		pLocalSliders.add( s, gbc );
    		gbc.gridy++;
    	}

    	gbc.gridy = 2; gbc.gridx = 1;
    	slidersPanel.add( pLocalSliders, gbc );

    	gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.75;
    	this.add( slidersPanel, gbc ); // 2,1
    }
}
