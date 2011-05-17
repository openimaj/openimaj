/*
 *  OpenIMAJGrabber.cp
 *  OpenIMAJGrabber
 *
 *  Created by Jonathon Hare on 14/05/2011.
 *  Copyright 2011 University of Southampton. All rights reserved.
 *
 */

#import <Cocoa/Cocoa.h>
#include "OpenIMAJGrabber.h"
#include "CaptureUtilities.h"
#include <vector>
#include <string.h>

void error(const char *str) {
    fprintf(stderr, "%s", str);
}

Device::Device(const char* name, const char* identifier) {
    this->name = new char[strlen(name) + 1];
    this->identifier = new char[strlen(identifier) + 1];
    
    strcpy((char*)this->name, name);
    strcpy((char*)this->identifier, identifier);
}

Device::~Device() {
    delete [] name;
    delete [] identifier;
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
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];
    
    mCaptureSession = nil;
    mCaptureDeviceInput = nil;
    mCaptureDecompressedVideoOutput = nil;
    delegate = nil;
    
    [pool drain];
}

OpenIMAJGrabber::~OpenIMAJGrabber() {
    stopSession();
}

int OpenIMAJGrabber::getWidth() {
    return width;
}

int OpenIMAJGrabber::getHeight() {
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

void OpenIMAJGrabber::nextFrame() {
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];

	double sleepTime = 0.005; 
    
	NSDate *loopUntil = [NSDate dateWithTimeIntervalSinceNow:sleepTime];
	while (![delegate updateImage] &&
		   [[NSRunLoop currentRunLoop] runMode: NSDefaultRunLoopMode beforeDate:loopUntil])
		loopUntil = [NSDate dateWithTimeIntervalSinceNow:sleepTime]; 
    
    [pool drain];
}

unsigned char* OpenIMAJGrabber::getImage() {
    return [delegate getOutput];
}

bool OpenIMAJGrabber::startSession(int width, int height) {
    return startSession(width, height, NULL);
}

bool OpenIMAJGrabber::startSession(int w, int h, Device * dev) {
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    QTCaptureDevice* device;
    
    if (dev == NULL) {
        device = getDefaultVideoDevice();
    } else {
        NSString * ident = [NSString stringWithCString:dev->getIdentifier() encoding:NSUTF8StringEncoding];
        device = getDeviceByIdentifier(ident);
    }
    
    if( device == nil ) {
        [pool drain];

        return false;
    }
    
    width = w;
    height = h;
        
    NSError *err = nil;
    
    // If we've already started with this device, return
    if( [device isEqual:[mCaptureDeviceInput device]] &&
        mCaptureSession != nil &&
       [mCaptureSession isRunning] ) {
        [pool drain];
        
        return true;
    } else if( mCaptureSession != nil ){
        stopSession();
    }
    	
	// Create the capture session
    mCaptureSession = [[QTCaptureSession alloc] init];
	if( ![device open:&err] ){
		error( "Could not create capture session.\n" );
        
        [mCaptureSession release];
        mCaptureSession = nil;
		
        [pool drain];
        return false;
	}
    
	// Create input object from the device
	mCaptureDeviceInput = [[QTCaptureDeviceInput alloc] initWithDevice:device];
	if (![mCaptureSession addInput:mCaptureDeviceInput error:&err]) {
		error( "Could not convert device to input device.\n");
        
        [mCaptureSession release];
        [mCaptureDeviceInput release];
        mCaptureSession = nil;
        mCaptureDeviceInput = nil;
        
        [pool drain];
		return false;
	}
    
	// Decompressed video output
	mCaptureDecompressedVideoOutput = [[QTCaptureDecompressedVideoOutput alloc] init];
    
    NSDictionary * options = [NSDictionary dictionaryWithObjectsAndKeys:
                              [NSNumber numberWithDouble:w], (id)kCVPixelBufferWidthKey,
                              [NSNumber numberWithDouble:h], (id)kCVPixelBufferHeightKey,
                              [NSNumber numberWithUnsignedInt:kCVPixelFormatType_32ARGB], (id)kCVPixelBufferPixelFormatTypeKey,
                              nil];
    
    [mCaptureDecompressedVideoOutput setPixelBufferAttributes:options];
    
    delegate = [[CaptureDelegate alloc] init];
	[mCaptureDecompressedVideoOutput setDelegate:delegate];
    
	if (![mCaptureSession addOutput:mCaptureDecompressedVideoOutput error:&err]) {
		error( "Could not create decompressed output.\n");
        
        [mCaptureSession release];
        [mCaptureDeviceInput release];
        [mCaptureDecompressedVideoOutput release];
        [delegate release];
        
        mCaptureSession = nil;
        mCaptureDeviceInput = nil;
        mCaptureDecompressedVideoOutput = nil;
        delegate = nil;
        
        [pool drain];
        
		return false;
	}
        
	[mCaptureSession startRunning];
    
    [pool drain];
    
    getImage();
    
    return true;
}

void OpenIMAJGrabber::stopSession() {
    if (mCaptureSession == nil) return;
    
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    
	[mCaptureSession stopRunning];
	
    QTCaptureDevice * device = [mCaptureDeviceInput device];
	if ([device isOpen])  [device close];
	
	[mCaptureSession release];
    [mCaptureDeviceInput release];
    mCaptureSession = nil;
    mCaptureDeviceInput = nil;
	
	[mCaptureDecompressedVideoOutput setDelegate:mCaptureDecompressedVideoOutput]; 
	[mCaptureDecompressedVideoOutput release]; 
    mCaptureDecompressedVideoOutput = nil;
	
    [delegate release];
    delegate = nil;

	[pool drain];
}
