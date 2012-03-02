/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos.sandbox.asm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.shape.PointListConnections;
import org.openimaj.util.pair.IndependentPair;

public class ASFDataset {
	private List<IndependentPair<PointList, FImage>> data = new ArrayList<IndependentPair<PointList, FImage>>();
	private PointListConnections connections;
	
	public ASFDataset(File baseDir) throws IOException {
		this(baseDir, 0);
	}
	
	public ASFDataset(File baseDir, int n) throws IOException {
		File[] files = baseDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".asf");
			}
		});
		
		if (n>0)
			files = Arrays.copyOf(files, Math.min(files.length, n));
		
		for (File f : files) {
			getData().add(readASF(f));
		}
		
		connections = readASFConnections(files[0]);
	}
	
	public static IndependentPair<PointList, FImage> readASF(File file) throws IOException {
		PointList pl = new PointList();
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("#")) {
				String[] parts = line.split("\\s+");

				if (parts.length < 7)
					continue;

				float x = Float.parseFloat(parts[2].trim());
				float y = Float.parseFloat(parts[3].trim());

				pl.points.add(new Point2dImpl(x, y));
			}
		}
		br.close();

		File imgFile = new File(file.getAbsolutePath().replace(".asf", ".bmp"));
		if (!imgFile.exists())
			imgFile = new File(file.getAbsolutePath().replace(".asf", ".jpg"));
			
		FImage image = ImageUtilities.readF(imgFile);

		pl.scaleXY(image.width, image.height);

		return new IndependentPair<PointList, FImage>(pl, image);
	}

	private PointListConnections readASFConnections(File file) throws IOException {
		PointListConnections plc = new PointListConnections();
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("#")) {
				String[] parts = line.split("\\s+");

				if (parts.length < 7)
					continue;

				int from = Integer.parseInt(parts[4].trim());
				int to = Integer.parseInt(parts[6].trim());

				plc.addConnection(from, to);
			}
		}
		br.close();

		return plc;
	}

	public List<IndependentPair<PointList, FImage>> getData() {
		return data;
	}
	
	public PointListConnections getConnections() {
		return connections;
	}
}
