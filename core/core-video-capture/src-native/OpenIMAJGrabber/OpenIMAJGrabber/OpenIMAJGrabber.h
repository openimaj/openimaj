/*
 *  OpenIMAJGrabber.h
 *  OpenIMAJGrabber
 *
 *  Created by Jonathon Hare on 14/05/2011.
 *  Copyright 2011 University of Southampton. All rights reserved.
 *
 */

#ifndef OpenIMAJGrabber_
#define OpenIMAJGrabber_

#import <QTKit/QTKit.h>
#import "CaptureDelegate.h"

/* The classes below are exported */
#pragma GCC visibility push(default)

class Device {
    private:
        const char* name;
        const char* identifier;
    public:
        Device(const char* name, const char* identifier);
        ~Device();
        const char* getName();
        const char* getIdentifier();
};

class DeviceList {
private:
    int nDevices;
    Device** devices;
public:
    DeviceList(Device ** devices, int nDevices);
    ~DeviceList();
    int getNumDevices();
    Device * getDevice(int i);
};

class OpenIMAJGrabber
{
    public:
        OpenIMAJGrabber();
        ~OpenIMAJGrabber();
    
        static DeviceList* getVideoDevices();
        
        bool setDevice(Device * device);
    
        unsigned char* getImage();
        void nextFrame();
        bool startSession(int width, int height);
        void stopSession();
        
        int getWidth();
        int getHeight();
    
    private:
        QTCaptureSession                    *mCaptureSession;
        QTCaptureDeviceInput                *mCaptureDeviceInput;
        QTCaptureDecompressedVideoOutput    *mCaptureDecompressedVideoOutput;
        QTCaptureDevice                     *device;
        CaptureDelegate                     *delegate;
        int height, width;
};

#pragma GCC visibility pop
#endif
