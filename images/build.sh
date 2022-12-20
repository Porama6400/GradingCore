#!/bin/bash

cd c
docker build -t grader-c .
cd ..

cd cpp
cp ../c/Dockerfile .
docker build -t grader-cpp .
rm Dockerfile
cd ..