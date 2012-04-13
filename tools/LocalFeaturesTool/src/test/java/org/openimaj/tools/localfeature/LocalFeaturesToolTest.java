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
package org.openimaj.tools.localfeature;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.tools.localfeature.LocalFeaturesTool;


public class LocalFeaturesToolTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private File tmpImageFile;
	private File tmpNormImageFile;
	private MBFImage loaded;
	private MBFImage normalised;

	@Before
	public void setup() throws IOException{
		InputStream is = this.getClass().getResourceAsStream("/org/openimaj/image/data/cat.jpg");
		tmpImageFile = folder.newFile("cat.jpg");
		tmpNormImageFile = folder.newFile("catIntensityNormalised.jpg");
		
		FileOutputStream fos = new FileOutputStream(tmpImageFile);
		byte[] arr = new byte[1024];
		int read = is.read(arr);
		while(read != -1) {
			try{
				fos.write(arr, 0, read);
				read = is.read(arr);
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
		
		loaded = ImageUtilities.readMBF(tmpImageFile);
		normalised = Transforms.RGB_TO_RGB_NORMALISED(loaded);
		
		ImageUtilities.write(loaded, "jpg", tmpImageFile);
		ImageUtilities.write(normalised.getBand(1) , "jpg", tmpNormImageFile);
		System.out.println("Image out: " + tmpImageFile );
		System.out.println("Normalised Image out: " + tmpNormImageFile);
	}
	
	@Test
	public void testKeypointGeneration() throws IOException{
		File tmpKeypointFile = folder.newFile("keypoint-testKeypointGeneration.key");
		File tmpASCIIKeypointFile = folder.newFile("keypoint-testKeypointGeneration2.key");
		String[] args = null;
		args = new String[]{
			"-i",tmpImageFile.getAbsolutePath(),
			"-o",tmpKeypointFile.getAbsolutePath()
		};
		LocalFeaturesTool.main(args);
		
		args = new String[]{
			"-a",
			"-i",tmpImageFile.getAbsolutePath(),
			"-o",tmpASCIIKeypointFile.getAbsolutePath()
		};
		LocalFeaturesTool.main(args);
		
		List<Keypoint> binary = MemoryLocalFeatureList.read(tmpKeypointFile, Keypoint.class);
		List<Keypoint> ascii = MemoryLocalFeatureList.read(tmpASCIIKeypointFile, Keypoint.class);
		for(int i = 0; i<binary.size();i++){
			assertTrue(Arrays.equals(binary.get(i).ivec,ascii.get(i).ivec));
		}
	}
	
	@Test
	public void testGridKeypointScales() throws IOException{
//		File tmpNormalKeypointFile = File.createTempFile("keypoint", ".key");
//		File tmpMoreKeypointFile = File.createTempFile("keypoint", ".key");
//		File tmpLessKeypointFile = File.createTempFile("keypoint", ".key");
//		String[] args = null;
//		args = new String[]{
//			"-i",tmpImageFile.getAbsolutePath(),
//			"-o",tmpNormalKeypointFile.getAbsolutePath(),
//			"-m",KeypointMode.GRID.toString()
//		};
//		JKeypoint.main(args);
//		
//		args = new String[]{
//				"-i",tmpImageFile.getAbsolutePath(),
//				"-o",tmpLessKeypointFile.getAbsolutePath(),
//				"-m",KeypointMode.GRID.toString(),
//				"-g","3.0"
//			};
//		JKeypoint.main(args);
//		args = new String[]{
//				"-i",tmpImageFile.getAbsolutePath(),
//				"-o",tmpMoreKeypointFile.getAbsolutePath(),
//				"-m",KeypointMode.GRID.toString(),
//				"-g","0.5"
//			};
//		JKeypoint.main(args);
//		
//		
//		List<Keypoint> normal = MemoryLocalFeatureList.read(tmpNormalKeypointFile, Keypoint.class);
//		List<Keypoint> less = MemoryLocalFeatureList.read(tmpLessKeypointFile, Keypoint.class);
//		List<Keypoint> more = MemoryLocalFeatureList.read(tmpMoreKeypointFile, Keypoint.class);
//		
//		System.out.println("Normal: " + normal.size());
//		System.out.println("Less (x3 step): " + less.size());
//		System.out.println("More (x0.5 step): " + more.size());
//		
//		assertTrue(normal.size() > less.size());
//		assertTrue(normal.size() < more.size());
		
	}
	
	@Test public void testKeypointModes() throws IOException{
//		for(KeypointMode mode : KeypointMode.values()){
//			File tmpKeypointFile = File.createTempFile("keypoint", ".key");
//			File tmpASCIIKeypointFile = File.createTempFile("keypoint", ".key");
//			System.out.println("Generating Keypoint: " + mode );
//			String[] args = new String[]{
//					"-i",tmpImageFile.getAbsolutePath(),
//					"-o",tmpKeypointFile.getAbsolutePath(),
//					"-m",mode.toString()
//					
//			};
//			JKeypoint.main(args);
//			args = new String[]{
//					"-a",
//					"-i",tmpImageFile.getAbsolutePath(),
//					"-o",tmpASCIIKeypointFile.getAbsolutePath(),
//					"-m",mode.toString()
//			};
//			JKeypoint.main(args);
//			
//			Class<? extends Keypoint> clz = Keypoint.class;
//			if (mode == KeypointMode.MIN_MAX_SIFT)
//				clz = MinMaxKeypoint.class;
//			if (mode == KeypointMode.ASIFTENRICHED)
//				clz = AffineSimulationKeypoint.class;
//			
//			List<? extends Keypoint> binary = MemoryLocalFeatureList.read(tmpKeypointFile, clz);
//			List<? extends Keypoint> ascii = MemoryLocalFeatureList.read(tmpASCIIKeypointFile, clz);
//			
//			System.out.println("... Comparing ASCII version to Binary version");
//			for(int i = 0; i<binary.size();i++){
//				assertTrue(Arrays.equals(binary.get(i).ivec,ascii.get(i).ivec));
//			}
//		}
	}
	
//	@Test
//	public void testKeypointColour() throws IOException{
//		File tmpKeypointFile = File.createTempFile("keypoint", ".key");
//		File tmpRedKeypointFile = File.createTempFile("keypointRGB", ".key");
//		tmpRedKeypointFile.delete();
//		
//		String[] args = null;
//		args = new String[]{
//			"-i",tmpImageFile.getAbsolutePath(),
//			"-o",tmpKeypointFile.getAbsolutePath()
//		};
//		LocalFeaturesTool.main(args);
//		
//		args = new String[]{
//			"-i",tmpImageFile.getAbsolutePath(),
//			"-o",tmpRedKeypointFile.getAbsolutePath(),
//			"-a",
//			"-m","SIFT",
//			"-cm","INTENSITY_COLOUR"
//		};
//		LocalFeaturesTool.main(args);
//		
//		assertTrue(tmpRedKeypointFile.exists());
//		List<Keypoint> blue = MemoryLocalFeatureList.read(tmpRedKeypointFile, Keypoint.class);
//		assertTrue(blue.get(0).ivec.length == 128 * 4);
//	}
	
	@Test
	public void testKeypointImageTransform() throws IOException {
		File tmpKeypointFile = folder.newFile("keypoint-testKeypointImageTransform.key");
		File tmpResizedKeypointFile = folder.newFile("keypointResized-testKeypointImageTransform.key");
		tmpResizedKeypointFile.delete();
		
		String[] args = null;
		args = new String[]{
			"-i",tmpImageFile.getAbsolutePath(),
			"-o",tmpKeypointFile.getAbsolutePath()
		};
		LocalFeaturesTool.main(args);
		
		args = new String[]{
			"-i",tmpImageFile.getAbsolutePath(),
			"-o",tmpResizedKeypointFile.getAbsolutePath(),
			"-a",
			"-m","SIFT",
			"-it","RESIZE_MAX",
			"-dmax","1024"
		};
		LocalFeaturesTool.main(args);
		
		assertTrue(tmpResizedKeypointFile.exists());
		//TODO
		@SuppressWarnings("unused")
		List<Keypoint> resizedKeypoints = MemoryLocalFeatureList.read(tmpResizedKeypointFile, Keypoint.class);
		@SuppressWarnings("unused")
		List<Keypoint> normalKeypoints = MemoryLocalFeatureList.read(tmpResizedKeypointFile, Keypoint.class);
		@SuppressWarnings("unused")
		boolean set = false;
		@SuppressWarnings("unused")
		int resizedMinX, resizedMaxX,resizedMinY,resizedMaxY;
		@SuppressWarnings("unused")
		int normalMinX, normalMaxX,normalMinY,normalMaxY;
	}
}
