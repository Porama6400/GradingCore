#!/bin/bash
sudo rm images.json config.json
cd ..
gradle clean build &&
cd test &&
sudo java -jar $(find ../build/libs/ -name "*-all.jar")
