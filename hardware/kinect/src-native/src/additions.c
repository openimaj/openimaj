#include "libfreenect.h"
#include "additions.h"

int freenect_set_video_mode_proxy(freenect_device* dev, freenect_resolution res, freenect_video_format fmt) {
	freenect_set_video_mode(dev,freenect_find_video_mode(res,fmt));
}

int freenect_get_video_buffer_size(freenect_device* dev)
{
	return freenect_get_current_video_mode(dev).bytes;
}

int freenect_get_depth_buffer_size(freenect_device* dev)
{
	return freenect_get_current_depth_mode(dev).bytes;
}

