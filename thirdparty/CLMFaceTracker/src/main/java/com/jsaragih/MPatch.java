package com.jsaragih;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.FImage;

/**
 * Multiple patches
 * 
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MPatch {
	/** Width of patch */
	public int _w;

	/** Height of patch */
	public int _h;

	/** List of patches */
	public Patch[] _p;

	private FImage res_;

	MPatch(Patch[] p) {
		_w = p[0].matcher.getTemplate().width;
		_h = p[0].matcher.getTemplate().height;

		for (int i = 1; i < p.length; i++) {
			if ((p[i].matcher.getTemplate().width != _w)
					|| (p[i].matcher.getTemplate().height != _h)) {
				throw new IllegalArgumentException(
						"Patches must all have the same size");
			}
		}

		_p = p;
		res_ = new FImage(0, 0);
	}

	MPatch() {
	}

	static MPatch load(final String fname) throws FileNotFoundException {
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
		s.write(IO.Types.MPATCH.ordinal() + " " + _w + " " + _h + " "
				+ _p.length + " ");

		for (int i = 0; i < _p.length; i++)
			_p[i].write(s);
	}

	static MPatch read(Scanner s, boolean readType) {
		if (readType) {
			int type = s.nextInt();
			assert (type == IO.Types.MPATCH.ordinal());
		}

		MPatch mpatch = new MPatch();

		mpatch._w = s.nextInt();
		mpatch._h = s.nextInt();
		int n = s.nextInt();

		mpatch._p = new Patch[n];
		for (int i = 0; i < n; i++)
			mpatch._p[i] = Patch.read(s, true);

		return mpatch;
	}

	final void sum2one(FImage M) {
		M.divideInplace(M.sum());
	}

	void response(FImage im, FImage resp) {
		assert ((im.height >= _h) && (im.width >= _w));

		int h = im.height - _h + 1, w = im.width - _w + 1;

		if (resp.height != h || resp.width != w)
			resp.internalAssign(new FImage(w, h));

		if (res_ == null)
			res_ = new FImage(w, h);
		if (res_.height != h || res_.width != w)
			res_.internalAssign(new FImage(w, h));

		if (_p.length == 1) {
			_p[0].response(im, resp);
			sum2one(resp);
		} else {
			resp.fill(1);

			for (int i = 0; i < _p.length; i++) {
				_p[i].response(im, res_);
				sum2one(res_);
				resp.multiplyInplace(res_);
			}

			sum2one(resp);
		}
	}

	/**
	 * Returns a copy of this MPatch
	 * 
	 * @return A copy of this object
	 */
	public MPatch copy() {
		MPatch m = new MPatch();
		m._w = _w;
		m._h = _h;
		m.res_ = res_;
		m._p = new Patch[_p.length];
		for (int i = 0; i < _p.length; i++)
			m._p[i] = _p[i].copy();
		return m;
	}
}
