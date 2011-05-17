//
//  main.cpp
//  TestGrabber
//
//  Created by Jonathon Hare on 15/05/2011.
//  Copyright 2011 University of Southampton. All rights reserved.
//

#import <iostream>
#import <vector>
#import "OpenIMAJGrabber.h"

int main (int argc, const char * argv[])
{
    DeviceList * devices = OpenIMAJGrabber::getVideoDevices();
    
    for (int i=0; i<devices->getNumDevices(); i++) {
        std::cout << devices->getDevice(i)->getName();
        std::cout << "\n";
    }
    
    OpenIMAJGrabber * grabber = new OpenIMAJGrabber::OpenIMAJGrabber();
    grabber->setDevice(devices->getDevice(0));
    
    if (!grabber->startSession(320, 240)) {
        std::cout << "Error starting grabber\n";
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

