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

#include "videoInput.h"

class Device {
    private:
        const char* name;
        const char* identifier;
    public:
        __declspec(dllexport) Device(const char* name, const char* identifier);
        __declspec(dllexport) ~Device();
        __declspec(dllexport) const char* getName();
        __declspec(dllexport) const char* getIdentifier();
};

class DeviceList {
private:
    int nDevices;
    Device** devices;
public:
    __declspec(dllexport) DeviceList(Device ** devices, int nDevices);
    __declspec(dllexport) ~DeviceList();
    __declspec(dllexport) int getNumDevices();
    __declspec(dllexport) Device * getDevice(int i);
};

struct videoData{
	int height, width, size;
	videoInput * VI;
	int device;
	unsigned char * buffer;
};

class OpenIMAJGrabber
{
    public:
        __declspec(dllexport) OpenIMAJGrabber();
        __declspec(dllexport) OpenIMAJGrabber(Device * device);
        __declspec(dllexport) ~OpenIMAJGrabber();

		__declspec(dllexport)  DeviceList * getVideoDevices();
        
        __declspec(dllexport) unsigned char* getImage();
        __declspec(dllexport) void nextFrame();
        __declspec(dllexport) bool startSession(int width, int height,double rate,Device * device);
		__declspec(dllexport) bool startSession(int width, int height, double rate);
        __declspec(dllexport) void stopSession();
        
        __declspec(dllexport) int getWidth();
        __declspec(dllexport) int getHeight();
    
    private:
		void* data;
        
};

#endif