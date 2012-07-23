package com.jsaragih;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.FImage;

import Jama.Matrix;

/**
 * Multiple face checker
 * 
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MFCheck {
	/** FCheck for each view */
	FCheck[] _fcheck;

	static MFCheck load(final String fname) throws FileNotFoundException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			Scanner sc = new Scanner(br);
			return read(sc, true);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	void save(final String fname) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));

			write(bw);
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
			}
		}
	}

	void write(BufferedWriter s) throws IOException {
		s.write(IO.Types.MFCHECK.ordinal() + " " + _fcheck.length + " ");

		for (int i = 0; i < _fcheck.length; i++)
			_fcheck[i].write(s);
	}

	/**
	 * Read the a {@link MFCheck}
	 * 
	 * @param s
	 * @param readType
	 * @return the {@link MFCheck} 
	 */
	public static MFCheck read(Scanner s, boolean readType) {
		if (readType) {
			int type = s.nextInt();
			assert (type == IO.Types.MFCHECK.ordinal());
		}

		MFCheck mfcheck = new MFCheck();

		int n = s.nextInt();
		mfcheck._fcheck = new FCheck[n];

		for (int i = 0; i < n; i++)
			mfcheck._fcheck[i] = FCheck.read(s, true);

		return mfcheck;
	}

	/**
	 * Check the whether its actually a face
	 * @param idx
	 * @param im
	 * @param s
	 * @return true if face; false otherwise
	 */
	public boolean check(int idx, FImage im, Matrix s) {
		assert ((idx >= 0) && (idx < _fcheck.length));

		return _fcheck[idx].check(im, s);
	}
}
