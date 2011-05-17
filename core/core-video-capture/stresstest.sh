#!/bin/sh

for i in {1..100}
do
    echo $i
    java -cp ../core-feature/target/classes:../core-image/target/classes:../core-math/target/classes:../core-video-capture/target/classes:../core-video/target/classes:../core/target/classes:.target/classes/:/Users/jsh2/.m2/repository/com/nativelibs4java/bridj/0.4.1/bridj-0.4.1.jar:/Users/jsh2/.m2/repository/log4j/log4j/1.2.8/log4j-1.2.8.jar org.openimaj.video.Test
done