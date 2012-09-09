package org.openimaj.image.neardups.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;


public class CompressSimulation extends Simulation {
	protected float minCompression = 0.1f;
	protected float maxCompression = 1f;
	
	public CompressSimulation(int seed) {
		super(seed);
	}
	
	public CompressSimulation(int seed, float minCompression, float maxCompression) {
		super(seed);
		this.maxCompression = maxCompression;
		this.minCompression = minCompression;
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		try {
			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			
			float compressionFactor = this.randomFloatInRange(minCompression, maxCompression);
			
			iwp.setCompressionQuality(compressionFactor);

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			writer.setOutput(new MemoryCacheImageOutputStream(output));
			
			IIOImage image = new IIOImage(ImageUtilities.createBufferedImage(input), null, null);
			writer.write(null, image, iwp);
			
			return ImageUtilities.readMBF(new ByteArrayInputStream(output.toByteArray()));
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
