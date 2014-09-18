#!/bin/bash
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


# g++ -march=armv6 -mfpu=vfp -mfloat-abi=hard -fno-rtti -fno-exceptions -fPIC -g -c OpenIMAJGrabber.cpp
# g++ -march=armv6 -mfpu=vfp -mfloat-abi=hard -fno-rtti -fno-exceptions -fPIC -g -c capture.cpp 
# g++ -march=armv6 -mfpu=vfp -mfloat-abi=hard -fno-rtti -fno-exceptions -fPIC -g -c support.cpp
# gcc -march=armv6 -mfpu=vfp -mfloat-abi=hard -nostdlibs -static-libgcc --shared -Wl,-soname,OpenIMAJGrabber.so -o OpenIMAJGrabber.so OpenIMAJGrabber.o capture.o support.o -lrt -lc -lv4l2 -lv4lconvert -ljpeg
# cp OpenIMAJGrabber.so ../../src/main/resources/org/openimaj/video/capture/nativelib/linux_armhf/
#rm OpenIMAJGrabber.so
#echo "Building ARMEL"
#g++ -march=armv6 -mabi=aapcs-linux -mfloat-abi=soft -meabi=4 -fno-rtti -fno-exceptions -fPIC -g -c OpenIMAJGrabber.cpp
#g++ -march=armv6 -mabi=aapcs-linux -mfloat-abi=soft -meabi=4 -fno-rtti -fno-exceptions -fPIC -g -c capture.cpp 
#g++ -march=armv6 -mabi=aapcs-linux -mfloat-abi=soft -meabi=4 -fno-rtti -fno-exceptions -fPIC -g -c support.cpp
#gcc -march=armv6 -mabi=aapcs-linux -mfloat-abi=soft -meabi=4 -nostdlibs -static-libgcc --shared -Wl,-soname,OpenIMAJGrabber.so -o OpenIMAJGrabber.so OpenIMAJGrabber.o capture.o support.o -lrt -lc -lv4l2 -lv4lconvert -ljpeg
#cp OpenIMAJGrabber.so ../../src/main/resources/org/openimaj/video/capture/nativelib/linux_armel/


g++ -fno-rtti -fno-exceptions -fPIC -g -c OpenIMAJGrabber.cpp
g++ -fno-rtti -fno-exceptions -fPIC -g -c capture.cpp 
g++ -fno-rtti -fno-exceptions -fPIC -g -c support.cpp
gcc -nostdlibs -static-libgcc --shared -Wl,-soname,OpenIMAJGrabber.so -o OpenIMAJGrabber.so OpenIMAJGrabber.o capture.o support.o -lrt -lc -lv4l2 -lv4lconvert -ljpeg


cp OpenIMAJGrabber.so ../../src/main/resources/org/openimaj/video/capture/nativelib/linux_armel/
