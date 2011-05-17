//
//  CaptureUtilities.m
//  OpenIMAJGrabber
//
//  Created by Jonathon Hare on 16/05/2011.
//  Copyright 2011 University of Southampton. All rights reserved.
//

#import "CaptureUtilities.h"


// Returns the default video device or nil if none found.
QTCaptureDevice* getDefaultVideoDevice() {
	QTCaptureDevice *device = nil;
    
	device = [QTCaptureDevice defaultInputDeviceWithMediaType:QTMediaTypeVideo];
	if( device == nil ) {
        device = [QTCaptureDevice defaultInputDeviceWithMediaType:QTMediaTypeMuxed];
	}
    
    return device;
}

NSArray * getDevices() {
    NSMutableArray *results = [NSMutableArray arrayWithCapacity:3];
    
    [results addObjectsFromArray:[QTCaptureDevice inputDevicesWithMediaType:QTMediaTypeVideo]];
    [results addObjectsFromArray:[QTCaptureDevice inputDevicesWithMediaType:QTMediaTypeMuxed]];
    
    return results;
}

// Returns the named capture device or nil if not found.
QTCaptureDevice* getDeviceByIdentifier(NSString * identifier) {
    NSArray *devices = getDevices();
    
	for( QTCaptureDevice *dev in devices ){
        if ( [identifier isEqualToString:[dev uniqueID]] ) {
            return dev;
        }
    }
    
    return nil;
}
