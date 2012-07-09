/**
 * 
 */
package org.openimaj.image.processing.face.tracking.clm.demo.model;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.tracking.clm.CLMFaceTracker;
import org.openimaj.image.processing.face.tracking.clm.Tracker;
import org.openimaj.image.processing.face.tracking.clm.Tracker.TrackedFace;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *	
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
        
		/**
		 * 	Constructor
		 */
        public ModelView()
        {
        	this.setPreferredSize( new Dimension(600,600) );
        	
        	CLMFaceTracker t = new CLMFaceTracker();
        	this.triangles = t.getReferenceTriangles();
        	this.connections = t.getReferenceConnections();
        	face = new TrackedFace( new Rectangle(100,100,500,500), t.getInitialVars() );
        }
        
        /**
         *	{@inheritDoc}
         * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        @Override
        public void paint( Graphics g )
        {
        	vis.zero();
        	
        	CLMFaceTracker.drawFaceModel( vis, face, true, true, true, 
        			true, true, triangles, connections, 1 );  
        	
        	g.drawImage( ImageUtilities.createBufferedImage( vis ), 0, 0, null );
        }
    }

    /** The view of the model */
    private ModelView modelView = null;
    
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
    	modelView = new ModelView();
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = gbc.weighty = 1;
    	gbc.gridx = gbc.gridy = 1;
    	this.add( modelView, gbc );
    }
}
