package org.openimaj.workinprogress.sgdsvm;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.openimaj.util.array.SparseBinSearchFloatArray;
import org.openimaj.util.array.SparseFloatArray;

import gnu.trove.list.array.TDoubleArrayList;

public class Loader {
	String filename;
	boolean compressed;
	boolean binary;
	DataInputStream bfs;
	Scanner tis;

	public Loader(String name) throws FileNotFoundException, IOException {
		filename = name;
		compressed = binary = false;
		if (filename.endsWith(".txt.gz"))
			compressed = true;
		else if (filename.endsWith(".bin.gz"))
			compressed = binary = true;
		else if (filename.endsWith(".bin"))
			binary = true;
		else if (filename.endsWith(".txt"))
			binary = false;
		else
			throw new AssertionError("Filename suffix should be one of: .bin, .txt, .bin.gz, .txt.gz");
		InputStream fs;
		if (compressed)
			fs = new GZIPInputStream(new FileInputStream(name), 65536);
		else
			fs = new BufferedInputStream(new FileInputStream(name), 65536);

		if (binary)
			bfs = new DataInputStream(fs);
		else
			tis = new Scanner(fs);
	}

	public int load(List<SparseFloatArray> xp, TDoubleArrayList yp, boolean normalize, int maxrows, int[] p_maxdim,
			int[] p_pcount, int[] p_ncount) throws IOException
	{
		int ncount = 0;
		int pcount = 0;
		while (maxrows-- != 0) {
			final SparseFloatArray x = new SparseBinSearchFloatArray(0);
			final double y;
			if (binary) {
				y = (bfs.read() == 1) ? +1 : -1;
				load(x, bfs);
			} else {
				if (!tis.hasNextDouble())
					break;
				// final f >> std::skipws >> y >> std::ws;
				y = tis.nextDouble();
				// if (f.peek() == '|') f.get();
				// if (tis.hasNext("^|"))
				// tis.skip("^|");
				// f >> x;
				load(x, tis);
			}

			if (normalize) {
				final double d = x.dotProduct(x);
				if (d > 0 && d != 1.0)
					x.multiplyInplace(1.0 / Math.sqrt(d));
			}
			if (y != +1 && y != -1)
				throw new AssertionError("Label should be +1 or -1.");
			xp.add(x);
			yp.add(y);
			if (y > 0)
				pcount += 1;
			else
				ncount += 1;
			if (p_maxdim != null && x.size() > p_maxdim[0])
				p_maxdim[0] = x.size();
		}
		if (p_pcount != null)
			p_pcount[0] = pcount;
		if (p_ncount != null)
			p_ncount[0] = ncount;
		return pcount + ncount;
	}

	private void load(SparseFloatArray v, Scanner sc) {
		int sz = 0;
		int msz = 1024;
		v.setLength(msz);
		final String line = sc.nextLine();

		final String[] parts = line.trim().split("\\s");
		for (final String p : parts) {
			final String[] p2 = p.trim().split(":");
			final int idx = Integer.parseInt(p2[0].trim());
			final float val = Float.parseFloat(p2[1].trim());

			if (idx >= sz)
				sz = idx + 1;
			if (idx >= msz) {
				while (idx >= msz)
					msz += msz;
				v.setLength(msz);
			}

			v.set(idx, val);
		}
		v.compact();
	}

	private void load(SparseFloatArray x, DataInputStream fs) throws IOException {
		int sz = 0;
		int msz = 1024;
		x.setLength(msz);
		final int npairs = fs.readInt();

		if (npairs < 0)
			throw new AssertionError("bad format");
		for (int i = 0; i < npairs; i++) {
			final int idx = fs.readInt();
			final float val = fs.readFloat();

			if (idx >= sz)
				sz = idx + 1;
			if (idx >= msz) {
				while (idx >= msz)
					msz += msz;
				x.setLength(msz);
			}

			x.set(idx, val);
		}
		x.compact();
	}

}
