package org.openimaj.hadoop.tools.exif;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifTool.Tag;
import com.thebuzzmedia.exiftool.RDFExifTool;

public enum EXIFOutputMode {
	TEXT{

		@Override
		public void output(PrintWriter pw, File tmp, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException {
			Map<Tag, String> allExif = tool.getImageMeta(tmp.getAbsoluteFile(), ExifTool.Tag.values());
			for(Entry<Tag,String> entry: allExif.entrySet()){
				pw.print(entry.getKey());
				pw.print(' ');
				pw.print('"');
				pw.print(entry.getValue());
				pw.print('"');
				pw.println();
			}
			pw.close();
		}
	},
	RDF{
		@Override
		public void output(PrintWriter pw, File tmp, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException {
			String allExif = tool.getImageRDF(tmp.getAbsoluteFile(), ExifTool.Tag.values());
			pw.print(allExif);
			pw.close();
		}
		@Override
		public void output(PrintWriter pw, File tmp, String realName, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException
		{
			String allExif = tool.getImageRDF(tmp.getAbsoluteFile(), realName,ExifTool.Tag.values());
			pw.print(allExif);
			pw.close();
		}
	};

	public abstract void output(PrintWriter output, File tmp, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException;
	public void output(PrintWriter output, File tmp, String realName, RDFExifTool tool) throws IllegalArgumentException, SecurityException, IOException
	{
		output(output,tmp,tool);
	}
	
}
