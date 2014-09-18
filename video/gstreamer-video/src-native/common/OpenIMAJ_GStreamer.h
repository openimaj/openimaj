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

//
// Heavily inspired by the OpenCV gstreamer interface, which has the following license:
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree to this license.
//  If you do not agree to this license, do not download, install,
//  copy or use the software.
//
//
//                        Intel License Agreement
//                For Open Source Computer Vision Library
//
// Copyright (C) 2008, 2011, Nils Hasler, all rights reserved.
// Third party copyrights are property of their respective owners.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//   * Redistribution's of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//
//   * Redistribution's in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//
//   * The name of Intel Corporation may not be used to endorse or promote products
//     derived from this software without specific prior written permission.
//
// This software is provided by the copyright holders and contributors "as is" and
// any express or implied warranties, including, but not limited to, the implied
// warranties of merchantability and fitness for a particular purpose are disclaimed.
// In no event shall the Intel Corporation or contributors be liable for any direct,
// indirect, incidental, special, exemplary, or consequential damages
// (including, but not limited to, procurement of substitute goods or services;
// loss of use, data, or profits; or business interruption) however caused
// and on any theory of liability, whether in contract, strict liability,
// or tort (including negligence or otherwise) arising in any way out of
// the use of this software, even if advised of the possibility of such damage.
//

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
	CAP_PROP_POS_MSEC       =0,
	CAP_PROP_POS_FRAMES     =1,
	CAP_PROP_POS_AVI_RATIO  =2,
	CAP_PROP_FRAME_WIDTH    =3,
	CAP_PROP_FRAME_HEIGHT   =4,
	CAP_PROP_FPS            =5,
    CAP_GSTREAMER_QUEUE_LENGTH	= 200
};

class OpenIMAJCapGStreamer
{
public:
    OpenIMAJCapGStreamer();
    ~OpenIMAJCapGStreamer();
    
    bool open( const char* filename );
    void close();
    
    double getProperty(int);
    bool setProperty(int, double);
    
    bool nextFrame();
    int getBands();
    int getWidth();
    int getHeight();
    unsigned char* getImage();
    
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
