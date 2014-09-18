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
		__declspec(dllexport) void setTimeout(int timeout);
        __declspec(dllexport) int nextFrame();
        __declspec(dllexport) bool startSession(int width, int height, int millisPerFrame, Device * device);
		__declspec(dllexport) bool startSession(int width, int height, int millisPerFrame);
        __declspec(dllexport) void stopSession();
        
        __declspec(dllexport) int getWidth();
        __declspec(dllexport) int getHeight();
    
    private:
		void* data;
        int timeout;
};

#endif