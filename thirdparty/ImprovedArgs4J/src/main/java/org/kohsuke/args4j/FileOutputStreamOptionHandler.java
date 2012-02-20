package org.kohsuke.args4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class FileOutputStreamOptionHandler extends OptionHandler<FileOutputStream> {
	public FileOutputStreamOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super FileOutputStream> setter) {
		super(parser, option, setter);
	}

	@Override
	public String getDefaultMetaVariable() {
		return "outputFile";
	}

	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		File file = new File(params.getParameter(0));
		try {
			setter.addValue(new FileOutputStream(file));
		} catch (IOException e) {
			throw new CmdLineException(owner, "Error opening stream to output file " + file, e);
		}
		
		return 1;
	}
}

