//
//  CaptureUtilities.h
//  OpenIMAJGrabber
//
//  Created by Jonathon Hare on 16/05/2011.
//  Copyright 2011 University of Southampton. All rights reserved.
//

#import <QTKit/QTKit.h>

#ifdef __cplusplus 
extern "C" {
#endif
    
// Returns the default video device or nil if none found.
QTCaptureDevice* getDefaultVideoDevice();

// Get a list of all potential devices
NSArray * getDevices();

// Returns the capture device corresponding to the device object or nil if not found.
QTCaptureDevice* getDeviceByIdentifier(NSString * identifier);

#ifdef __cplusplus
}
#endif