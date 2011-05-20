#include <iostream>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/videodev2.h>
#include <stdlib.h>
#include <string.h>

using namespace std;

int main(int argc, const char * argv[]) {
    char dummy[256];
    char * device = "/dev/video0";
    int fd;

    fd = open(device, O_RDONLY);
    if (-1 == fd) {
        cerr << "error opening device" << endl;
        exit(1);
    }

    if (-1 == ioctl(fd, VIDIOC_QUERYCAP, dummy)) {
        cerr << "not a v4l2 device" << endl;
        exit(1);
    }

    struct v4l2_capability capability;
    memset(&capability, 0, sizeof(capability));
    if (-1 == ioctl(fd, VIDIOC_QUERYCAP, &capability)) {
        cerr << "error getting capability" << endl;
        exit(1);
    }

    if (!(capability.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
        cerr << "device doesn't support capture" << endl;
        exit(1);
    }

    cout << capability.card << endl;

    struct v4l2_input input;
    memset(&input, 0, sizeof(input));
    if (-1 == ioctl(fd, VIDIOC_ENUMINPUT, &input)) {
        cerr << "error getting input" << endl;
        exit(1);
    }

    cout << input.name << endl;


    return 0;
}
