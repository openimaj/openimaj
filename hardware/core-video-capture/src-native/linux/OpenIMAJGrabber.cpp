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
#include "OpenIMAJGrabber.h"
#include "capture.h"
#include <stdio.h>

#ifdef GLIBC_2_4
__asm__(".symver memcpy,memcpy@GLIBC_2.4");
#else
__asm__(".symver memcpy,memcpy@GLIBC_2.2.5");
#endif

#define NUM_DEVICES_SEARCH 16

#define VG ((VideoGrabber*)this->data)

#define CLEAR(x) memset(&(x), 0, sizeof(x))

Device * getDeviceInfo(const char * device);

Device * getDeviceInfo(const char * device) {
    char dummy[256];
    int fd;

    if (-1 == access(device, F_OK)) {
        return NULL;
    }

    fd = open(device, O_RDONLY);
    if (-1 == fd) {
        fprintf(stderr, "warning: error opening device\n");
        return NULL;
    }

    if (-1 == ioctl(fd, VIDIOC_QUERYCAP, dummy)) {
        fprintf(stderr, "warning: not a v4l2 device\n");
        close(fd);
        return NULL;
    }

    struct v4l2_capability capability;
    CLEAR(capability);
    if (-1 == ioctl(fd, VIDIOC_QUERYCAP, &capability)) {
        fprintf(stderr, "error getting capability\n");
        close(fd);
        return NULL;
    }

    if (!(capability.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
        fprintf(stderr, "device doesn't support capture\n");
        close(fd);
        return NULL;
    }
    close(fd);

    return new Device((const char*)capability.card, device);
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
    data = NULL;
}

OpenIMAJGrabber::~OpenIMAJGrabber() {
    if (data != NULL) {
        delete (VideoGrabber*)data;
    }
}

DeviceList* OpenIMAJGrabber::getVideoDevices() {
    Device** list = new Device*[NUM_DEVICES_SEARCH];
    int count = 0;
    for (int i=0; i<NUM_DEVICES_SEARCH; i++) {
        char devpath[255];
        sprintf(devpath, "/dev/video%d", i);
        Device * dev = getDeviceInfo(devpath);
        if (dev != NULL) {
            list[count] = dev;
            count++;
        }
    }
    return new DeviceList(list, count);
}

unsigned char* OpenIMAJGrabber::getImage() {
    return (unsigned char*)(VG->rgb_buffer.start);
}

int OpenIMAJGrabber::nextFrame() {
    return grabNextFrame(VG);
}

void OpenIMAJGrabber::setTimeout(int timeout) {
    VG->timeout = timeout;
}

bool OpenIMAJGrabber::startSession(int width, int height, int millisPerFrame) {
    return startSession(width, height, millisPerFrame, NULL);
}

bool OpenIMAJGrabber::startSession(int width, int height, int millisPerFrame, Device * device) {
    if (device == NULL) {
        DeviceList * list = getVideoDevices();
        if (list->getNumDevices() > 0) {
            device = list->getDevice(0);
        }
        delete list;
    }

    if (device == NULL) {
        return false;
    }

    data = new VideoGrabber();
    strcpy(VG->dev_name, device->getIdentifier());

    VG->requested_width = width;
    VG->requested_height = height;
    if (millisPerFrame > 0)
        VG->requested_rate = 1000.0/(double)millisPerFrame;
    else
        VG->requested_rate = 0;
    VG->timeout = 5000;

    if (open_device(VG) < 0) return false;
    if (init_device(VG) < 0) return false;
    if (start_capturing(VG) < 0) return false;

    return true;
}

void OpenIMAJGrabber::stopSession() {
    if (data == NULL) return;
    
    stop_capturing(VG);
    uninit_device(VG);
    close_device(VG);

    delete VG;
    data = NULL;
}

int OpenIMAJGrabber::getWidth() {
    return VG->format.fmt.pix.width;
}

int OpenIMAJGrabber::getHeight() {
    return VG->format.fmt.pix.height;
}

void process_image(VideoGrabber* grabber, void* buffer, size_t length) {
    if (grabber->rgb_buffer.length != length) {
        if (grabber->rgb_buffer.start != NULL) {
            free(grabber->rgb_buffer.start);
        }
        grabber->rgb_buffer.start = malloc(length);
        grabber->rgb_buffer.length = length;
    }

    memcpy(grabber->rgb_buffer.start, buffer, length);
}









