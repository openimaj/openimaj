//
//  CaptureDelegate.h
//  OpenIMAJGrabber
//
//  Created by Jonathon Hare on 16/05/2011.
//  Copyright 2011 University of Southampton. All rights reserved.
//

#import <QTKit/QTKit.h>


@interface CaptureDelegate : NSObject
{
	int newFrame; 
    CVImageBufferRef  mCurrentImageBuffer;
	unsigned char* imagedata; 
	unsigned char* rgb_imagedata;
	size_t currSize; 
}

- (void)captureOutput:(QTCaptureOutput *)captureOutput 
  didOutputVideoFrame:(CVImageBufferRef)videoFrame 
	 withSampleBuffer:(QTSampleBuffer *)sampleBuffer 
	   fromConnection:(QTCaptureConnection *)connection;

- (void)captureOutput:(QTCaptureOutput *)captureOutput 
    didDropVideoFrameWithSampleBuffer:(QTSampleBuffer *)sampleBuffer 
	   fromConnection:(QTCaptureConnection *)connection;

- (int)updateImage; 
- (unsigned char*)getOutput;
@end
