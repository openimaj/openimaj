package org.openimaj.video.capture;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.quicktime.VideoCapture;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.Image;
import com.lti.civil.VideoFormat;
import com.lti.civil.impl.common.VideoFormatImpl;

public class GenericVideoCapture extends Video<MBFImage> implements CaptureObserver {

    private int width;
    private int height;

    private int[] pixels;

    private MBFImage mbfimage;
    CaptureSystemFactory factory;
	private CaptureSystem system;
	private CaptureStream captureStream;
    
    public void initCapture() throws CaptureException
	{
    	FImage image = new FImage(width,height);
    	mbfimage = new MBFImage(new FImage[]{image.clone(),image.clone(),image.clone()});
		factory = DefaultCaptureSystemFactorySingleton.instance();
		system = factory.createCaptureSystem();
		system.init();
		List list = system.getCaptureDeviceInfoList();
		for (int i = 0; i < list.size(); ++i)
		{
			CaptureDeviceInfo info = (CaptureDeviceInfo) list.get(i);
			
			System.out.println("Device ID " + i + ": " + info.getDeviceID());
			System.out.println("Description " + i + ": " + info.getDescription());
			captureStream = system.openCaptureDeviceStream(info.getDeviceID());
			captureStream.setObserver(this);
			
			break;
		}
		captureStream.setVideoFormat(new VideoFormatImpl(VideoFormat.RGB32,width,height,VideoFormat.FPS_UNKNOWN));
		
		
	}
    public GenericVideoCapture(int width, int height) throws Exception {
        this.width = width;
        this.height = height;
        
        try {
        	initCapture();
        	captureStream.start();
        } catch (Exception e) {
            throw e;
        }
    }

    public void dispose() {
        try {
    		if (captureStream != null)
    		{	System.out.println("disposeCapture: stopping capture stream...");
    			captureStream.stop();
    			System.out.println("disposeCapture: stopped capture stream.");
    			captureStream.dispose();
    			captureStream = null;
    		}
    		
    		if (system != null)
    			system.dispose();
    		System.out.println("disposeCapture done.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    @Override 
    public double getFPS(){
    	return -1;
    }
	@Override
	public MBFImage getNextFrame() {
        return getCurrentFrame();
	}

	@Override
	public MBFImage getCurrentFrame() {
		return mbfimage;
	}
	@Override
	public void onNewImage(CaptureStream sender, Image image) {
//		mbfimage.internalAssign( imageToMBFImage(image));
		mbfimage.internalAssign(image.getBytes(), width, height);
		image.getBytes();
	}
	@Override
	public void onError(CaptureStream sender, CaptureException e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String args[]) throws Exception{
		GenericVideoCapture c = new GenericVideoCapture(640, 480);
		VideoDisplay<MBFImage> videoFrame = VideoDisplay.createVideoDisplay(c);
	}
}