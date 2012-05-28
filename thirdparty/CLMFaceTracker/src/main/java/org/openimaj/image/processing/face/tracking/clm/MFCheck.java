package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.FImage;

import Jama.Matrix;

public class MFCheck {
	FCheck[] _fcheck; /**< FCheck for each view */

	//===========================================================================
	static MFCheck Load(final String fname) throws FileNotFoundException
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			Scanner sc = new Scanner(br);
			return Read(sc, true);
		} finally {
			try { br.close(); } catch (IOException e) {}
		}
	}

	//===========================================================================
	void Save(final String fname) throws IOException
	{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));

			Write(bw);
		} finally {
			try {
				if (bw != null) bw.close();
			} catch (IOException e) {}
		}
	}

	//===========================================================================
	void Write(BufferedWriter s) throws IOException
	{
		s.write(IO.Types.MFCHECK.ordinal() + " " + _fcheck.length + " ");

		for (int i = 0; i < _fcheck.length; i++)
			_fcheck[i].Write(s); 
	}

	//===========================================================================
	static MFCheck Read(Scanner s, boolean readType)
	{
		if (readType) { 
			int type = s.nextInt(); 
			assert(type == IO.Types.MFCHECK.ordinal());
		}
		
		MFCheck mfcheck = new MFCheck();
		
		int n = s.nextInt();
		mfcheck._fcheck = new FCheck[n];

		for (int i = 0; i < n; i++)
			mfcheck._fcheck[i] = FCheck.Read(s, true);
		
		return mfcheck;
	}

	//===========================================================================
	boolean Check(int idx, FImage im, Matrix s)
	{
		assert((idx >= 0) && (idx < _fcheck.length));

		return _fcheck[idx].Check(im,s);
	}
}
