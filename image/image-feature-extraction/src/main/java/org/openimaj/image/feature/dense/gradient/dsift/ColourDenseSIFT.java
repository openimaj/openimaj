package org.openimaj.image.feature.dense.gradient.dsift;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Implementation of colour dense-sift. The algorithm works by applying the
 * {@link DenseSIFT} extractor to each channel of the image (in the target
 * {@link ColourSpace}) and then aggregating the descriptors across all
 * {@link ColourSpace}s for the same spatial location.
 * <p>
 * Any {@link ColourSpace} can be used. To compute the contrast energy of a
 * descriptor, the luminance is extracted across channels using
 * {@link ColourSpace#computeIntensity(float[])}. This means that if you choose
 * a {@link ColourSpace} that doesn't support intensities, then the contrast
 * energy will be zero. In practice this should not matter as it's most usual to
 * use the {@link ColourSpace#RGB}, {@link ColourSpace#HSV} or
 * {@link ColourSpace#OPPONENT} colour spaces, which can compute intensities.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ColourDenseSIFT extends AbstractDenseSIFT<MBFImage> {
	DenseSIFT dsift;
	ColourSpace colourSpace;

	float[][] descriptors;
	float[] energies;

	/**
	 * Construct with the given internal {@link DenseSIFT} extractor to apply to
	 * each band of the image created by converting the input to
	 * {@link #analyseImage(MBFImage)} or
	 * {@link #analyseImage(MBFImage, Rectangle)} to the given
	 * {@link ColourSpace}.
	 * 
	 * @param dsift
	 *            the dense sift extractor
	 * @param colourSpace
	 *            the target colour space
	 */
	public ColourDenseSIFT(DenseSIFT dsift, ColourSpace colourSpace) {
		this.dsift = dsift;
		this.colourSpace = colourSpace;
	}

	@Override
	public void analyseImage(MBFImage image, Rectangle bounds) {
		// handle colour conversion?
		final MBFImage cimg = colourSpace.convert(image);

		// first band:
		dsift.analyseImage(cimg.bands.get(0), bounds);
		final int len = dsift.descriptors[0].length;
		descriptors = new float[dsift.descriptors.length][len * cimg.bands.size()];
		final float[][] tmpEnergies = new float[dsift.descriptors.length][cimg.bands.size()];

		for (int i = 0; i < descriptors.length; i++) {
			System.arraycopy(dsift.descriptors[i], 0, descriptors[i], 0, len);
			tmpEnergies[i][0] = dsift.energies[i];
		}

		// other bands
		for (int j = 1; j < cimg.bands.size(); j++) {
			dsift.analyseImage(cimg.bands.get(j), bounds);
			for (int i = 0; i < descriptors.length; i++) {
				System.arraycopy(dsift.descriptors[i], 0, descriptors[i], j * len, len);
				tmpEnergies[i][j] = dsift.energies[i];
			}
		}

		// deal with energies
		energies = new float[descriptors.length];
		for (int i = 0; i < descriptors.length; i++) {
			energies[i] = colourSpace.computeIntensity(tmpEnergies[i]);
		}
	}

	@Override
	public LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints() {
		final MemoryLocalFeatureList<FloatDSIFTKeypoint> keys = new MemoryLocalFeatureList<FloatDSIFTKeypoint>(
				dsift.numOriBins
						* dsift.numBinsX * dsift.numBinsY, descriptors.length);

		final int frameSizeX = dsift.binWidth * (dsift.numBinsX - 1) + 1;
		final int frameSizeY = dsift.binHeight * (dsift.numBinsY - 1) + 1;

		final float deltaCenterX = 0.5F * dsift.binWidth * (dsift.numBinsX - 1);
		final float deltaCenterY = 0.5F * dsift.binHeight * (dsift.numBinsY - 1);

		for (int framey = dsift.data.boundMinY, i = 0; framey <= dsift.data.boundMaxY - frameSizeY + 1; framey += dsift.stepY)
		{
			for (int framex = dsift.data.boundMinX; framex <= dsift.data.boundMaxX - frameSizeX + 1; framex += dsift.stepX, i++)
			{
				keys.add(new FloatDSIFTKeypoint(framex + deltaCenterX, framey + deltaCenterY, descriptors[i], energies[i]));
			}
		}

		return keys;
	}

	@Override
	public LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints() {
		final MemoryLocalFeatureList<ByteDSIFTKeypoint> keys = new MemoryLocalFeatureList<ByteDSIFTKeypoint>(
				dsift.numOriBins
						* dsift.numBinsX * dsift.numBinsY, descriptors.length);

		final int frameSizeX = dsift.binWidth * (dsift.numBinsX - 1) + 1;
		final int frameSizeY = dsift.binHeight * (dsift.numBinsY - 1) + 1;

		final float deltaCenterX = 0.5F * dsift.binWidth * (dsift.numBinsX - 1);
		final float deltaCenterY = 0.5F * dsift.binHeight * (dsift.numBinsY - 1);

		for (int framey = dsift.data.boundMinY, i = 0; framey <= dsift.data.boundMaxY - frameSizeY + 1; framey += dsift.stepY)
		{
			for (int framex = dsift.data.boundMinX; framex <= dsift.data.boundMaxX - frameSizeX + 1; framex += dsift.stepX, i++)
			{
				keys.add(new ByteDSIFTKeypoint(framex + deltaCenterX, framey + deltaCenterY, descriptors[i], energies[i]));
			}
		}

		return keys;
	}

	@Override
	public LocalFeatureList<FloatDSIFTKeypoint> getFloatKeypoints(float energyThreshold) {
		final MemoryLocalFeatureList<FloatDSIFTKeypoint> keys = new MemoryLocalFeatureList<FloatDSIFTKeypoint>(
				dsift.numOriBins
						* dsift.numBinsX * dsift.numBinsY);

		final int frameSizeX = dsift.binWidth * (dsift.numBinsX - 1) + 1;
		final int frameSizeY = dsift.binHeight * (dsift.numBinsY - 1) + 1;

		final float deltaCenterX = 0.5F * dsift.binWidth * (dsift.numBinsX - 1);
		final float deltaCenterY = 0.5F * dsift.binHeight * (dsift.numBinsY - 1);

		for (int framey = dsift.data.boundMinY, i = 0; framey <= dsift.data.boundMaxY - frameSizeY + 1; framey += dsift.stepY)
		{
			for (int framex = dsift.data.boundMinX; framex <= dsift.data.boundMaxX - frameSizeX + 1; framex += dsift.stepX, i++)
			{
				if (energies[i] >= energyThreshold)
					keys.add(new FloatDSIFTKeypoint(framex + deltaCenterX, framey + deltaCenterY, descriptors[i],
							energies[i]));
			}
		}

		return keys;
	}

	@Override
	public LocalFeatureList<ByteDSIFTKeypoint> getByteKeypoints(float energyThreshold) {
		final MemoryLocalFeatureList<ByteDSIFTKeypoint> keys = new MemoryLocalFeatureList<ByteDSIFTKeypoint>(
				dsift.numOriBins
						* dsift.numBinsX * dsift.numBinsY);

		final int frameSizeX = dsift.binWidth * (dsift.numBinsX - 1) + 1;
		final int frameSizeY = dsift.binHeight * (dsift.numBinsY - 1) + 1;

		final float deltaCenterX = 0.5F * dsift.binWidth * (dsift.numBinsX - 1);
		final float deltaCenterY = 0.5F * dsift.binHeight * (dsift.numBinsY - 1);

		for (int framey = dsift.data.boundMinY, i = 0; framey <= dsift.data.boundMaxY - frameSizeY + 1; framey += dsift.stepY)
		{
			for (int framex = dsift.data.boundMinX; framex <= dsift.data.boundMaxX - frameSizeX + 1; framex += dsift.stepX, i++)
			{
				if (energies[i] >= energyThreshold)
					keys.add(new ByteDSIFTKeypoint(framex + deltaCenterX, framey + deltaCenterY, descriptors[i],
							energies[i]));
			}
		}

		return keys;
	}

	@Override
	public void setBinWidth(int size) {
		this.dsift.setBinWidth(size);
	}

	@Override
	public void setBinHeight(int size) {
		this.dsift.setBinHeight(size);
	}

	@Override
	public int getBinWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBinHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumBinsX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumBinsY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumOriBins() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float[][] getDescriptors() {
		return descriptors;
	}
}
