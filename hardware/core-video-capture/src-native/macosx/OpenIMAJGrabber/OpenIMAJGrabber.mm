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
#import <Cocoa/Cocoa.h>
#include "OpenIMAJGrabber.h"
#include "OpenIMAJGrabberPriv.h"
#include "CaptureUtilities.h"
#include <vector>

#include <string.h>
#include <stdlib.h>
#include <sys/time.h>

void error(const char *str) {
    fprintf(stderr, "%s", str);
}

Device::Device(const char* name, const char* identifier) {
    this->name = strdup(name);
    this->identifier = strdup(identifier);
}

Device::~Device() {
    free((void*)name);
    free((void*)identifier);
}

DeviceList::DeviceList(Device** devices, int nDevices) {
    this->nDevices = nDevices;
    this->devices = devices;
}

DeviceList::~DeviceList() {
    delete [] devices;
}

int DeviceList::getNumDevices() {
    return nDevices;
}

Device * DeviceList::getDevice(int i) {
    return devices[i];
}

const char* Device::getName() {
    return name;
}

const char* Device::getIdentifier() {
    return identifier;
}

OpenIMAJGrabber::OpenIMAJGrabber() {
    data = new OpenIMAJGrabberPriv::OpenIMAJGrabberPriv();
}
    
OpenIMAJGrabberPriv::OpenIMAJGrabberPriv() {
    mCaptureSession = NULL;
    mCaptureDeviceInput = NULL;
    mCaptureDecompressedVideoOutput = NULL;
    delegate = NULL;
    timeout = 5; //secs
}

OpenIMAJGrabber::~OpenIMAJGrabber() {
    delete (OpenIMAJGrabberPriv*)data;
}

OpenIMAJGrabberPriv::~OpenIMAJGrabberPriv() {
    stopSession();
}

int OpenIMAJGrabber::getWidth() {
    return ((OpenIMAJGrabberPriv*)data)->getWidth();
}

int OpenIMAJGrabberPriv::getWidth() {
    return width;
}

int OpenIMAJGrabber::getHeight() {
    return ((OpenIMAJGrabberPriv*)data)->getHeight();
}

int OpenIMAJGrabberPriv::getHeight() {
    return height;
}

DeviceList* OpenIMAJGrabber::getVideoDevices() {
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];
    
    NSArray *results = getDevices();
    int count = (int)[results count];
    
    Device ** devices = new Device*[count];
    
    for (int i=0; i<count; i++) {
        const char * name = [[[results objectAtIndex:i] localizedDisplayName] UTF8String];
        const char * identifier = [[(QTCaptureDevice *)[results objectAtIndex:i] uniqueID] UTF8String];
        
        devices[i] = new Device(name, identifier);
    }
    
    [pool drain];
    
    return new DeviceList(devices, count);
}

int OpenIMAJGrabber::nextFrame() {
    return ((OpenIMAJGrabberPriv*)data)->nextFrame();
}

double getTime() {
    struct timeval  tv;
    gettimeofday(&tv, NULL);
    
    return (((tv.tv_sec * 1000.0) + (tv.tv_usec / 1000.0))) / 1000;
}

int OpenIMAJGrabberPriv::nextFrame() {
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];

	double sleepTime = 0.005;
    double accum = 0;
    int ret = -1;
    
    while (accum < timeout) {
        if ([delegate updateImage]) {
            ret = 1;
            break;
        }
    
        double t1 = getTime();
        NSDate *loopUntil = [NSDate dateWithTimeIntervalSinceNow:sleepTime];
        [[NSRunLoop currentRunLoop] runMode: NSDefaultRunLoopMode beforeDate:loopUntil];
        accum += getTime() - t1;
    }
    
    [pool drain];
    
    return ret;
}

void OpenIMAJGrabber::setTimeout(int timeoutMS) {
    return ((OpenIMAJGrabberPriv*)data)->setTimeout(timeoutMS);
}

void OpenIMAJGrabberPriv::setTimeout(int timeoutMS) {
    this->timeout = timeoutMS / 1000.0;
}

unsigned char* OpenIMAJGrabber::getImage() {
    return ((OpenIMAJGrabberPriv*)data)->getImage();
}

unsigned char* OpenIMAJGrabberPriv::getImage() {
    return [delegate getOutput];
}

bool OpenIMAJGrabber::startSession(int width, int height, int reqMillisPerFrame) {
    return ((OpenIMAJGrabberPriv*)data)->startSession(width, height, reqMillisPerFrame, NULL);
}

bool OpenIMAJGrabber::startSession(int w, int h, int reqMillisPerFrame, Device * dev) {
    return ((OpenIMAJGrabberPriv*)data)->startSession(w, h, reqMillisPerFrame, dev);
}

bool OpenIMAJGrabberPriv::startSession(int w, int h, int reqMillisPerFrame, Device * dev) {
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    QTCaptureDevice* device = NULL;
    
    if (dev == NULL) {
        device = getDefaultVideoDevice();
    } else {
        NSString * ident = [NSString stringWithCString:dev->getIdentifier() encoding:NSUTF8StringEncoding];
        device = getDeviceByIdentifier(ident);
    }
    
    if( device == NULL ) {
        [pool drain];

        return false;
    }
    
    width = w;
    height = h;

    NSError *err = NULL;
    
    // If we've already started with this device, return
    if( [device isEqual:[mCaptureDeviceInput device]] &&
        mCaptureSession != NULL &&
       [mCaptureSession isRunning] ) {
        [pool drain];
        
        return true;
    } else if( mCaptureSession != NULL ){
        stopSession();
    }
    	
	// Create the capture session
    mCaptureSession = [[QTCaptureSession alloc] init];
	if( ![device open:&err] ){
		error( "Could not create capture session.\n" );
        
        [mCaptureSession release];
        mCaptureSession = NULL;
		
        [pool drain];
        return false;
	}
    
	// Create input object from the device
	mCaptureDeviceInput = [[QTCaptureDeviceInput alloc] initWithDevice:device];
	if (![mCaptureSession addInput:mCaptureDeviceInput error:&err]) {
		error( "Could not convert device to input device.\n");
        
        [mCaptureSession release];
        [mCaptureDeviceInput release];
        mCaptureSession = NULL;
        mCaptureDeviceInput = NULL;
        
        [pool drain];
		return false;
	}
    
	// Decompressed video output
	mCaptureDecompressedVideoOutput = [[QTCaptureDecompressedVideoOutput alloc] init];
    
    NSDictionary * options = [NSDictionary dictionaryWithObjectsAndKeys:
                              [NSNumber numberWithDouble:w], (id)kCVPixelBufferWidthKey,
                              [NSNumber numberWithDouble:h], (id)kCVPixelBufferHeightKey,
                              [NSNumber numberWithUnsignedInt:kCVPixelFormatType_32ARGB], (id)kCVPixelBufferPixelFormatTypeKey,
                              NULL];
    
    [mCaptureDecompressedVideoOutput setPixelBufferAttributes:options];
    
    delegate = [[CaptureDelegate alloc] init];
	[mCaptureDecompressedVideoOutput setDelegate:delegate];
    
    [mCaptureDecompressedVideoOutput setAutomaticallyDropsLateVideoFrames:YES];

    if (reqMillisPerFrame > 0)
        [mCaptureDecompressedVideoOutput setMinimumVideoFrameInterval: ((double)reqMillisPerFrame / 1000.0)];
    
	if (![mCaptureSession addOutput:mCaptureDecompressedVideoOutput error:&err]) {
		error( "Could not create decompressed output.\n");
        
        [mCaptureSession release];
        [mCaptureDeviceInput release];
        [mCaptureDecompressedVideoOutput release];
        [delegate release];
        
        mCaptureSession = NULL;
        mCaptureDeviceInput = NULL;
        mCaptureDecompressedVideoOutput = NULL;
        delegate = NULL;
        
        [pool drain];
        
		return false;
	}
        
	[mCaptureSession startRunning];
    
    [pool drain];
    
    getImage();
    
    return true;
}

void OpenIMAJGrabber::stopSession() {
    ((OpenIMAJGrabberPriv*)data)->stopSession();
}

void OpenIMAJGrabberPriv::stopSession() {
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    
    while (mCaptureSession != NULL) {
        [mCaptureSession stopRunning];

        if ([mCaptureSession isRunning]) {
            [[NSRunLoop currentRunLoop] runUntilDate:[NSDate dateWithTimeIntervalSinceNow: 0.1]];
        } else {
            [mCaptureSession release];
            [mCaptureDeviceInput release];
            mCaptureSession = NULL;
            mCaptureDeviceInput = NULL;
	
            [mCaptureDecompressedVideoOutput setDelegate:mCaptureDecompressedVideoOutput]; 
            [mCaptureDecompressedVideoOutput release]; 
            mCaptureDecompressedVideoOutput = NULL;
	
            [delegate release];
            delegate = NULL;
        }
    }

	[pool drain];
}
