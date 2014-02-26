package org.openimaj.image.model.asm.datasets;

import java.util.List;

import org.openimaj.data.dataset.ListDataset;
import org.openimaj.image.Image;
import org.openimaj.image.model.asm.ActiveShapeModel;
import org.openimaj.math.geometry.shape.PointDistributionModel;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.util.pair.IndependentPair;

/**
 * Dataset representing pairs of images and fixed size sets of points, together
 * with a set of connections between points which are valid across all
 * instances. Useful for training {@link PointDistributionModel}s and
 * {@link ActiveShapeModel}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <IMAGE>
 *            type of the images in the collection
 */
public interface ShapeModelDataset<IMAGE extends Image<?, IMAGE>>
		extends
		ListDataset<IndependentPair<PointList, IMAGE>>
{
	/**
	 * Get the connections between the points
	 * 
	 * @return the connections
	 */
	public PointListConnections getConnections();

	/**
	 * Get the points for each instance
	 * 
	 * @return the point list for each instance
	 */
	public List<PointList> getPointLists();

	/**
	 * Get the image for each instance
	 * 
	 * @return the image for each instance
	 */
	public List<IMAGE> getImages();
}
