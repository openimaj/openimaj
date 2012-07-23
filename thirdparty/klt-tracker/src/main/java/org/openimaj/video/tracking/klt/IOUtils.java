/**
 * This source code file is part of a direct port of Stan Birchfield's implementation
 * of a Kanade-Lucas-Tomasi feature tracker. The original implementation can be found
 * here: http://www.ces.clemson.edu/~stb/klt/
 *
 * As per the original code, the source code is in the public domain, available
 * for both commercial and non-commercial use.
 */
package org.openimaj.video.tracking.klt;

class IOUtils {
	enum StructureType {FEATURE_LIST, FEATURE_HISTORY, FEATURE_TABLE}

	static String warning_line = "!!! Warning:  This is a KLT data file. Do not modify below this line !!!\n";
	static String binheader_fl = "KLTFL1";
	static String binheader_fh = "KLTFH1";
	static String binheader_ft = "KLTFT1";

	private IOUtils() {}

	static String [] setupTxtFormat(
			String fmt)		/* Input: format (e.g., %5.1f or %3d) */
	{
		String format;	/* Output: format (e.g., (%5.1f,%5.1f)=%3d) */
		String type;	/* Output: either 'f' or 'd', based on input format */
		final int val_width = 5;

		/* Parse format */
		if (fmt.charAt(0) != '%')
			throw new RuntimeException(String.format("(KLTWriteFeatures) Bad Format: %s\n", fmt));

		type = fmt.substring(fmt.length()-1);

		if (!type.equals("f") && !type.equals("d"))
			throw new RuntimeException("(KLTWriteFeatures) Format must end in 'f' or 'd'.");

		/* Construct feature format */
		format = String.format("(%s,%s)=%%%dd ", fmt, fmt, val_width);

		return new String[] {format, type};
	}

	static String getNhyphens(int n)
	{
		String s = "";
		for (int i = 0 ; i < n ; i++)
			s += "-";
		return s;
	}

	static String getInteger(int integer, int width)
	{
		String fmt;
		fmt = String.format("%%%dd", width);
		return String.format(fmt, integer);
	}

	/*********************************************************************
	 * _findStringWidth
	 *
	 * Calculates the length of a string after expansion.  E.g., the
	 * length of "(%6.1f)" is eight -- six for the floating-point number,
	 * and two for the parentheses.
	 */
	private static int findStringWidth(String str)
	{
		int width = 0;
		int add;
		int maxi = str.length() - 1;
		int i = 0;


		while (i <= maxi)  {
			if (str.charAt(i) == '%')  {

				if (Character.isDigit(str.charAt(i+1))) {
					//Scanner s = new Scanner(str.substring(i+1));
					//add = s.nextInt();
					int stop = i+2;
					while (stop<=maxi && Character.isDigit(str.charAt(stop))) stop++;
					add = Integer.parseInt(str.substring(i+1, stop));
					
					width += add;
					i += 2;
					while (!"diouxefgn".contains(str.charAt(i)+"")) {
						i++;
						if (i > maxi)
							throw new RuntimeException(
									String.format("(_findStringWidth) Can't determine length of string '%s'", str)
							);
					}
					i++;
				} else if (str.charAt(i+1) == 'c')  {
					width++;
					i += 2;
				} else 
					throw new RuntimeException(
							String.format("(_findStringWidth) Can't determine length of string '%s'", str)
					);
			} else {
				i++;
				width++;
			}
		}

		return width;
	}

	static String getHeader(
			String format,
			StructureType id,
			int nFrames,
			int nFeatures,
			boolean comments)
	{
		int width = findStringWidth(format);
		String s = "";

		assert(id == StructureType.FEATURE_LIST || id == StructureType.FEATURE_HISTORY || id == StructureType.FEATURE_TABLE);

		if (comments)  {
			s += "Feel free to place comments here.\n\n\n";
			s += "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n";
			s += warning_line;
			s += "\n";
		}
		s += "------------------------------\n";
		switch (id)  {
		case FEATURE_LIST: 		s += "KLT Feature List\n";    break;
		case FEATURE_HISTORY: 	s += "KLT Feature History\n"; break;
		case FEATURE_TABLE: 	s += "KLT Feature Table\n";   break;
		}

		s += "------------------------------\n\n";
		switch (id)  {
		case FEATURE_LIST: 		s += String.format("nFeatures = %d\n\n", nFeatures);    break;
		case FEATURE_HISTORY: 	s += String.format("nFrames = %d\n\n", nFrames); 		break;
		case FEATURE_TABLE: 	s += String.format("nFrames = %d, nFeatures = %d\n\n", nFrames, nFeatures);   break;
		}

		switch (id)  {
		case FEATURE_LIST: s += "feature | (x,y)=val\n";
		s += "--------+-";
		s += getNhyphens(width);
		s += "\n";   
		break;
		case FEATURE_HISTORY: s+= "frame | (x,y)=val\n";
		s += "------+-";
		s += getNhyphens(width);
		s += "\n";   
		break;
		case FEATURE_TABLE: s += "feature |          frame\n";   
		s += "        |";   
		for (int i = 0 ; i < nFrames ; i++) s += getInteger(i, width);
		s += "\n--------+-";   
		for (int i = 0 ; i < nFrames ; i++) s += getNhyphens(width);
		s += "\n";   
		break;
		}

		return s;
	}
}
