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
package org.openimaj.hadoop.tools.clusterquantiser;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.hadoop.tools.clusterquantiser.HadoopClusterQuantiserTool;
import org.openimaj.hadoop.tools.localfeature.HadoopLocalFeaturesTool;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;

import org.openimaj.tools.clusterquantiser.ClusterQuantiser;


public class HadoopClusterQuantiserToolTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private File imageSeqFile;
	private File featureSeqFile;
	private File codebookFile;
	private File hadoopQuantisedOutput;
	private File normalQuantisedOutput;
	private File keypointOut;
//	private ArrayList<String> inputImages;
	private HashMap<String, String> UUID2Name;
	private HashMap<String, File> name2File;
	private File quantisedKeypointOut;
	
	@Before public void setup() throws Exception{
		// Load the resources as an array
		imageSeqFile = folder.newFile("seq.images");
		imageSeqFile.delete();
		featureSeqFile = folder.newFile("seq.features");
		featureSeqFile.delete();
		codebookFile = folder.newFile("codebook.temp");
		hadoopQuantisedOutput = folder.newFile("seq.quantised");
		normalQuantisedOutput = folder.newFile("normal.quantised");
		keypointOut = folder.newFile("keypoint.out");
		quantisedKeypointOut = folder.newFile("seq.quantised");
		hadoopQuantisedOutput.delete();
		normalQuantisedOutput.delete();
		keypointOut.delete();
		quantisedKeypointOut.delete();
		
		TextBytesSequenceFileUtility tbsfu = new TextBytesSequenceFileUtility(imageSeqFile.getAbsolutePath(),false);
		InputStream[] inputs = new InputStream[]{
			this.getClass().getResourceAsStream("testimages/ukbench00000.jpg"),
			this.getClass().getResourceAsStream("testimages/ukbench00001.jpg"),
		};
		String[] inputNames = new String[]{
			"ukbench00000.jpg",
			"ukbench00001.jpg"
		};
		UUID2Name = new HashMap<String,String>();
		name2File = new HashMap<String,File>();
		int i = 0;
		for(InputStream input : inputs){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copyBytes(input, baos, new Configuration(), false);
			byte[] imageData = baos.toByteArray(); 
			BytesWritable bytesWriteable = new BytesWritable(imageData);
			
			
			// Write the image to a sequence file
			String uuidname = UUID.randomUUID().toString();
			tbsfu.appendData(new Text(uuidname),bytesWriteable);
			
			
			// Write a image to a location
			File imageFile = folder.newFile("image.jpg");
			ByteArrayInputStream imageBytes = new ByteArrayInputStream(imageData);
			FileOutputStream imageOutput = new FileOutputStream(imageFile);
			IOUtils.copyBytes(imageBytes, imageOutput, new Configuration(), false);
			name2File.put(inputNames[i], imageFile);
			
			
			// Correlate the uuid to the image name
			UUID2Name.put(uuidname,inputNames[i]);
			i++;
			
		}
		tbsfu.close();
		HadoopLocalFeaturesTool.main(new String[]{"-D","mapred.child.java.opts=\"-Xmx3000M\"","-i",imageSeqFile.getAbsolutePath(),"-o",featureSeqFile.getAbsolutePath()});
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(featureSeqFile.getAbsolutePath(), "part");
		for(Path path : sequenceFiles){
			SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(path.toUri(), true);
			utility.exportData(keypointOut.getAbsolutePath());
		}
		
		InputStream codebookInput = this.getClass().getResourceAsStream("codebook/codebook-100-sift.voc");
		
//		ClusterTypeTest cqt = new ClusterTypeTest();
//		cqt.setup();
//		IntCluster<?> cluster = cqt.testRandomSet();
//		uk.ac.soton.ecs.jsh2.utils.IOUtils.writeBinary(codebook,cluster);
		
//		FileInputStream codebookInput = new FileInputStream(codebook);
		FileOutputStream codebookOutput = new FileOutputStream(codebookFile);
		IOUtils.copyBytes(codebookInput, codebookOutput, new Configuration(), false);
		codebookOutput.close();
//		this.inputImages = new ArrayList<String>();
		
	}

	@Test public void testBottomUp() throws Exception{
		HadoopClusterQuantiserTool.main(
				new String[]{
						"-D","mapred.child.java.opts=\"-Xmx2000M\"",
						"-D","mapred.job.map.memory.mb=1100","-D","mapred.job.reduce.memory.mb=1100", 
						"-i",featureSeqFile.getAbsolutePath(),
						"-o",hadoopQuantisedOutput.getAbsolutePath(),
						"-q",codebookFile.getAbsolutePath(),
						"-t","BINARY_KEYPOINT"
		});
		
		String[] inputImages = new String[name2File.size()];
		int i = 0;
		for(File entry: keypointOut.listFiles()){
			inputImages[i++] = entry.getAbsolutePath();
		}
		
		ClusterQuantiser.main(new String[]{
				"-o",normalQuantisedOutput.getAbsolutePath(),
				"-q",codebookFile.getAbsolutePath(),
				"-t","BINARY_KEYPOINT",
				"-k", "1",
				"-d", "1",
				inputImages[0],inputImages[1]
				
		});
		
		// Get hadoop quantised keypoints
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(hadoopQuantisedOutput.getAbsolutePath(), "part");
		for(Path path : sequenceFiles){
			SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(path.toUri(), true);
			utility.exportData(quantisedKeypointOut.getAbsolutePath());
		}
		
		// Get the quantised keypoint file matched to it's UUID
		Map<String,FileLocalFeatureList<QuantisedKeypoint> > UUID2QuantisedHDFS = new HashMap<String,FileLocalFeatureList<QuantisedKeypoint> >();
		Map<String,FileLocalFeatureList<QuantisedKeypoint> > UUID2QuantisedNormal = new HashMap<String,FileLocalFeatureList<QuantisedKeypoint> >();
		
		for(String key:UUID2Name.keySet())
		{
			for(File f : normalQuantisedOutput.listFiles()){
				if(f.getAbsolutePath().indexOf(key) != -1){
					UUID2QuantisedNormal.put(key, FileLocalFeatureList.read(f, QuantisedKeypoint.class));
				}
			}
			for(File f : quantisedKeypointOut.listFiles()){
				if(f.getAbsolutePath().indexOf(key) != -1){
					UUID2QuantisedHDFS.put(key, FileLocalFeatureList.read(f, QuantisedKeypoint.class));
				}
			}
			int[][] qflHDFS = UUID2QuantisedHDFS.get(key).asDataArray(new int[UUID2QuantisedHDFS.get(key).size()][]);
			int[][] qflNormal = UUID2QuantisedNormal.get(key).asDataArray(new int[UUID2QuantisedNormal.get(key).size()][]);
			assertTrue(qflHDFS.length == qflNormal.length);
			for(int j = 0 ; j < qflNormal.length ; j++){
				 Integer qfNormal = qflNormal[j][0];
				 Integer qfHDFS = qflHDFS[j][0];
				 assertTrue(qfNormal == qfHDFS);
			}
		}
		System.out.println("Done!");
		
	}

}
