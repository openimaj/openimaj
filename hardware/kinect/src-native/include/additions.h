#include "libfreenect.h"

int freenect_set_video_mode_proxy(freenect_device* dev, freenect_resolution res, freenect_video_format fmt);
int freenect_get_video_buffer_size(freenect_device* dev);
int freenect_get_depth_buffer_size(freenect_device* dev);

