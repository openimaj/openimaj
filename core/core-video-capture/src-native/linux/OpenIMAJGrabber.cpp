#include "OpenIMAJGrabber.h"
#include "capture.h"

#define NUM_DEVICES_SEARCH 16

#define VG ((VideoGrabber*)this->data)

#define CLEAR(x) memset(&(x), 0, sizeof(x))

using namespace std;

Device * getDeviceInfo(const char * device);

Device * getDeviceInfo(const char * device) {
    char dummy[256];
    int fd;

    if (-1 == access(device, F_OK)) {
        return NULL;
    }

    fd = open(device, O_RDONLY);
    if (-1 == fd) {
        cerr << "warning: error opening device" << endl;
        return NULL;
    }

    if (-1 == ioctl(fd, VIDIOC_QUERYCAP, dummy)) {
        cerr << "warning: not a v4l2 device" << endl;
        close(fd);
        return NULL;
    }

    struct v4l2_capability capability;
    CLEAR(capability);
    if (-1 == ioctl(fd, VIDIOC_QUERYCAP, &capability)) {
        cerr << "error getting capability" << endl;
        close(fd);
        return NULL;
    }

    if (!(capability.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
        cerr << "device doesn't support capture" << endl;
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

void OpenIMAJGrabber::nextFrame() {
    grabNextFrame(VG);
}

bool OpenIMAJGrabber::startSession(int width, int height) {
    return startSession(width, height, NULL);
}

bool OpenIMAJGrabber::startSession(int width, int height, Device * device) {
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
    VG->io = IO_METHOD_READ;

    VG->requested_width = width;
    VG->requested_height = height;

    open_device(VG);
    init_device(VG);
    start_capturing(VG);

    return true;
}

void OpenIMAJGrabber::stopSession() {
    stop_capturing(VG);
    uninit_device(VG);
    close_device(VG);

    delete VG;
    data = NULL;
}

int OpenIMAJGrabber::getWidth() {
    cerr << "getting width " << VG->format.fmt.pix.width << endl;

    return VG->format.fmt.pix.width;
}

int OpenIMAJGrabber::getHeight() {
    return VG->format.fmt.pix.height;
}

void process_image(VideoGrabber*grabber, void* buffer, size_t length) {
    if (grabber->rgb_buffer.length != length) {
        if (grabber->rgb_buffer.start != NULL) {
            free(grabber->rgb_buffer.start);
        }
        grabber->rgb_buffer.start = malloc(length);
        grabber->rgb_buffer.length = length;
    }

    memcpy(grabber->rgb_buffer.start, buffer, length);
}









