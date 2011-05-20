#include <iostream>
#include <linux/videodev2.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <malloc.h>

#ifndef capture_
#define capture_

typedef enum {
        IO_METHOD_READ,
        IO_METHOD_MMAP,
        IO_METHOD_USERPTR,
} io_method;

struct buffer {
    void * start;
    size_t length;
};

typedef struct _VideoGrabber {
    char dev_name[256];
    int fd;
    struct v4l2_format format;

    io_method io;

    int requested_width;
    int requested_height;

    struct buffer * buffers;
    unsigned int n_buffers;

    buffer rgb_buffer;
} VideoGrabber;

void open_device(VideoGrabber * grabber);
void init_device(VideoGrabber * grabber);
void start_capturing(VideoGrabber * grabber);
void grabNextFrame(VideoGrabber * grabber);
void stop_capturing(VideoGrabber * grabber);
void uninit_device(VideoGrabber * grabber);
void close_device(VideoGrabber * grabber);

void process_image(VideoGrabber*grabber, void* buffer, size_t length);

#endif
