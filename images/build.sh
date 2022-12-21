#!/bin/bash

cd cpp
cp ../common/scrubber.cpp .
docker build -t grader-cpp .
rm scrubber.cpp
cd ..

cd c
cp ../common/scrubber.cpp .
cp ../cpp/Dockerfile .
docker build -t grader-c .
rm Dockerfile
rm scrubber.cpp
cd ..