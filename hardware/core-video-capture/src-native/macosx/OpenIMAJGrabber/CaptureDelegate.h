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
//
//  CaptureDelegate.h
//  OpenIMAJGrabber
//
//  Created by Jonathon Hare on 16/05/2011.
//  Copyright 2011 University of Southampton. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreVideo/CoreVideo.h>
#import <AVFoundation/AVFoundation.h>
#import <CoreMedia/CoreMedia.h>


@interface CaptureDelegate : NSObject<AVCaptureVideoDataOutputSampleBufferDelegate>
{
	int newFrame; 
    CVImageBufferRef  mCurrentImageBuffer;
    CVPixelBufferRef mGrabbedPixels;
	unsigned char* imagedata; 
	unsigned char* rgb_imagedata;
	size_t currSize;
    NSCondition *mHasNewFrame;
}

-  (void)captureOutput:(AVCaptureOutput*) output
            didOutput: (CMSampleBufferRef)sampleBuffer
                 from:(AVCaptureConnection*) connection;

//- (void)captureOutput:(AVCaptureOutput *)captureOutput
//  didOutputVideoFrame:(CVImageBufferRef)videoFrame
//     withSampleBuffer:(AVSampleBuffer *)sampleBuffer
//       fromConnection:(AVCaptureConnection *)connection;
//
//- (void)captureOutput:(AVCaptureOutput *)captureOutput
//    didDropVideoFrameWithSampleBuffer:(AVSampleBuffer *)sampleBuffer
//       fromConnection:(AVCaptureConnection *)connection;
- (BOOL)grabImageUntilDate: (NSDate *)limit;
- (int)updateImage; 
- (unsigned char*)getOutput;
@end
