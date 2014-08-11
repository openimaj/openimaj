//
//  main.cpp
//  Test
//
//  Created by Jonathon Hare on 08/08/2014.
//  Copyright (c) 2014 Jonathon Hare. All rights reserved.
//

#include <iostream>
#include "OpenIMAJ_GStreamer.h"

int main(int argc, const char * argv[])
{
    printf("Start\n");
    OpenIMAJCapGStreamer* gs = new OpenIMAJCapGStreamer;
    gs->open("filesrc location=/Users/jon/BigBuckBunny_115k.mov ! qtdemux ! h264parse ! avdec_h264 ! videoconvert ! appsink");
    
    printf("Open\n");
    
    while (1) {
        printf("%d\n", gs->nextFrame());
    }
    
    std::cout << "Hello, World!\n";
    
    
    return 0;
}

