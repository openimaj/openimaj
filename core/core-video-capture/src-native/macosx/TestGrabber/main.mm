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

