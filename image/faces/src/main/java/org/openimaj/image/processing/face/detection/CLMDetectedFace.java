package org.openimaj.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

/**
 * A constrained local model detected face. In addition to the patch
 * and detection rectangle, also provides the shape matrix (describing the
 * 2D point positions, and the weight vectors for the model pose (relative to
 * the detection image) and shape.
 *   
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jason M. Saragih", "Simon Lucey", "Jeffrey F. Cohn" },
		title = "Face alignment through subspace constrained mean-shifts",
		year = "2009",
		booktitle = "IEEE 12th International Conference on Computer Vision, ICCV 2009, Kyoto, Japan, September 27 - October 4, 2009",
		pages = { "1034", "1041" },
		publisher = "IEEE",
		customData = {
			"doi", "http://dx.doi.org/10.1109/ICCV.2009.5459377",
			"researchr", "http://researchr.org/publication/SaragihLC09",
			"cites", "0",
			"citedby", "0"
		}
	)
public class CLMDetectedFace extends DetectedFace {
	private Matrix shape;
	private Matrix poseParameters;
	private Matrix shapeParameters;

	protected CLMDetectedFace() {}
	
	/**
	 * Construct with the given bounds, shape and pose parameters and
	 * detection image. The face patch is extracted automatically.
	 * 
	 * @param bounds
	 * @param shape
	 * @param poseParameters
	 * @param shapeParameters
	 * @param fullImage
	 */
	public CLMDetectedFace(Rectangle bounds, Matrix shape, Matrix poseParameters, Matrix shapeParameters, FImage fullImage) {
		super(bounds, fullImage.extractROI(bounds));
		this.shape = shape;
		this.poseParameters = poseParameters;
		this.shapeParameters = shapeParameters;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		
		IOUtils.write(shape, out);
		IOUtils.write(poseParameters, out);
		IOUtils.write(shapeParameters, out);
	}

	@Override
	public byte[] binaryHeader() {
		return "DF".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		shape = IOUtils.read(in);
		poseParameters = IOUtils.read(in);
		shapeParameters = IOUtils.read(in);
	}
}
