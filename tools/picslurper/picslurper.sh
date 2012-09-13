#!/usr/bin/env bash
java -Xmx2G -cp .:target/picslurper.jar org.openimaj.picslurper.PicSlurper $*
