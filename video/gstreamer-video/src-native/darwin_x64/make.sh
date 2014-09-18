#
# Copyright (c) 2011, The University of Southampton and the individual contributors.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
#
#   * 	Redistributions of source code must retain the above copyright notice,
# 	this list of conditions and the following disclaimer.
#
#   *	Redistributions in binary form must reproduce the above copyright notice,
# 	this list of conditions and the following disclaimer in the documentation
# 	and/or other materials provided with the distribution.
#
#   *	Neither the name of the University of Southampton nor the names of its
# 	contributors may be used to endorse or promote products derived from this
# 	software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
# ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

c++ -g -arch x86_64 \
    -shared \
    -o libOpenIMAJGStreamer.dylib \
    ../common/OpenIMAJ_GStreamer.cpp \
    -I../common -I/usr/local/include/gstreamer-1.0/ \
    -I/usr/local/include/glib-2.0 \
    -I/usr/local/opt/gettext/include \
    -I/usr/local/lib/glib-2.0/include \
    -L/usr/local/Cellar/gst-plugins-base/1.4.0/lib \
    -L/usr/local/Cellar/gstreamer/1.4.0/lib \
    -L/usr/local/Cellar/glib/2.40.0_1/lib \
    -lglib-2.0 \
    -lgobject-2.0 \
    -lgstapp-1.0 \
    -lgstbase-1.0 \
    -lgstpbutils-1.0 \
    -lgstreamer-1.0 \
    -lgstriff-1.0 \
    -lgstvideo-1.0
mkdir -p ../../src/main/resources/org/openimaj/video/gstreamer/nativelib//darwin_x64/
mv libOpenIMAJGStreamer.dylib ../../src/main/resources/org/openimaj/video/gstreamer/nativelib/darwin_x64/

