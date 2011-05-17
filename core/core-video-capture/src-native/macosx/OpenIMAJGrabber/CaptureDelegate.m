//
//  CaptureDelegate.m
//  OpenIMAJGrabber
//
//  Created by Jonathon Hare on 16/05/2011.
//  Copyright 2011 University of Southampton. All rights reserved.
//

#import "CaptureDelegate.h"


@implementation CaptureDelegate 

- (id)init {
	[super init]; 
	newFrame = 0; 
	imagedata = NULL; 
	rgb_imagedata = NULL; 
	currSize = 0;
	return self; 
}


-(void)dealloc {
	if (imagedata != NULL) free(imagedata); 
	if (rgb_imagedata != NULL) free(rgb_imagedata); 
	[super dealloc]; 
}

- (void)captureOutput:(QTCaptureOutput *)captureOutput 
  didOutputVideoFrame:(CVImageBufferRef)videoFrame 
	 withSampleBuffer:(QTSampleBuffer *)sampleBuffer 
	   fromConnection:(QTCaptureConnection *)connection {
	
    CVBufferRetain(videoFrame);
	CVImageBufferRef imageBufferToRelease  = mCurrentImageBuffer;
    
    @synchronized (self) {
        mCurrentImageBuffer = videoFrame;
		newFrame = 1; 
    }
	
	CVBufferRelease(imageBufferToRelease);
    
}
- (void)captureOutput:(QTCaptureOutput *)captureOutput 
didDropVideoFrameWithSampleBuffer:(QTSampleBuffer *)sampleBuffer 
	   fromConnection:(QTCaptureConnection *)connection {
    //do nothing
}

-(unsigned char*) getOutput {
	return rgb_imagedata; 
}

-(int) updateImage {
	if (newFrame==0) return 0; 
    
	CVPixelBufferRef pixels; 
	
	@synchronized (self) {
		pixels = CVBufferRetain(mCurrentImageBuffer);
		newFrame = 0; 
	}
	
	CVPixelBufferLockBaseAddress(pixels, 0);
	uint32_t* baseaddress = (uint32_t*)CVPixelBufferGetBaseAddress(pixels);
	
	size_t width = CVPixelBufferGetWidth(pixels);
	size_t height = CVPixelBufferGetHeight(pixels);
	size_t rowBytes = CVPixelBufferGetBytesPerRow(pixels);
	
	if (rowBytes != 0) {
		if (currSize != rowBytes*height*sizeof(char)) {
			currSize = rowBytes*height*sizeof(char); 
			if (imagedata != NULL) free(imagedata);
			if (rgb_imagedata != NULL) free(rgb_imagedata);
            
			imagedata = (unsigned char*)malloc(currSize); 
			rgb_imagedata = (unsigned char*)malloc(width*height*3*sizeof(unsigned char));
		}
		
		memcpy(imagedata, baseaddress, currSize);
		
        //convert the ARGB rep to a RGB one
		for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                rgb_imagedata[3*(x + width*y) + 0] = imagedata[4*x + rowBytes*y + 1];
                rgb_imagedata[3*(x + width*y) + 1] = imagedata[4*x + rowBytes*y + 2];
                rgb_imagedata[3*(x + width*y) + 2] = imagedata[4*x + rowBytes*y + 3];
            }
        }
	}
	
	CVPixelBufferUnlockBaseAddress(pixels, 0);
	CVBufferRelease(pixels);
	
	return 1; 
}

@end
