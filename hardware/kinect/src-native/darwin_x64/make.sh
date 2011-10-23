#!/bin/sh

gcc -shared -o libfreenect-combined.dylib ../src/additions.c libusb-1.0.a libfreenect.a -I ../include/ -framework CoreFoundation -framework IOKit
mkdir -p ../../src/main/resources/org/openimaj/hardware/kinect/nativelib/darwin_x64/
mv libfreenect-combined.dylib ../../src/main/resources/org/openimaj/hardware/kinect/nativelib/darwin_x64/
