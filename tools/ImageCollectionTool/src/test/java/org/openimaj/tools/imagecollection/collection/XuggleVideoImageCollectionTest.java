package org.openimaj.tools.imagecollection.collection;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.XuggleVideoImageCollection;
import org.openimaj.tools.imagecollection.collection.YouTubeVideoImageCollection;
import org.openimaj.tools.imagecollection.collection.XuggleVideoImageCollection.FromFile;
import org.openimaj.tools.imagecollection.collection.XuggleVideoImageCollection.FromURL;

import com.xuggle.ferry.JNILibraryLoader;


public class XuggleVideoImageCollectionTest {
	String aVideo = "/org/openimaj/video/data/a_video.avi";
	private File videoFile;
	private URL videoURL;
	private ImageCollectionConfig fileConfig;
	private ImageCollectionConfig urlConfig;
	
	@Before
	public void setup() throws IOException{
		InputStream s = XuggleVideoImageCollectionTest.class.getResourceAsStream(aVideo);
		videoFile = File.createTempFile("xuggle", ".avi");
		videoURL = videoFile.toURI().toURL();
		
		FileOutputStream fos = new FileOutputStream(videoFile);
		int read = 0;
		byte[] buffer = new byte[1024];
		while((read = s.read(buffer))!=-1){
			fos.write(buffer, 0, read);
		}
		fos.close();
		String jsonVideoFile = String.format("{video:{file:\"%s\"}}",videoFile);
		String jsonVideoURL = String.format("{video:{url:\"%s\"}}",videoFile);
		
		fileConfig = new ImageCollectionConfig(jsonVideoFile);
		urlConfig = new ImageCollectionConfig(jsonVideoURL);
	}
	
	@Test
	public void testURLFileXuggleVideoImageCollection() throws ImageCollectionSetupException{
		try{
			FromFile fileVideo = new XuggleVideoImageCollection.FromFile();
			fileVideo.setup(fileConfig);
			List<ImageCollectionEntry<MBFImage>> fileFrames = fileVideo.getAll();
			FromURL urlVideo = new XuggleVideoImageCollection.FromURL();
			urlVideo.setup(urlConfig);
			List<ImageCollectionEntry<MBFImage>> urlFrames = urlVideo.getAll();
			assertTrue(urlFrames.size() > 0);
			assertEquals(urlFrames.size(),fileFrames.size());
		}
		catch(UnsatisfiedLinkError e){
			
		}
		
	}
	
	@Test
	public void testYouTubeVideoImageCollection() throws ImageCollectionSetupException{
		try{
			String youtubeURLStr = "http://www.youtube.com/watch?v=Bz9yMZPAz2s";
			String youtubeJSON = String.format("{video:{url:\"%s\"}}",youtubeURLStr);
			ImageCollectionConfig youtubeConfig = new ImageCollectionConfig(youtubeJSON);
			
			YouTubeVideoImageCollection col = new YouTubeVideoImageCollection();
			col.setup(youtubeConfig);
			
			int i = 0;
			for(ImageCollectionEntry<MBFImage> im : col){
				if(i++ > 10) return;
				
			}
		}
		catch(UnsatisfiedLinkError e){
			
		}
	}
}
