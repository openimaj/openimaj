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

#include <iostream>
#include <fstream>
#include <sstream>
using namespace std;

int main (int argc, const char * argv[])
{
    int w = 640;
    int h = 480;
    
    OpenIMAJGrabber * grabber = new OpenIMAJGrabber::OpenIMAJGrabber();
    DeviceList * devices = grabber->getVideoDevices();
    
    for (int i=0; i<devices->getNumDevices(); i++) {
        std::cout << devices->getDevice(i)->getName();
        std::cout << "\n";
    }
    
    int sz = devices->getNumDevices();
    
    OpenIMAJGrabber ** grabbers = new OpenIMAJGrabber::OpenIMAJGrabber*[sz];
    
    for (int i=0; i<sz; i++) {
        grabbers[i] = new OpenIMAJGrabber::OpenIMAJGrabber();
        
        if (!grabbers[i]->startSession(w, h, 1.0, devices->getDevice(i))) {
            std::cout << "Error starting grabber\n";
            return 1;
        }
    }
    
    for (int j=0; j<10; j++) {
        for (int i=0; i<sz; i++) {
            grabbers[i]->nextFrame();
            unsigned char * data = grabbers[i]->getImage();
            printf("dev %d : %p\n", i, data);
            
            std::ostringstream stringStream;
            stringStream << "/Users/jsh2/Desktop/capture/" << i << "-" << j << ".dat";
            
            ofstream myfile;
            myfile.open (stringStream.str().c_str());
            myfile.write((const char *)data, 3*w * h);
            myfile.close();
            
            printf("done\n");
        }
        //usleep(1000 * 1000 / 25);
        usleep(1000 * 1000 * 1);
    }
    
    for (int i=0; i<sz; i++) {
        grabbers[i]->stopSession();
        delete grabbers[i];
    }
    
    delete [] grabbers;
    
    return 0;
}

