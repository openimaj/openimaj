package org.kohsuke.args4j;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;


public class MBFImageOptionHandler extends OptionHandler<MBFImage> {
	public MBFImageOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super MBFImage> setter) {
		super(parser, option, setter);
	}

	@Override
	public String getDefaultMetaVariable() {
		return "imageFile";
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		File file = new File(params.getParameter(0));
		try {
			setter.addValue(ImageUtilities.readMBF(file));
		} catch (IOException e) {
			throw new CmdLineException(owner, "Error opening image file " + file, e);
		}
		
		return 1;
	}
}