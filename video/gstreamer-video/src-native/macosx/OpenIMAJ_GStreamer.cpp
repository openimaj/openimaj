//
//  OpenIMAJ_GStreamer.cpp
//  OpenIMAJ-GStreamer
//
//  Created by Jonathon Hare on 07/08/2014.
//  Copyright (c) 2014 Jonathon Hare. All rights reserved.
//

#include <unistd.h>
#include <string.h>
#include <map>
#include <stdio.h>
#include <stdlib.h>
#include <cmath>
#include "OpenIMAJ_GStreamer.h"

#define COLOR_ELEM "videoconvert"

#ifdef NDEBUG
#define CV_WARN(message)
#else
#define CV_WARN(message) fprintf(stderr, "warning: %s (%s:%d)\n", message, __FILE__, __LINE__)
#endif

static bool isInited = false;

void toFraction(double decimal, double &numerator, double &denominator);
void handleMessage(GstElement * pipeline);


OpenIMAJCapGStreamer::OpenIMAJCapGStreamer() { init(); }
OpenIMAJCapGStreamer::~OpenIMAJCapGStreamer() { close(); }

/*!
 * \brief OpenIMAJCapGStreamer::init
 * inits the class
 */
void OpenIMAJCapGStreamer::init()
{
    pipeline = NULL;
    uridecodebin = NULL;
    color = NULL;
    sink = NULL;
    sample = NULL;
    info = new GstMapInfo;
    buffer = NULL;
    caps = NULL;
    buffer_caps = NULL;
    frame = NULL;
}

/*!
 * \brief OpenIMAJCapGStreamer::close
 * Closes the pipeline and destroys all instances
 */
void OpenIMAJCapGStreamer::close()
{
    if (isPipelinePlaying())
        this->stopPipeline();
    
    if(pipeline) {
        gst_element_set_state(GST_ELEMENT(pipeline), GST_STATE_NULL);
        gst_object_unref(GST_OBJECT(pipeline));
        pipeline = NULL;
    }
    if(uridecodebin){
        gst_object_unref(GST_OBJECT(uridecodebin));
        uridecodebin = NULL;
    }
    if(color){
        gst_object_unref(GST_OBJECT(color));
        color = NULL;
    }
    if(sink){
        gst_object_unref(GST_OBJECT(sink));
        sink = NULL;
    }
    if(buffer) {
        gst_buffer_unref(buffer);
        buffer = NULL;
    }
    if(frame) {
        frame = NULL;
    }
    if(caps){
        gst_caps_unref(caps);
        caps = NULL;
    }
    if(buffer_caps){
        gst_caps_unref(buffer_caps);
        buffer_caps = NULL;
    }
    if(sample){
        gst_sample_unref(sample);
        sample = NULL;
    }
}

/*!
 * \brief OpenIMAJCapGStreamer::grabFrame
 * \return
 * Grabs a sample from the pipeline, awaiting consumation by retreiveFrame.
 * The pipeline is started if it was not running yet
 */
bool OpenIMAJCapGStreamer::nextFrame()
{
    if(!pipeline)
        return false;
    
    // start the pipeline if it was not in playing state yet
    if(!this->isPipelinePlaying())
        this->startPipeline();
    
    // bail out if EOS
    if(gst_app_sink_is_eos(GST_APP_SINK(sink)))
        return false;
    
    if(sample)
        gst_sample_unref(sample);
    
    sample = gst_app_sink_pull_sample(GST_APP_SINK(sink));
    
    if(!sample)
        return false;
    
    buffer = gst_sample_get_buffer(sample);
    
    if(!buffer)
        return false;
    
    return true;
}

/*!
 * \brief OpenIMAJCapGStreamer::retrieveFrame
 * \return IplImage pointer. [Transfer Full]
 *  Retreive the previously grabbed buffer, and wrap it in an IPLImage structure
 */
unsigned char* OpenIMAJCapGStreamer::getImage()
{
    if(!buffer)
        return 0;
    
    //construct a frame header if we did not have any yet
//    if(!frame)
//    {
//        gint height, width;
//        
//        //reuse the caps ptr
//        if (buffer_caps)
//            gst_caps_unref(buffer_caps);
//        
//        buffer_caps = gst_sample_get_caps(sample);
//
//        // bail out in no caps
//        assert(gst_caps_get_size(buffer_caps) == 1);
//        GstStructure* structure = gst_caps_get_structure(buffer_caps, 0);
//        
//        // bail out if width or height are 0
//        if(!gst_structure_get_int(structure, "width", &width) ||
//           !gst_structure_get_int(structure, "height", &height))
//        {
//            return 0;
//        }
//        
//        
//        int depth = 3;
//
//        depth = 0;
//        const gchar* name = gst_structure_get_name(structure);
//        const gchar* format = gst_structure_get_string(structure, "format");
//        
//        if (!name || !format)
//            return 0;
//        
//        // we support 3 types of data:
//        //     video/x-raw, format=BGR   -> 8bit, 3 channels
//        //     video/x-raw, format=GRAY8 -> 8bit, 1 channel
//        //     video/x-bayer             -> 8bit, 1 channel
//        // bayer data is never decoded, the user is responsible for that
//        // everything is 8 bit, so we just test the caps for bit depth
//        
//        if (strcasecmp(name, "video/x-raw") == 0)
//        {
//            if (strcasecmp(format, "BGR") == 0) {
//                depth = 3;
//            }
//            else if(strcasecmp(format, "GRAY8") == 0){
//                depth = 1;
//            }
//        }
//        else if (strcasecmp(name, "video/x-bayer") == 0)
//        {
//            depth = 1;
//        }
//
//        if (depth > 0) {
//            frame = cvCreateImageHeader(cvSize(width, height), IPL_DEPTH_8U, depth);
//        }else{
//            return 0;
//        }
//    }
    
    // gstreamer expects us to handle the memory at this point
    // so we can just wrap the raw buffer and be done with it
    // the data ptr in GstMapInfo is only valid throughout the mapifo objects life.
    // TODO: check if reusing the mapinfo object is ok.
    
    gboolean success = gst_buffer_map(buffer,info, (GstMapFlags)GST_MAP_READ);
    if (!success){
        //something weird went wrong here. abort. abort.
        //fprintf(stderr,"GStreamer: unable to map buffer");
        return 0;
    }
    frame = (unsigned char*)info->data;
    gst_buffer_unmap(buffer,info);
    
    return frame;
}


/*!
 * \brief OpenIMAJCapGStreamer::isPipelinePlaying
 * \return if the pipeline is currently playing.
 */
bool OpenIMAJCapGStreamer::isPipelinePlaying()
{
    GstState current, pending;
    GstClockTime timeout = 5*GST_SECOND;
    if(!GST_IS_ELEMENT(pipeline)){
        return false;
    }
    
    GstStateChangeReturn ret = gst_element_get_state(GST_ELEMENT(pipeline),&current, &pending, timeout);
    if (!ret){
        //fprintf(stderr, "GStreamer: unable to query pipeline state\n");
        return false;
    }
    
    return current == GST_STATE_PLAYING;
}

/*!
 * \brief OpenIMAJCapGStreamer::startPipeline
 * Start the pipeline by setting it to the playing state
 */
void OpenIMAJCapGStreamer::startPipeline()
{
    //fprintf(stderr, "relinked, pausing\n");
    if(gst_element_set_state(GST_ELEMENT(pipeline), GST_STATE_PLAYING) ==
       GST_STATE_CHANGE_FAILURE) {
        //CV_ERROR(1, "GStreamer: unable to start pipeline\n");
        gst_object_unref(pipeline);
        return;
    }
    
    //printf("state now playing\n");
    handleMessage(pipeline);
}


/*!
 * \brief OpenIMAJCapGStreamer::stopPipeline
 * Stop the pipeline by setting it to NULL
 */
void OpenIMAJCapGStreamer::stopPipeline()
{
    //fprintf(stderr, "restarting pipeline, going to ready\n");
    if(gst_element_set_state(GST_ELEMENT(pipeline), GST_STATE_NULL) ==
       GST_STATE_CHANGE_FAILURE) {
        //CV_ERROR(1, "GStreamer: unable to stop pipeline\n");
        gst_object_unref(pipeline);
        return;
    }
}

/*!
 * \brief OpenIMAJCapGStreamer::restartPipeline
 * Restart the pipeline
 */
void OpenIMAJCapGStreamer::restartPipeline()
{
    handleMessage(pipeline);
    
    this->stopPipeline();
    this->startPipeline();
}


/*!
 * \brief OpenIMAJCapGStreamer::setFilter
 * \param prop the property name
 * \param type glib property type
 * \param v1 the value
 * \param v2 second value of property type requires it, else NULL
 * Filter the output formats by setting appsink caps properties
 */
void OpenIMAJCapGStreamer::setFilter(const char *prop, GType type, int v1, int v2)
{
    if(!caps || !( GST_IS_CAPS (caps) ))
    {
        if(type == G_TYPE_INT)
        {
            caps = gst_caps_new_simple("video/x-raw","format",G_TYPE_STRING,"BGR", prop, type, v1, NULL);
        }
        else
        {
            caps = gst_caps_new_simple("video/x-raw","format",G_TYPE_STRING,"BGR", prop, type, v1, v2, NULL);
        }
    }
    else
    {
        if (! gst_caps_is_writable(caps))
            caps = gst_caps_make_writable (caps);
        if(type == G_TYPE_INT){
            gst_caps_set_simple(caps, prop, type, v1, NULL);
        }else{
            gst_caps_set_simple(caps, prop, type, v1, v2, NULL);
        }
    }
    
    caps = gst_caps_fixate(caps);
    
    gst_app_sink_set_caps(GST_APP_SINK(sink), caps);
}


/*!
 * \brief OpenIMAJCapGStreamer::removeFilter
 * \param filter filter to remove
 * remove the specified filter from the appsink template caps
 */
void OpenIMAJCapGStreamer::removeFilter(const char *filter)
{
    if(!caps)
        return;
    
    if (! gst_caps_is_writable(caps))
        caps = gst_caps_make_writable (caps);
    
    GstStructure *s = gst_caps_get_structure(caps, 0);
    gst_structure_remove_field(s, filter);
    
    gst_app_sink_set_caps(GST_APP_SINK(sink), caps);
}

/*!
 * \brief OpenIMAJCapGStreamer::newPad link dynamic padd
 * \param pad
 * \param data
 * decodebin creates pads based on stream information, which is not known upfront
 * on receiving the pad-added signal, we connect it to the colorspace conversion element
 */
void OpenIMAJCapGStreamer::newPad(GstElement * /*elem*/,
                                 GstPad     *pad,
                                 gpointer    data)
{
    GstPad *sinkpad;
    GstElement *color = (GstElement *) data;
    
    sinkpad = gst_element_get_static_pad (color, "sink");
    if (!sinkpad){
        //fprintf(stderr, "Gstreamer: no pad named sink\n");
        return;
    }
    
    gst_pad_link (pad, sinkpad);
    gst_object_unref (sinkpad);
}

/*!
 * \brief OpenIMAJCapGStreamer::open Open the given file with gstreamer
 * \param type CvCapture type. One of CV_CAP_GSTREAMER_*
 * \param filename Filename to open in case of CV_CAP_GSTREAMER_FILE
 * \return boolean. Specifies if opening was succesful.
 *
 * In case of CV_CAP_GSTREAMER_V4L(2), a pipelin is constructed as follows:
 *    v4l2src ! autoconvert ! appsink
 *
 *
 * The 'filename' parameter is not limited to filesystem paths, and may be one of the following:
 *
 *  - a normal filesystem path:
 *        e.g. video.avi or /path/to/video.avi or C:\\video.avi
 *  - an uri:
 *        e.g. file:///path/to/video.avi or rtsp:///path/to/stream.asf
 *  - a gstreamer pipeline description:
 *        e.g. videotestsrc ! videoconvert ! appsink
 *        the appsink name should be either 'appsink0' (the default) or 'opencvsink'
 *
 *  When dealing with a file, OpenIMAJCapGStreamer will not drop frames if the grabbing interval
 *  larger than the framerate period. (Unlike the uri or manual pipeline description, which assume
 *  a live source)
 *
 *  The pipeline will only be started whenever the first frame is grabbed. Setting pipeline properties
 *  is really slow if we need to restart the pipeline over and over again.
 *
 */
bool OpenIMAJCapGStreamer::open(const char* filename )
{
    if(!isInited) {
        //FIXME: threadsafety
        gst_init (NULL, NULL);
        isInited = true;
    }

    
    bool stream = false;
    bool manualpipeline = false;
    char *uri = NULL;
    uridecodebin = NULL;
    
    // test if we have a valid uri. If so, open it with an uridecodebin
    // else, we might have a file or a manual pipeline.
    // if gstreamer cannot parse the manual pipeline, we assume we were given and
    // ordinary file path.
    if(!gst_uri_is_valid(filename))
    {
        uri = realpath(filename, NULL);
        stream = false;
        if(uri)
        {
            uri = g_filename_to_uri(uri, NULL, NULL);
            if(!uri) {
                CV_WARN("GStreamer: Error opening file\n");
                close();
                return false;
            }
        }
        else
        {
            GError *err = NULL;
            uridecodebin = gst_parse_launch(filename, &err);
            if(!uridecodebin) {
                //fprintf(stderr, "GStreamer: Error opening bin: %s\n", err->message);
                //close();
                return false;
            }
            stream = true;
            manualpipeline = true;
        }
    } else {
        stream = true;
        uri = g_strdup(filename);
    }
    
    bool element_from_uri = false;
    if(!uridecodebin)
    {
        // At this writing, the v4l2 element (and maybe others too) does not support caps renegotiation.
        // This means that we cannot use an uridecodebin when dealing with v4l2, since setting
        // capture properties will not work.
        // The solution (probably only until gstreamer 1.2) is to make an element from uri when dealing with v4l2.
        gchar * protocol = gst_uri_get_protocol(uri);
        if (!strcasecmp(protocol , "v4l2"))
        {
            uridecodebin = gst_element_make_from_uri(GST_URI_SRC, uri, "src", NULL);

            element_from_uri = true;
        }else{
            uridecodebin = gst_element_factory_make ("uridecodebin", NULL);
            g_object_set(G_OBJECT(uridecodebin),"uri",uri, NULL);
        }
        g_free(protocol);
        
        if(!uridecodebin) {
            //fprintf(stderr, "GStreamer: Error opening bin: %s\n", err->message);
            close();
            return false;
        }
    }
    
    if(manualpipeline)
    {
        GstIterator *it = NULL;
        it = gst_bin_iterate_sinks (GST_BIN(uridecodebin));
        
        gboolean done = FALSE;
        GstElement *element = NULL;
        gchar* name = NULL;
        GValue value = G_VALUE_INIT;
        
        while (!done) {
            switch (gst_iterator_next (it, &value)) {
                case GST_ITERATOR_OK:
                    element = GST_ELEMENT (g_value_get_object (&value));
                    name = gst_element_get_name(element);
                    if (name){
                        if(strstr(name, "opencvsink") != NULL || strstr(name, "appsink") != NULL) {
                            sink = GST_ELEMENT ( gst_object_ref (element) );
                            done = TRUE;
                        }
                        g_free(name);
                    }
                    g_value_unset (&value);
                    
                    break;
                case GST_ITERATOR_RESYNC:
                    gst_iterator_resync (it);
                    break;
                case GST_ITERATOR_ERROR:
                case GST_ITERATOR_DONE:
                    done = TRUE;
                    break;
            }
        }
        gst_iterator_free (it);
        
        
        if (!sink){
            //CV_ERROR(1, "GStreamer: cannot find appsink in manual pipeline\n");
            return false;
        }
        
        pipeline = uridecodebin;
    }
    else
    {
        pipeline = gst_pipeline_new (NULL);
        // videoconvert (in 0.10: ffmpegcolorspace) automatically selects the correct colorspace
        // conversion based on caps.
        color = gst_element_factory_make(COLOR_ELEM, NULL);
        sink = gst_element_factory_make("appsink", NULL);
        
        gst_bin_add_many(GST_BIN(pipeline), uridecodebin, color, sink, NULL);
        
        if(element_from_uri) {
            if(!gst_element_link(uridecodebin, color)) {
                //CV_ERROR(1, "GStreamer: cannot link color -> sink\n");
                gst_object_unref(pipeline);
                return false;
            }
        }else{
            g_signal_connect(uridecodebin, "pad-added", G_CALLBACK(newPad), color);
        }
        
        if(!gst_element_link(color, sink)) {
            //CV_ERROR(1, "GStreamer: cannot link color -> sink\n");
            gst_object_unref(pipeline);
            return false;
        }
    }
    
    //TODO: is 1 single buffer really high enough?
    gst_app_sink_set_max_buffers (GST_APP_SINK(sink), 1);
    gst_app_sink_set_drop (GST_APP_SINK(sink), stream);
    //do not emit signals: all calls will be synchronous and blocking
    gst_app_sink_set_emit_signals (GST_APP_SINK(sink), 0);
    
    // support 1 and 3 channel 8 bit data, as well as bayer (also  1 channel, 8bit)
    caps = gst_caps_from_string("video/x-raw, format=(string){BGR, GRAY8}; video/x-bayer,format=(string){rggb,bggr,grbg,gbrg}");
    gst_app_sink_set_caps(GST_APP_SINK(sink), caps);
    gst_caps_unref(caps);
    
    //we do not start recording here just yet.
    // the user probably wants to set capture properties first, so start recording whenever the first frame is requested
    
    return true;
}

/*!
 * \brief OpenIMAJCapGStreamer::getProperty retreive the requested property from the pipeline
 * \param propId requested property
 * \return property value
 *
 * There are two ways the properties can be retreived. For seek-based properties we can query the pipeline.
 * For frame-based properties, we use the caps of the lasst receivef sample. This means that some properties
 * are not available until a first frame was received
 */
double OpenIMAJCapGStreamer::getProperty( int propId )
{
    GstFormat format;
    gint64 value;
    gboolean status;
    
#define FORMAT format
    
    if(!pipeline) {
        CV_WARN("GStreamer: no pipeline");
        return false;
    }
    
    switch(propId) {
        case CV_CAP_PROP_POS_MSEC:
            format = GST_FORMAT_TIME;
            status = gst_element_query_position(sink, FORMAT, &value);
            if(!status) {
                CV_WARN("GStreamer: unable to query position of stream");
                return false;
            }
            return value * 1e-6; // nano seconds to milli seconds
        case CV_CAP_PROP_POS_FRAMES:
            format = GST_FORMAT_DEFAULT;
            status = gst_element_query_position(sink, FORMAT, &value);
            if(!status) {
                CV_WARN("GStreamer: unable to query position of stream");
                return false;
            }
            return value;
        case CV_CAP_PROP_POS_AVI_RATIO:
            format = GST_FORMAT_PERCENT;
            status = gst_element_query_position(sink, FORMAT, &value);
            if(!status) {
                CV_WARN("GStreamer: unable to query position of stream");
                return false;
            }
            return ((double) value) / GST_FORMAT_PERCENT_MAX;
        case CV_CAP_PROP_FRAME_WIDTH: {
            if (!buffer_caps){
                CV_WARN("GStreamer: unable to query width of frame; no frame grabbed yet");
                return 0;
            }
            GstStructure* structure = gst_caps_get_structure(buffer_caps, 0);
            gint width = 0;
            if(!gst_structure_get_int(structure, "width", &width)){
                CV_WARN("GStreamer: unable to query width of frame");
                return 0;
            }
            return width;
            break;
        }
        case CV_CAP_PROP_FRAME_HEIGHT: {
            if (!buffer_caps){
                CV_WARN("GStreamer: unable to query height of frame; no frame grabbed yet");
                return 0;
            }
            GstStructure* structure = gst_caps_get_structure(buffer_caps, 0);
            gint height = 0;
            if(!gst_structure_get_int(structure, "height", &height)){
                CV_WARN("GStreamer: unable to query height of frame");
                return 0;
            }
            return height;
            break;
        }
        case CV_CAP_PROP_FPS: {
            if (!buffer_caps){
                CV_WARN("GStreamer: unable to query framerate of stream; no frame grabbed yet");
                return 0;
            }
            GstStructure* structure = gst_caps_get_structure(buffer_caps, 0);
            gint num = 0, denom=1;
            if(!gst_structure_get_fraction(structure, "framerate", &num, &denom)){
                CV_WARN("GStreamer: unable to query framerate of stream");
                return 0;
            }
            return (double)num/(double)denom;
            break;
        }
        case CV_CAP_GSTREAMER_QUEUE_LENGTH:
            if(!sink) {
                CV_WARN("GStreamer: there is no sink yet");
                return false;
            }
            return gst_app_sink_get_max_buffers(GST_APP_SINK(sink));
        default:
            CV_WARN("GStreamer: unhandled property");
            break;
    }
    
#undef FORMAT
    
    return false;
}

/*!
 * \brief OpenIMAJCapGStreamer::setProperty
 * \param propId
 * \param value
 * \return success
 * Sets the desired property id with val. If the pipeline is running,
 * it is briefly stopped and started again after the property was set
 */
bool OpenIMAJCapGStreamer::setProperty( int propId, double value )
{
    GstFormat format;
    GstSeekFlags flags;
    
    if(!pipeline) {
        CV_WARN("GStreamer: no pipeline");
        return false;
    }
    
    bool wasPlaying = this->isPipelinePlaying();
    if (wasPlaying)
        this->stopPipeline();
    
    
    switch(propId) {
        case CV_CAP_PROP_POS_MSEC:
            format = GST_FORMAT_TIME;
            flags = (GstSeekFlags) (GST_SEEK_FLAG_FLUSH|GST_SEEK_FLAG_ACCURATE);
            if(!gst_element_seek_simple(GST_ELEMENT(pipeline), format,
                                        flags, (gint64) (value * GST_MSECOND))) {
                CV_WARN("GStreamer: unable to seek");
            }
            break;
        case CV_CAP_PROP_POS_FRAMES:
            format = GST_FORMAT_DEFAULT;
            flags = (GstSeekFlags) (GST_SEEK_FLAG_FLUSH|GST_SEEK_FLAG_ACCURATE);
            if(!gst_element_seek_simple(GST_ELEMENT(pipeline), format,
                                        flags, (gint64) value)) {
                CV_WARN("GStreamer: unable to seek");
            }
            break;
        case CV_CAP_PROP_POS_AVI_RATIO:
            format = GST_FORMAT_PERCENT;
            flags = (GstSeekFlags) (GST_SEEK_FLAG_FLUSH|GST_SEEK_FLAG_ACCURATE);
            if(!gst_element_seek_simple(GST_ELEMENT(pipeline), format,
                                        flags, (gint64) (value * GST_FORMAT_PERCENT_MAX))) {
                CV_WARN("GStreamer: unable to seek");
            }
            break;
        case CV_CAP_PROP_FRAME_WIDTH:
            if(value > 0)
                setFilter("width", G_TYPE_INT, (int) value, 0);
            else
                removeFilter("width");
            break;
        case CV_CAP_PROP_FRAME_HEIGHT:
            if(value > 0)
                setFilter("height", G_TYPE_INT, (int) value, 0);
            else
                removeFilter("height");
            break;
        case CV_CAP_PROP_FPS:
            if(value > 0) {
                double num=0, denom = 1;
                toFraction(value, num,  denom);
                setFilter("framerate", GST_TYPE_FRACTION, value, denom);
            } else
                removeFilter("framerate");
            break;
        case CV_CAP_GSTREAMER_QUEUE_LENGTH:
            if(!sink)
                break;
            gst_app_sink_set_max_buffers(GST_APP_SINK(sink), (guint) value);
            break;
        default:
            CV_WARN("GStreamer: unhandled property");
    }
    
    if (wasPlaying)
        this->startPipeline();
    
    return false;
}

///*!
// * \brief cvCreateCapture_GStreamer
// * \param type
// * \param filename
// * \return
// */
//OpenIMAJCapGStreamer* OpenIMAJCapGStreamer( const char* filename )
//{
//    OpenIMAJCapGStreamer* capture = new OpenIMAJCapGStreamer;
//    
//    if( capture->open( filename ))
//        return capture;
//    
//    delete capture;
//    return 0;
//}

// utility functions

/*!
 * \brief toFraction
 * \param decimal
 * \param numerator
 * \param denominator
 * Split a floating point value into numerator and denominator
 */
void toFraction(double decimal, double &numerator, double &denominator)
{
    double dummy;
    double whole;
    decimal = modf (decimal, &whole);
    for (denominator = 1; denominator<=100; denominator++){
        if (modf(denominator * decimal, &dummy) < 0.001f)
            break;
    }
    numerator = denominator * decimal;
}


/*!
 * \brief handleMessage
 * Handles gstreamer bus messages. Mainly for debugging purposes and ensuring clean shutdown on error
 */
void handleMessage(GstElement * pipeline)
{
    GError *err = NULL;
    gchar *debug = NULL;
    GstBus* bus = NULL;
    GstStreamStatusType tp;
    GstElement * elem = NULL;
    GstMessage* msg  = NULL;
    
    bus = gst_element_get_bus(pipeline);
    
    while(gst_bus_have_pending(bus)) {
        msg = gst_bus_pop(bus);
        
        //printf("Got %s message\n", GST_MESSAGE_TYPE_NAME(msg));
        
        if(gst_is_missing_plugin_message(msg))
        {
            //CV_ERROR(1, "GStreamer: your gstreamer installation is missing a required plugin\n");
            fprintf(stderr, "GStreamer: your gstreamer installation is missing a required plugin\n");
        }
        else
        {
            switch (GST_MESSAGE_TYPE (msg)) {
                case GST_MESSAGE_STATE_CHANGED:
                    GstState oldstate, newstate, pendstate;
                    gst_message_parse_state_changed(msg, &oldstate, &newstate, &pendstate);
                    //fprintf(stderr, "state changed from %s to %s (pending: %s)\n", gst_element_state_get_name(oldstate),
                    //                gst_element_state_get_name(newstate), gst_element_state_get_name(pendstate));
                    break;
                case GST_MESSAGE_ERROR:
                    gst_message_parse_error(msg, &err, &debug);
                    
                    //fprintf(stderr, "GStreamer Plugin: Embedded video playback halted; module %s reported: %s\n",
                    //                gst_element_get_name(GST_MESSAGE_SRC (msg)), err->message);
                    
                    g_error_free(err);
                    g_free(debug);
                    
                    gst_element_set_state(GST_ELEMENT(pipeline), GST_STATE_NULL);
                    break;
                case GST_MESSAGE_EOS:
                    //fprintf(stderr, "reached the end of the stream.");
                    break;
                case GST_MESSAGE_STREAM_STATUS:
                    
                    gst_message_parse_stream_status(msg,&tp,&elem);
                    //fprintf(stderr, "stream status: elem %s, %i\n", GST_ELEMENT_NAME(elem), tp);
                    break;
                default:
                    //fprintf(stderr, "unhandled message\n");
                    break;
            }
        }
        gst_message_unref(msg);
    }
    
    gst_object_unref(GST_OBJECT(bus));
}