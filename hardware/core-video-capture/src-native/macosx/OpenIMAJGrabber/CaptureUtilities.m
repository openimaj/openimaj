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
#import "CaptureUtilities.h"
#import <Foundation/Foundation.h>

/*
 * Helper functions to get information from AVKit on the
 * main thread. 
 * Note: doing these calls directly on a background thread
 * seems to cause segfaults in 32-bit mode, although it works
 * OK in 64-bit mode. In particular, it seems AVCaptureDevice
 * needs to be created on the main thread...
 */

@interface GrabberHelper : NSObject {
}
- (void)getDefaultVideoDevice:(NSMutableDictionary*)wrapper;
- (void)getDevices:(NSMutableDictionary*)wrapper;
- (void)getDeviceByIdentifier:(NSMutableDictionary*)wrapper;
@end

@implementation GrabberHelper
- (void)getDefaultVideoDevice:(NSMutableDictionary*)data {
    AVCaptureDevice *device = nil;
    
    device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
	if( device == nil ) {
        device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeMuxed];
	}
    
    if (device) [data setObject:device forKey:@"result"];
}

- (void)getDevices:(NSMutableDictionary*)data {
    NSMutableArray *results = [NSMutableArray arrayWithCapacity:3];
        
    [results addObjectsFromArray:[AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo]];
    [results addObjectsFromArray:[AVCaptureDevice devicesWithMediaType:AVMediaTypeMuxed]];
    
    [data setObject:results forKey:@"result"];
}

- (void)getDeviceByIdentifier:(NSMutableDictionary*)data {
    NSArray *devices = getDevices();
    NSString * identifier = [data objectForKey:@"identifier"];
    AVCaptureDevice * device = nil;
    
	for( AVCaptureDevice *dev in devices ){
        if ( [identifier isEqualToString:[dev uniqueID]] ) {
            device = dev;
            break;
        }
    }
    
    if (device) [data setObject:device forKey:@"result"];
}
@end

// Returns the default video device or nil if none found.
AVCaptureDevice* getDefaultVideoDevice() {
	GrabberHelper * helper = [[GrabberHelper alloc] init];
    NSMutableDictionary * wrapper = [[NSMutableDictionary alloc] init];
    
    [helper performSelectorOnMainThread:@selector(getDefaultVideoDevice:) withObject:wrapper waitUntilDone:YES];
    AVCaptureDevice * device = [[wrapper objectForKey:@"result"] retain];
    
    [wrapper release];
    [helper release];
    
    return [device autorelease];
}

NSArray * getDevices() {
    GrabberHelper * helper = [[GrabberHelper alloc] init];
    NSMutableDictionary * wrapper = [[NSMutableDictionary alloc] init];
    
    [helper performSelectorOnMainThread:@selector(getDevices:) withObject:wrapper waitUntilDone:YES];
    NSArray * results = [[wrapper objectForKey:@"result"] retain];

    [wrapper release];
    [helper release];
    
    return [results autorelease];
}

// Returns the named capture device or nil if not found.
AVCaptureDevice* getDeviceByIdentifier(NSString * identifier) {
    GrabberHelper * helper = [[GrabberHelper alloc] init];
    NSMutableDictionary * wrapper = [[NSMutableDictionary alloc] init];
    
    [wrapper setObject:identifier forKey:@"identifier"];
    [helper performSelectorOnMainThread:@selector(getDeviceByIdentifier:) withObject:wrapper waitUntilDone:YES];
    AVCaptureDevice * device = [[wrapper objectForKey:@"result"] retain];
    
    [wrapper release];
    [helper release];
    
    return [device autorelease];
}
