#!/bin/bash

g++ -fPIC -g -c OpenIMAJGrabber.cpp
g++ -fPIC -g -c capture.cpp 
g++ -shared -Wl,-soname,libOpenIMAJGrabber.so -o libOpenIMAJGrabber.so OpenIMAJGrabber.o capture.o -lv4l2 -lv4lconvert -ljpeg -lrt
cp libOpenIMAJGrabber.so ../../src/main/resources/org/openimaj/video/capture/nativelib/linux_x64/

