#!/bin/bash
cd ..
gradle clean build &&
cd test
sudo java -jar $(find ../core/build/libs/ -name "*-all.jar")
