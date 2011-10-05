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
#include "capture.h"

#include <libv4l2.h>

#define CLEAR(x) memset (&(x), 0, sizeof (x))

static void errno_exit(const char * s)
{
        fprintf (stderr, "%s error %d, %s\n",
                 s, errno, strerror (errno));

        exit (EXIT_FAILURE);
}

static int xioctl(int fd, int request, void * arg) {
        int r;

        do r = v4l2_ioctl (fd, request, arg);
        while (-1 == r && EINTR == errno);

        return r;
}

static int read_frame(VideoGrabber* grabber)
{
        struct v4l2_buffer buf;
        unsigned int i;

        switch (grabber->io) {
        case IO_METHOD_READ:
                if (-1 == v4l2_read (grabber->fd, grabber->buffers[0].start, grabber->buffers[0].length)) {
                        switch (errno) {
                        case EAGAIN:
                                return 0;

                        case EIO:
                                /* Could ignore EIO, see spec. */

                                /* fall through */

                        default:
                                errno_exit ("read");
                        }
                }

                process_image(grabber, grabber->buffers[0].start, grabber->buffers[0].length);

                break;

        case IO_METHOD_MMAP:
                CLEAR (buf);

                buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
                buf.memory = V4L2_MEMORY_MMAP;

                if (-1 == xioctl (grabber->fd, VIDIOC_DQBUF, &buf)) {
                        switch (errno) {
                        case EAGAIN:
                                return 0;

                        case EIO:
                                /* Could ignore EIO, see spec. */

                                /* fall through */

                        default:
                                errno_exit ("VIDIOC_DQBUF");
                        }
                }

                //assert (buf.index < grabber->n_buffers);

                process_image (grabber, grabber->buffers[buf.index].start, grabber->buffers[buf.index].length);

                if (-1 == xioctl (grabber->fd, VIDIOC_QBUF, &buf))
                        errno_exit ("VIDIOC_QBUF");

                break;

        case IO_METHOD_USERPTR:
                CLEAR (buf);

                buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
                buf.memory = V4L2_MEMORY_USERPTR;

                if (-1 == xioctl (grabber->fd, VIDIOC_DQBUF, &buf)) {
                        switch (errno) {
                        case EAGAIN:
                                return 0;

                        case EIO:
                                /* Could ignore EIO, see spec. */

                                /* fall through */

                        default:
                                errno_exit ("VIDIOC_DQBUF");
                        }
                }

                for (i = 0; i < grabber->n_buffers; ++i)
                        if (buf.m.userptr == (unsigned long) grabber->buffers[i].start
                            && buf.length == grabber->buffers[i].length)
                                break;

                //assert (i < grabber->n_buffers);

                process_image (grabber, (void *) buf.m.userptr, buf.length);

                if (-1 == xioctl (grabber->fd, VIDIOC_QBUF, &buf))
                        errno_exit ("VIDIOC_QBUF");

                break;
        }

        return 1;
}

void grabNextFrame(VideoGrabber * grabber) {
                for (;;) {
                        fd_set fds;
                        struct timeval tv;
                        int r;

                        FD_ZERO (&fds);
                        FD_SET (grabber->fd, &fds);

                        /* Timeout. */
                        tv.tv_sec = 5;
                        tv.tv_usec = 0;

                        r = select (grabber->fd + 1, &fds, NULL, NULL, &tv);

                        if (-1 == r) {
                                if (EINTR == errno)
                                        continue;

                                errno_exit ("select");
                        }

                        if (0 == r) {
                                fprintf (stderr, "select timeout\n");
                                exit (EXIT_FAILURE);
                        }

                        if (read_frame(grabber))
                                break;

                        /* EAGAIN - continue select loop. */
                }
}

void stop_capturing(VideoGrabber * grabber) {
        enum v4l2_buf_type type;

        switch (grabber->io) {
        case IO_METHOD_READ:
                /* Nothing to do. */
                break;

        case IO_METHOD_MMAP:
        case IO_METHOD_USERPTR:
                type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

                if (-1 == xioctl (grabber->fd, VIDIOC_STREAMOFF, &type))
                        errno_exit ("VIDIOC_STREAMOFF");

                break;
        }
}

void start_capturing(VideoGrabber * grabber) {
        unsigned int i;
        enum v4l2_buf_type type;

        switch (grabber->io) {
        case IO_METHOD_READ:
                /* Nothing to do. */
                break;

        case IO_METHOD_MMAP:
                for (i = 0; i < grabber->n_buffers; ++i) {
                        struct v4l2_buffer buf;

                        CLEAR (buf);

                        buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
                        buf.memory      = V4L2_MEMORY_MMAP;
                        buf.index       = i;

                        if (-1 == xioctl (grabber->fd, VIDIOC_QBUF, &buf))
                                errno_exit ("VIDIOC_QBUF");
                }

                type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

                if (-1 == xioctl (grabber->fd, VIDIOC_STREAMON, &type))
                        errno_exit ("VIDIOC_STREAMON");

                break;

        case IO_METHOD_USERPTR:
                for (i = 0; i < grabber->n_buffers; ++i) {
                        struct v4l2_buffer buf;

                        CLEAR (buf);

                        buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
                        buf.memory      = V4L2_MEMORY_USERPTR;
                        buf.index       = i;
                        buf.m.userptr   = (unsigned long) grabber->buffers[i].start;
                        buf.length      = grabber->buffers[i].length;

                        if (-1 == xioctl (grabber->fd, VIDIOC_QBUF, &buf))
                                errno_exit ("VIDIOC_QBUF");
                }

                type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

                if (-1 == xioctl (grabber->fd, VIDIOC_STREAMON, &type))
                        errno_exit ("VIDIOC_STREAMON");

                break;
        }
}

void uninit_device(VideoGrabber * grabber) {
        unsigned int i;

        switch (grabber->io) {
        case IO_METHOD_READ:
                free (grabber->buffers[0].start);
                break;

        case IO_METHOD_MMAP:
                for (i = 0; i < grabber->n_buffers; ++i)
                        if (-1 == munmap (grabber->buffers[i].start, grabber->buffers[i].length))
                                errno_exit ("munmap");
                break;

        case IO_METHOD_USERPTR:
                for (i = 0; i < grabber->n_buffers; ++i)
                        free (grabber->buffers[i].start);
                break;
        }

        free (grabber->buffers);
}

static void init_read(VideoGrabber* grabber, unsigned int buffer_size) {
        grabber->buffers = (buffer*)calloc (1, sizeof (*(grabber->buffers)));

        if (!grabber->buffers) {
                fprintf (stderr, "Out of memory\n");
                exit (EXIT_FAILURE);
        }

        grabber->buffers[0].length = buffer_size;
        grabber->buffers[0].start = malloc (buffer_size);

        if (!grabber->buffers[0].start) {
                fprintf (stderr, "Out of memory\n");
                exit (EXIT_FAILURE);
        }
}

static void init_mmap(VideoGrabber* grabber)
{
        struct v4l2_requestbuffers req;

        CLEAR (req);

        req.count               = 4;
        req.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        req.memory              = V4L2_MEMORY_MMAP;

        if (-1 == xioctl (grabber->fd, VIDIOC_REQBUFS, &req)) {
                if (EINVAL == errno) {
                        fprintf (stderr, "%s does not support "
                                 "memory mapping\n", grabber->dev_name);
                        exit (EXIT_FAILURE);
                } else {
                        errno_exit ("VIDIOC_REQBUFS");
                }
        }

        if (req.count < 2) {
                fprintf (stderr, "Insufficient buffer memory on %s\n",
                         grabber->dev_name);
                exit (EXIT_FAILURE);
        }

        grabber->buffers = (buffer*)calloc (req.count, sizeof (*(grabber->buffers)));

        if (!grabber->buffers) {
                fprintf (stderr, "Out of memory\n");
                exit (EXIT_FAILURE);
        }

        for (grabber->n_buffers = 0; grabber->n_buffers < req.count; ++(grabber->n_buffers)) {
                struct v4l2_buffer buf;

                CLEAR (buf);

                buf.type        = V4L2_BUF_TYPE_VIDEO_CAPTURE;
                buf.memory      = V4L2_MEMORY_MMAP;
                buf.index       = grabber->n_buffers;

                if (-1 == xioctl (grabber->fd, VIDIOC_QUERYBUF, &buf))
                        errno_exit ("VIDIOC_QUERYBUF");

                grabber->buffers[grabber->n_buffers].length = buf.length;
                grabber->buffers[grabber->n_buffers].start =
                        mmap (NULL /* start anywhere */,
                              buf.length,
                              PROT_READ | PROT_WRITE /* required */,
                              MAP_SHARED /* recommended */,
                              grabber->fd, buf.m.offset);

                if (MAP_FAILED == grabber->buffers[grabber->n_buffers].start)
                        errno_exit ("mmap");
        }
}

static void init_userp(VideoGrabber* grabber, unsigned int buffer_size)
{
        struct v4l2_requestbuffers req;
        unsigned int page_size;

        page_size = getpagesize ();
        buffer_size = (buffer_size + page_size - 1) & ~(page_size - 1);

        CLEAR (req);

        req.count               = 4;
        req.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        req.memory              = V4L2_MEMORY_USERPTR;

        if (-1 == xioctl (grabber->fd, VIDIOC_REQBUFS, &req)) {
                if (EINVAL == errno) {
                        fprintf (stderr, "%s does not support "
                                 "user pointer i/o\n", grabber->dev_name);
                        exit (EXIT_FAILURE);
                } else {
                        errno_exit ("VIDIOC_REQBUFS");
                }
        }

        grabber->buffers = (buffer*)calloc (4, sizeof (*(grabber->buffers)));

        if (!grabber->buffers) {
                fprintf (stderr, "Out of memory\n");
                exit (EXIT_FAILURE);
        }

        for (grabber->n_buffers = 0; grabber->n_buffers < 4; ++(grabber->n_buffers)) {
                grabber->buffers[grabber->n_buffers].length = buffer_size;
                grabber->buffers[grabber->n_buffers].start = memalign (/* boundary */ page_size,
                                                     buffer_size);

                if (!grabber->buffers[grabber->n_buffers].start) {
                        fprintf (stderr, "Out of memory\n");
                        exit (EXIT_FAILURE);
                }
        }
}

void init_device(VideoGrabber * grabber) {
        struct v4l2_capability cap;
        struct v4l2_cropcap cropcap;
        struct v4l2_crop crop;
        unsigned int min;

        if (-1 == xioctl (grabber->fd, VIDIOC_QUERYCAP, &cap)) {
                if (EINVAL == errno) {
                        fprintf (stderr, "%s is no V4L2 device\n",
                                 grabber->dev_name);
                        exit (EXIT_FAILURE);
                } else {
                        errno_exit ("VIDIOC_QUERYCAP");
                }
        }

        if (!(cap.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
                fprintf (stderr, "%s is no video capture device\n",
                         grabber->dev_name);
                exit (EXIT_FAILURE);
        }

        switch (grabber->io) {
        case IO_METHOD_READ:
                if (!(cap.capabilities & V4L2_CAP_READWRITE)) {
                        fprintf (stderr, "%s does not support read i/o\n",
                                 grabber->dev_name);
                        exit (EXIT_FAILURE);
                }

                break;

        case IO_METHOD_MMAP:
        case IO_METHOD_USERPTR:
                if (!(cap.capabilities & V4L2_CAP_STREAMING)) {
                        fprintf (stderr, "%s does not support streaming i/o\n",
                                 grabber->dev_name);
                        exit (EXIT_FAILURE);
                }

                break;
        }


        /* Select video input, video standard and tune here. */


        CLEAR (cropcap);

        cropcap.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

        if (0 == xioctl (grabber->fd, VIDIOC_CROPCAP, &cropcap)) {
                crop.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
                crop.c = cropcap.defrect; /* reset to default */

                if (-1 == xioctl (grabber->fd, VIDIOC_S_CROP, &crop)) {
                        switch (errno) {
                        case EINVAL:
                                /* Cropping not supported. */
                                break;
                        default:
                                /* Errors ignored. */
                                break;
                        }
                }
        } else {
                /* Errors ignored. */
        }

        CLEAR (grabber->format);

        grabber->format.type                = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        grabber->format.fmt.pix.width       = grabber->requested_width;
        grabber->format.fmt.pix.height      = grabber->requested_height;
        grabber->format.fmt.pix.pixelformat = V4L2_PIX_FMT_RGB24;
        grabber->format.fmt.pix.field       = V4L2_FIELD_NONE;

        fprintf(stderr, "trying rgb24\n");
        if (-1 == xioctl (grabber->fd, VIDIOC_S_FMT, &(grabber->format))) {
                    errno_exit ("VIDIOC_S_FMT");
        }

        /* Note VIDIOC_S_FMT may change width and height. */

        /* Buggy driver paranoia. */
        min = grabber->format.fmt.pix.width * 2;
        if (grabber->format.fmt.pix.bytesperline < min)
                grabber->format.fmt.pix.bytesperline = min;
        min = grabber->format.fmt.pix.bytesperline * grabber->format.fmt.pix.height;
        if (grabber->format.fmt.pix.sizeimage < min)
                grabber->format.fmt.pix.sizeimage = min;

        switch (grabber->io) {
        case IO_METHOD_READ:
                init_read (grabber, grabber->format.fmt.pix.sizeimage);
                break;

        case IO_METHOD_MMAP:
                init_mmap (grabber);
                break;

        case IO_METHOD_USERPTR:
                init_userp (grabber, grabber->format.fmt.pix.sizeimage);
                break;
        }
}

void close_device(VideoGrabber * grabber) {
        if (-1 == v4l2_close (grabber->fd))
                errno_exit ("close");

        grabber->fd = -1;
}

void open_device(VideoGrabber * grabber) {
        struct stat st;

        if (-1 == stat (grabber->dev_name, &st)) {
                fprintf (stderr, "Cannot identify '%s': %d, %s\n",
                         grabber->dev_name, errno, strerror (errno));
                exit (EXIT_FAILURE);
        }

        if (!S_ISCHR (st.st_mode)) {
                fprintf (stderr, "%s is no device\n", grabber->dev_name);
                exit (EXIT_FAILURE);
        }

        grabber->fd = v4l2_open (grabber->dev_name, O_RDWR /* required */ | O_NONBLOCK, 0);

        if (-1 == grabber->fd) {
                fprintf (stderr, "Cannot open '%s': %d, %s\n",
                         grabber->dev_name, errno, strerror (errno));
                exit (EXIT_FAILURE);
        }
}

