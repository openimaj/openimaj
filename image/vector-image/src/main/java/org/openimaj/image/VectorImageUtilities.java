package org.openimaj.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

public class VectorImageUtilities {

	private VectorImageUtilities() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Write an SVG image to a file with the given transcoder to determine the
	 * format
	 * 
	 * @param image
	 *            the image
	 * @param output
	 *            the file to write to
	 * @param trans
	 *            the transcoder
	 * @throws IOException
	 *             if an error occurs during writing
	 * @throws TranscoderException
	 *             if an error occurs during transcoding
	 */
	public static void write(final SVGImage image, final File output, Transcoder trans) throws IOException,
			TranscoderException
	{
		final TranscoderInput input = new TranscoderInput(image.createRenderer().getDocument());
		final TranscoderOutput toutput = new TranscoderOutput(new FileOutputStream(output));

		trans.transcode(input, toutput);
	}
}
