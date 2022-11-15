#!/bin/bash
sudo rm images.json config.json
cd ..
cd ..
gradle clean build &&
cd core && cd test
sudo java -jar $(find ../build/libs/ -name "*-all.jar")
