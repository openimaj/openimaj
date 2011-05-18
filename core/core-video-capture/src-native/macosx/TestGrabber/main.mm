//
//  main.cpp
//  TestGrabber
//
//  Created by Jonathon Hare on 15/05/2011.
//  Copyright 2011 University of Southampton. All rights reserved.
//

#import <iostream>
#import <vector>
#import "../OpenIMAJGrabber/OpenIMAJGrabber.h"

int main (int argc, const char * argv[])
{
    OpenIMAJGrabber * grabber = new OpenIMAJGrabber::OpenIMAJGrabber();
    DeviceList * devices = grabber->getVideoDevices();
    
    for (int i=0; i<devices->getNumDevices(); i++) {
        std::cout << devices->getDevice(i)->getName();
        std::cout << "\n";
    }
    
    if (!grabber->startSession(320, 240, devices->getDevice(0))) {
        std::cout << "Error starting grabber\n";
        return 1;
    }
    
    for (int i=0; i<10; i++) {
        grabber->nextFrame();
        printf("%p\n", grabber->getImage());
        
        usleep(1000 * 1000 / 25);
    }
    grabber->stopSession();
    
    delete grabber;
    
    return 0;
}

