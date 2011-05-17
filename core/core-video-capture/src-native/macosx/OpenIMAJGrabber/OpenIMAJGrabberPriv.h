/*
 *  OpenIMAJGrabber.h
 *  OpenIMAJGrabber
 *
 *  Created by Jonathon Hare on 14/05/2011.
 *  Copyright 2011 University of Southampton. All rights reserved.
 *
 */

#ifndef OpenIMAJGrabberPriv_
#define OpenIMAJGrabberPriv_

#import "OpenIMAJGrabber.h"
#import <QTKit/QTKit.h>
#import "CaptureDelegate.h"

class OpenIMAJGrabberPriv
{
    public:
        OpenIMAJGrabberPriv();
        ~OpenIMAJGrabberPriv();
        
        unsigned char* getImage();
        void nextFrame();
        bool startSession(int width, int height, Device * device);
        void stopSession();
        
        int getWidth();
        int getHeight();

    private:
        QTCaptureSession                    *mCaptureSession;
        QTCaptureDeviceInput                *mCaptureDeviceInput;
        QTCaptureDecompressedVideoOutput    *mCaptureDecompressedVideoOutput;
        CaptureDelegate                     *delegate;
        int height, width;
};

#endif
