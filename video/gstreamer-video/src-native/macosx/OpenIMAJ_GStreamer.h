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

#ifndef __OpenIMAJ_GStreamer__OpenIMAJ_GStreamer__
#define __OpenIMAJ_GStreamer__OpenIMAJ_GStreamer__

#include <gst/gst.h>
#include <gst/gstbuffer.h>
#include <gst/video/video.h>
#include <gst/app/gstappsink.h>
#include <gst/app/gstappsrc.h>
#include <gst/riff/riff-media.h>
#include <gst/pbutils/missing-plugins.h>

/* The classes below are exported */
#pragma GCC visibility push(default)

enum
{
    // modes of the controlling registers (can be: auto, manual, auto single push, absolute Latter allowed with any other mode)
    // every feature can have only one mode turned on at a time
	CV_CAP_PROP_POS_MSEC       =0,
	CV_CAP_PROP_POS_FRAMES     =1,
	CV_CAP_PROP_POS_AVI_RATIO  =2,
	CV_CAP_PROP_FRAME_WIDTH    =3,
	CV_CAP_PROP_FRAME_HEIGHT   =4,
	CV_CAP_PROP_FPS            =5,
    CV_CAP_GSTREAMER_QUEUE_LENGTH	= 200
};

class OpenIMAJCapGStreamer
{
public:
    OpenIMAJCapGStreamer();// { init(); }
    virtual ~OpenIMAJCapGStreamer();// { close(); }
    
    virtual bool open( const char* filename );
    virtual void close();
    
    virtual double getProperty(int);
    virtual bool setProperty(int, double);
    
    virtual bool nextFrame();
    virtual int getWidth() { return getProperty(CV_CAP_PROP_FRAME_WIDTH); };
    virtual int getHeight() { return getProperty(CV_CAP_PROP_FRAME_HEIGHT); };
    virtual unsigned char* getImage();
    
protected:
    void init();
    bool reopen();
    bool isPipelinePlaying();
    void startPipeline();
    void stopPipeline();
    void restartPipeline();
    void setFilter(const char* prop, GType type, int v1, int v2 = 0);
    void removeFilter(const char *filter);
    static void newPad(GstElement *myelement,
                       GstPad     *pad,
                       gpointer    data);
    GstElement*   pipeline;
    GstElement*   uridecodebin;
    GstElement*   color;
    GstElement*   sink;
    GstSample*    sample;
    GstMapInfo*   info;
    GstBuffer*    buffer;
    GstCaps*      caps;
    GstCaps*      buffer_caps;
    unsigned char* frame;
};

#pragma GCC visibility pop

#endif /* defined(__OpenIMAJ_GStreamer__OpenIMAJ_GStreamer__) */
