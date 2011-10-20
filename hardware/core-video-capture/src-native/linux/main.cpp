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
